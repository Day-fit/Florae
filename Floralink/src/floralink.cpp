#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include <LittleFS.h>
#include <ESPDateTime.h>
#include <DHT.h>
#include <deque>
#include <Ticker.h>

#define MAX_MOISTURE_VALUE 2800
#define MIN_MOISTURE_VALUE 800

bool isInDevMode = true;
const String BASE_URL = isInDevMode ? "http://192.168.252.192:8080" : "https://florae.dayfit.pl";

struct Config {
    String ssid;
    String password;
    String floraeKey;
    int wifiTimeout = 30;
};

static Config config;
static bool isConnected = false;
static bool isConnecting = false;
static bool isTimeSet = false;
static bool modApiKey = false;

const char* DEFAULT_PASSWORD = "password";
const unsigned int DEFAULT_TIMEOUT = 30;
unsigned long lastSampleMillis = 0;

bool isDev = false;
const String FLORAE_URL = isDev ? "http://192.168.241.192:8080" : "https://florae.dayfit.pl";

AsyncWebServer server(80);
Ticker ticker;

enum SensorType {
    DHT11_TYPE,
    DHT22_TYPE,
    SOIL_MOISTURE_TYPE,
    LUX_SENSOR_TYPE,
};

struct ValueTimestamp {
    double value;
    time_t timestamp;
};

struct SensorData {
    String type;
    String unit;

    double currentValue;
    double lowest24hValue;
    double highest24hValue;

    double lastSecondAverage = 0.0;
    int lastSample = 0;

    double avg24h = 0.0;

    std::deque<ValueTimestamp> samplesLastSecond;
    std::deque<ValueTimestamp> samples24h;

    time_t lowest24hTimestamp = 0;
    time_t highest24hTimestamp = 0;
};

struct Sensor {
    SensorType sensorType;
    uint8_t pin;
    std::vector<SensorData> sensorReadings;
};

std::vector<Sensor> possibleSensors = {
    {DHT11_TYPE, 16},
    {DHT22_TYPE, 17},
    {SOIL_MOISTURE_TYPE, 34},
    {LUX_SENSOR_TYPE, 32},
};

std::vector<Sensor> sensors;

double calculateMoisture(int rawReading)
{
    rawReading = constrain(rawReading, MIN_MOISTURE_VALUE, MAX_MOISTURE_VALUE);
    return 100 * (MAX_MOISTURE_VALUE - rawReading) / (MAX_MOISTURE_VALUE - MIN_MOISTURE_VALUE);
}

double calculateLux(int rawReading)
{
    return (rawReading / 4095.0) * 100;
}

String formatTimestampISO8601(time_t t) {
    if (t == 0) return "";
    struct tm tmstruct;
    gmtime_r(&t, &tmstruct);
    char buf[40];
    snprintf(buf, sizeof(buf), "%04d-%02d-%02dT%02d:%02d:%02dZ",
        tmstruct.tm_year + 1900,
        tmstruct.tm_mon + 1,
        tmstruct.tm_mday,
        tmstruct.tm_hour,
        tmstruct.tm_min,
        tmstruct.tm_sec
    );
    return String(buf);
}

// void claimApiKey()
// {
//     if (!isConnected)
//     {
//         Serial.println('skipping');
//         return;
//     }
    
//     HTTPClient http;
//     http.setTimeout(3000); // 3s timeout
//     http.begin(BASE_URL + "/api/v1/floralink/connect-api");
//     http.addHeader("X-API-KEY", config.floraeKey);
//     int httpCode = http.POST("");
//     yield(); // prevent watchdog reset
//     http.end();
//     yield();
// }

void sendCurrentData()
{
    DynamicJsonDocument doc(512);
    JsonArray arr = doc.to<JsonArray>();

    for (const auto& sensor : sensors) {
        for (const auto& reading : sensor.sensorReadings) {
            JsonObject obj = arr.createNestedObject();
            obj["type"] = reading.type;
            obj["value"] = String(reading.currentValue, 2);
        }
    }

    String payload;
    serializeJson(arr, payload);

    if (!WiFi.isConnected()) return;
    HTTPClient http;
    http.setTimeout(3000);
    http.begin(BASE_URL + "/api/v1/sensor-data");
    http.addHeader("Content-Type", "application/json");
    http.addHeader("X-API-KEY", config.floraeKey);

    int httpCode = http.POST(payload);
    yield();
    http.end();
    yield();
}

