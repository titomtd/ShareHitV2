package com.example.sharehitv2.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sharehitv2.Model.Bookmark;
import com.example.sharehitv2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookmarkAdapter extends
        RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    Context context;
    private List<Bookmark> mBookmark;

    private FirebaseAuth mAuth;

    public BookmarkAdapter(List<Bookmark> bookmarks) {
        this.mBookmark = bookmarks;
        mAuth = FirebaseAuth.getInstance();
    }



    @Override
    public BookmarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View bookmarkView = inflater.inflate(R.layout.bookmark_item_display, parent, false);

        ViewHolder viewHolder = new ViewHolder(bookmarkView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BookmarkAdapter.ViewHolder viewHolder, final int position) {
        final Bookmark bookmark = mBookmark.get(position);
        final String newLine = System.getProperty("line.separator");
        final String messageSuppression;

        TextView titre = viewHolder.titreBookmark;
        TextView sousTitre = viewHolder.sousTitreBookmark;
        final TextView type = viewHolder.typeBookmark;

        switch (bookmark.getRecommandation().getType()){
            case "artist":
                titre.setText(bookmark.getRecommandation().getArtist());
                sousTitre.setText("");
                type.setText("Artiste");
                messageSuppression = bookmark.getRecommandation().getArtist();
                break;
            case "album":
                titre.setText(bookmark.getRecommandation().getAlbum());
                sousTitre.setText(bookmark.getRecommandation().getArtist());
                type.setText("Album");
                messageSuppression = bookmark.getRecommandation().getAlbum() + ", " + bookmark.getRecommandation().getArtist();
                break;
            case "track":
                titre.setText(bookmark.getRecommandation().getTrack());
                sousTitre.setText(bookmark.getRecommandation().getArtist());
                type.setText("Morceau");
                messageSuppression = bookmark.getRecommandation().getTrack() + ", " + bookmark.getRecommandation().getArtist();
                break;
            case "game":
                titre.setText(bookmark.getRecommandation().getArtist());
                sousTitre.setText("");
                type.setText("Jeu vidéo");
                messageSuppression = bookmark.getRecommandation().getArtist();
                break;
            case "serie":
                titre.setText(bookmark.getRecommandation().getArtist());
                sousTitre.setText("");
                type.setText("Série");
                messageSuppression = bookmark.getRecommandation().getArtist();
                break;
            case "movie":
                titre.setText(bookmark.getRecommandation().getArtist());
                sousTitre.setText("");
                type.setText("Film");
                messageSuppression = bookmark.getRecommandation().getArtist();
                break;
            default:
                throw new IllegalStateException("Bug");
        }

        ImageView imageView = viewHolder.imgBookmark;
        if(!bookmark.getRecommandation().getUrlImage().equals("")) Picasso.with(context).load(bookmark.getRecommandation().getUrlImage()).fit().centerInside().into(imageView);
        final ImageView bookmarkDelete = viewHolder.bookmarkDelete;
        bookmarkDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Supprimer ?")
                        .setMessage(messageSuppression + newLine + type.getText())
                        .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("bookmarks").child(bookmark.getKeyBookmark()).removeValue();
                                mBookmark.remove(bookmark);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .setIcon(R.drawable.ic_exclamation_mark)
                        .show();

            }
        });

        ImageView bookmarkLink = viewHolder.bookmarkLink;
        bookmarkLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link ="";
                if (bookmark.getRecommandation().getType().equals("track")){
                    link="https://www.deezer.com/fr/track/"+bookmark.getRecommandation().getId();
                } else if (bookmark.getRecommandation().getType().equals("album")){
                    link="https://www.deezer.com/fr/album/"+bookmark.getRecommandation().getId();
                }else if (bookmark.getRecommandation().getType().equals("artist")){
                    link="https://www.deezer.com/fr/artist/"+bookmark.getRecommandation().getId();
                } else{
                    link="https://www.imdb.com/title/"+bookmark.getRecommandation().getId();
                }
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse(link));
                context.startActivity(viewIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mBookmark.size();
    }

    public void clear() {
        mBookmark.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Bookmark> list) {
        mBookmark.addAll(list);
        notifyDataSetChanged();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titreBookmark, sousTitreBookmark, typeBookmark;
        public ImageView imgBookmark, bookmarkDelete, bookmarkLink;
        public LinearLayout bookmarkMain;

        public ViewHolder(View itemView) {
            super(itemView);

            titreBookmark = (TextView) itemView.findViewById(R.id.bookmarkTitre);
            sousTitreBookmark = (TextView) itemView.findViewById(R.id.bookmarkSoustitre);
            typeBookmark = (TextView) itemView.findViewById(R.id.bookmarkType);
            imgBookmark = (ImageView) itemView.findViewById(R.id.bookmarkImage);
            bookmarkDelete = (ImageView) itemView.findViewById(R.id.bookmarkDelete);
            bookmarkLink = (ImageView) itemView.findViewById(R.id.bookmarkLink);
            bookmarkMain = (LinearLayout) itemView.findViewById(R.id.bookmarkMain);
        }
    }
}
