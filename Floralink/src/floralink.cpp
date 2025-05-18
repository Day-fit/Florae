#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <LittleFS.h>
#include <string.h>

void saveData(String key, String value);
String loadData(String key);

AsyncWebServer server(80);

const char* DEFAULT_PASSWORD = "password";

const String TARGET_SSID_KEY = "targetSsid";
const String TARGET_PASSWORD_KEY = "targetPassword";
const String FLORAE_ACCESS_KEY = "floraeAccessKey";

String targetWifiSsid = "";
String targetWifiPassword = "";
String floraeAccessKey = "";

void setup() 
{
    Serial.begin(115200);
    WiFi.softAP("FloraLink", DEFAULT_PASSWORD);

    if (!LittleFS.begin())
    {
        Serial.println("Failed to load files !");
    }

    Serial.println("Loading configuration file...");

    targetWifiSsid = loadData(TARGET_SSID_KEY);
    targetWifiPassword = loadData(TARGET_PASSWORD_KEY);
    floraeAccessKey = loadData(FLORAE_ACCESS_KEY);

    Serial.println("Starting server...");
    
    server.serveStatic("/", LittleFS, "/").setDefaultFile("index.html");
    server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request){
        if (request->hasParam(TARGET_SSID_KEY))
        {
            targetWifiSsid = request->getParam(TARGET_SSID_KEY)->value();
            saveData(TARGET_SSID_KEY, targetWifiSsid);
        }
        
        if (request->hasParam(TARGET_PASSWORD_KEY))
        {
            targetWifiPassword = request->getParam(TARGET_PASSWORD_KEY)->value();
            saveData(TARGET_PASSWORD_KEY, targetWifiPassword);
        }

        if (request->hasParam(FLORAE_ACCESS_KEY))
        {
            floraeAccessKey = request->getParam(FLORAE_ACCESS_KEY)->value();
            saveData(FLORAE_ACCESS_KEY, floraeAccessKey);
        }
    });

    server.begin();
}

void loop()
{
    
}

void saveData(String key, String value)
{
    StaticJsonDocument<256> json;
    json[key] = value;

    File file = LittleFS.open("/config.json", "w");
    serializeJson(json, file);
    file.close();
}

String loadData(String key)
{
    StaticJsonDocument<256> json;
    File file = LittleFS.open("/config.json", "r");

    if (!file)
    {
        return "";
    }
    
    DeserializationError error = deserializeJson(json, file);
    file.close();

    if (error)
    {
        Serial.println("Error during loading config file");
        return "";
    }

    return json[key].as<String>();
}