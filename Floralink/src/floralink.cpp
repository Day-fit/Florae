#include <Arduino.h>
#include <DHT.h>
#include <Wire.h>
#include <BH1750.h>
#include <WiFi.h>
#include <WebSocketsClient.h>
#include <Ticker.h>
#include <ArduinoJson.h>
#include <NimBLEDevice.h>
#include <Preferences.h>
#include <nvs_flash.h>

bool enableDevMode = false;

float readVWC(int pin);
void handleWebSocketEvent(WStype_t type, uint8_t *payload, size_t lenght);
String handleSensorSampling();
void handleSendingReadings();
void handleAddingWater(double amouth);
void handleEnablingBLE();
const char* getWifiSsid();
const char* getWifiPassword();
const char* getValueFromConfig(const char* value);
void handleWifiEvents(WiFiEvent_t event);
bool connectToWiFi(const char* &ssid, const char* &password, unsigned long timeoutMs = 10000);
void handleCredentialsUpdate(const String &json);

constexpr const char* BLE_SERVICE_UUID = "53020f00-319c-4d97-a2b1-9e706baba77a";
constexpr const char* BLE_CHARACTERISTIC_UUID = "f87709b3-63a7-4605-9bb5-73c383462296";

constexpr const char* PROD_BASE_URL = "florae.dayfit.pl";
constexpr const char* DEV_BASE_URL = "192.168.238.192";
constexpr const char* API_KEY_HEADER = "X-API-KEY";

constexpr uint16_t PUMP_EFFICIENCY = 120; //Liters per hour

constexpr uint8_t DHT22_PIN = 4;
constexpr uint8_t SOIL_MOISTURE_PIN = 34;
constexpr uint8_t PUMP_PIN = 18; //Actually, it's the MOSFET "Gate" pin

DHT dht(DHT22_PIN, DHT22);
BH1750 luxMeter(0x23);
WebSocketsClient wsClient;

Ticker ticker;
Ticker BleTicker;

struct SensorFunction 
{
  const char* type;
  std::function<float()> read;
};

constexpr const char* WIFI_SSID = "wifi_ssid";
constexpr const char* WIFI_PASSWORD = "wifi_password";
constexpr const char* API_KEY = "api_key";

class BLECallback : public NimBLECharacteristicCallbacks
{
    void onWrite(NimBLECharacteristic* pCharacteristic, NimBLEConnInfo& connInfo) override
    {
        std::string data = pCharacteristic->getValue();

        if (data.empty())
            return;

        Serial.println(data.c_str());

        handleCredentialsUpdate(String(data.c_str()));
    }
};

SensorFunction readers[] = 
{
  {"ENV_HUMIDITY", []() { return dht.readHumidity(); }},
  {"ENV_TEMPERATURE", []() {return dht.readTemperature(); }},
  {"SOIL_MOISTURE", []() {return readVWC(SOIL_MOISTURE_PIN); }},
  {"LIGHT_LUX", []() {return luxMeter.readLightLevel(); }}
};

void setup()
{
    Serial.begin(115200);
    Wire.begin(22, 21);
    dht.begin();

    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
      nvs_flash_erase();
      ret = nvs_flash_init();
    }
    if (ret != ESP_OK) {
      Serial.printf("NVS init failed: %d\n", ret);
      ESP.restart();
    }

    if(!luxMeter.begin(BH1750::CONTINUOUS_HIGH_RES_MODE_2))
    {
        ESP.restart();
    }; 

    pinMode(SOIL_MOISTURE_PIN, INPUT);
    pinMode(PUMP_PIN, OUTPUT);
    
    wsClient.onEvent(handleWebSocketEvent);

    const char* wifiSsid = getWifiSsid();
    const char* wifiPassword = getWifiPassword();

    if (wifiSsid == nullptr || wifiPassword == nullptr)
    {
      handleEnablingBLE();
      Serial.print("starting");
      return;
    }

    connectToWiFi(wifiSsid, wifiPassword);
}

void loop()
{
    wsClient.loop();
}

float readVWC(int pin) {
  int raw = analogRead(pin);
  float volt = raw * (3.3 / 4095.0);
  float VWC;

  if (volt >= 2.5) {
    VWC = 0.0;
  }
  else if (volt >= 2.0) {
    VWC = (2.5 - volt) * 20;
  }
  else if (volt >= 1.5) {
    VWC = (2.0 - volt) * 30 + 10;
  }
  else if (volt >= 1.0) {
    VWC = (1.5 - volt) * 50 + 25;
  }
  else {
    VWC = (1.0 - volt) * 40 + 50;
  }

  return constrain(VWC, 0.0, 100.0);
}

void handleWebSocketEvent(WStype_t type, uint8_t *payload, size_t length)
{
  switch (type)
  {
      case WStype_CONNECTED:
        Serial.println("Connected!");
        ticker.attach(1, handleSendingReadings);
        break;
      
      case WStype_ERROR:
      case WStype_DISCONNECTED:
        Serial.println("Disconected!");
        ticker.detach();

        handleEnablingBLE();
        break;

      case WStype_TEXT:
      {
        JsonDocument doc;
        String jsonResponse(reinterpret_cast<const char*>(payload), length);
        DeserializationError error = deserializeJson(doc, jsonResponse);
      
        String commandType = doc["type"];

        Serial.println(commandType);

        if (commandType.equalsIgnoreCase("WATERING"))
        {
          handleAddingWater(doc["value"]);
        }
        
        if (commandType.equalsIgnoreCase("ENABLE_BLE"))
        {
          handleEnablingBLE();
          Serial.println("enabling");
          BleTicker.attach(60 * 5, [](){ NimBLEDevice::deinit(); }); //disable after 5 minutes
        }
      }        
      default:
        break;
  }    
}

