#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ESPAsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <LittleFS.h>
#include <ESPDateTime.h>
#include <DHT.h>
#include <vector>
#include <list>

struct Config {
    String ssid;
    String password;
    String floraeKey;
    int wifiTimeout = 30;
};

static Config config;
static bool isConnected = false;
static bool shouldCheckWiFi = false;

const char* DEFAULT_PASSWORD = "password";
const unsigned int DEFAULT_TIMEOUT = 30;
const String FLORAE_URL = "http://localhost:8080";

AsyncWebServer server(80);

enum SensorType {
    DHT11_TYPE,
    DHT22_TYPE
};

struct Sensor {
    SensorType type;
    int pin;
};

struct ValueTimestamp {
    double value;
    unsigned long ts;
};

struct SensorData {
    String type;
    double currentValue;
    double lowest24hValue;
    double highest24hValue;
    std::vector<double> lastSecondReadings;
    double lastSecondAverage = 0.0;
    unsigned long lastPurge = 0;
    std::vector<ValueTimestamp> last24hReadings;
    double avg24h = 0.0;
    unsigned long lowest24hTimestamp = 0;
    unsigned long highest24hTimestamp = 0;
};

std::list<Sensor> possibleSensors = {
    {DHT11_TYPE, 12},
    {DHT22_TYPE, 12}
};

std::list<Sensor> sensors;
std::list<SensorData> sensorData;

void loadConfig() {
    File file = LittleFS.open("/config.json", "r");
    if (!file) return;
    JsonDocument doc;
    if (deserializeJson(doc, file) == DeserializationError::Ok) {
        config.ssid = doc["targetSsid"] | "";
        config.password = doc["targetPassword"] | "";
        config.floraeKey = doc["floraeAccessKey"] | "";
        config.wifiTimeout = doc["wifiTimeout"] | DEFAULT_TIMEOUT;
    }
    file.close();
}

void saveConfig() {
    JsonDocument doc;
    doc["targetSsid"] = config.ssid;
    doc["targetPassword"] = config.password;
    doc["floraeAccessKey"] = config.floraeKey;
    doc["wifiTimeout"] = config.wifiTimeout;
    File file = LittleFS.open("/config.json", "w");
    serializeJson(doc, file);
    file.close();
}

void checkWiFiConnection() {
    static int timeoutCounter = 0;
    if (WiFi.isConnected()) {
        isConnected = true;
        shouldCheckWiFi = false;
        timeoutCounter = 0;
        DateTime.setTimeZone("CET-1CEST,M3.5.0/2,M10.5.0/3");
        return;
    }
    timeoutCounter++;
    if (timeoutCounter >= config.wifiTimeout) {
        WiFi.disconnect(true);
        timeoutCounter = 0;
        shouldCheckWiFi = false;
    }
}

void loadSensors() {
    for (auto const& s : possibleSensors) {
        DHT dht(s.pin, s.type == DHT11_TYPE ? DHT11 : DHT22);
        dht.begin();
        double temp = dht.readTemperature();
        double hum = dht.readHumidity();
        if (!isnan(temp) && !isnan(hum)) {
            sensors.push_back(s);
            SensorData sd;
            sd.type = (s.type == DHT11_TYPE ? "DHT11" : "DHT22");
            sensorData.push_back(sd);
        }
    }
}

void saveData(String key, String value) {
    JsonDocument doc;
    File file = LittleFS.open("/config.json", "r");
    if (file) {
        if (deserializeJson(doc, file) != DeserializationError::Ok) {
            doc.clear();
        }
        file.close();
    }
    doc[key] = value;
    file = LittleFS.open("/config.json", "w");
    serializeJson(doc, file);
    file.close();
}

String loadData(String key) {
    JsonDocument doc;
    File file = LittleFS.open("/config.json", "r");
    if (!file) return "null";
    if (deserializeJson(doc, file) != DeserializationError::Ok) {
        file.close();
        return "null";
    }
    file.close();
    return doc[key].as<String>();
}