// bool isValidApiKey(String apiKey)
// {
//     if (!isConnected)
//     {
//         return true;
//     }
    

//     HTTPClient http;
//     http.setTimeout(3000);
//     String url = BASE_URL + "/api/v1/check-key?apiKey=" + apiKey;
//     http.begin(url);
//     int httpCode = http.GET();
//     yield();

//     bool isValid = false;

//     if (httpCode == 200) {
//         String payload = http.getString();
//         DynamicJsonDocument doc(256);
//         if (deserializeJson(doc, payload) == DeserializationError::Ok && doc["result"].is<bool>()) {
//             isValid = doc["result"];
//         } else {
//             isValid = false;
//         }
//     }
//     http.end();
//     yield();

//     return isValid;
// }

void sendDailyReport()
{
    DynamicJsonDocument doc(1024);
    JsonArray arr = doc.to<JsonArray>();

    for (const auto& sensor : sensors) {
        for (const auto& reading : sensor.sensorReadings) {
            JsonObject obj = arr.createNestedObject();
            obj["type"] = reading.type;
            obj["minValue"] = reading.lowest24hValue;
            obj["minValueTimestamp"] = formatTimestampISO8601(reading.lowest24hTimestamp);
            obj["maxValue"] = reading.highest24hValue;
            obj["maxValueTimestamp"] = formatTimestampISO8601(reading.highest24hTimestamp);
            obj["averageValue"] = reading.avg24h;
        }
    }

    String payload;
    serializeJson(arr, payload);

    if (!WiFi.isConnected()) return;
    HTTPClient http;
    http.setTimeout(3000);
    http.begin(BASE_URL + "/api/v1/sensor-daily-report");
    http.addHeader("Content-Type", "application/json");
    http.addHeader("X-API-KEY", config.floraeKey);

    int httpCode = http.POST(payload);
    yield();
    http.end();
    yield();
}

void loadConfig() {
    File file = LittleFS.open("/config.json", "r");
    if (!file) return;

    JsonDocument json;

    if (deserializeJson(json, file) == DeserializationError::Ok) {
        config.ssid = json["targetSsid"] | "";
        config.password = json["targetPassword"] | "";
        config.floraeKey = json["floraeAccessKey"] | "";
        config.wifiTimeout = json["wifiTimeout"] | DEFAULT_TIMEOUT;
    }

    file.close();
}

void saveConfig() {
    JsonDocument json;

    json["targetSsid"] = config.ssid;
    json["targetPassword"] = config.password;
    json["floraeAccessKey"] = config.floraeKey;
    json["wifiTimeout"] = config.wifiTimeout;

    File file = LittleFS.open("/config.json", "w");
    if (!file) return;
    serializeJson(json, file);
    file.close();
}

void checkWiFiConnection() {
    static int timeoutCounter = 0;
    isConnecting = true;

    if (WiFi.isConnected()) {
        isConnected = true;
        isConnecting = false;
        timeoutCounter = 0;
        DateTime.setTimeZone("CET-1CEST,M3.5.0/2,M10.5.0/3");
        isTimeSet = true;
        ticker.detach();

        if (modApiKey)
        {
            // claimApiKey();
        }

        return;
    }

    timeoutCounter++;

    if (timeoutCounter >= config.wifiTimeout) {
        WiFi.disconnect(false);
        timeoutCounter = 0;
        isConnecting = false;
    }
}

