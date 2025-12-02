package com.example.conectamobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.models.Message;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constantes para identificar quién envía el mensaje
    private static final int TYPE_MINE = 1;   // Mensaje MÍO (Derecha)
    private static final int TYPE_THEIRS = 2; // Mensaje DEL OTRO (Izquierda)

    private List<Message> list;
    private String myUid;

    // Constructor: Recibe la lista de mensajes y MI ID para saber quién soy
    public MessagesAdapter(List<Message> list, String myUid) {
        this.list = list;
        this.myUid = myUid;
    }

    // 1. ESTA ES LA MAGIA: Decide qué diseño usar según quién envió el mensaje
    @Override
    public int getItemViewType(int position) {
        Message m = list.get(position);

        // Si el ID del remitente es igual a MI ID -> Es MÍO (TYPE_MINE)
        // Si no -> Es DEL OTRO (TYPE_THEIRS)
        if (m.senderId != null && m.senderId.equals(myUid)) {
            return TYPE_MINE;
        } else {
            return TYPE_THEIRS;
        }
    }

    // 2. Infla el diseño correcto (Verde o Blanco) basado en la decisión de arriba
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_MINE) {
            // Cargar diseño para mis mensajes (A la derecha)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_mine, parent, false);
        } else {
            // Cargar diseño para mensajes del otro (A la izquierda)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_theirs, parent, false);
        }

        return new MessageViewHolder(view);
    }

    // 3. Pone el texto dentro del globito
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = list.get(position);
        // Casteamos al ViewHolder para acceder a sus vistas
        ((MessageViewHolder) holder).bind(message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Método auxiliar para agregar mensajes nuevos a la lista y refrescar
    public void add(Message message) {
        list.add(message);
        notifyItemInserted(list.size() - 1);
    }

    // Clase interna para manejar las vistas (El texto del mensaje)
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(Message message) {
            tvMessage.setText(message.text);
        }
    }
}