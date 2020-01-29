package com.example.sharehitv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import com.example.sharehitv2.Adapter.ListLikeAdapter;
import com.example.sharehitv2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListLikePage extends AppCompatActivity {

    private DatabaseReference recosRef, usersRef;
    private FirebaseAuth mAuth;
    public RecyclerView recyclerLike;

    public ListLikeAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_like_page);

        // Initialisation page ListLike
        recyclerLike = (RecyclerView) findViewById(R.id.likeRecyclerView);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainerListlike);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Aimé par");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Bundle b = getIntent().getExtras();

        //Initialisation des références Firebase
        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos").child(b.getString("key")).child("likeUsersUid");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Création du swipe up pour refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.notifyDataSetChanged();
                chargerRecyclerView(chargerListUser());
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        chargerRecyclerView(chargerListUser());

    }

    private List<User> chargerListUser() {
        final List<User> listUser = new ArrayList<>();

        recosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot child : dataSnapshot.getChildren()){
                    usersRef.child(child.getValue().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                User user = new User(dataSnapshot.child("pseudo").getValue().toString(),dataSnapshot.child("pseudo_lower").getValue().toString(), child.getValue().toString());
                                listUser.add(user);
                                chargerRecyclerView(listUser);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return listUser;

    }

    public void chargerRecyclerView(List<User> list){
        adapter = new ListLikeAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerLike.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
        recyclerLike.setAdapter(adapter);
    }


}

