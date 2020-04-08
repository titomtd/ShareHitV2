package com.example.sharehitv2;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.sharehitv2.Adapter.RecommandationAdapter;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.NavigationFragment.FeedPageFragment;
import com.example.sharehitv2.NavigationFragment.Fragment.FeedFragment;
import com.example.sharehitv2.NavigationFragment.Fragment.FollowFragment;
import com.example.sharehitv2.NavigationFragment.MyProfilFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PagePrincipale extends AppCompatActivity implements RecommandationAdapter.MediaListener, FeedFragment.ActionBarInteraction, FollowFragment.ActionBarInteraction {

    private AppBarConfiguration mAppBarConfiguration;
    private long backPressedTime;
    private Toast backToast;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("testest", "load page principale");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_principale);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View header = navigationView.getHeaderView(0);
        final TextView text = (TextView) header.findViewById(R.id.pseudoProfilHeader);
        final TextView text2 = (TextView) header.findViewById(R.id.emailProfilHeader);
        final CircleImageView imageView = (CircleImageView) header.findViewById(R.id.imageProfilHeader);
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(dataSnapshot.child("pseudo").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        text2.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                Picasso.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/sharehitv2.appspot.com/o/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"?alt=media").fit().centerInside().into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Picasso.with(context).load("").fit().centerInside().into(viewHolder.getImgProfil());
                imageView.setImageResource(R.drawable.default_profile_picture);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void lancerMusique(Recommandation recommandation) {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if(fragment instanceof MyProfilFragment){
            ((MyProfilFragment) fragment).lancerMusique(recommandation);
        }
        if(fragment instanceof FeedPageFragment){
            ((FeedPageFragment) fragment).lancerMusique(recommandation);
        }
    }

    @Override
    public void lancerVideo(Recommandation recommandation) {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        Log.e("MediaListener", ""+fragment);
        if(fragment instanceof MyProfilFragment){
            ((MyProfilFragment) fragment).lancerVideo(recommandation);
        }
        if(fragment instanceof FeedPageFragment){
            ((FeedPageFragment) fragment).lancerVideo(recommandation);
        }

    }

    @Override
    public void stop() {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if(fragment instanceof MyProfilFragment){
            ((MyProfilFragment) fragment).stop();
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            finishAffinity();
            System.exit(0);
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Appuyez une seconde fois pour quitter", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }


    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }
}
