package com.example.sharehitv2.NavigationFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Adapter.SearchUserAdapter;
import com.example.sharehitv2.Model.User;
import com.example.sharehitv2.R;
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
    RelativeLayout aucunResultat;

    private int ancienneHauteurAucun;

    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private SearchUserAdapter searchUserAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search_user, container, false);


        recyclerViewSearchProfil = root.findViewById(R.id.recyclerViewSearchProfil);
        searchProfilBar = root.findViewById(R.id.searchProfilBar);
        aucunResultat = root.findViewById(R.id.aucunResultatProfil);

        ViewGroup.LayoutParams params = aucunResultat.getLayoutParams();
        ancienneHauteurAucun=params.height;
        params.height=0;
        aucunResultat.setLayoutParams(params);


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        searchProfilBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFriends(query);
                Log.e("friend", query );
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
        chargerRecyclerView(chargerListUser(text.toLowerCase()));

    }

    public void chargerRecyclerView(List<User> list){
        if(list.size()== 0){
            aucunResultat.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params1=aucunResultat.getLayoutParams();
            params1.height=ancienneHauteurAucun;
            aucunResultat.setLayoutParams(params1);
        }else {
            aucunResultat.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params1 = aucunResultat.getLayoutParams();
            params1.height = 0;
            aucunResultat.setLayoutParams(params1);
        }
        searchUserAdapter = new SearchUserAdapter(list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerViewSearchProfil.setLayoutManager(layoutManager);
        recyclerViewSearchProfil.setAdapter(searchUserAdapter);
    }

    public List<User> chargerListUser(final String text){
        final List<User> mUser = new ArrayList<>();
        Query query = usersRef.orderByChild("pseudo");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(!mAuth.getUid().equals(child.getKey())) {
                        if (child.child("pseudo").toString().toLowerCase().contains(text)) {
                            User user = new User(
                                    child.child("pseudo").getValue().toString(),
                                    child.getKey()
                            );
                            mUser.add(user);
                            Log.e("friend", user.getPseudo());
                        }
                    }
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
            pseudo = itemView.findViewById(R.id.pseudoSearchUser);
            image = itemView.findViewById(R.id.imageSearchUser);
        }


    }


}