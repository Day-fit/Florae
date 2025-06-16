#!/bin/sh

USER=${MQTT_USER:-mqttuser}
PASS=${MQTT_PASS:-mqttpass}

if [ ! -f /mosquitto/config/passwd ]; then
  mosquitto_passwd -b -c /mosquitto/config/passwd "$USER" "$PASS"
fi

exec mosquitto -c /mosquitto/config/mosquitto.conf