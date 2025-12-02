package com.example.conectamobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conectamobile.R;
import com.example.conectamobile.models.User;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private Context context;
    private List<User> users;
    private OnItemClickListener listener;

    public ContactsAdapter(Context context, List<User> users, OnItemClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvName.setText(user.name);

        // Si tu XML tiene un TextView para el email, úsalo. Si no, comenta esta línea:
        // holder.tvEmail.setText(user.email);

        // --- LÓGICA DE LOS GATOS ---
        // Verificamos el campo 'photoUrl' (que es donde guardamos el ID como string)
        try {
            if ("2".equals(String.valueOf(user.photoUrl)) || "2".equals(user.photoUrl)) {
                holder.ivProfile.setImageResource(R.drawable.img_default_2);
            } else {
                holder.ivProfile.setImageResource(R.drawable.img_default_1);
            }
        } catch (Exception e) {
            holder.ivProfile.setImageResource(R.drawable.img_default_1);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<User> filtered) {
        users = filtered;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageView ivProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            // Asegúrate que estos IDs coincidan con tu item_contact.xml
            tvName = itemView.findViewById(R.id.tvContactName);
            tvEmail = itemView.findViewById(R.id.tvContactEmail); // Opcional si lo agregaste
            ivProfile = itemView.findViewById(R.id.imgContactPhoto);
        }
    }
}