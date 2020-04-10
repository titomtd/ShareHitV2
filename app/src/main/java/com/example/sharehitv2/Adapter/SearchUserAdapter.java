package com.example.sharehitv2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Model.User;
import com.example.sharehitv2.ProfilPage;
import com.example.sharehitv2.ProfilPageAncienne;
import com.example.sharehitv2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchUserAdapter extends
        RecyclerView.Adapter<SearchUserAdapter.ViewHolder>{

    Context context;
    private List<User> mUser;
    private StorageReference mStorageRef;


    public SearchUserAdapter(List<User> users) {
        this.mUser = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View userView = inflater.inflate(R.layout.search_user_display, parent, false);

        ViewHolder viewHolder = new ViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = mUser.get(position);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        final Bundle b = new Bundle();
        final Intent intent3 = new Intent(context, ProfilPage.class);

        holder.pseudoProfil.setText(user.getPseudo());
        final StorageReference filepath = mStorageRef;
        filepath.child(user.getUserId()).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/sharehitv2.appspot.com/o/"+user.getUserId()+"?alt=media").fit().centerInside().into(holder.imageProfil);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Picasso.with(context).load("").fit().centerInside().into(viewHolder.getImgProfil());
                holder.imageProfil.setImageResource(R.drawable.default_profile_picture);
            }
        });

        holder.imageProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", user.userId);
                intent3.putExtras(b);
                context.startActivity(intent3);
            }
        });
        holder.pseudoProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", user.userId);
                intent3.putExtras(b);
                context.startActivity(intent3);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageProfil;
        TextView pseudoProfil;


        public ViewHolder(View itemView) {
            super(itemView);
            imageProfil = itemView.findViewById(R.id.imageSearchUser);
            pseudoProfil = itemView.findViewById(R.id.pseudoSearchUser);
        }
    }
}
