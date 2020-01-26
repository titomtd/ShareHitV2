package com.example.sharehitv2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.model.Progress;
import com.example.sharehitv2.CommentPage;
import com.example.sharehitv2.ListLikePage;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.ProfilPage;
import com.example.sharehitv2.R;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecommandationAdapter extends
        RecyclerView.Adapter<RecommandationAdapter.ViewHolder> {

    Context context;
    private DatabaseReference recosRef, usersRef;
    private FirebaseAuth mAuth;
    List<Recommandation> mRecommandation;
    private Animation buttonClick;
    private StorageReference mStorageRef;
    private MediaListener mediaListener;



    public RecommandationAdapter(List<Recommandation> recommandations) {
        this.mRecommandation = recommandations;
    }

    @Override
    public RecommandationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        mediaListener = (MediaListener) context;

        mStorageRef = FirebaseStorage.getInstance().getReference();
        buttonClick = AnimationUtils.loadAnimation(context, R.anim.click);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");

        final int[] tailleTableau = new int[1];
        recosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tailleTableau[0] = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        View recommandationView = inflater.inflate(R.layout.recommandation_item_display, parent, false);

        RecommandationAdapter.ViewHolder viewHolder = new RecommandationAdapter.ViewHolder(recommandationView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecommandationAdapter.ViewHolder viewHolder, final int position) throws NullPointerException {
        final Recommandation recommandation = mRecommandation.get(position);


        final Intent intent1 = new Intent(context, ListLikePage.class);
        final Intent intent2 = new Intent(context, CommentPage.class);
        final Intent intent3 = new Intent(context, ProfilPage.class);


        final String[] keyBookmark = new String[mRecommandation.size()];
        final boolean[] CURRENT_BOOKMARK = new boolean[mRecommandation.size()];
        final String[] keyLike = new String[mRecommandation.size()];
        final boolean[] CURRENT_LIKE = new boolean[mRecommandation.size()];


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
        viewHolder.setDesc(Html.fromHtml(desc));

        Picasso.with(context).load(recommandation.getUrlImage()).fit().centerInside().into(viewHolder.pictureRecommandation);

        recosRef.child(idReco).child("Coms").limitToLast(1).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("letesta", ""+dataSnapshot );
                if (!dataSnapshot.hasChildren()){
                    viewHolder.setPseudoCom("Aucun commentaire");
                    viewHolder.setAutreComment("0");
                    viewHolder.setNbrCom("");
                }
                else {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.child("com").exists()) {

                            viewHolder.setNbrCom(child.child("com").getValue().toString());
                        }
                        final String index = child.getKey();
                        String idUsr = dataSnapshot.child(index).child("uid").getValue().toString();
                        usersRef.child(idUsr).child("pseudo").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    viewHolder.setPseudoCom(dataSnapshot.getValue().toString() + " :");
                                } else {
                                    viewHolder.setPseudoCom("Compte supprimé :");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recosRef.child(idReco).child("Coms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    viewHolder.setAutreComment("" + dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viewHolder.setNbrLike(Long.toString(dataSnapshot.getChildrenCount()));
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
                Picasso.with(context).load("https://firebasestorage.googleapis.com/v0/b/share-hit.appspot.com/o/"+recommandation.getUserRecoUid()+"?alt=media").fit().centerInside().into(viewHolder.getImgProfil());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Picasso.with(context).load("").fit().centerInside().into(viewHolder.getImgProfil());
                viewHolder.getImgProfil().setImageResource(R.drawable.default_profile_picture);
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

                viewHolder.setTitre(Html.fromHtml(sourceString));

                long currentTimestamp = currentTimeSecsUTC();
                double searchTimestampD = recommandation.getTimestamp();
                long searchTimestamp = (long) searchTimestampD;
                long difference = Math.abs(currentTimestamp - searchTimestamp);
                if(TimeUnit.MILLISECONDS.toSeconds(currentTimestamp)==TimeUnit.MILLISECONDS.toSeconds(searchTimestamp)) {
                    viewHolder.setTime("À l'instant");
                }else {
                    viewHolder.setTime("Il y a " + convertTimeStampToHour(difference));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*if(!recommandation.getPlayable()){
            viewHolder.playButton.setVisibility(View.INVISIBLE);
            viewHolder.circle.setVisibility(View.INVISIBLE);
        }*/

        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heCanBePlayed(recommandation.getType())) {
                    viewHolder.playButton.startAnimation(buttonClick);
                    mediaListener.lancerMusique(recommandation);
                } else {
                    viewHolder.playButton.startAnimation(buttonClick);
                    mediaListener.stop();
                    mediaListener.lancerVideo(recommandation);

                    //Toast.makeText(context, "Impossible de lire ce contenu", Toast.LENGTH_LONG).show();
                }
            }

        });

        /*
        recosRef.child(idReco).child("type").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (heCanBePlayed(dataSnapshot.getValue().toString())
                            && viewHolder.playButton!=null) {
                        viewHolder.playButton.setVisibility(View.VISIBLE);
                        viewHolder.circle.setVisibility(View.VISIBLE);

                    }
                    else {
                        viewHolder.playButton = null;
                        viewHolder.circle = null;
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
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
                        keyBookmark[position] = ds.getRef().getKey();
                    }
                }
                if(test){
                    viewHolder.getBookButton().setImageResource(R.drawable.bookmark_ok);
                    CURRENT_BOOKMARK[position] = true;
                } else {
                    CURRENT_BOOKMARK[position] = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        viewHolder.getBookButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CURRENT_BOOKMARK[position] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").push().getKey(), idReco);
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").updateChildren(usersMap);
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(b.getString("key"))){
                                    Log.e("Bookmark key", ds.getRef().getKey());
                                    keyBookmark[position] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    viewHolder.getBookButton().setImageResource(R.drawable.bookmark_ok);
                    CURRENT_BOOKMARK[position] =true;

                } else if(CURRENT_BOOKMARK[position] == true){
                    usersRef.child(mAuth.getCurrentUser().getUid()).child("bookmarks").child(keyBookmark[position]).removeValue();
                    viewHolder.getBookButton().setImageResource(R.drawable.bookmark);
                    CURRENT_BOOKMARK[position] =false;

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
                        keyLike[position] = ds.getRef().getKey();
                    }
                }
                if(test){
                    viewHolder.getLikeButton().setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[position] = true;
                } else {
                    viewHolder.getLikeButton().setImageResource(R.drawable.heart);
                    CURRENT_LIKE[position] = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        viewHolder.getLikeButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(CURRENT_LIKE[position] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(recosRef.child(idReco).child("likeUsersUid").push().getKey(), mAuth.getCurrentUser().getUid());
                    recosRef.child(idReco).child("likeUsersUid").updateChildren(usersMap);
                    recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(mAuth.getCurrentUser().getUid())){
                                    Log.e("Like key", ds.getRef().getKey());
                                    keyLike[position] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    viewHolder.getLikeButton().setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[position] =true;

                } else if(CURRENT_LIKE[position] == true){
                    recosRef.child(idReco).child("likeUsersUid").child(keyLike[position]).removeValue();
                    viewHolder.getLikeButton().setImageResource(R.drawable.heart);
                    CURRENT_LIKE[position] =false;

                }

            }

        });

        final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(CURRENT_LIKE[position] == false){
                    HashMap usersMap = new HashMap();
                    usersMap.put(recosRef.child(idReco).child("likeUsersUid").push().getKey(), mAuth.getCurrentUser().getUid());
                    recosRef.child(idReco).child("likeUsersUid").updateChildren(usersMap);
                    recosRef.child(idReco).child("likeUsersUid").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                if(ds.getValue().equals(mAuth.getCurrentUser().getUid())){
                                    Log.e("Like key", ds.getRef().getKey());
                                    keyLike[position] = ds.getRef().getKey();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    viewHolder.getLikeButton().setImageResource(R.drawable.red_heart);
                    CURRENT_LIKE[position] =true;

                } else if(CURRENT_LIKE[position] == true){
                    recosRef.child(idReco).child("likeUsersUid").child(keyLike[position]).removeValue();
                    viewHolder.getLikeButton().setImageResource(R.drawable.heart);
                    CURRENT_LIKE[position] =false;

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
                } else{
                    link="https://www.imdb.com/title/"+recommandation.getId();
                }
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(link));
                context.startActivity(viewIntent);

                return true;
            }
        };



        final GestureDetector detector = new GestureDetector(listener);

        detector.setOnDoubleTapListener(listener);
        detector.setIsLongpressEnabled(true);

        viewHolder.getImg().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });

        viewHolder.getImgProfil().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", recommandation.getUserRecoUid());
                intent3.putExtras(b);
                context.startActivity(intent3);
            }
        });

        viewHolder.getDesc().setOnClickListener(new View.OnClickListener() {
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
                context.startActivity(viewIntent);
            }
        });


        viewHolder.autreComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.putString("key",idReco);
                intent2.putExtras(b);
                context.startActivity(intent2);
            }
        });

        viewHolder.getListLike().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key",idReco);
                intent1.putExtras(b);
                context.startActivity(intent1);

            }
        });

        viewHolder.getCommentButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.putString("key", idReco);
                intent2.putExtras(b);
                context.startActivity(intent2);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecommandation.size();
    }

    public void clear() {
        mRecommandation.clear();
        notifyDataSetChanged();
    }
    public void addAll(List<Recommandation> list) {
        mRecommandation.addAll(list);
        notifyDataSetChanged();
    }

    public boolean heCanBePlayed(String s){
        if(s.equals("album") || s.equals("artist") || s.equals("track")){
            return true;
        } else {
            return false;
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nbrlike;
        TextView nbrCom;

        TextView pseudoCom;
        TextView autreComment;

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

        final LinearLayout layout;
        final LinearLayout.LayoutParams params;

        public ViewHolder(View itemView) {
            super(itemView);

            nbrlike = itemView.findViewById(R.id.nbrLike);
            nbrCom = itemView.findViewById(R.id.nbrComment);

            pseudoCom = itemView.findViewById(R.id.pseudoComment);
            autreComment = itemView.findViewById(R.id.autreComment);

            commentButton = itemView.findViewById(R.id.commentButton);
            bookmarkButton = itemView.findViewById(R.id.bookButton);
            likeButton = itemView.findViewById(R.id.likeButton);


            pictureRecommandation = itemView.findViewById(R.id.img_ar);

            timeRecommandation = itemView.findViewById(R.id.time);
            descRecommandation = itemView.findViewById(R.id.desc);
            nomRecommandation = itemView.findViewById(R.id.name);
            pictureUserRecommandation = itemView.findViewById(R.id.imgProfil);

            playButton = itemView.findViewById(R.id.playButton);
            circle = itemView.findViewById(R.id.circle);


            //playButton.setVisibility(View.INVISIBLE);
            //circle.setVisibility(View.INVISIBLE);
            layout =itemView.findViewById(R.id.linearLayoutReco);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setAutreComment(String autreComment1){
            autreComment.setText(autreComment1);
        }

        public void setTime(String timeText){
            TextView time = itemView.findViewById(R.id.time);
            time.setText(timeText);
        }

        public void setPseudoCom(String pseudo){
            pseudoCom.setText(pseudo);
        }

        public void setDesc(Spanned desc){
            descRecommandation.setText(desc);
        }

        public TextView getDesc(){
            return descRecommandation;
        }

        public void setTitre(Spanned text){
            TextView nameR = itemView.findViewById(R.id.name);
            nameR.setText(text);
        }

        public ImageButton getImg() {
            ImageButton imgR = itemView.findViewById(R.id.img_ar);
            return imgR;
        }

        public CircleImageView getImgProfil(){
            CircleImageView imgProfil = itemView.findViewById(R.id.imgProfil);
            return imgProfil;
        }

        public ImageButton getLikeButton(){
            ImageButton imgButton = itemView.findViewById(R.id.likeButton);
            return imgButton;
        }

        public void setNbrLike(String text){
            nbrlike.setText(text);
        }

        public void setNbrCom(String text){
            nbrCom.setText(text);
        }

        public TextView getListLike(){
            TextView nbrLikeBut = itemView.findViewById(R.id.nbrLike);
            return  nbrLikeBut;
        }

        public ImageButton getCommentButton(){
            ImageButton button = itemView.findViewById(R.id.commentButton);
            return button;
        }

        public ImageButton getBookButton(){
            ImageButton img = itemView.findViewById(R.id.bookButton);
            return img;
        }

    }

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

    public interface MediaListener{
        void lancerMusique(Recommandation recommandation);
        void lancerVideo(Recommandation recommandation);
        void stop();
    }


}