void loadSensors() {
    for (auto const& sCfg : possibleSensors) {
        Sensor s;
        s.sensorType = sCfg.sensorType;
        s.pin = sCfg.pin;
        switch (sCfg.sensorType)
        {
            case DHT22_TYPE:
            case DHT11_TYPE:
            {
                DHT dht(s.pin, s.sensorType == DHT11_TYPE? DHT11 : DHT22);
                pinMode(s.pin, INPUT);
                dht.begin();

                double h = dht.readHumidity();
                double t = dht.readTemperature();

                if (!isnan(h) && !isnan(t)) {
                    SensorData tempData;
                    tempData.type = "temperature";
                    tempData.currentValue = 0;
                    tempData.lowest24hValue = std::numeric_limits<double>::infinity();
                    tempData.highest24hValue = -std::numeric_limits<double>::infinity();
                    tempData.lastSecondAverage = 0;
                    tempData.avg24h = 0;
                    tempData.lowest24hTimestamp = 0;
                    tempData.highest24hTimestamp = 0;
                    tempData.unit = "&deg; C";

                    SensorData humData;
                    humData.type = "humidity";
                    humData.currentValue = 0;
                    humData.lowest24hValue = std::numeric_limits<double>::infinity();
                    humData.highest24hValue = -std::numeric_limits<double>::infinity();
                    humData.lastSecondAverage = 0;
                    humData.avg24h = 0;
                    humData.lowest24hTimestamp = 0;
                    humData.highest24hTimestamp = 0;
                    humData.unit = "% RH";

                    s.sensorReadings.push_back(tempData);
                    s.sensorReadings.push_back(humData);

                    sensors.push_back(std::move(s));
                }
                break;
            }

            case SOIL_MOISTURE_TYPE:
            {
                int raw = analogRead(sCfg.pin);

                if (raw != 4095.0 && raw != 0)
                {
                    SensorData readings;

                    readings.type = "soilMoisture";
                    readings.currentValue = calculateMoisture(raw);
                    readings.avg24h = 0;
                    readings.lowest24hValue = std::numeric_limits<double>::infinity();
                    readings.highest24hValue = - std::numeric_limits<double>::infinity();
                    readings.lastSecondAverage = 0;
                    readings.highest24hTimestamp = 0;
                    readings.lowest24hTimestamp = 0;
                    readings.unit = "%";

                    s.sensorReadings.push_back(readings);

                    sensors.push_back(std::move(s));
                    break;
                }

                break;
            }

            case LUX_SENSOR_TYPE:
            {
                int raw = analogRead(sCfg.pin);

                if (raw != 4095.0 && raw != 0)
                {
                    SensorData readings;

                    readings.type = "light";
                    readings.currentValue = calculateLux(raw);
                    readings.avg24h = 0;
                    readings.lowest24hValue = std::numeric_limits<double>::infinity();
                    readings.highest24hValue = - std::numeric_limits<double>::infinity();
                    readings.highest24hTimestamp = 0;
                    readings.lowest24hTimestamp = 0;
                    readings.unit = "LUX";

                    s.sensorReadings.push_back(readings);

                    sensors.push_back(std::move(s));
                    break;
                }

                break;
            }

            default:
                break;
        }
    }
}

