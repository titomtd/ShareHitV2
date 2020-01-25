package com.example.sharehitv2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Model.User;
import com.example.sharehitv2.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListLikeAdapter extends
        RecyclerView.Adapter<ListLikeAdapter.ViewHolder> {

    Context context;
    private List<User> mUser;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    public ListLikeAdapter(List<User> users) {
        this.mUser = users;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public ListLikeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        View userView = inflater.inflate(R.layout.like_user_profil_display, parent, false);

        ListLikeAdapter.ViewHolder viewHolder = new ListLikeAdapter.ViewHolder(userView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ListLikeAdapter.ViewHolder viewHolder, final int position) {
        final User user = mUser.get(position);
        TextView textView = viewHolder.pseudoUser;
        textView.setText(user.getPseudo());
        final StorageReference filepath = mStorageRef;
        final CircleImageView circleImageView = viewHolder.pictureUser;

        filepath.child(user.userId).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+user.userId+"?alt=media").fit().centerInside().into(circleImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Picasso.with(context).load("").fit().centerInside().into(viewHolder.getImgProfil());
                circleImageView.setImageResource(R.drawable.default_profile_picture);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public void clear() {
        mUser.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<User> list) {
        mUser.addAll(list);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView pseudoUser;
        public CircleImageView pictureUser;

        public ViewHolder(View itemView) {
            super(itemView);
            pseudoUser = (TextView) itemView.findViewById(R.id.pseudoProfilListLike);
            pictureUser = (CircleImageView) itemView.findViewById(R.id.imageProfilListLike);

        }
    }
}
