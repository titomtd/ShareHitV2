package com.example.sharehitv2;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sharehitv2.Adapter.TypeAdapter;
import com.example.sharehitv2.Model.Album;
import com.example.sharehitv2.Model.Artist;
import com.example.sharehitv2.Model.Morceau;
import com.example.sharehitv2.Model.Recommandation;
import com.example.sharehitv2.Model.Type;
import com.example.sharehitv2.Model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class ApiManager extends AppCompatActivity implements TypeAdapter.OnItemclickListener, TypeAdapter.MusicListener {

    public static final String EXTRA_URL = "imgUrl";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_ID = "userRecoUid";
    public static final String EXTRA_PREVIEW = "userUrlPreview";
    public static final String EXTRA_LINK = "link";

    private RecyclerView mRecyclerView;
    private TypeAdapter mExampleAdapter;
    private ArrayList<Type> mExampleList;
    private RequestQueue mRequestQueue;
    private Context context;
    private SearchView search;
    public int type;
    private String typeRecom;
    private RelativeLayout aucun;


    private final static MediaPlayer mp = new MediaPlayer();

    private LinearLayout lecteur;
    private ProgressBar mSeekBarPlayer;
    private ImageButton stop;
    private ImageView musicImg;
    private ImageButton btnPause;
    private TextView nameLect;

    private int ancienneHauteurAucun = 0;

    private ValueAnimator lecteurApparait;
    private ValueAnimator lecteurDisparrait;

    private int ancienneHauteurLecteur;

    @Override
    public void onPause() {
        super.onPause();
        mp.pause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dezer_api);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        search = findViewById(R.id.searchv);

        Bundle b = getIntent().getExtras();
        if(b != null)
            type = b.getInt("key");

        if(type == 1){
            search.setQueryHint("Rechercher un artiste...");
        }
        if(type == 2) {
            search.setQueryHint("Rechercher un album...");
        }
        if(type == 3) {
            search.setQueryHint("Rechercher un morceau...");
        }
        if(type == 4) {
            search.setQueryHint("Rechercher un film...");
        }
        if(type == 5) {
            search.setQueryHint("Rechercher une série...");
        }
        if(type == 6) {
            search.setQueryHint("Rechercher un jeu vidéo...");
        }

        mRecyclerView = findViewById(R.id.recycler_view_d);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mExampleList = new ArrayList<Type>();
        mRequestQueue = Volley.newRequestQueue(this);

        lecteur = findViewById(R.id.lecteur);
        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        ancienneHauteurLecteur=params.height;
        params.height=0;
        lecteur.setLayoutParams(params);

        lecteurApparait = ValueAnimator.ofInt(lecteur.getMeasuredHeight(), ancienneHauteurLecteur);
        lecteurApparait.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lecteur.getLayoutParams();
                layoutParams.height = val;
                lecteur.setLayoutParams(layoutParams);
            }
        });
        lecteurApparait.setDuration(400);

        lecteurDisparrait = ValueAnimator.ofInt(ancienneHauteurLecteur,0);
        lecteurDisparrait.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = lecteur.getLayoutParams();
                layoutParams.height = val;
                lecteur.setLayoutParams(layoutParams);
            }
        });
        lecteurDisparrait.setDuration(400);

        aucun = findViewById(R.id.aucun);
        aucun.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
        ancienneHauteurAucun=params1.height;
        params1.height=0;
        aucun.setLayoutParams(params1);

        stop = lecteur.findViewById(R.id.button1);
        btnPause = lecteur.findViewById(R.id.button2);
        mSeekBarPlayer = lecteur.findViewById(R.id.progressBar);
        nameLect = lecteur.findViewById(R.id.nameLect);
        musicImg = lecteur.findViewById(R.id.musicImg);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                mExampleList.clear();
                if(type == 1){
                    parseJSONartist(query);
                    typeRecom="artist";
                }
                if(type == 2) {
                    parseJSONalbum(query);
                    typeRecom="album";
                }
                if(type == 3) {
                    parseJSONtrack(query);
                    typeRecom="track";
                }
                if(type == 4) {
                    parseJSONfilm(query);
                    typeRecom="movie";
                }
                if(type == 5) {
                    parseJSONserie(query);
                    typeRecom="serie";
                }
                if(type == 6) {
                    parseJSONjeu(query);
                    typeRecom="game";
                }

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });


    }

    private Map<String, String> parseJSONartist(String artistName) {
        Log.e("researchAPI", "inside");

        String url = "https://api.deezer.com/2.0/search/artist/?q="+artistName+"&index=0&nb_items=20&output=json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String name = data.getString("name");
                        String nbFan = data.getString("nb_fan");
                        String imgUrl = data.getString("picture_big");
                        String link = data.getString("id");
                        String tracklist = data.getString("tracklist");
                        final Artist artiste = new Artist(name, nbFan, imgUrl, link);
                        parseJSONartistPreview(tracklist, artiste);
                        mExampleList.add(artiste);
                        Log.e("researchAPI", "inside");
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("researchAPI", "erreur : "+ error);
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONartistPreview(String tracklist, final Artist artiste) {

        if(tracklist.endsWith("50"))
        {
            tracklist = tracklist.substring(0,tracklist.length() - 2);
            tracklist+="1";
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tracklist, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0 ; jsonArray.length() > i; i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        String urlPreview = data.getString("preview");
                        artiste.setSongUrl(urlPreview);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONalbum(String albumName) {

        String url = "https://api.deezer.com/2.0/search/album/?q="+albumName+"&index=0&nb_items=20&output=json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String name = data.getString("title");
                        JSONObject artiste = data.getJSONObject("artist");
                        String artistName = artiste.getString("name");
                        String imgUrl = data.getString("cover_big");
                        String link = data.getString("id");
                        String tracklist = data.getString("tracklist");
                        Album album = new Album(artistName, name, imgUrl, link);
                        parseJSONalbumPreview(tracklist, album);
                        mExampleList.add(album);
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONalbumPreview(String tracklist, final Album album) {

        if(tracklist.endsWith("50"))
        {
            tracklist = tracklist.substring(0,tracklist.length() - 2);
            tracklist+="1";
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, tracklist, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0 ; jsonArray.length() > i; i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        String urlPreview = data.getString("preview");
                        album.setSongUrl(urlPreview);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONtrack(String trackName) {
        Log.e("researchAPItrack", "+," +trackName);
        String url = "https://api.deezer.com/2.0/search/track/?q="+trackName+"&index=0&nb_items=20&output=json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        JSONObject artist = data.getJSONObject("artist");
                        JSONObject album = data.getJSONObject("album");
                        String albumTitle = album.getString("title");
                        String title = data.getString("title");
                        String artistName = artist.getString("name");
                        String imgUrl = artist.getString("picture_big");
                        String previewUrl = data.getString("preview");
                        String link = data.getString("id");
                        Morceau m = new Morceau(title, albumTitle, imgUrl,previewUrl,link, artistName);
                        mExampleList.add(m);
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONomdb(String search, final String type) {

        String url = "https://www.omdbapi.com/?s="+search+"&type="+type+"&apikey=a20c2297";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("Search");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String title = data.getString("Title");
                        String year = data.getString("Year");
                        String imgUrl = data.getString("Poster");
                        String imdbID = data.getString("imdbID");
                        mExampleList.add(new Video(title, year, imgUrl, imdbID, null));
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONfilm(String search) {

        String url = "https://api.betaseries.com/movies/search?key=0c0daac032e2&v=3.0&title="+search;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("movies");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String title = data.getString("title");
                        String year = data.getString("release_date");
                        String imgUrl = data.getString("poster");
                        String imdbID = data.getString("imdb_id");
                        String idYoutube = data.getString("trailer");
                        mExampleList.add(new Video(title, year, imgUrl, imdbID, idYoutube));
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONjeu(String search) {

        String url = "https://www.giantbomb.com/api/search/?api_key=2d29fc0a0a5b099941aa22e519c0fe4c72a1c1f7&format=json&query="+search+"&resources=game";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("results");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String title = data.getString("name");
                        String year = data.getString("expected_release_year");
                        JSONObject images = data.getJSONObject("image");
                        String imgUrl = images.getString("original_url");
                        String imdbID = data.getString("site_detail_url");
                        //String idYoutube = data.getString("video");
                        mExampleList.add(new Video(title, year, imgUrl, imdbID, "null"));
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    private Map<String, String> parseJSONserie(String search) {

        String url = "https://api.betaseries.com/shows/search?key=0c0daac032e2&v=3.0&title="+search;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("shows");
                    for(int i = 0 ; jsonArray.length() > i; i++){
                        JSONObject data = jsonArray.getJSONObject(i);
                        String title = data.getString("title");
                        String year = data.getString("creation");
                        JSONObject images = data.getJSONObject("images");
                        String imgUrl = images.getString("poster");
                        String imdbID = data.getString("imdb_id");
                        String idYoutube = data.getString("next_trailer");
                        mExampleList.add(new Video(title, year, imgUrl, imdbID, idYoutube));
                        Log.e("researchAPI", "inside");
                    }

                    if(mExampleList.size() == 0){
                        aucun.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams params1=aucun.getLayoutParams();
                        params1.height=ancienneHauteurAucun;
                        aucun.setLayoutParams(params1);
                    }else {
                        aucun.setVisibility(View.INVISIBLE);
                        ViewGroup.LayoutParams params1 = aucun.getLayoutParams();
                        params1.height = 0;
                        aucun.setLayoutParams(params1);
                    }

                    mExampleAdapter = new TypeAdapter(ApiManager.this, mExampleList);
                    mRecyclerView.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(ApiManager.this);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("researchAPI", "erreur : "+error);
                error.printStackTrace();
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-rapidapi-host", "deezerdevs-deezer.p.rapidapi.com");
                params.put("x-rapidapi-key", "e057a6cddamshcf40c6b8e5a6046p1233eajsnf273df986993");

                return params;
            }
        };

        mRequestQueue.add(request);
        return null;
    }

    @Override
    public void onItemClick(int position) {
        final Type clickedItem = mExampleList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_postrec,null);

        TextView name = dialogView.findViewById(R.id.name_post);
        name.setText(clickedItem.getName());

        ImageView img = dialogView.findViewById(R.id.picture_post);
        Picasso.with(getApplicationContext()).load(clickedItem.getImgUrl()).fit().centerInside().into(img);

        Button post = dialogView.findViewById(R.id.post_post_button);
        Button cancel = dialogView.findViewById(R.id.cancel_post_button);

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long time = new Date(System.currentTimeMillis()).getTime();
                Double timestamp = time.doubleValue();
                Recommandation recommandation = new Recommandation(
                        "",
                        "",
                        clickedItem.getLink(),
                        (double) currentTimeSecsUTC(),
                        "",
                        typeRecom,
                        clickedItem.getImgUrl(),
                        "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );
                if(clickedItem instanceof Morceau){
                    recommandation.setTrack(clickedItem.getName());
                    recommandation.setUrlPreview(clickedItem.getSongUrl());
                    recommandation.setAlbum(((Morceau) clickedItem).getAlbum());
                    recommandation.setArtist(clickedItem.getSpec());
                }
                else if(clickedItem instanceof Artist){
                    recommandation.setArtist(clickedItem.getName());
                    recommandation.setUrlPreview(clickedItem.getSongUrl());
                }
                else if(clickedItem instanceof Video){
                    recommandation.setUrlPreview(((Video) clickedItem).getIdYoutube());
                    recommandation.setArtist(clickedItem.getName());
                }
                else if(clickedItem instanceof Album){
                    recommandation.setAlbum(clickedItem.getName());
                    recommandation.setArtist(clickedItem.getSpec());
                    recommandation.setUrlPreview(clickedItem.getSongUrl());
                }else{
                    recommandation.setArtist(clickedItem.getName());
                }


                HashMap usersMap = new HashMap();
                DatabaseReference recomRef = FirebaseDatabase.getInstance().getReference().child("recos");

                /*usersMap.put("id",recommendation.getLink());
                usersMap.put("timestamp", new Timestamp(System.currentTimeMillis()).getTime());
                usersMap.put("track",recommendation.getName());
                usersMap.put("type",recommendation.getType());
                usersMap.put("urlImage", recommendation.getImg());
                usersMap.put("urlPreview", "");
                usersMap.put("userRecoUid", recommendation.getUserRecoUid());*/

                //HashMap mapFinal = new HashMap<>();

                String key = recomRef.push().getKey();
                usersMap.put(key,recommandation);
                recomRef.updateChildren(usersMap);
                startActivity(new Intent(ApiManager.this, PagePrincipale.class));
            }
        });

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void lancerMusique(Type model){
        //if (model instanceof Morceau) {
        //Morceau son = (Morceau) model;
        /*}else if (model instanceof Artist) {
            Artist son = (Artist) model;
        }else if (model instanceof Album) {
            Album son = (Album) model;
        }*/
        mp.seekTo(mp.getDuration());
        mp.reset();
        ViewGroup.LayoutParams params = lecteur.getLayoutParams();
        if(params.height==0) {
            lecteurApparait.start();
        }
        try{
            //Log.e("testest", ""+model.getName() );
            mp.setDataSource(model.getSongUrl());
        }
        catch (IOException ex){
            //Log.e("testest", "Can't found data:"+model.getSongUrl());
        }

        nameLect.setText(model.getName());

        /*if(model.getType().equals("track"))
            nameLect.setText(model.getTrack());
        else if(model.getType().equals("artist"))
            nameLect.setText(model.getArtist());
        else if(model.getType().equals("album"))
            nameLect.setText(model.getAlbum());*/
                        /*recosViewHolder.playButton.setVisibility(View.INVISIBLE);
                        recosViewHolder.player.setVisibility(View.VISIBLE);*/


        Picasso.with(context).load(model.getImgUrl()).fit().centerInside().into(musicImg);
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration = mp.getDuration();
                mSeekBarPlayer.setMax(duration);
                mp.start();
                mSeekBarPlayer.postDelayed(onEverySecond, 500);
            }
        });

        //recosViewHolder.playButton.startAnimation(buttonClick);

        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mp.stop();
                mp.reset();
                lecteurDisparrait.start();
            }
        });


        btnPause.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    btnPause.setImageResource(R.drawable.ic_play);
                                    /*recosViewHolder.playButton.setVisibility(View.VISIBLE);
                                    recosViewHolder.playButton.setImageResource(R.drawable.ic_pause);
                                    recosViewHolder.player.setVisibility(View.INVISIBLE);*/

                }
                else {
                    btnPause.setImageResource(R.drawable.ic_pause);
                                    /*recosViewHolder.playButton.setVisibility(View.INVISIBLE);
                                    recosViewHolder.player.setVisibility(View.VISIBLE);*/
                    try {
                        mp.prepare();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mp.start();
                    mSeekBarPlayer.postDelayed(onEverySecond, 1000);
                }

            }
        });
    }

    public static long currentTimeSecsUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis() / 1000;
    }

    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run(){
            if(mp != null) {
                mSeekBarPlayer.setProgress(mp.getCurrentPosition());
            }

            if(mp.isPlaying()) {
                btnPause.setImageResource(R.drawable.ic_pause);
                mSeekBarPlayer.postDelayed(onEverySecond, 100);
            }else{
                btnPause.setImageResource(R.drawable.ic_play);
            }
        }
    };



}
