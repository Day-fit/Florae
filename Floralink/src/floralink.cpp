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

unsigned long lastRead = 0;

bool isConnected = false;

using dataValuesTypes = std::variant<String, int>;
std::map<String, dataValuesTypes> configValues;

std::list<Sensor> possibleSensors= 
{
    {DHT11, 14},
    {DHT22, 14}
};

std::list<Sensor> sensors;
std::list<SensorData> sensorData;

enum SensorType{
    DHT11,
    DHT22,
};

struct SensorData
{
    String type;
    double currentValue;
    double lowest24hValue;
    
    double highest24hValue;
};

struct Sensor

struct Sensor
{
    SensorType type;
    int pinout;
};

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

    server.on("/sensorsStatus", HTTP_GET, [](AsyncWebServerRequest *request)
    {

        DynamicJsonDocument json(1024);
        JsonArray arr = json.to<JsonArray>();

        for (const auto& data : sensorData) {
            JsonObject obj = arr.createNestedObject();
            obj["type"] = data.type;
            obj["value"] = data.currentValue;
            obj["lowest24hValue"] = data.lowest24hValue;
            obj["highest24hValue"] = data.highest24hValue;
        }

        String response;
        serializeJson(json, response);
        request->send(200, "application/json", response);
    });

    server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request){
        int parametersNumb = request->params();
        bool moddifiesWifiCredentials = false;

        for (int i = 0; i < parametersNumb; i++)
        {

            String key = request->getParam(i)->name();
            String value = request->getParam(i)->value();

            for (String allowedParameter : allowedParameters)
            {
                if (allowedParameter == key)
                {
                    configValues[key] = value;
                    saveData(key, value);

                    if (key == TARGET_SSID_KEY || key == TARGET_PASSWORD_KEY)
                    {
                        moddifiesWifiCredentials = true;
                    }
                }
            }
        }

        if (moddifiesWifiCredentials)
        {
            WiFi.begin(std::get<String>(configValues[TARGET_SSID_KEY]), std::get<String>(configValues[TARGET_PASSWORD_KEY]));
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
    for (auto const sensor : possibleSensors)
    {

    }
}

void setUpTime()
{
    DateTime.setTimeZone("CET-1CEST,M3.5.0/2,M10.5.0/3");
}

void saveData(String key, String value)
{
    const size_t capacity = 512;
    DynamicJsonDocument json(capacity);

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