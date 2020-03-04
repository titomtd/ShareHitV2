package com.example.sharehitv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        listFollowRecyclerView = (RecyclerView) findViewById(R.id.listFollowRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        b = getIntent().getExtras();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        usersRef.child(b.getString("key")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                actionBar.setTitle("Abonnements de " + dataSnapshot.child("pseudo").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chargerRecyclerView(chargerListUser(chagerListFollow()));


    }

    public List<String> chagerListFollow(){
        final List<String> l = new ArrayList<>();
        usersRef.child(b.getString("key")).child("followed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    l.add(child.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return l;
    }

    public List<User> chargerListUser(List<String> list){
        final List<User> listUser = new ArrayList<>();
        for(final String id : list){
            usersRef.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        User user = new User(dataSnapshot.child("pseudo").getValue().toString(),dataSnapshot.child("pseudo_lower").getValue().toString(), id);
                        listUser.add(user);
                        chargerRecyclerView(listUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return listUser;
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
