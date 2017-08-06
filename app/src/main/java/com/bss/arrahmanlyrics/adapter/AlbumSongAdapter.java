package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.activites.albumSongList;
import com.bss.arrahmanlyrics.models.slideSong;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;

/**
 * Created by mohan on 5/20/17.
 */

public class AlbumSongAdapter extends RecyclerView.Adapter<AlbumSongAdapter.MyViewHolder> {
    private View.OnClickListener mClickListener;
    private Context mContext;
    private List<slideSong> songlist;
    QuickAction quickAction;
    albumSongList s;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, lyricist, movietitle, tracNo;
        ImageButton dots;
        LinearLayout layout;


        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.favSongtitle);
            lyricist = (TextView) view.findViewById(R.id.favSonglyricist);


            dots = (ImageButton) view.findViewById(R.id.menu_button);
            tracNo = (TextView) view.findViewById(R.id.trackNo);
            layout = (LinearLayout) view.findViewById(R.id.albumsongdetails);
            dots.setOnClickListener(this);
            layout.setOnClickListener(this);

            //albumCover = (ImageView) view.findViewById(R.id.album_artwork);
        }

        @Override
        public void onClick(View v) {
            final int id = v.getId();
            quickAction = new QuickAction(v.getContext(), QuickAction.VERTICAL);
            ActionItem play = new ActionItem(1, "Play", R.drawable.playicon);
            ActionItem playAll = new ActionItem(2, "Play All", R.drawable.playall);
            ActionItem queue = new ActionItem(3, "Add to Queue", R.drawable.queue);
            ActionItem fav = new ActionItem(4, "Add to Favorite", R.drawable.favadd);
            ActionItem favremove = new ActionItem(5, "Remove From Favorite", R.drawable.favremove);

            //add action items into QuickAction
            quickAction.addActionItem(play);
            quickAction.addActionItem(playAll);
            quickAction.addActionItem(queue);
            quickAction.addActionItem(fav);
            quickAction.addActionItem(favremove);


            quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionItem item) {
                    if (item.getActionId() == 1) {
                        s.play(getmodel(name.getText().toString()));
                    } else if (item.getActionId() == 2) {
                        s.playAll(getmodel(name.getText().toString()));
                    } else if (item.getActionId() == 3) {
                        s.addToQueue(getmodel(name.getText().toString()));
                    } else if (item.getActionId() == 4) {
                        //songWithTitle song = getmodel(movietitle.getText().toString(),name.getText().toString());
                        s.addToFavorite(getmodel(name.getText().toString()));
                    } else if (item.getActionId() == 5) {
                        s.removeFromFavorite(getmodel(name.getText().toString()));
                    }
                }
            });


            if (R.id.menu_button == id) {
                quickAction.show(v);

            }
            if (R.id.albumsongdetails== id) {
                s.play(getmodel(name.getText().toString()));
            }

        }
    }

    public AlbumSongAdapter(Context mContext, List<slideSong> songlist,albumSongList s) {
        this.mContext = mContext;
        this.songlist = songlist;
        this.s = s;


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_song_list_view, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        return (MyViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        slideSong actualsong = songlist.get(position);




        holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongName()));
        //holder.name.setText(actualsong.getSongTitle());


        holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistNames()));
        holder.tracNo.setText((actualsong.getTrackNo()));
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
    public slideSong getmodel(String songTitle){

        for(slideSong songwith:songlist){

            if(songwith.getSongName().equalsIgnoreCase(songTitle)){

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

    public void setFilter(List<slideSong> songlists){
       songlist = new ArrayList<>();
        songlist.addAll(songlists);
        notifyDataSetChanged();
    }
}
