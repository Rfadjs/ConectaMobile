package com.example.conectamobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnIrContactos, btnIrPerfil, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular los botones del XML
        btnIrContactos = findViewById(R.id.btnIrContactos);
        btnIrPerfil = findViewById(R.id.btnIrPerfil);
        btnLogout = findViewById(R.id.btnLogout);

        // 1. Ir a la lista de Contactos (Chat)
        btnIrContactos.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ContactsActivity.class));
        });

        // 2. Ir al Perfil (Cambiar foto/nombre)
        btnIrPerfil.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // 3. Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }
}