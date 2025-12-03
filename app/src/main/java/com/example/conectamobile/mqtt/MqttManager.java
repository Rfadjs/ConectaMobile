package com.example.conectamobile.mqtt;

import android.util.Log;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import java.nio.charset.StandardCharsets;

public class MqttManager {

    private static final String TAG = "MqttManager";
    private static final String MQTT_HOST = "broker.hivemq.com";

    // Tópico Base (La carpeta principal)
    private static final String TOPIC_BASE = "ST/CONECTAMOBILE/2025/";

    private Mqtt3AsyncClient client;

    // 1. CONECTAR (Igual que tu proyecto antiguo)
    public void connect(String clientId) {
        try {
            if (client != null && client.getState().isConnected()) return;

            // Usamos la misma construcción que tu proyecto antiguo
            client = MqttClient.builder()
                    .useMqttVersion3() // Forzamos versión 3 como en tu ejemplo
                    .identifier(clientId)
                    .serverHost(MQTT_HOST)
                    .serverPort(1883) // Puerto estándar
                    .buildAsync();

            client.connect().whenComplete((connAck, error) -> {
                if (error != null) {
                    Log.e(TAG, "ERROR AL CONECTAR: " + error.getMessage());
                } else {
                    Log.i(TAG, "CONECTADO A HIVEMQ CORRECTAMENTE");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Excepción al conectar: " + e.getMessage());
        }
    }

    // 2. SUSCRIBIRSE (Para recibir mensajes)
    public void subscribeToUser(String myUid, MqttMessageCallback callback) {
        if (client == null) return;

        // Me suscribo a MI carpeta personal
        String topic = TOPIC_BASE + myUid;

        client.subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    callback.onMessage(topic, payload);
                })
                .send()
                .whenComplete((subAck, error) -> {
                    if (error != null) Log.e(TAG, "Error suscribiendo");
                    else Log.i(TAG, "Suscrito a: " + topic);
                });
    }

    // 3. ENVIAR (Para hablarle a otro)
    public void sendToUser(String targetUid, String message) {
        if (client == null) return;

        // Envío a la carpeta del OTRO usuario
        String topic = TOPIC_BASE + targetUid;

        client.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((pub, error) -> {
                    if (error != null) Log.e(TAG, "Error enviando");
                    else Log.i(TAG, "Enviado a: " + topic);
                });
    }

    public interface MqttMessageCallback {
        void onMessage(String topic, String message);
    }

    public void disconnect() {
        if (client != null) client.disconnect();
    }
}