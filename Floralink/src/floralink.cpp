#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <LittleFS.h>
#include <string.h>
#include <map>
#include <list>
#include <variant>
#include <Ticker.h>
#include <ESPDateTime.h>
#include <DHT.h>

bool isInDev = false;
String FLORAE_URL = isInDev? "http://localhost:8080" : "florae.dayfit.pl";

void loadSensors();
void saveData(String key, String value);
String loadData(String key);
void checkWiFiConnection();

Ticker ticker;
AsyncWebServer server(80);

const char* DEFAULT_PASSWORD = "password";
const unsigned int DEFAULT_TIMEOUT = 30;

const String TARGET_SSID_KEY = "targetSsid";
const String TARGET_PASSWORD_KEY = "targetPassword";
const String FLORAE_ACCESS_KEY = "floraeAccessKey";
const String WIFI_TIMEOUT = "wifiTimeout";

unsigned long lastRead;

bool isConnected = false;

using dataValuesTypes = std::variant<String, int>;
std::map<String, dataValuesTypes> configValues;

enum SensorType{
    DHT11_TYPE,
    DHT22_TYPE,
};

struct Sensor
{
    SensorType type;
    int pinout;
};

struct SensorData
{
    String type;
    double currentValue;
    double lowest24hValue;
    
    double highest24hValue;
};

std::list<Sensor> possibleSensors = 
{
    {DHT11_TYPE, 14},
    {DHT22_TYPE, 14}
};

std::list<Sensor> sensors;
std::list<SensorData> sensorData;

const String allowedParameters[3] = 
{
    TARGET_SSID_KEY,
    TARGET_PASSWORD_KEY,
    FLORAE_ACCESS_KEY
};

void setup() 
{
    DateTime.begin();
    Serial.begin(115200);

    WiFi.mode(WIFI_AP_STA);
    WiFi.softAP("FloraLink", DEFAULT_PASSWORD);

    struct tm DEFAULT_TIME;
    DEFAULT_TIME.tm_hour = 12;
    DEFAULT_TIME.tm_min = 0;
    DEFAULT_TIME.tm_sec = 0;

    DateTime.setTime(mktime(&DEFAULT_TIME));

    if (!LittleFS.begin())
    {
        Serial.println("Failed to load files !");
    }

    Serial.println("Loading configuration file...");

    String wifiTimeoutStr = loadData(WIFI_TIMEOUT);
    configValues[WIFI_TIMEOUT] = (wifiTimeoutStr == "null") ? static_cast<int>(DEFAULT_TIMEOUT) : wifiTimeoutStr.toInt();
    configValues[TARGET_SSID_KEY] = loadData(TARGET_SSID_KEY);
    configValues[TARGET_PASSWORD_KEY] = loadData(TARGET_PASSWORD_KEY);
    configValues[FLORAE_ACCESS_KEY] = loadData(FLORAE_ACCESS_KEY);

    if (std::get<String>(configValues[TARGET_PASSWORD_KEY]) != "" && std::get<String>(configValues[TARGET_SSID_KEY]) != "")
    {
        WiFi.begin(std::get<String>(configValues[TARGET_SSID_KEY]), std::get<String>(configValues[TARGET_PASSWORD_KEY]));
        ticker.attach(1, checkWiFiConnection);
    }
    
    loadSensors();

    Serial.println("Starting server...");
    
    server.serveStatic("/", LittleFS, "/").setDefaultFile("index.html");

    server.on("/connectionStatus", HTTP_GET, [](AsyncWebServerRequest *request){
        request->send(200, "application/json", "{\"connected\": \"" + String(isConnected) + "\"}");
    });

    server.on("/validate-api-key", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL, [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total)
    {
        if (!WiFi.isConnected())
        {
            request->send(HTTP_CODE_SERVICE_UNAVAILABLE, "application/json", "{\"error\": \"No internet connection.\"}");
            return;
        }

        JsonDocument json;
        DeserializationError err = deserializeJson(json, data, len);

        if (err)
        {
            request->send(HTTP_CODE_BAD_REQUEST, "application/json", "{\"error\": \"Invalid JSON\"}");
            return;
        }
        
        HTTPClient http;

        String apiKey = json["apiKey"];

        WiFiClient client;
        String url = FLORAE_URL + "/api/v1/check-key?apiKey=" + std::get<String>(configValues[FLORAE_ACCESS_KEY]);
        http.begin(client, url);
        int responseCode = http.GET();

        if (responseCode == HTTP_CODE_OK)
        {
            request->send(HTTP_CODE_OK, "application/json", http.getString());
        }

        request->send(HTTP_CODE_INTERNAL_SERVER_ERROR, "application/json" , "\"error\": \"Server responded with code "+ String(responseCode) +"\"");
    });

    server.on("/sensors-status", HTTP_GET, [](AsyncWebServerRequest *request)
    {
        JsonDocument json;
        JsonArray arr = json.to<JsonArray>();

        for (const auto& data : sensorData) {
            JsonObject obj = arr.add<JsonObject>();
            obj["type"] = data.type;
            obj["value"] = data.currentValue;
            obj["lowest24hValue"] = data.lowest24hValue;
            obj["highest24hValue"] = data.highest24hValue;
        }

        String response;
        serializeJson(json, response);
        request->send(200, "application/json", response);
    });

    server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL, [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total){
        bool moddifiesWifiCredentials = false;

        JsonDocument json;
        DeserializationError err = deserializeJson(json, data, len);

        if(err)
        {
            request->send(HTTP_CODE_BAD_REQUEST, "application/json", "\"error\": \"Invalid JSON\"");
            return;
        }

        for (String allowedParameter : allowedParameters)
        {
            if(!json[allowedParameter])
            {
                continue;
            }

            String value = json[allowedParameter];
            configValues[allowedParameter] = value;
            saveData(allowedParameter, value);

            Serial.println(allowedParameter + " " + value);

            if (allowedParameter == TARGET_SSID_KEY || allowedParameter == TARGET_PASSWORD_KEY)
            {
                moddifiesWifiCredentials = true;
            }
        }

        if (moddifiesWifiCredentials)
        {
            WiFi.begin(std::get<String>(configValues[TARGET_SSID_KEY]), std::get<String>(configValues[TARGET_PASSWORD_KEY]));
            WiFi.printDiag(Serial);
            ticker.attach(1, checkWiFiConnection);
        }

        request->send(200, "text/plain", "Configuration saved");
    });


    server.begin();
}

