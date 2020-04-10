package com.example.sharehitv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import com.example.sharehitv2.Model.Comment;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.Utilities.DividerItemDecorator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecommandationPage extends AppCompatActivity {

    TextView nbrlike;

    TextView autreComment;
    TextView titreReco;

    ImageButton commentButton;
    ImageButton bookmarkButton;
    ImageButton likeButton;

    ImageButton pictureRecommandation;

    TextView timeRecommandation;
    TextView descRecommandation;
    TextView nomRecommandation;
    CircleImageView pictureUserRecommandation;

    ImageView playButton;
    ImageView circle;

    LinearLayout layout;
    LinearLayout.LayoutParams params;

    private DatabaseReference recosRef, followRef, usersRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    private Animation buttonClick;

    private final static MediaPlayer mp = new MediaPlayer();

    private ProgressBar mSeekBarPlayer;
    private ImageButton stop;
    private ImageButton btnPause;
    private LinearLayout lecteur;
    private TextView nameLect;
    private ImageView musicImg;

    private RecyclerView commentList;
    private DatabaseReference comRef;

    private ValueAnimator lecteurApparait;
    private ValueAnimator lecteurDisparrait;

    private int ancienneHauteurLecteur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommandation_page);

        String jsonMyObject = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("reco");
        }

        Log.e("recommandation", ""+jsonMyObject );

        final Recommandation recommandation = new Gson().fromJson(jsonMyObject, Recommandation.class);

        Toolbar toolbar = findViewById(R.id.toolbarRecommandationPage);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        String title = "";
        if(recommandation.getType().equals("track")){
            title =" " + recommandation.getTrack()+" de "+recommandation.getArtist();
        } else if(recommandation.getType().equals("album")){
            title =" " + recommandation.getAlbum()+" de "+recommandation.getArtist();
        } else {
            title =" " + recommandation.getArtist();
        }
        actionBar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommandationPage.super.onBackPressed();
            }
        });

        commentList = findViewById(R.id.recyclerViewComment);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);
        commentList.setLayoutManager(layoutManager);

        nbrlike = findViewById(R.id.nbrLike);

        autreComment = findViewById(R.id.autreComment);
        titreReco = findViewById(R.id.name);

        commentButton = findViewById(R.id.commentButton);
        bookmarkButton = findViewById(R.id.bookButton);
        likeButton = findViewById(R.id.likeButton);


        pictureRecommandation = findViewById(R.id.img_ar);

        timeRecommandation = findViewById(R.id.time);
        descRecommandation = findViewById(R.id.desc);
        nomRecommandation = findViewById(R.id.name);
        pictureUserRecommandation = findViewById(R.id.imgProfil);

        playButton = findViewById(R.id.playButton);
        circle = findViewById(R.id.circle);


        //playButton.setVisibility(View.INVISIBLE);
        //circle.setVisibility(View.INVISIBLE);
        layout = findViewById(R.id.linearLayoutReco);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonClick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click);

        lecteur = findViewById(R.id.lecteur);
        stop = findViewById(R.id.button1);
        btnPause = findViewById(R.id.button2);
        mSeekBarPlayer = findViewById(R.id.progressBar);
        nameLect = findViewById(R.id.nameLect);
        musicImg = findViewById(R.id.musicImg);

        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        ancienneHauteurLecteur=params.height;
        params.height=0;
        lecteur.setLayoutParams(params);

        lecteurApparait = ValueAnimator.ofInt(lecteur.getMeasuredHeight(), ancienneHauteurLecteur);
        lecteurApparait.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lecteur.getLayoutParams();
                layoutParams.height = val;
                lecteur.setLayoutParams(layoutParams);
            }
        });
        lecteurApparait.setDuration(400);

        lecteurDisparrait = ValueAnimator.ofInt(ancienneHauteurLecteur,0);
        lecteurDisparrait.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lecteur.getLayoutParams();
                layoutParams.height = val;
                lecteur.setLayoutParams(layoutParams);
            }
        });
        lecteurDisparrait.setDuration(400);

        Bundle b1 = getIntent().getExtras();

        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        followRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("followed");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        comRef = FirebaseDatabase.getInstance().getReference().child("recos").child(recommandation.getCleReco()).child("Coms");




        final Intent intent1 = new Intent(this, ListLikePage.class);
        final Intent intent2 = new Intent(this, CommentPage.class);
        final Intent intent3 = new Intent(this, ProfilPage.class);


        final String[] keyBookmark = new String[1];
        final boolean[] CURRENT_BOOKMARK = new boolean[1];
        final String[] keyLike = new String[1];
        final boolean[] CURRENT_LIKE = new boolean[1];


        final Bundle b = new Bundle();

        final String idReco = recommandation.getCleReco();

        String desc = "";
        if(recommandation.getType().equals("track")){
            desc ="<b>"+recommandation.getTrack()+"</b>"+" de "+"<b>"+recommandation.getArtist()+"</b>";
        } else if(recommandation.getType().equals("album")){
            desc ="<b>"+recommandation.getAlbum()+"</b>"+" de "+"<b>"+recommandation.getArtist()+"</b>";
        } else {
            desc ="<b>"+recommandation.getArtist()+"</b>";
        }
        descRecommandation.setText(Html.fromHtml(desc));

        Picasso.with(this).load(recommandation.getUrlImage()).fit().centerInside().into(pictureRecommandation);

        recosRef.child(idReco).child("Coms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    autreComment.setText("" + dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nbrlike.setText(Long.toString(dataSnapshot.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        // Create a storage reference from our app
        final StorageReference filepath = mStorageRef;

        //Log.e("testest","id de l'utilisateur : "+ recommandation.getUserRecoUid());
        //Log.e("testest", filepath.child(recommandation.getUserRecoUid()).toString());

        filepath.child(recommandation.getUserRecoUid()).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/sharehitv2.appspot.com/o/"+recommandation.getUserRecoUid()+"?alt=media").fit().centerInside().into(pictureUserRecommandation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Picasso.with(context).load("").fit().centerInside().into(viewHolder.getImgProfil());
                pictureUserRecommandation.setImageResource(R.drawable.default_profile_picture);
            }
        });
        //Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+recommandation.getUserRecoUid()+"?alt=media&token=1d93f69f-a530-455a-83d2-929ce42c3667").fit().centerInside().into(viewHolder.getImgProfil());

        usersRef.child(recommandation.getUserRecoUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String pseudo;
                if(dataSnapshot.exists()){
                    pseudo = dataSnapshot.child("pseudo").getValue().toString();
                } else {
                    pseudo = "Compte supprimé";
                }

                String typeReco="";
                if (recommandation.getType().equals("track")) {
                    typeReco = "un morceau";
                } else if (recommandation.getType().equals("artist")){
                    typeReco = "un artiste";
                } else if (recommandation.getType().equals("album")){
                    typeReco="un album";
                } else if (recommandation.getType().equals("movie")){
                    typeReco="un film";
                } else if (recommandation.getType().equals("serie")){
                    typeReco="une série";
                } else if (recommandation.getType().equals("game")){
                    typeReco="un jeu vidéo";
                }
                final String sourceString = "<b>"+pseudo+"</b>"+ " a recommandé " +"<b>"+typeReco+"</b>";

                titreReco.setText(Html.fromHtml(sourceString));

                long currentTimestamp = currentTimeSecsUTC();
                double searchTimestampD = recommandation.getTimestamp();
                long searchTimestamp = (long) searchTimestampD;
                long difference = Math.abs(currentTimestamp - searchTimestamp);
                if(TimeUnit.MILLISECONDS.toSeconds(currentTimestamp)==TimeUnit.MILLISECONDS.toSeconds(searchTimestamp)) {
                    timeRecommandation.setText("À l'instant");
                }else {
                    timeRecommandation.setText("Il y a " + convertTimeStampToHour(difference));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heCanBePlayed(recommandation.getType())) {
                    playButton.startAnimation(buttonClick);
                    mediaListener.lancerMusique(recommandation);
                } else {
                    viewHolder.playButton.startAnimation(buttonClick);
                    mediaListener.stop();
                    mediaListener.lancerVideo(recommandation);

                    //Toast.makeText(context, "Impossible de lire ce contenu", Toast.LENGTH_LONG).show();
                }
            }

        });

         */
        //INTREACTION AVEC LA RECOMMANDATION

        usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean test = false;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.getValue().equals(idReco)){
                        test = true;
                        keyBookmark[0] = ds.getRef().getKey();
                    }
                }
                if(test){
                    bookmarkButton.setImageResource(R.drawable.bookmark_ok);
                    CURRENT_BOOKMARK[0] = true;
                } else {
                    CURRENT_BOOKMARK[0] = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CURRENT_BOOKMARK[0] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").push().getKey(), idReco);
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").updateChildren(usersMap);
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(b.getString("key"))){
                                    Log.e("Bookmark key", ds.getRef().getKey());
                                    keyBookmark[0] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    bookmarkButton.setImageResource(R.drawable.bookmark_ok);
                    CURRENT_BOOKMARK[0] =true;

                } else if(CURRENT_BOOKMARK[0] == true){
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").child(keyBookmark[0]).removeValue();
                    bookmarkButton.setImageResource(R.drawable.bookmark);
                    CURRENT_BOOKMARK[0] =false;

                }


            }
        });

        recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean test = false;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.getValue().equals(mAuth.getCurrentUser().getUid())){
                        test = true;
                        keyLike[0] = ds.getRef().getKey();
                    }
                }
                if(test){
                    likeButton.setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[0] = true;
                } else {
                    likeButton.setImageResource(R.drawable.heart);
                    CURRENT_LIKE[0] = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(CURRENT_LIKE[0] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(recosRef.child(idReco).child("likeUsersUid").push().getKey(), mAuth.getCurrentUser().getUid());
                    recosRef.child(idReco).child("likeUsersUid").updateChildren(usersMap);
                    recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(mAuth.getCurrentUser().getUid())){
                                    Log.e("Like key", ds.getRef().getKey());
                                    keyLike[0] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    likeButton.setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[0] =true;

                } else if(CURRENT_LIKE[0] == true){
                    recosRef.child(idReco).child("likeUsersUid").child(keyLike[0]).removeValue();
                    likeButton.setImageResource(R.drawable.heart);
                    CURRENT_LIKE[0] =false;

                }

            }

        });

        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(CURRENT_LIKE[0] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(recosRef.child(idReco).child("likeUsersUid").push().getKey(), mAuth.getCurrentUser().getUid());
                    recosRef.child(idReco).child("likeUsersUid").updateChildren(usersMap);
                    recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(mAuth.getCurrentUser().getUid())){
                                    Log.e("Like key", ds.getRef().getKey());
                                    keyLike[0] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    likeButton.setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[0] =true;

                } else if(CURRENT_LIKE[0] == true){
                    recosRef.child(idReco).child("likeUsersUid").child(keyLike[0]).removeValue();
                    likeButton.setImageResource(R.drawable.heart);
                    CURRENT_LIKE[0] =false;

                }
                return true;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {

                String link ="";
                if (recommandation.getType().equals("track")){
                    link="https://www.deezer.com/fr/track/"+recommandation.getId();
                } else if (recommandation.getType().equals("album")){
                    link="https://www.deezer.com/fr/album/"+recommandation.getId();
                }else if (recommandation.getType().equals("artist")){
                    link="https://www.deezer.com/fr/artist/"+recommandation.getId();
                }else if (recommandation.getType().equals("game")){
                    link=recommandation.getId();
                }
                else{
                    link="https://www.imdb.com/title/"+recommandation.getId();
                }
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(link));
                startActivity(viewIntent);

                return true;
            }
        };



        final GestureDetector detector = new GestureDetector(listener);

        detector.setOnDoubleTapListener(listener);
        detector.setIsLongpressEnabled(true);

        pictureRecommandation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });

        pictureUserRecommandation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", recommandation.getUserRecoUid());
                intent3.putExtras(b);
                startActivity(intent3);
            }
        });

        descRecommandation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link ="";
                if (recommandation.getType().equals("track")){
                    link="https://www.deezer.com/fr/track/"+recommandation.getId();
                } else if (recommandation.getType().equals("album")){
                    link="https://www.deezer.com/fr/album/"+recommandation.getId();
                }else if (recommandation.getType().equals("artiste")){
                    link="https://www.deezer.com/fr/artist/"+recommandation.getId();
                } else{
                    link="https://www.imdb.com/title/"+recommandation.getId();
                }
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(link));
                startActivity(viewIntent);
            }
        });


        autreComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.putString("key",idReco);
                intent2.putExtras(b);
                startActivity(intent2);
            }
        });

        nbrlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key",idReco);
                intent1.putExtras(b);
                startActivity(intent1);

            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", idReco);
                intent2.putExtras(b);
                startActivity(intent2);

            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heCanBePlayed(recommandation.getType())) {
                    playButton.startAnimation(buttonClick);
                    lancerMusique(recommandation);
                } else {
                    playButton.startAnimation(buttonClick);
                    stop();
                    lancerVideo(recommandation);

                    //Toast.makeText(context, "Impossible de lire ce contenu", Toast.LENGTH_LONG).show();
                }
            }

        });

        displayAllComment();


    }

    public void stop() {
        mp.stop();
        mp.reset();
        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        if(params.height==ancienneHauteurLecteur) {
            lecteurDisparrait.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    public void lancerVideo(Recommandation recommandation) {
        if(!recommandation.getUrlPreview().equals("") && !recommandation.getUrlPreview().equals("null")){
            stop();
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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

    public void lancerMusique(Recommandation model) {
        mp.seekTo(mp.getDuration());
        mp.reset();
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
                lecteurDisparrait.start();
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

        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        if(params.height==0) {
            lecteurApparait.start();
        }

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

    public static long currentTimeSecsUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis() / 1000;
    }

    private String convertTimeStampToHour(long millis) {
        millis = millis * 1000;
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

    public boolean heCanBePlayed(String s){
        return s.equals("album") || s.equals("artist") || s.equals("track");
    }

    public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(Resources.getSystem(), x);
    }

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }
    private void displayAllComment() {
        final Intent intent3 = new Intent(getApplicationContext(), ProfilPage.class);
        final Bundle b = new Bundle();
        FirebaseRecyclerAdapter<Comment, CommentPage.CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentPage.CommentViewHolder>
                (
                        Comment.class,
                        R.layout.comment_display,
                        CommentPage.CommentViewHolder.class,
                        comRef
                ) {
            @Override
            protected void populateViewHolder(final CommentPage.CommentViewHolder commentViewHolder, final Comment comment, int i) {
                commentViewHolder.setMessage(comment.getCom());

                Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cl.setTimeInMillis(comment.getTimestamp() * 1000);
                DateFormat dateFormat = new SimpleDateFormat("dd"+"/"+"MM"+"/"+"yyyy"+" à "+"H"+":"+"mm");
                commentViewHolder.setTime("Le "+dateFormat.format(cl.getTime()));

                final StorageReference filepath = mStorageRef;

                filepath.child(comment.getUid()).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/sharehitv2.appspot.com/o/"+comment.getUid()+"?alt=media").fit().centerInside().into(commentViewHolder.getImgProfilComment());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        commentViewHolder.getImgProfilComment().setImageResource(R.drawable.default_profile_picture);
                    }
                });

                usersRef.child(comment.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            commentViewHolder.setPseudoComment(dataSnapshot.child("pseudo").getValue().toString());
                        } else {
                            commentViewHolder.setPseudoComment("Compte supprimé");
                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                commentViewHolder.getImgProfilComment().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        b.putString("key", comment.getUid());
                        intent3.putExtras(b);
                        startActivity(intent3);
                    }
                });

            }
        };
        commentList.setAdapter(firebaseRecyclerAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(getApplicationContext(), R.drawable.recycler_view_divider));
        commentList.addItemDecoration(dividerItemDecoration);

    }




}
