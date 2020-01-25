package com.example.sharehitv2.NavigationFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sharehitv2.Adapter.BookmarkAdapter;
import com.example.sharehitv2.Model.Bookmark;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookmarkPrivateFragment extends Fragment {

    private DatabaseReference recosRef, bookRef;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerview;
    private FloatingActionButton typeListBookmark;

    private boolean tri;
    private int idx;

    public BookmarkAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    private List<Bookmark> bookmarks;

    private boolean isCharged;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmark_private, container, false);

        swipeContainer = (SwipeRefreshLayout) root.findViewById(R.id.swipeContainer);
        recyclerview = (RecyclerView) root.findViewById(R.id.postBookmarkRecyclerView);
        typeListBookmark = (FloatingActionButton) root.findViewById(R.id.typeBookmarkList);


        mAuth = FirebaseAuth.getInstance();
        recosRef = FirebaseDatabase.getInstance().getReference().child("recos");
        bookRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("bookmarks");

        isCharged = true;


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!tri){
                    isCharged = true;
                    adapter.notifyDataSetChanged();
                    chargerRecyclerView(chargerListBookmark());
                } else {
                    //chargerRecyclerView(trierList(idx));
                }
                swipeContainer.setRefreshing(false);

            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        typeListBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String options[] = {"Artiste","Album","Morceau","Serie","Film","Jeux vidéos", "Aucun tri"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Trier par :");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            idx = 0;
                            chargerRecyclerView(trierList(0));
                        } else if (which==1){
                            idx = 1;
                            chargerRecyclerView(trierList(1));
                        } else if (which == 2){
                            idx = 2;
                            chargerRecyclerView(trierList(2));
                        } else if (which == 3){
                            idx = 3;
                            chargerRecyclerView(trierList(3));
                        } else if (which == 4){
                            idx = 4;
                            chargerRecyclerView(trierList(4));
                        } else if (which == 5){
                            idx = 5;
                            chargerRecyclerView(trierList(5));
                        } else if (which == 6){
                            chargerRecyclerView(chargerListBookmark());
                            tri = false;
                        }
                    }
                });
                builder.create().show();
            }
        });

        chargerRecyclerView(chargerListBookmark());

        return root;
    }
    public List<Bookmark> trierList(int i){
        tri = true;
        final List<Bookmark> listBookmark = new ArrayList<>();
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
        for(Bookmark bookmark : bookmarks){
            if(bookmark.getRecommandation().getType().equals(type)){
                listBookmark.add(bookmark);
                chargerRecyclerView(listBookmark);
            }
        }
        return listBookmark;
    }

    public final List<Bookmark> chargerListBookmark(){
        final List<Bookmark> listBookmark = new ArrayList<>();
        bookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isCharged) {
                    for(final DataSnapshot child : dataSnapshot.getChildren()){
                        recosRef.child(child.getValue().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Recommandation recommandation = new Recommandation(
                                        dataSnapshot.child("album").getValue().toString(),
                                        dataSnapshot.child("artist").getValue().toString(),
                                        dataSnapshot.child("id").getValue().toString(),
                                        Double.parseDouble(dataSnapshot.child("timestamp").getValue().toString()),
                                        dataSnapshot.child("track").getValue().toString(),
                                        dataSnapshot.child("type").getValue().toString(),
                                        dataSnapshot.child("urlImage").getValue().toString(),
                                        dataSnapshot.child("urlPreview").getValue().toString(),
                                        dataSnapshot.child("userRecoUid").getValue().toString(),
                                        child.getValue().toString());
                                Bookmark bookmark = new Bookmark(child.getKey(), recommandation);
                                listBookmark.add(bookmark);
                                bookmarks = listBookmark;
                                chargerRecyclerView(listBookmark);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                        isCharged = false;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        return listBookmark;
    }

    public void chargerRecyclerView(List<Bookmark> list){
        adapter = new BookmarkAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerview.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
        recyclerview.setAdapter(adapter);
        recyclerview.setVisibility(View.VISIBLE);
    }

}