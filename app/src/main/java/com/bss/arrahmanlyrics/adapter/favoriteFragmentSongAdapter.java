package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 5/20/17.
 */

public class favoriteFragmentSongAdapter extends RecyclerView.Adapter<favoriteFragmentSongAdapter.MyViewHolder> {
    private View.OnClickListener mClickListener;
    private Context mContext;
    private List<songWithTitle> songlist;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, lyricist;
        ImageView dots;
        CircularImageView imageView;


        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.favSongtitle);
            lyricist = (TextView) view.findViewById(R.id.favSonglyricist);
           // dots = (ImageButton) view.findViewById(R.id.favmenu_button);
            imageView = (CircularImageView) view.findViewById(R.id.favsongCover);


            //albumCover = (ImageView) view.findViewById(R.id.album_artwork);
        }
    }

    public favoriteFragmentSongAdapter(Context mContext, List<songWithTitle> songlist) {
        this.mContext = mContext;
        this.songlist = songlist;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_song_list_view, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        return (MyViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        songWithTitle actualsong = songlist.get(position);

        final Typeface title = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
        final Typeface lyricist = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
        holder.name.setTypeface(title);
        holder.lyricist.setTypeface(lyricist);

        holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongTitle()));
        //holder.name.setText(actualsong.getSongTitle());
        Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);

        holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistName())+" | Movie: "+FirstLetterUpperCase.convert(actualsong.getMovietitle()));
        //holder.lyricist.setText("Lyricist : " + actualsong.getLyricistNames());


      /*  holder.dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.dots);
            }
        });*/
    }





    @Override
    public int getItemCount() {
        return songlist.size();
    }

    public String FirstLetterUpperCase(String source) {
        source = source.toLowerCase();

        StringBuffer res = new StringBuffer();

        String[] strArr = source.split(" ");
        for (String str : strArr) {
            char[] stringArray = str.trim().toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            str = new String(stringArray);

            res.append(str).append(" ");
        }

        return res.toString().trim();
    }

    public void setFilter(List<songWithTitle> songlists){
       songlist = new ArrayList<>();
        songlist.addAll(songlists);
        notifyDataSetChanged();
    }
}