void loop()
{
    
}

void loadSensors()
{
    for (auto const& sensor : possibleSensors)
    {
        switch (sensor.type)
        {
            case DHT11_TYPE:
            {
                DHT dht(sensor.pinout, DHT11);
                dht.begin();
                if (!isnan(dht.readTemperature()) && !isnan(dht.readHumidity()))
                {
                    sensors.push_back(sensor);
                }
                break;
            }
            default:
                break;
        }
    }
}

void setUpTime()
{
    DateTime.setTimeZone("CET-1CEST,M3.5.0/2,M10.5.0/3");
}

void saveData(String key, String value)
{
    JsonDocument json;

    File file = LittleFS.open("/config.json", "r");
    if (file) {
        deserializeJson(json, file);
        file.close();
    }

    json[key] = value;

    file = LittleFS.open("/config.json", "w");
    serializeJson(json, file);
    file.close();
}

String loadData(String key)
{
    JsonDocument json;
    File file = LittleFS.open("/config.json", "r");

    if (!file)
    {
        return "null";
    }
    
    DeserializationError error = deserializeJson(json, file);
    file.close();

    if (error)
    {
        Serial.println("Error during loading config file");
        return "null";
    }

    return json[key].as<String>();
}

void checkWiFiConnection() {
    static int timeoutCounter = 0;
    int maxTimeout = std::get<int>(configValues[WIFI_TIMEOUT]); 

    if (WiFi.isConnected()) {
        ticker.detach();
        isConnected = true;
        timeoutCounter = 0;
        Serial.println("Connected to WiFi successfully!");
        setUpTime();
        return;
    }

    timeoutCounter++;
    Serial.println("Attempting to connect to WiFi... " + String(timeoutCounter) + "/" + String(maxTimeout));
    
    if (timeoutCounter >= maxTimeout) {
        ticker.detach();
        WiFi.disconnect(true);
        Serial.println("WiFi connection timed out!");
        timeoutCounter = 0;
    }
}