void setup() {
    DateTime.begin();
    Serial.begin(115200);
    WiFi.mode(WIFI_AP_STA);
    WiFi.softAP("FloraLink", DEFAULT_PASSWORD);
    struct tm DEFAULT_TIME;
    DEFAULT_TIME.tm_hour = 12;
    DEFAULT_TIME.tm_min = 0;
    DEFAULT_TIME.tm_sec = 0;
    DateTime.setTime(mktime(&DEFAULT_TIME));
    LittleFS.begin();
    loadConfig();
    if (config.ssid.length() && config.password.length()) {
        WiFi.begin(config.ssid.c_str(), config.password.c_str());
        shouldCheckWiFi = true;
    }
    loadSensors();
    server.serveStatic("/", LittleFS, "/").setDefaultFile("index.html");
    server.on("/connectionStatus", HTTP_GET, [](AsyncWebServerRequest *request){
        JsonDocument doc;
        doc["connected"] = isConnected;
        doc["ip"] = WiFi.localIP().toString();
        String out;
        serializeJson(doc, out);
        request->send(200, "application/json", out);
    });
    server.on("/validate-api-key", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            if (!WiFi.isConnected()) {
                request->send(503, "application/json", "{\"error\":\"No internet connection.\"}");
                return;
            }
            JsonDocument j;
            if (deserializeJson(j, data, len) != DeserializationError::Ok) {
                request->send(400, "application/json", "{\"error\":\"Invalid JSON\"}");
                return;
            }
            if (!j.containsKey("apiKey") || j["apiKey"].as<String>().length() < 1) {
                request->send(400, "application/json", "{\"error\":\"Missing or invalid apiKey\"}");
                return;
            }
            if (config.floraeKey.length() < 1) {
                request->send(400, "application/json", "{\"error\":\"No API key configured\"}");
                return;
            }
            WiFiClient client;
            HTTPClient http;
            String url = FLORAE_URL + "/api/v1/check-key?apiKey=" + config.floraeKey;
            http.begin(client, url);
            int code = http.GET();
            if (code == HTTP_CODE_OK) {
                String resp = http.getString();
                request->send(200, "application/json", resp);
                http.end();
                return;
            }
            http.end();
            JsonDocument err;
            err["error"] = "Server responded with code " + String(code);
            String out;
            serializeJson(err, out);
            request->send(500, "application/json", out);
        }
    );
    server.on("/sensors-status", HTTP_GET, [](AsyncWebServerRequest *request) {
        JsonDocument doc;
        JsonArray arr = doc.to<JsonArray>();
        for (auto const& d : sensorData) {
            JsonObject o = arr.createNestedObject();
            o["type"] = d.type;
            o["value"] = d.lastSecondAverage;
            o["lowest24hValue"] = d.lowest24hValue;
            o["highest24hValue"] = d.highest24hValue;
            o["lowest24hTimestamp"] = d.lowest24hTimestamp;
            o["highest24hTimestamp"] = d.highest24hTimestamp;
            o["avg24h"] = d.avg24h;
        }
        String out;
        serializeJson(doc, out);
        request->send(200, "application/json", out);
    });
    server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            JsonDocument j;
            if (deserializeJson(j, data, len) != DeserializationError::Ok) {
                request->send(400, "application/json", "\"error\":\"Invalid JSON\"");
                return;
            }
            bool modWiFi = false;
            if (j.containsKey("targetSsid")) {
                config.ssid = j["targetSsid"].as<String>();
                saveData("targetSsid", config.ssid);
                modWiFi = true;
            }
            if (j.containsKey("targetPassword")) {
                config.password = j["targetPassword"].as<String>();
                saveData("targetPassword", config.password);
                modWiFi = true;
            }
            if (j.containsKey("floraeAccessKey")) {
                config.floraeKey = j["floraeAccessKey"].as<String>();
                saveData("floraeAccessKey", config.floraeKey);
            }
            if (j.containsKey("wifiTimeout")) {
                config.wifiTimeout = j["wifiTimeout"].as<int>();
                saveData("wifiTimeout", String(config.wifiTimeout));
            }
            if (modWiFi) {
                WiFi.begin(config.ssid.c_str(), config.password.c_str());
                shouldCheckWiFi = true;
            }
            request->send(200, "text/plain", "Configuration saved");
        }
    );
    server.begin();
}

void loop() {
    static unsigned long lastSample = 0;
    static unsigned long lastCheck = 0;
    unsigned long now = millis();
    if (shouldCheckWiFi && now - lastCheck >= 1000) {
        lastCheck = now;
        checkWiFiConnection();
    }
    if (now - lastSample >= 100) {
        lastSample = now;
        int i = 0;
        for (auto& s : sensors) {
            DHT d(s.pin, s.type == DHT11_TYPE ? DHT11 : DHT22);
            d.begin();
            double v = d.readTemperature();
            if (!isnan(v)) {
                auto it = std::next(sensorData.begin(), i);
                it->lastSecondReadings.push_back(v);
                it->currentValue = v;
                unsigned long p = millis();
                if (p - it->lastPurge > 1000) {
                    while (it->lastSecondReadings.size() > 10)
                        it->lastSecondReadings.erase(it->lastSecondReadings.begin());
                    it->lastPurge = p;
                }
                double sum = 0;
                for (double x : it->lastSecondReadings) sum += x;
                if (!it->lastSecondReadings.empty())
                    it->lastSecondAverage = sum / it->lastSecondReadings.size();
                unsigned long ts = millis();
                it->last24hReadings.push_back({v, ts});
                while (!it->last24hReadings.empty() && (ts - it->last24hReadings.front().ts > 86400000UL)) {
                    it->last24hReadings.erase(it->last24hReadings.begin());
                }
                if (!it->last24hReadings.empty()) {
                    double minV = it->last24hReadings[0].value;
                    double maxV = it->last24hReadings[0].value;
                    unsigned long minT = it->last24hReadings[0].ts;
                    unsigned long maxT = it->last24hReadings[0].ts;
                    double sum24 = 0;
                    for (auto const& vt : it->last24hReadings) {
                        if (vt.value < minV) { minV = vt.value; minT = vt.ts; }
                        if (vt.value > maxV) { maxV = vt.value; maxT = vt.ts; }
                        sum24 += vt.value;
                    }
                    it->lowest24hValue = minV;
                    it->highest24hValue = maxV;
                    it->lowest24hTimestamp = minT;
                    it->highest24hTimestamp = maxT;
                    it->avg24h = sum24 / it->last24hReadings.size();
                }
            }
            i++;
        }
    }
}
