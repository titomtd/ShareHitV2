package com.example.sharehitv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

public class ListFollowPage extends AppCompatActivity {

    private RecyclerView listFollowRecyclerView;

    private DatabaseReference recosRef, usersRef;
    private FirebaseAuth mAuth;

    public ListLikeAdapter adapter;

    Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_follow_page);

        listFollowRecyclerView = findViewById(R.id.listFollowRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        b = getIntent().getExtras();

        Toolbar toolbar = findViewById(R.id.toolbarListFollow);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        toolbar.setNavigationIcon(R.drawable.ic_back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListFollowPage.super.onBackPressed();
            }
        });

        usersRef.child(b.getString("key")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                actionBar.setTitle("Abonnements de " + dataSnapshot.child("pseudo").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chargerRecyclerView(chargerListFollow());


    }

    public final List<User> chargerListFollow(){
        final List<User> list = new ArrayList<>();
        usersRef.child(b.getString("key")).child("followed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(final DataSnapshot child : dataSnapshot.getChildren()){
                    usersRef.child(child.getValue().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                User user = new User(dataSnapshot.child("pseudo").getValue().toString(), child.getValue().toString());
                                list.add(user);
                                chargerRecyclerView(list);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}});

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}});
        return list;
    }


    public void chargerRecyclerView(List<User> list){
        adapter = new ListLikeAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        listFollowRecyclerView.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
        listFollowRecyclerView.setAdapter(adapter);
    }
}