void sampleSensors()
{
    if (!WiFi.isConnected() || !isTimeSet)
    {
        return;
    }
    
    time_t now = DateTime.now();

    for (auto& sensor : sensors)
    {
        switch (sensor.sensorType)
        {
            case DHT11_TYPE:
            case DHT22_TYPE:
            {
                DHT dht(sensor.pin, sensor.sensorType == DHT11_TYPE? DHT11 : DHT22);
                dht.begin();

                double humidity = dht.readHumidity();
                double temperature = dht.readTemperature();

                for (auto& sensorData : sensor.sensorReadings)
                {
                    double value = sensorData.type == "humidity" ? humidity : sensorData.type == "temperature" ? temperature : NAN;
                    if (isnan(value)) continue;

                    sensorData.currentValue = value;

                    if (value < sensorData.lowest24hValue) {
                        sensorData.lowest24hValue = value;
                        sensorData.lowest24hTimestamp = now;
                    }

                    if (value > sensorData.highest24hValue) {
                        sensorData.highest24hValue = value;
                        sensorData.highest24hTimestamp = now;
                    }

                    sensorData.samples24h.push_back({value, now});
                    sensorData.samplesLastSecond.push_back({value, now});

                    while (!sensorData.samples24h.empty() && now - sensorData.samples24h.front().timestamp > 86400) {
                        sensorData.samples24h.pop_front();
                    }

                    while (!sensorData.samplesLastSecond.empty() && now - sensorData.samplesLastSecond.front().timestamp > 1) {
                        sensorData.samplesLastSecond.pop_front();
                    }

                    double sumLastSecond = 0;
                    for (const auto& sample : sensorData.samplesLastSecond) {
                        sumLastSecond += sample.value;
                    }
                    sensorData.lastSecondAverage = sensorData.samplesLastSecond.empty() ? 0 : sumLastSecond / sensorData.samplesLastSecond.size();

                    double sum24h = 0;
                    for (const auto& sample : sensorData.samples24h) {
                        sum24h += sample.value;
                    }
                    sensorData.avg24h = sensorData.samples24h.empty() ? 0 : sum24h / sensorData.samples24h.size();

                    sensorData.lastSample = static_cast<int>(value);
                }
                
                break;
            }

            case SOIL_MOISTURE_TYPE:
            {
                for (auto &sensorData : sensor.sensorReadings)
                {
                    int rawReading = analogRead(sensor.pin);
                    double value = sensorData.type == "soilMoisture"? calculateMoisture(rawReading) : NAN;

                    if (isnan(value)) continue;

                    sensorData.currentValue = value;

                    if (value < sensorData.lowest24hValue) {
                        sensorData.lowest24hValue = value;
                        sensorData.lowest24hTimestamp = now;
                    }

                    if (value > sensorData.highest24hValue) {
                        sensorData.highest24hValue = value;
                        sensorData.highest24hTimestamp = now;
                    }

                    sensorData.samples24h.push_back({value, now});
                    sensorData.samplesLastSecond.push_back({value, now});

                    while (!sensorData.samples24h.empty() && now - sensorData.samples24h.front().timestamp > 86400) {
                        sensorData.samples24h.pop_front();
                    }

                    while (!sensorData.samplesLastSecond.empty() && now - sensorData.samplesLastSecond.front().timestamp > 1) {
                        sensorData.samplesLastSecond.pop_front();
                    }

                    double sumLastSecond = 0;

                    for (const auto& sample : sensorData.samplesLastSecond) {
                        sumLastSecond += sample.value;
                    }

                    sensorData.lastSecondAverage = sensorData.samplesLastSecond.empty() ? 0 : sumLastSecond / sensorData.samplesLastSecond.size();

                    double sum24h = 0;

                    for (const auto& sample : sensorData.samples24h) {
                        sum24h += sample.value;
                    }

                    sensorData.avg24h = sensorData.samples24h.empty() ? 0 : sum24h / sensorData.samples24h.size();
                    sensorData.lastSample = static_cast<int>(value);
                    break;
                }

                break;
            }

            case LUX_SENSOR_TYPE:
            {
                for (auto &sensorData : sensor.sensorReadings)
                {
                    int rawReading = analogRead(sensor.pin);
                    double value = sensorData.type == "light"? calculateLux(rawReading) : NAN;

                    if (isnan(value)) continue;

                    sensorData.currentValue = value;

                    if (value < sensorData.lowest24hValue) {
                        sensorData.lowest24hValue = value;
                        sensorData.lowest24hTimestamp = now;
                    }

                    if (value > sensorData.highest24hValue) {
                        sensorData.highest24hValue = value;
                        sensorData.highest24hTimestamp = now;
                    }

                    sensorData.samples24h.push_back({value, now});
                    sensorData.samplesLastSecond.push_back({value, now});

                    while (!sensorData.samples24h.empty() && now - sensorData.samples24h.front().timestamp > 86400) {
                        sensorData.samples24h.pop_front();
                    }

                    while (!sensorData.samplesLastSecond.empty() && now - sensorData.samplesLastSecond.front().timestamp > 1) {
                        sensorData.samplesLastSecond.pop_front();
                    }

                    double sumLastSecond = 0;

                    for (const auto& sample : sensorData.samplesLastSecond) {
                        sumLastSecond += sample.value;
                    }

                    sensorData.lastSecondAverage = sensorData.samplesLastSecond.empty() ? 0 : sumLastSecond / sensorData.samplesLastSecond.size();

                    double sum24h = 0;

                    for (const auto& sample : sensorData.samples24h) {
                        sum24h += sample.value;
                    }

                    sensorData.avg24h = sensorData.samples24h.empty() ? 0 : sum24h / sensorData.samples24h.size();
                    sensorData.lastSample = static_cast<int>(value);
                    break;
                }

                break;
            }

            default:
                break;
        }
    }
}


