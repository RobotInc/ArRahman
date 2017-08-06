package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.songList;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.slideSong;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bss.arrahmanlyrics.utils.GifImageView;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import es.claucookie.miniequalizerlibrary.EqualizerView;
import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;

/**
 * Created by mohan on 5/20/17.
 */

public class fragmentSongAdapter extends RecyclerView.Adapter<fragmentSongAdapter.MyViewHolder> {
    private View.OnClickListener mClickListener;
    private Context mContext;
    private List<songWithTitle> songlist;
    QuickAction quickAction;
    songList s;



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, lyricist, movietitle;

        ImageButton dots;
        LinearLayout layout;

        CircularImageView imageView;


        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.Songtitle);
            lyricist = (TextView) view.findViewById(R.id.Songlyricist);
            movietitle = (TextView) view.findViewById(R.id.MovieTitle);
            dots = (ImageButton) view.findViewById(R.id.menu_button);
            layout = (LinearLayout) view.findViewById(R.id.playlistlayout);

            imageView = (CircularImageView) view.findViewById(R.id.songCover);
            dots.setOnClickListener(this);
            layout.setOnClickListener(this);

            //albumCover = (ImageView) view.findViewById(R.id.album_artwork);
        }


        @Override
        public void onClick(View v) {
            final int id = v.getId();
            quickAction = new QuickAction(v.getContext(), QuickAction.VERTICAL);
            ActionItem play= new ActionItem(1,"Play",R.drawable.playicon);

            ActionItem queue= new ActionItem(3,"Remove from Queue",R.drawable.remove);


            //add action items into QuickAction
            quickAction.addActionItem(play);

            quickAction.addActionItem(queue);



            quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionItem item) {
                    if(item.getActionId()==1){

                        s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }else if(item.getActionId()==3){
                        s.removeFromQueue(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }
                }
            });


            if(R.id.menu_button == id){
                quickAction.show(v);

            }
            if(R.id.playlistlayout == id){
                s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
            }

        }
    }

    public fragmentSongAdapter(Context mContext, List<songWithTitle> songlist,songList s) {
        this.mContext = mContext;
        this.songlist = songlist;
        this.s = s;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_list_view, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        return (MyViewHolder) holder;
    }

    public void onBindViewHolder(final MyViewHolder holder, int position) {
        songWithTitle actualsong = songlist.get(position);

        final Typeface title = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
        final Typeface lyricist = Typeface.createFromAsset(mContext.getAssets(), "MavenPro.ttf");
        holder.name.setTypeface(title);
        holder.lyricist.setTypeface(lyricist);

        holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongTitle()));
        //holder.name.setText(actualsong.getSongTitle());
        Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);

        holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistName()));
        holder.movietitle.setText(FirstLetterUpperCase.convert("Movie: " + actualsong.getMovietitle()));
       // holder.eq.setVisibility(View.INVISIBLE);

       // holder.eq.animateBars();

        //holder.lyricist.setText("Lyricist : " + actualsong.getLyricistNames());


       /* holder.dots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.dots);
            }
        });*/
    }
    public void showPopupMenu (View view)
    {
        PopupMenu menu = new PopupMenu (mContext, view);

        menu.inflate (R.menu.songlistmenu);
        menu.show();
    }


    @Override
    public int getItemCount() {
        return songlist.size();
    }
    public songWithTitle getmodel(String movie,String songTitle){
        movie = movie.replaceAll("Movie: ","");
        for(songWithTitle songwith:songlist){

            if(songwith.getMovietitle().trim().equalsIgnoreCase(movie)&&songwith.getSongTitle().trim().equalsIgnoreCase(songTitle)){

                return songwith;
            }
        }

        return null;
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

    public void setFilter(List<songWithTitle> songlists) {
        songlist = new ArrayList<>();
        songlist.addAll(songlists);
        notifyDataSetChanged();
    }


}