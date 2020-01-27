package com.example.sharehitv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehitv2.Adapter.RecommandationAdapter;
import com.example.sharehitv2.Model.Recommandation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilPage extends AppCompatActivity implements RecommandationAdapter.MediaListener {

    private CircleImageView pdp;
    private TextView pseudo, textView;
    private RecyclerView post;
    private Button follow;

    private DatabaseReference recosRef, usersRef;
    private FirebaseAuth mAuth;

    public boolean CURRENT_LIKE, CURRENT_FOLLOW;

    private final static MediaPlayer mp = new MediaPlayer();

    private String keyFollowed;

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

    private String userId;

    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_page);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerProfilPage);
        post = (RecyclerView) findViewById(R.id.postProfilPageRecyclerView);

        CURRENT_FOLLOW = false;
        isCharged = true;

        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Création du swipe up pour refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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


        buttonClick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);

        lecteur = findViewById(R.id.lecteur);
        stop = findViewById(R.id.button1);
        btnPause = findViewById(R.id.button2);
        mSeekBarPlayer = findViewById(R.id.progressBar);
        nameLect = findViewById(R.id.nameLect);
        musicImg = findViewById(R.id.musicImg);

        lecteur.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        params.height=0;
        lecteur.setLayoutParams(params);

        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        pdp = (CircleImageView) findViewById(R.id.pdpProfilPage);
        pseudo = (TextView) findViewById(R.id.pseudoProfilPage);
        follow = (Button) findViewById(R.id.followProfilPage);
        textView = (TextView) findViewById(R.id.textView8);


        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        post.setLayoutManager(layoutManager);

        final Bundle b = getIntent().getExtras();

        if (mAuth.getCurrentUser().getUid().equals(b.getString("key"))) {
            follow.setVisibility(View.INVISIBLE);
            textView.setText("Mes recommandations");
        }

        userId = b.getString("key");

        usersRef.child(mAuth.getCurrentUser().getUid()).child("followed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getValue().equals(b.getString("key"))) {
                        CURRENT_FOLLOW = true;
                        keyFollowed = ds.getRef().getKey();
                        follow.setText("Ne plus suivre");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CURRENT_FOLLOW == false) {
                    HashMap usersMap = new HashMap();
                    usersMap.put(usersRef.child(mAuth.getCurrentUser().getUid()).child("followed").push().getKey(), b.getString("key"));
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("followed").updateChildren(usersMap);
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("followed").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.getValue().equals(b.getString("key"))) {
                                    Log.e("Followed key", ds.getRef().getKey());
                                    keyFollowed = ds.getRef().getKey();

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    follow.setText("Ne plus suivre");
                    CURRENT_FOLLOW = true;

                } else if (CURRENT_FOLLOW == true) {
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("followed").child(keyFollowed).removeValue();
                    follow.setText("Suivre");
                    CURRENT_FOLLOW = false;
                }
            }
        });

        final StorageReference filepath = mStorageRef;

        filepath.child(userId).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+userId+"?alt=media").fit().centerInside().into(pdp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                pdp.setImageResource(R.drawable.default_profile_picture);
            }
        });

        Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+userId+"?alt=media").fit().centerInside().into(pdp);
        //Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+b.getString("key")+"?alt=media&token=1d93f69f-a530-455a-83d2-929ce42c3667").fit().centerInside().into(pdp);

        usersRef.child(b.getString("key")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    actionBar.setTitle("Profil de "+dataSnapshot.child("pseudo").getValue().toString());
                    pseudo.setText(dataSnapshot.child("pseudo").getValue().toString());
                } else {
                    pseudo.setText("Compte supprimé");
                    follow.setEnabled(false);
                    follow.setVisibility(View.INVISIBLE);



                    Log.e("Timestamp", String.valueOf(ServerValue.TIMESTAMP));
                    Date d = new Date();

                    Log.e("Timestamp", String.valueOf(currentTimeSecsUTC()));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chargerRecyclerView(chargerListRecommandation());




    }

    public static long currentTimeSecsUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis() / 1000;
    }

    @Override
    public void onPause() {
        super.onPause();
        mp.pause();
    }

    public List<Recommandation> chargerListRecommandation(){
        final List<Recommandation> list = new ArrayList<>();
        Query postUser = recosRef.orderByChild("userRecoUid").startAt(userId).endAt(userId+"\uf8ff");
        postUser.addValueEventListener(new ValueEventListener() {
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
                        list.add(recommandation);
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

    public void chargerRecyclerView(List<Recommandation> list){
        adapter = new RecommandationAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        post.setLayoutManager(layoutManager);
        post.setAdapter(adapter);
    }



    private String convertTimeStampToBelleHeureSaMere(long millis) {
        if (millis < 0) {
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
        if (days != 0) {
            if (days == 1) {
                sb.append(days);
                sb.append(" jour ");
            } else {
                sb.append(days);
                sb.append(" jours ");
            }

        } else if (hours != 0) {
            if (hours == 1) {
                sb.append(hours);
                sb.append(" heure ");
            } else {
                sb.append(hours);
                sb.append(" heures ");
            }

        } else if (minutes != 0) {
            if (minutes == 1) {
                sb.append(minutes);
                sb.append(" minute ");
            } else {
                sb.append(minutes);
                sb.append(" minutes ");
            }
        } else if (seconds != 0) {
            sb.append(seconds);
            sb.append(" secondes");
        }


        return (sb.toString());
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

    @Override
    public void lancerMusique(Recommandation model) {
        mp.seekTo(mp.getDuration());
        mp.reset();
        if (lecteur.getVisibility()==View.INVISIBLE) {
            lecteur.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = lecteur.getLayoutParams();
            params.height = android.app.ActionBar.LayoutParams.WRAP_CONTENT;
            lecteur.setLayoutParams(params);
        }
        try{
            Log.e("testest", ""+model.getUrlPreview() );mp.setDataSource(model.getUrlPreview());
        }
        catch (IOException ex){
            Log.e("testest", "Can't found data:"+model.getUrlPreview());
        }


        if(model.getType().equals("track"))
            nameLect.setText(model.getTrack());
        else if(model.getType().equals("artist"))
            nameLect.setText(model.getArtist());
        else if(model.getType().equals("album"))
            nameLect.setText(model.getAlbum());
                        /*recosViewHolder.playButton.setVisibility(View.INVISIBLE);
                        recosViewHolder.player.setVisibility(View.VISIBLE);*/


        Picasso.with(getApplicationContext()).load(model.getUrlImage()).fit().centerInside().into(musicImg);
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mp.getDuration();
                mSeekBarPlayer.setMax(duration);
                mp.start();
                mSeekBarPlayer.postDelayed(onEverySecond, 500);
            }
        });

        //recosViewHolder.playButton.startAnimation(buttonClick);

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mp.stop();
                mp.reset();
                lecteur.setVisibility(View.INVISIBLE);



                                /*recosViewHolder.playButton.setVisibility(View.VISIBLE);
                                recosViewHolder.playButton.setImageResource(R.drawable.ic_play);
                                recosViewHolder.player.setVisibility(View.INVISIBLE);*/

                ViewGroup.LayoutParams params = lecteur.getLayoutParams();
                params.height=0;
                lecteur.setLayoutParams(params);
            }
        });


        btnPause.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    btnPause.setImageResource(R.drawable.ic_play);
                                    /*recosViewHolder.playButton.setVisibility(View.VISIBLE);
                                    recosViewHolder.playButton.setImageResource(R.drawable.ic_pause);
                                    recosViewHolder.player.setVisibility(View.INVISIBLE);*/

                }
                else {
                    btnPause.setImageResource(R.drawable.ic_pause);
                                    /*recosViewHolder.playButton.setVisibility(View.INVISIBLE);
                                    recosViewHolder.player.setVisibility(View.VISIBLE);*/
                    try {
                        mp.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mp.start();
                    mSeekBarPlayer.postDelayed(onEverySecond, 1000);
                }

            }
        });
    }

    @Override
    public void lancerVideo(Recommandation recommandation) {
        if(!recommandation.getUrlPreview().equals("") && !recommandation.getUrlPreview().equals("null")){
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfilPage.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_video, null);
            final WebView webView = dialogView.findViewById(R.id.webview);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            webView.loadUrl("https://www.youtube.com/embed/"+recommandation.getUrlPreview());
            //webView.loadData("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/rrwycJ08PSA\" frameborder=\"0\" allow=\"autoplay\" allowfullscreen></iframe>", "text/html", "utf-8");
            webView.setWebChromeClient(new WebChromeClient());
            builder.setView(dialogView);
            final AlertDialog dialog = builder.create();
            dialog.show();
        } else if(recommandation.getType().equals("game")){
            Toast.makeText(getApplicationContext(), "Les bandes annonces pour les jeux vidéos arrivent prochainement", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Aucune bande annonce pour cette recommandation", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void stop() {
        mp.stop();
        mp.stop();
        mp.reset();
        lecteur.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        params.height=0;
        lecteur.setLayoutParams(params);
    }
}
