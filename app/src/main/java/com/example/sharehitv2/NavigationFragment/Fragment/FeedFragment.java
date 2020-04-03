package com.example.sharehitv2.NavigationFragment.Fragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sharehitv2.Adapter.RecommandationAdapter;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.NavigationFragment.FeedPageFragment;
import com.example.sharehitv2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeedFragment extends Fragment implements FeedPageFragment.InteractionMenu {

    RecyclerView recyclerView;
    private DatabaseReference recosRef, followRef, usersRef;
    private FirebaseAuth mAuth;

    private RecommandationAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    private boolean isCharged;

    private List<Recommandation> recommandations;
    private int type=-1;

    private Button tout;
    private Button morceau;
    private Button album;
    private Button artiste;
    private Button film;
    private Button serie;
    private Button jeu;

    private ActionBarInteraction actionBarInteraction;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.e("testest", "load feed fragment");
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_feed, null);
        recyclerView = root.findViewById(R.id.postRecyclerView);
        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipeContainerFeed);

        tout = root.findViewById(R.id.tout);
        morceau = root.findViewById(R.id.morceau);
        album = root.findViewById(R.id.album);
        album = root.findViewById(R.id.album);
        artiste = root.findViewById(R.id.artiste);
        film = root.findViewById(R.id.film);
        serie = root.findViewById(R.id.serie);
        jeu = root.findViewById(R.id.jeu);

        actionBarInteraction = (ActionBarInteraction) getActivity();

        tout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(chargerListRecommandation());
                type=-1;
                swipeContainer.setRefreshing(false);
            }
        });
        morceau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(2));
                type=2;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Morceau");
            }
        });
        artiste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(0));
                type=0;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Artiste");
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(1));
                type=1;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Album");
            }
        });
        serie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(3));
                type=3;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Série");
            }
        });
        film.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(4));
                type=4;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Film");
            }
        });
        jeu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(filtrerList(5));
                type=5;
                swipeContainer.setRefreshing(false);
                actionBarInteraction.setTitle("Acceuil : Jeu Vidéo");
            }
        });

        //callBack = (FeedFragment.MyListenerFeed) getActivity();

        isCharged = true;

        // Création du swipe up pour refresh

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isCharged = true;
                adapter.notifyDataSetChanged();
                if (type>-1){
                    chargerRecyclerView(filtrerList(type));
                }else {
                    chargerRecyclerView(chargerListRecommandation());
                }
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_dark);




        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        followRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("followed");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        recommandations=chargerListRecommandation();
        chargerRecyclerView(recommandations);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private String convertTimeStampToBelleHeureSaMere(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if(days != 0){
            if(days == 1){
                sb.append(days);
                sb.append(" jour ");
            } else {
                sb.append(days);
                sb.append(" jours ");
            }

        } else if(hours != 0){
            if(hours == 1){
                sb.append(hours);
                sb.append(" heure ");
            } else {
                sb.append(hours);
                sb.append(" heures ");
            }

        } else if(minutes != 0){
            if(minutes == 1){
                sb.append(minutes);
                sb.append(" minute ");
            } else {
                sb.append(minutes);
                sb.append(" minutes ");
            }
        } else if(seconds != 0){
            sb.append(seconds);
            sb.append(" secondes");
        }




        return(sb.toString());
    }

    public void chargerRecyclerView(List<Recommandation> list){
        adapter = new RecommandationAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private boolean loadFragement(Fragment fragment){
        if(fragment != null){
            return true;
        }
        return false;
    }

    public List<Recommandation> filtrerList(int i){
        final List<Recommandation> listReco = new ArrayList<>();
        final String type;
        switch (i){
            case 0: type = "artist";
                break;
            case 1: type = "album";
                break;
            case 2: type = "track";
                break;
            case 3: type = "serie";
                break;
            case 4: type = "movie";
                break;
            case 5: type = "game";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + i);
        }
        for(Recommandation recos : recommandations){
            if(recos.getType().equals(type)){
                listReco.add(recos);
                chargerRecyclerView(listReco);
            }
        }
        return listReco;
    }


    public List<Recommandation> chargerListRecommandation(){
        final List<Recommandation> list = new ArrayList<>();
        recosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isCharged){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        Recommandation recommandation = new Recommandation(
                                child.child("album").getValue().toString(),
                                child.child("artist").getValue().toString(),
                                child.child("id").getValue().toString(),
                                Double.parseDouble(child.child("timestamp").getValue().toString()),
                                child.child("track").getValue().toString(),
                                child.child("type").getValue().toString(),
                                child.child("urlImage").getValue().toString(),
                                child.child("urlPreview").getValue().toString(),
                                child.child("userRecoUid").getValue().toString(),
                                child.getKey());
                        if (recommandation.getType().equals("artist")){
                            recommandation.setPlayable(true);
                        }else if (recommandation.getType().equals("album")){
                            recommandation.setPlayable(true);
                        } else if (recommandation.getType().equals("track")){
                            recommandation.setPlayable(true);
                        }
                        //recommandation.setPlayable(true);
                        list.add(recommandation);
                        Log.e("testest", "taille de la liste : "+list.size());
                        chargerRecyclerView(list);


                    }
                    isCharged = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return list;

    }

    @Override
    public void rechargerPage() {
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
    }

    public interface ActionBarInteraction{
        public void setTitle(String title);
    }
}

