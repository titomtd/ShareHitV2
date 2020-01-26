package com.example.sharehitv2.NavigationFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sharehitv2.ApiManager;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.NavigationFragment.Fragment.FeedFragment;
import com.example.sharehitv2.NavigationFragment.Fragment.FollowFragment;
import com.example.sharehitv2.PagePrincipale;
import com.example.sharehitv2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedPageFragment extends Fragment {

    SpaceNavigationView navigationView;
    FirebaseAuth firebaseAuth;
    ImageButton artiste;
    ImageButton album;
    ImageButton morceau;
    ImageButton jeuVideo;
    ImageButton serie;
    ImageButton film;
    FrameLayout container;
    int currentItem=0;
    ImageButton notification, search;

    private FirebaseAuth mAuth;
    private DatabaseReference notifRef;
    CircleImageView profilePicture;

    private Bundle b;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key" + "AAAA7jlTwSc:APA91bEri7kGWiI-rXn3iE1LOttF0c8FJWcRr6MJwYUAhhTIjf4flxtr_lMEso12JyidGtZqIu-Gb3R74xU_m2ioLhyRNm-OnqyuW8uOY9DY9UIK4IZdSJVDURFAQd4sfdcVxFWfajp7";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;



    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null) {
            startActivity(new Intent(getContext(), PagePrincipale.class));
        }
    }

    private boolean loadFragement(Fragment fragment, boolean left){
        if(fragment != null){
            //getSupportFragmentManager().beginTransaction().replace(id.container, fragment).commit();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            if(left==true){
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_left);
            }else {
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right );
            }
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack("test");
            transaction.commit();
            return true;
        }
        return false;
    }

    private boolean loadFragement(Fragment fragment){
        if(fragment != null){
            //getSupportFragmentManager().beginTransaction().replace(id.container, fragment).commit();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.commit();

            return true;
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_feed_page, container, false);



        mAuth = FirebaseAuth.getInstance();
        container = root.findViewById(R.id.container);
        navigationView = root.findViewById(R.id.space);




        navigationView.initWithSaveInstanceState(savedInstanceState);
        navigationView.addSpaceItem(new SpaceItem("Tous", R.drawable.time_icon));
        navigationView.addSpaceItem(new SpaceItem("Suivi", R.drawable.star_icon));
        //navigationView.showIconOnly();






        final Dialog d = new Dialog(getContext(), R.style.DialogTheme);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.new_recommendation);
        ImageButton i = d.findViewById(R.id.artiste);
        Window window = d.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);


        //window.setAttributes(wlp);
        b = getActivity().getIntent().getExtras();


        Fragment fragment = new FeedFragment();
        loadFragement(fragment,true);

        navigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            Fragment fragment = null;
            @Override
            public void onCentreButtonClick() {

                final Intent intent = new Intent(getContext(), ApiManager.class);
                final Bundle b = new Bundle();
                d.show();
                artiste = d.findViewById(R.id.artiste);
                artiste.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        b.putInt("key", 1); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        startActivity(intent);
                        //finish();
                    }
                });
                album = d.findViewById(R.id.album);
                album.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        b.putInt("key", 2); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        startActivity(intent);
                        //finish();
                    }
                });

                morceau = d.findViewById(R.id.morceau);
                morceau.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        b.putInt("key", 3);
                        intent.putExtras(b);
                        startActivity(intent);
                        //finish();
                    }
                });

                film = d.findViewById(R.id.film);
                film.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        b.putInt("key", 4);
                        intent.putExtras(b);
                        startActivity(intent);
                        //finish();
                    }
                });

                jeuVideo = d.findViewById(R.id.jeuVideo);
                jeuVideo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        b.putInt("key", 6);
                        intent.putExtras(b);
                        startActivity(intent);
                        //finish();
                    }
                });

                serie = d.findViewById(R.id.serie);
                serie.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        b.putInt("key", 5);
                        intent.putExtras(b);
                        startActivity(intent);
                        //finish();
                    }
                });


            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                int anciennePage = currentItem;
                if(itemIndex == 0){
                    fragment = new FeedFragment();
                }else if (itemIndex == 1){
                    fragment = new FollowFragment();
                }else if(itemIndex == 2){
                    //fragment = new BookmarkFragment();
                    //
                }else if(itemIndex == 3){
                    //fragment = new ProfilFragment();
                }
                if(currentItem>itemIndex){
                    loadFragement(fragment,false);
                } else{
                    loadFragement(fragment,true);
                }
                currentItem=itemIndex;
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                //Toast.makeText(FeedPage.this, itemIndex + " " + itemName, Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }






}