void setup() {
    isTimeSet = false;
    DateTime.begin();
    Serial.begin(115200);

    Serial.println("Starting WiFi AP");
    WiFi.mode(WIFI_AP_STA);
    WiFi.softAP("FloraLink", DEFAULT_PASSWORD);

    struct tm DEFAULT_TIME = {};
    DEFAULT_TIME.tm_year = 2025 - 1900;
    DEFAULT_TIME.tm_mon = 0;
    DEFAULT_TIME.tm_mday = 1;
    DEFAULT_TIME.tm_hour = 12;
    DEFAULT_TIME.tm_min = 0;
    DEFAULT_TIME.tm_sec = 0;

    DateTime.setTime(mktime(&DEFAULT_TIME));
    LittleFS.begin();
    loadConfig();

    if (config.ssid.length() && config.password.length()) {
        WiFi.begin(config.ssid.c_str(), config.password.c_str());
        ticker.attach(1, checkWiFiConnection);
    }

    Serial.println("Loading Sensors");
    loadSensors();

    Serial.println("Starting web server");
    server.serveStatic("/", LittleFS, "/").setDefaultFile("index.html");

    server.on("/validate-api-key", HTTP_GET, [](AsyncWebServerRequest *request)
    {
        String response = String("{\"result\":") + ("true") + "}";
        request->send(200, "application/json", response);
    });

    server.on("/connection-status", HTTP_GET, [](AsyncWebServerRequest *request){
        JsonDocument json;

        json["isConnected"] = isConnected;
        json["isConnecting"] = isConnecting;
        json["ip"] = WiFi.localIP().toString();

        String out;
        serializeJson(json, out);
        request->send(200, "application/json", out);
    });

    server.on("/sensors-status", HTTP_GET, [](AsyncWebServerRequest *request) {
        JsonDocument doc;
        JsonArray arr = doc.to<JsonArray>();

        for (const Sensor &sensor : sensors)
        {
            for (const auto& reading : sensor.sensorReadings) {
                JsonObject object = arr.add<JsonObject>();

                object["type"] = reading.type;
                object["value"] = round(reading.lastSecondAverage * 100.0) / 100.0;
                object["minValue"] = reading.lowest24hValue;
                object["maxValue"] = reading.highest24hValue;
                object["minValueTimestamp"] = formatTimestampISO8601(reading.lowest24hTimestamp);
                object["maxValueTimestamp"] = formatTimestampISO8601(reading.highest24hTimestamp);
                object["averageValue"] = round(reading.avg24h * 100.0) / 100.0;
                object["unit"] = reading.unit;
            }
        }

        String out;
        serializeJson(doc, out);
        request->send(200, "application/json", out);
    });

    server.on("/save", HTTP_POST, [](AsyncWebServerRequest *request){}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            JsonDocument json;

            bool modifiesWifi = false;
            bool modifiesApiKey = false;
            bool configChanged = false;

            if (deserializeJson(json, data, len) != DeserializationError::Ok) {
                request->send(400, "application/json", "\"error\":\"Invalid JSON\"\"");
                return;
            }

            if (json["targetSsid"].is<String>() && config.ssid != json["targetSsid"].as<String>()) {
                config.ssid = json["targetSsid"].as<String>();
                modifiesWifi = true;
                configChanged = true;
            }

            if (json["targetPassword"].is<String>() && config.password != json["targetPassword"].as<String>()) {
                config.password = json["targetPassword"].as<String>();
                modifiesWifi = true;
                configChanged = true;
            }

            if (json["floraeAccessKey"].is<String>() && config.floraeKey != json["floraeAccessKey"].as<String>()) {
                config.floraeKey = json["floraeAccessKey"].as<String>();
                modifiesApiKey = true;
                configChanged = true;
            }

            if (json["wifiTimeout"].is<int>() && config.wifiTimeout != json["wifiTimeout"].as<int>()) {
                config.wifiTimeout = json["wifiTimeout"].as<int>();
                configChanged = true;
            }

            request->send(200, "text/plain", "Configuration saved");

            if (configChanged) {
                saveConfig();
            }

            modApiKey = modifiesApiKey;

            if (modifiesWifi) {
                WiFi.begin(config.ssid.c_str(), config.password.c_str());
                ticker.attach(1, checkWiFiConnection);
            }

            if (modifiesApiKey && !modifiesWifi)
            {
                // claimApiKey();
            }
        }
    );

    server.begin();
}

void loop() {
    if (millis() - lastSampleMillis >= 1000) {
        lastSampleMillis = millis();
        sampleSensors();
        //sendCurrentData();
    }

    yield();
}