String handleSensorSampling()
{
  String sensorJson = "[";
  size_t readersSize = sizeof(readers) / sizeof(SensorFunction);

  for (size_t i = 0; i < readersSize; ++i)
  {
      SensorFunction reader = readers[i];
      String lastSign = (readersSize - 1 == i)? "" : ",";
      sensorJson += "{\"type\": \"" + String(reader.type) + "\", \"value\": \"" + String(reader.read()) + "\"}" + lastSign;
  }

  sensorJson += "]";

  return sensorJson;
}

void handleSendingReadings()
{
  if (WiFi.isConnected())
  {
      wsClient.sendTXT(handleSensorSampling().c_str());
  }
}

void handleAddingWater(double amount)
{
  pinMode(PUMP_PIN, OUTPUT);

  const double seconds = amount / PUMP_EFFICIENCY;
  const unsigned long milliseconds = static_cast<unsigned long>(seconds * 1000);

  digitalWrite(PUMP_PIN, HIGH);
  delay(milliseconds);
  digitalWrite(PUMP_PIN, LOW);
}

void handleEnablingBLE()
{
  NimBLEDevice::init("FloraLink");

  NimBLEServer* server = NimBLEDevice::createServer();
  NimBLEService* service = server->createService(BLE_SERVICE_UUID);
  NimBLECharacteristic* characteristic = service->createCharacteristic(
      BLE_CHARACTERISTIC_UUID,
      NIMBLE_PROPERTY::WRITE
  );

  characteristic->setCallbacks(new BLECallback());
  service->start();

  NimBLEAdvertising* advertising = NimBLEDevice::getAdvertising();
  advertising->enableScanResponse(true);
  advertising->setName("FloraLink");
  advertising->addServiceUUID(BLE_SERVICE_UUID);
  advertising->start();

  advertising->addServiceUUID(BLE_SERVICE_UUID);
  advertising->start();
}

void handleCredentialsUpdate(const String &json)
{
  JsonDocument doc;
  DeserializationError error = deserializeJson(doc, json);

  if (error)
  {
    return;
  }

  const char* ssid = doc[WIFI_SSID];
  const char* password = doc[WIFI_PASSWORD];
  const char* apiKey = doc[API_KEY];

  if (! apiKey || !connectToWiFi(ssid, password)) {
    return;
  }

  Preferences prefs;
  prefs.begin("cfg", false);

  String ssidStr = String(ssid);
  ssidStr.trim();

  String passwordStr = String(password);
  passwordStr.trim();

  String apiKeyStr = String(apiKey);
  apiKeyStr.trim();

  prefs.putString(WIFI_SSID, ssidStr);
  prefs.putString(WIFI_PASSWORD, passwordStr);
  prefs.putString(API_KEY, apiKeyStr);

  prefs.end();
}

bool connectToWiFi(const char* &ssid, const char* &password, unsigned long timeoutMs) {
  if (ssid == nullptr || password == nullptr)
  {
    return false;
  }

  WiFi.hostname("FloraLink");
  WiFi.onEvent(handleWifiEvents);
  WiFi.begin(ssid, password);

  unsigned long startAttemptTime = millis();

  while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < timeoutMs) {
    delay(100);
  }

  if (!WiFi.isConnected())
  {
    WiFi.disconnect();
    return false;
  }

  return true;
}

const char* getApiKey()
{
  return getValueFromConfig(API_KEY);
}

const char* getWifiSsid()
{
  return getValueFromConfig(WIFI_SSID);
}

const char* getWifiPassword()
{
  return getValueFromConfig(WIFI_PASSWORD);
}

const char* getValueFromConfig(const char* key)
{
  if (key != WIFI_SSID && key != WIFI_PASSWORD && key != API_KEY)
    return nullptr;

  Preferences prefs;
  if (!prefs.begin("cfg", true)) {
    prefs.end();
    return nullptr;
  }

  String value = prefs.getString(key, "");
  prefs.end();

  return value.isEmpty() ? nullptr : strdup(value.c_str());
}

void handleWifiEvents(WiFiEvent_t event)
{
  switch (event)
  {
    case SYSTEM_EVENT_STA_GOT_IP:
    {
      NimBLEDevice::deinit();

      const char* apiKey = getApiKey();

      if (apiKey == nullptr) {
          Serial.println("Missing API key");
          return;
      }

      String headers = String(API_KEY_HEADER) + ": " + apiKey;
      wsClient.setExtraHeaders(headers.c_str());

      if (enableDevMode)
      {
        wsClient.begin(DEV_BASE_URL, 8080, "/ws/floralink");
        break;
      }
      
      wsClient.beginSSL(PROD_BASE_URL, 443, "/ws/floralink");
      break;
    }
    
    case SYSTEM_EVENT_STA_LOST_IP:
    case SYSTEM_EVENT_STA_DISCONNECTED:
      handleEnablingBLE();
      break;
    
    default:
      break;
  }
}