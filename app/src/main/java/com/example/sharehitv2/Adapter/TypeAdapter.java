package com.example.sharehitv2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Model.Album;
import com.example.sharehitv2.Model.Artist;
import com.example.sharehitv2.Model.Film;
import com.example.sharehitv2.Model.JeuVideo;
import com.example.sharehitv2.Model.Morceau;
import com.example.sharehitv2.Model.Serie;
import com.example.sharehitv2.Model.Type;
import com.example.sharehitv2.Model.Video;
import com.example.sharehitv2.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeViewHolder> {

    private Context context;
    private ArrayList<Type> types;
    private OnItemclickListener listener;

    private Animation buttonClick;

    private MusicListener musicListener;


    public interface OnItemclickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemclickListener listener){
        this.listener = listener;
    }

    public TypeAdapter(Context context, ArrayList<Type> matistList) {
        this.context = context;
        types = matistList;
        buttonClick = AnimationUtils.loadAnimation(context, R.anim.click);
        musicListener = (MusicListener) context;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.search_recommandation_item_display, parent, false);
        return new TypeViewHolder(v);
        /*if (types.size()==0){
            View v = LayoutInflater.from(context).inflate(R.layout.aucun_resultat, parent, false);
            return new TypeViewHolder(v);
        }else {
            View v = LayoutInflater.from(context).inflate(R.layout.search_recommandation_item_display, parent, false);
            return new TypeViewHolder(v);
        }*/

    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        final Type currentItem = types.get(position);
        String imageUrl = currentItem.getImgUrl();
        String name = currentItem.getName();
        String spec = currentItem.getSpec();
        String pre="";
        if (currentItem instanceof Album){
            pre="Artiste: ";
            Album album = (Album) currentItem;
            currentItem.setSongUrl(album.getSongUrl());
        }
        if (currentItem instanceof Artist){
            pre="Nombre de fans: ";
            Artist album = (Artist) currentItem;
            currentItem.setSongUrl(album.getSongUrl());
        }
        if (currentItem instanceof Film){
            holder.playButton.setVisibility(View.INVISIBLE);
            holder.circle.setVisibility(View.INVISIBLE);
            pre="Année: ";
        }
        if (currentItem instanceof Video){
            holder.playButton.setVisibility(View.INVISIBLE);
            holder.circle.setVisibility(View.INVISIBLE);
            pre="Année: ";
        }
        if (currentItem instanceof JeuVideo){
            holder.playButton.setVisibility(View.INVISIBLE);
            holder.circle.setVisibility(View.INVISIBLE);
            pre="Année: ";

        }
        if (currentItem instanceof Morceau){
            pre="Artiste: ";
            Morceau album = (Morceau) currentItem;
            currentItem.setSongUrl(album.getSongUrl());
        }
        if (currentItem instanceof Serie){
            holder.playButton.setVisibility(View.INVISIBLE);
            holder.circle.setVisibility(View.INVISIBLE);
            pre="Année: ";
        }

        /*holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //holder.playButton.startAnimation(buttonClick);
                musicListener.lancerMusique(currentItem);

            }
        });*/

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicListener.lancerMusique(currentItem);
            }
        });

        //holder.mediaPlayer  = this.mediaPlayer;
        holder.name_ar.setText(name);
        holder.spec.setText(pre + spec);
        if(!imageUrl.isEmpty()) {
            Picasso.with(context).load(imageUrl).fit().centerInside().into(holder.img_ar);
        }
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public class TypeViewHolder extends RecyclerView.ViewHolder {

        public ImageButton img_ar;
        public TextView name_ar;
        public TextView spec;
        public ImageView playButton;
        public ImageView circle;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);



            img_ar = itemView.findViewById(R.id.img_ar);
            name_ar = itemView.findViewById(R.id.name_ar);
            spec = itemView.findViewById(R.id.spec);
            playButton = itemView.findViewById(R.id.playButton);
            circle = itemView.findViewById(R.id.circle);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            img_ar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    public interface MusicListener {
        void lancerMusique(Type type);
    }
}

