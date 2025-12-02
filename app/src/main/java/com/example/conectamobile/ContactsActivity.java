package com.example.conectamobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.adapters.ContactsAdapter;
import com.example.conectamobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView rvContacts;
    private EditText etSearch;
    private ImageButton btnProfile;

    private List<User> usersList;
    private ContactsAdapter adapter;
    private DatabaseReference usersRef;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Vincular Vistas
        rvContacts = findViewById(R.id.rvContacts);
        etSearch = findViewById(R.id.etSearch);

        // Habilitamos el botón de perfil (asegúrate que en tu XML el ID sea btnProfile)
        btnProfile = findViewById(R.id.btnProfile);

        myUid = FirebaseAuth.getInstance().getUid();
        usersList = new ArrayList<>();

        // Configurar Adaptador y Click al Chat
        adapter = new ContactsAdapter(this, usersList, user -> {
            Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
            i.putExtra("peerUid", user.uid); // ID para saber con quién hablo
            i.putExtra("name", user.name);   // Nombre para el título del chat
            startActivity(i);
        });

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadUsers();

        // Buscador
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Botón Perfil
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);

                    if (user != null) {
                        // --- LA LÍNEA MÁGICA ---
                        // Asignamos el UID usando la clave de la carpeta de Firebase
                        // Esto soluciona que la lista aparezca vacía
                        user.uid = child.getKey();

                        // Solo agregar si no soy yo mismo
                        if (user.uid != null && !user.uid.equals(myUid)) {
                            usersList.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactsActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        List<User> filtered = new ArrayList<>();
        for (User u : usersList) {
            if (u.name != null && u.name.toLowerCase().contains(text.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.updateList(filtered);
    }
}