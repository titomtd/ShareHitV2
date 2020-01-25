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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class FollowFragment extends Fragment {


    RecyclerView recyclerView;
    private DatabaseReference recosRef, followRef, usersRef;
    private FirebaseAuth mAuth;
    private String current_user_id;
    public boolean CURRENT_LIKE, CURRENT_BOOK, test=false;
    private final static MediaPlayer mp = new MediaPlayer();
    private ProgressBar mSeekBarPlayer;
    private ImageButton stop;
    private ImageButton btnPause;
    private LinearLayout lecteur;
    private TextView nameLect;
    private ImageView musicImg;

    private Animation buttonClick;

    private RecommandationAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    private boolean isCharged;

    private boolean isFollow;
    private List<String> userFollow;

    @Override
    public void onPause() {
        super.onPause();
        mp.pause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_follow, null);
        recyclerView = root.findViewById(R.id.postFollowRecyclerView);
        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipeContainerFollow);


        isCharged = true;
        userFollow = new ArrayList<>();

        // Création du swipe up pour refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chargerListFollow();
                isCharged = true;
                adapter.notifyDataSetChanged();
                chargerRecyclerView(chargerListRecommandation());
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        buttonClick = AnimationUtils.loadAnimation(getContext(), R.anim.click);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        followRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("followed");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        lecteur = root.findViewById(R.id.lecteur);
        stop = root.findViewById(R.id.button1);
        btnPause = root.findViewById(R.id.button2);
        mSeekBarPlayer = root.findViewById(R.id.progressBar);
        nameLect = root.findViewById(R.id.nameLect);
        musicImg = root.findViewById(R.id.musicImg);

        lecteur.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        params.height=0;
        lecteur.setLayoutParams(params);

        chargerListFollow();

        chargerRecyclerView(chargerListRecommandation());


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

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            if(mp != null) {
                mSeekBarPlayer.setProgress(mp.getCurrentPosition());
            }

            if(mp.isPlaying()) {
                btnPause.setImageResource(R.drawable.ic_pause);
                mSeekBarPlayer.postDelayed(onEverySecond, 100);
            }else{
                btnPause.setImageResource(R.drawable.ic_play);
            }
        }
    };

    public List<Recommandation> chargerListRecommandation(){
        final List<Recommandation> list = new ArrayList<>();
        recosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isCharged){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(userRecoIsFollow(child.child("userRecoUid").getValue().toString())){
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
                            list.add(recommandation);
                            Log.e("isFollow", "true");
                        }
                        Log.e("isFollow", "false");

                        chargerRecyclerView(list);


                    }
                    isCharged = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*
        recosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chargerRecyclerView(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

         */
        return list;

    }



    private void chargerListFollow(){
        userFollow.clear();
        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    userFollow.add(child.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean userRecoIsFollow(String userId){
        isFollow = false;
        for(String follow : userFollow){
            if(userId.equals(follow)){
                isFollow = true;
            }
        }
        return isFollow;
    }

}
