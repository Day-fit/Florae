#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <LittleFS.h>
#include <string.h>
#include <map>
#include <variant>
#include <Ticker.h>

void saveData(String key, String value);
String loadData(String key);
void checkWiFiConnection();

Ticker ticker;
AsyncWebServer server(80);

const char* DEFAULT_PASSWORD = "password";
const int DEFAULT_TIMEOUT = 30;

const String TARGET_SSID_KEY = "targetSsid";
const String TARGET_PASSWORD_KEY = "targetPassword";
const String FLORAE_ACCESS_KEY = "floraeAccessKey";
const String WIFI_TIMEOUT = "wifiTimeout";

bool isConnected = false;

using dataValuesTypes = std::variant<String, int>;
std::map<String, dataValuesTypes> dataValues;

const String allowedParameters[3] = 
{
    TARGET_SSID_KEY,
    TARGET_PASSWORD_KEY,
    FLORAE_ACCESS_KEY
};

void setup() 
{
    Serial.begin(115200);
    WiFi.softAP("FloraLink", DEFAULT_PASSWORD);

    if (!LittleFS.begin())
    {
        Serial.println("Failed to load files !");
    }

    Serial.println("Loading configuration file...");

    String wifiTimeoutStr = loadData(WIFI_TIMEOUT);
    dataValues[WIFI_TIMEOUT] = wifiTimeoutStr == "null" ? DEFAULT_TIMEOUT : wifiTimeoutStr.toInt();
    dataValues[TARGET_SSID_KEY] = loadData(TARGET_SSID_KEY);
    dataValues[TARGET_PASSWORD_KEY] = loadData(TARGET_PASSWORD_KEY);
    dataValues[FLORAE_ACCESS_KEY] = loadData(FLORAE_ACCESS_KEY);

    if (std::get<String>(dataValues[TARGET_PASSWORD_KEY]) != "" && std::get<String>(dataValues[TARGET_SSID_KEY]) != "")
    {
        WiFi.begin(std::get<String>(dataValues[TARGET_SSID_KEY]), std::get<String>(dataValues[TARGET_PASSWORD_KEY]));
        ticker.attach(1, checkWiFiConnection);
    }
    
    Serial.println("Starting server...");
    
    server.serveStatic("/", LittleFS, "/").setDefaultFile("index.html");

    server.on("/connectionStatus", HTTP_GET, [](AsyncWebServerRequest *request){
        request->send(200, "application/json", "{\"connected\": \"" + String(isConnected) + "\"}");
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
                    dataValues[key] = value;
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
            WiFi.begin(std::get<String>(dataValues[TARGET_SSID_KEY]), std::get<String>(dataValues[TARGET_PASSWORD_KEY]));
            ticker.attach(1, checkWiFiConnection);
        }

        request->send(200, "text/plain", "Configuration saved");
    });


    server.begin();
}

void loop()
{
    
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
    int maxTimeout = std::get<int>(dataValues[WIFI_TIMEOUT]); 

    if (WiFi.isConnected()) {
        ticker.detach();
        isConnected = true;
        Serial.println("Connected to WiFi successfully!");
    } else {
        timeoutCounter++;
        Serial.println("Attempting to connect to WiFi... " + String(timeoutCounter) + "/" + String(maxTimeout));
        
        if (timeoutCounter >= maxTimeout) {
            ticker.detach();
            WiFi.disconnect(true);
            Serial.println("WiFi connection timed out!");
            timeoutCounter = 0;
        }
    }
}