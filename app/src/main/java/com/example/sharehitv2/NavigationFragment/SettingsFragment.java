package com.example.sharehitv2.NavigationFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.sharehitv2.Authentification.LoginPage;
import com.example.sharehitv2.PagePrincipale;
import com.example.sharehitv2.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

public class SettingsFragment extends Fragment {

    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int PICK_IMAGE_REQUEST = 111;
    static String[] storagePermissions;
    private static Uri imguri;
    private static StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private static FirebaseUser user;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new Settings())
                .commit();

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        return root;
    }

    public static class Settings extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {


            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference deconnexion = findPreference("signout");
            Preference changePdp = findPreference("changePdp");
            Preference changePseudo = findPreference("changePseudo");

            deconnexion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Se déconnecter ?")
                            .setPositiveButton("Déconnexion", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(getActivity(), LoginPage.class));
                                }
                            })
                            .setNegativeButton("Annuler", null)
                            .setIcon(R.drawable.ic_exclamation_mark)
                            .show();
                    return false;
                }
            });

            changePdp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                    return false;
                }

            });

            changePseudo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showNameUpdateDialog();
                    return false;
                }

            });
        }

        private boolean checkStoragePermission(){
            boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == (PackageManager.PERMISSION_GRANTED);
            return  result;
        }

        private void requestStoragePermission(){
            ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);

        }


        private void showNameUpdateDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Changer de pseudo");
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setPadding(10,10,10,10);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            final EditText editText = new EditText(getActivity());
            editText.setHint("Nouveau pseudo...");
            linearLayout.addView(editText);
            builder.setView(linearLayout);
            builder.setPositiveButton("Changer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String value = editText.getText().toString().trim();
                    if(!TextUtils.isEmpty(value)){
                        if(editText.getText().toString().trim().length() < 16){
                            //pd.show();
                            HashMap reslt = new HashMap();
                            reslt.put("pseudo", value);
                            reslt.put("pseudo_lower", value.toLowerCase());
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                            usersRef.updateChildren(reslt);
                            Toast.makeText(getContext(), "Pseudo changé", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Le pseudo doit être inférieur à 16 caractères", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        Toast.makeText(getContext(), "Veuillez entrer un pseudo", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.create().show();
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            switch (requestCode){
                case STORAGE_REQUEST_CODE:{


                    if(grantResults.length >0){
                        boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        if(writeStorageAccepted){
                            pickFromGallery();
                        }else {
                            Toast.makeText(getActivity(), "Veuillez autoriser l'accès à la galerie", Toast.LENGTH_SHORT);

                        }
                    }

                }


            }

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }



        private void pickFromGallery() {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);

        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);


            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                imguri = data.getData();
            /*File compressedImageFile = new File(imguri.getPath());
            try {
                OutputStream os = new FileOutputStream(compressedImageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("application/octet-stream")
                        .build();
                final StorageReference filepath = mStorageRef.child(user.getUid());
                filepath.putFile(imguri, metadata).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        deleteCache(getContext());
                        Toast.makeText(getContext(), "Photo de profil changée", Toast.LENGTH_SHORT).show();
                    }

                });


            }


        }

        public static void deleteCache(Context context) {
            try {
                File dir = context.getCacheDir();
                deleteDir(dir);
            } catch (Exception e) { e.printStackTrace();}
        }

        public static boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if(dir!= null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        }

    }

}