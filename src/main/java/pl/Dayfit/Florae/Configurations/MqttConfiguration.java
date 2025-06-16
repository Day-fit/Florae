package pl.Dayfit.Florae.Configurations;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

@Configuration
public class MqttConfiguration {
    @Value("${mqtt.broker.username}")
    private String MQTT_USERNAME;

    @Value("${mqtt.broker.password}")
    private String MQTT_PASSWORD;

    @Value("${mqtt.broker.host}")
    private String MQTT_HOST;

    @Bean
    MqttPahoClientFactory mqttPahoClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setUserName(MQTT_USERNAME);
        options.setPassword(MQTT_PASSWORD.toCharArray());
        options.setServerURIs(new String[] { MQTT_HOST });

        DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
        clientFactory.setConnectionOptions(options);
        return clientFactory;
    }
}
