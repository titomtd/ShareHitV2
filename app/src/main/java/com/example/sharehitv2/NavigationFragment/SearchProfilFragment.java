package com.example.sharehitv2.NavigationFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Adapter.SearchUserAdapter;
import com.example.sharehitv2.Model.User;
import com.example.sharehitv2.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchProfilFragment extends Fragment {

    RecyclerView recyclerViewSearchProfil;
    SearchView searchProfilBar;

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private SearchUserAdapter searchUserAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_user, container, false);

        recyclerViewSearchProfil = (RecyclerView) root.findViewById(R.id.recyclerViewSearchProfil);
        searchProfilBar = (SearchView) root.findViewById(R.id.searchProfilBar);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        searchProfilBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriends(query);
                /*
                List list = chargerListUser(query);
                chargerRecyclerView(list);

                 */
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return root;
    }

    private void searchFriends(String text) {
        chargerRecyclerView(chargerListUser(text));

    }

    public void chargerRecyclerView(List<User> list){
        searchUserAdapter = new SearchUserAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewSearchProfil.setLayoutManager(layoutManager);
        recyclerViewSearchProfil.setAdapter(searchUserAdapter);
    }

    public List<User> chargerListUser(String text){
        final List<User> mUser = new ArrayList<>();
        Query query = usersRef.orderByChild("pseudo").startAt(text).endAt(text + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    User user = new User(
                            child.getValue().toString(),
                            child.getKey()
                    );
                    mUser.add(user);
                    chargerRecyclerView(mUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return mUser;
    }

    public class SearchProfilHolder extends RecyclerView.ViewHolder {

        TextView pseudo;
        CircleImageView image;

        public SearchProfilHolder(View itemView) {
            super(itemView);
            pseudo = (TextView) itemView.findViewById(R.id.pseudoSearchUser);
            image = (CircleImageView) itemView.findViewById(R.id.imageSearchUser);
        }


    }


}