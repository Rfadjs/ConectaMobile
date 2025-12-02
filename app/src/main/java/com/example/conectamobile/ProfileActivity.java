package com.example.conectamobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail;
    private ImageView ivProfile;
    private Button btnChangePhoto, btnSave, btnBack, btnLogout;

    // Control para alternar imágenes
    // 1 = Gato Siames, 2 = Gato Gris
    private int currentPhotoId = 1;

    private FirebaseUser user;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        ivProfile = findViewById(R.id.ivProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);

        user = FirebaseAuth.getInstance().getCurrentUser();
        // Conectamos solo a la base de datos (que es GRATIS)
        db = FirebaseDatabase.getInstance().getReference("users");

        // Cargar datos actuales
        loadUserData();

        // LOGICA: Botón para cambiar visualmente la foto
        btnChangePhoto.setOnClickListener(v -> {
            toggleImage();
        });

        // Guardar cambios en la Base de Datos
        btnSave.setOnClickListener(v -> saveChanges());

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Cambia la variable local y la imagen en pantalla
    private void toggleImage() {
        if (currentPhotoId == 1) {
            currentPhotoId = 2;
            ivProfile.setImageResource(R.drawable.img_default_2);
        } else {
            currentPhotoId = 1;
            ivProfile.setImageResource(R.drawable.img_default_1);
        }
    }

    private void loadUserData() {
        if (user == null) return;

        db.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                // Leemos el ID de la foto (por defecto 1 si no existe)
                Integer photoId = snapshot.child("photoId").getValue(Integer.class);

                etName.setText(name != null ? name : "");
                etEmail.setText(email != null ? email : "");

                // Cargar la foto correcta según lo que diga la base de datos
                if (photoId != null && photoId == 2) {
                    currentPhotoId = 2;
                    ivProfile.setImageResource(R.drawable.img_default_2);
                } else {
                    currentPhotoId = 1;
                    ivProfile.setImageResource(R.drawable.img_default_1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void saveChanges() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Guardamos el nombre
        db.child(user.getUid()).child("name").setValue(newName);

        // 2. Guardamos EL NUMERO de la foto (1 o 2) en la base de datos
        // Esto es gratis y no requiere Storage
        db.child(user.getUid()).child("photoId").setValue(currentPhotoId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}