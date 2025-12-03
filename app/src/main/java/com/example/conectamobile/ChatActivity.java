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

    // IDs de las fotos (Por defecto gato 1)
    private int myPhotoId = 1;
    private int peerPhotoId = 1;

    private MqttManager mqttManager;
    private ArrayList<Message> messages;
    private MessagesAdapter adapter;
    private DatabaseReference messagesRef;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myUid = FirebaseAuth.getInstance().getUid();
        peerUid = getIntent().getStringExtra("peerUid");

        if (myUid == null || peerUid == null) {
            finish(); return;
        }

        txtMessage = findViewById(R.id.txtMessage);
        btnSend = findViewById(R.id.btnSend);
        listMessages = findViewById(R.id.listMessages);
        listMessages.setLayoutManager(new LinearLayoutManager(this));

        messages = new ArrayList<>();

        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(myUid).child(peerUid);

        mqttManager = new MqttManager();
        mqttManager.connect(myUid);
        mqttManager.subscribeToUser(myUid, (topic, text) -> runOnUiThread(() -> receiveMessage(text)));

        // --- PRIMERO CARGAMOS LAS FOTOS, LUEGO EL CHAT ---
        loadUserPhotosAndInitChat();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // Método nuevo para buscar qué gato usa cada uno
    private void loadUserPhotosAndInitChat() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 1. Buscar MI foto
        usersRef.child(myUid).child("photoId").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                try { myPhotoId = Integer.parseInt(task.getResult().getValue().toString()); } catch (Exception e){}
            }

            // 2. Buscar SU foto
            usersRef.child(peerUid).child("photoId").get().addOnCompleteListener(task2 -> {
                if (task2.isSuccessful() && task2.getResult().getValue() != null) {
                    try { peerPhotoId = Integer.parseInt(task2.getResult().getValue().toString()); } catch (Exception e){}
                }

                // 3. INICIAR ADAPTADOR CON LAS FOTOS CORRECTAS
                adapter = new MessagesAdapter(messages, myUid, myPhotoId, peerPhotoId);
                listMessages.setAdapter(adapter);

                // 4. Cargar mensajes
                loadMessages();
            });
        });
    }

    private void sendMessage() {
        String text = txtMessage.getText().toString().trim();
        if (text.isEmpty()) return;
        long timestamp = System.currentTimeMillis();
        Message msg = new Message(null, myUid, peerUid, text, timestamp);

        String id = messagesRef.push().getKey();
        msg.id = id;
        messagesRef.child(id).setValue(msg);
        mqttManager.sendToUser(peerUid, text);
        txtMessage.setText("");
    }

    private void receiveMessage(String msgText) {
        long timestamp = System.currentTimeMillis();
        Message msg = new Message(null, peerUid, myUid, msgText, timestamp);
        String id = messagesRef.push().getKey();
        msg.id = id;
        messagesRef.child(id).setValue(msg);

        // Actualizamos la lista visualmente también
        adapter.add(msg);
        listMessages.scrollToPosition(adapter.getItemCount() - 1);
    }

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
                if (messages.size() > 0) listMessages.scrollToPosition(messages.size() - 1);
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttManager != null) mqttManager.disconnect();
    }
}