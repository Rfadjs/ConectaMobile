package com.example.conectamobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.adapters.MessagesAdapter;
import com.example.conectamobile.models.Message;
import com.example.conectamobile.mqtt.MqttManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private EditText txtMessage;
    private Button btnSend;
    private RecyclerView listMessages;

    private String myUid;
    private String peerUid;

    private MqttManager mqttManager;

    private ArrayList<Message> messages;
    private MessagesAdapter adapter;
    private DatabaseReference messagesRef;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Obtener datos del Intent y Usuario actual
        myUid = FirebaseAuth.getInstance().getUid();
        peerUid = getIntent().getStringExtra("peerUid");

        // Validación de seguridad para que no se cierre la app
        if (myUid == null || peerUid == null) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Vincular Vistas
        txtMessage = findViewById(R.id.txtMessage);
        btnSend = findViewById(R.id.btnSend);
        listMessages = findViewById(R.id.listMessages);

        // 3. Configurar RecyclerView
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(messages, myUid);
        listMessages.setLayoutManager(new LinearLayoutManager(this));
        listMessages.setAdapter(adapter);

        // 4. Referencia a Firebase (Historial)
        // Ruta: messages / MI_ID / ID_DEL_OTRO
        messagesRef = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(myUid)
                .child(peerUid);

        // 5. Configurar MQTT
        mqttManager = new MqttManager();
        mqttManager.connect(myUid); // Me conecto con MI identidad

        // ESCUCHAR: Me suscribo a MI propio tópico para recibir mensajes
        mqttManager.subscribeToUser(myUid, (topic, text) -> {
            runOnUiThread(() -> receiveMessage(text));
        });

        // 6. Cargar historial antiguo
        loadMessages();

        // 7. Botón Enviar
        btnSend.setOnClickListener(v -> sendMessage());
    }

    /** ============================================
     * ENVÍO DE MENSAJE (YO -> OTRO)
     * ============================================ */
    private void sendMessage() {
        String text = txtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        long timestamp = System.currentTimeMillis();

        Message msg = new Message(
                null,
                myUid,    // Sender: YO
                peerUid,  // Receiver: EL OTRO
                text,
                timestamp
        );

        // A. Guardar en MI historial de Firebase
        String id = messagesRef.push().getKey();
        msg.id = id;
        messagesRef.child(id).setValue(msg);

        // B. Enviar por MQTT al tópico del OTRO (Velocidad)
        mqttManager.sendToUser(peerUid, text);

        // Limpiar caja de texto
        txtMessage.setText("");
    }

    /** ============================================
     * RECIBIR MENSAJE (EL OTRO -> YO)
     * ============================================ */
    private void receiveMessage(String msgText) {
        long timestamp = System.currentTimeMillis();

        // Creamos el objeto mensaje invertido (porque lo recibí)
        Message msg = new Message(
                null,
                peerUid,  // Sender: EL OTRO
                myUid,    // Receiver: YO
                msgText,
                timestamp
        );

        // A. Guardar en MI historial de Firebase
        // Como messagesRef apunta a "messages/MY_UID/PEER_UID", al guardarlo aquí
        // se queda en mi historial para siempre.
        String id = messagesRef.push().getKey();
        msg.id = id;
        messagesRef.child(id).setValue(msg);

        // B. Actualizar la pantalla (Opcional, porque el listener de abajo ya lo hace,
        // pero esto lo hace más rápido visualmente)
        // adapter.add(msg);
        // listMessages.scrollToPosition(adapter.getItemCount() - 1);

        Log.d(TAG, "Mensaje recibido por MQTT: " + msgText);
    }

    /** ============================================
     * CARGAR HISTORIAL DE FIREBASE
     * ============================================ */
    private void loadMessages() {
        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Message msg = child.getValue(Message.class);
                    if (msg != null) messages.add(msg);
                }
                adapter.notifyDataSetChanged();
                // Bajar al último mensaje
                if (messages.size() > 0) {
                    listMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desconectar al salir para ahorrar batería/datos
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
    }
}