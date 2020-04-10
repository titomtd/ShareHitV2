package com.example.sharehitv2.NavigationFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sharehitv2.R;

public class ConfidentialitéFragment extends Fragment {

    private FrameLayout frameAPropos, frameCondition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_condidentialite, container, false);

        frameAPropos = root.findViewById(R.id.frameAPropos);
        frameCondition = root.findViewById(R.id.frameCondition);


        frameAPropos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Ce bouton redirige sur le site, il est actuellement en cours de création", Toast.LENGTH_LONG).show();
            }
        });

        frameCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Ce bouton redirige sur le site, il est actuellement en cours de création", Toast.LENGTH_LONG).show();
            }
        });


        return root;
    }
}