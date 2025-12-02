package com.example.conectamobile.models;

public class User {
    public String uid;
    public String name;
    public String email;
    public String photoUrl; // <-- Agrega esta línea

    // Constructor vacío requerido por Firebase
    public User() {}

    // Constructor completo opcional
    public User(String uid, String name, String email, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }
}
