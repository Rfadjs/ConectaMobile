package com.example.conectamobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.models.Message;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private static final int TYPE_MINE = 1;
    private static final int TYPE_THEIRS = 2;

    private List<Message> list;
    private String myUid;

    // Variables para saber qué gato usa cada uno
    private int myPhotoId;
    private int peerPhotoId;

    // Constructor actualizado: Ahora recibe los IDs de los gatos
    public MessagesAdapter(List<Message> list, String myUid, int myPhotoId, int peerPhotoId) {
        this.list = list;
        this.myUid = myUid;
        this.myPhotoId = myPhotoId;
        this.peerPhotoId = peerPhotoId;
    }

    @Override
    public int getItemViewType(int position) {
        Message m = list.get(position);
        return m.senderId != null && m.senderId.equals(myUid) ? TYPE_MINE : TYPE_THEIRS;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_MINE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_mine, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_theirs, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = list.get(position);
        holder.tvMessage.setText(message.text);

        // LÓGICA DE LOS GATOS:
        // Si el mensaje es MIO -> uso myPhotoId
        // Si el mensaje es DEL OTRO -> uso peerPhotoId
        int targetPhotoId = (getItemViewType(position) == TYPE_MINE) ? myPhotoId : peerPhotoId;

        if (targetPhotoId == 2) {
            holder.imgProfile.setImageResource(R.drawable.img_default_2);
        } else {
            holder.imgProfile.setImageResource(R.drawable.img_default_1);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void add(Message message) {
        list.add(message);
        notifyItemInserted(list.size() - 1);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        ImageView imgProfile; // Aquí conectamos la imagen del XML

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}