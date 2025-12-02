package com.example.conectamobile.mqtt;

import android.util.Log;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

import java.nio.charset.StandardCharsets;

public class MqttManager {

    private static final String TAG = "MqttManager";

    // Tu broker:
    private static final String MQTT_HOST = "broker.hivemq.com";

    // Tu topic base:
    // EJEMPLO DE ENVÍO REAL:
    // ST/CONECTAMOBILE/2025/UID_DEL_DESTINATARIO
    private static final String TOPIC_BASE = "ST/CONECTAMOBILE/2025/";

    private Mqtt3AsyncClient client;


    // ------------------------------
    // 1) CONECTAR A MQTT
    // ------------------------------
    public void connect(String clientId) {
        try {
            if (client != null && client.getState().isConnected()) return;

            client = Mqtt3Client.builder()
                    .identifier(clientId)          // Usar el UID del usuario
                    .serverHost(MQTT_HOST)
                    .serverPort(1883)
                    .buildAsync();

            client.connect().whenComplete((connAck, error) -> {
                if (error != null) {
                    Log.e(TAG, "MQTT CONNECTION ERROR → " + error.getMessage());
                } else {
                    Log.i(TAG, "MQTT CONNECTED SUCCESSFULLY");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "MQTT connect exception → " + e.getMessage());
        }
    }


    // ------------------------------
    // 2) SUSCRIBIRSE A TU PROPIO TOPIC
    // ------------------------------
    public void subscribeToUser(String myUid, MqttMessageCallback callback) {
        if (client == null) return;

        String topic = TOPIC_BASE + myUid;

        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String payload = "";
                    if (publish.getPayload() != null) {
                        payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    }
                    callback.onMessage(topic, payload);
                })
                .send()
                .whenComplete((subAck, error) -> {
                    if (error != null)
                        Log.e(TAG, "MQTT SUBSCRIBE ERROR → " + error.getMessage());
                    else
                        Log.i(TAG, "SUBSCRIBED TO → " + topic);
                });
    }


    // ------------------------------
    // 3) ENVIAR MENSAJES A OTRO USUARIO POR MQTT
    // ------------------------------
    public void sendToUser(String targetUid, String message) {
        if (client == null) return;

        String topic = TOPIC_BASE + targetUid;

        client.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((pub, error) -> {
                    if (error != null)
                        Log.e(TAG, "MQTT PUBLISH ERROR → " + error.getMessage());
                    else
                        Log.i(TAG, "MQTT SENT → topic: " + topic + ", msg: " + message);
                });
    }


    // ------------------------------
    // 4) CALLBACK INTERFACE
    // ------------------------------
    public interface MqttMessageCallback {
        void onMessage(String topic, String message);
    }


    // ------------------------------
    // 5) DESCONECTAR
    // ------------------------------
    public void disconnect() {
        try {
            if (client != null) client.disconnect();
        } catch (Exception ignored) { }
    }
}
