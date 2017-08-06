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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.favorites;
import com.bss.arrahmanlyrics.R;
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

public class favoriteFragmentSongAdapter extends RecyclerView.Adapter<favoriteFragmentSongAdapter.MyViewHolder> {
    private View.OnClickListener mClickListener;
    private Context mContext;
    private QuickAction quickAction;
    private List<songWithTitle> songlist;
    favorites s;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, lyricist,movietitle;
        ImageButton dots;
        CircularImageView imageView;
        LinearLayout layout;



        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.favSongtitle);
            lyricist = (TextView) view.findViewById(R.id.favSonglyricist);
            dots = (ImageButton) view.findViewById(R.id.menu_button);
            movietitle= (TextView) view.findViewById(R.id.favMovietitle);
            layout = (LinearLayout) view.findViewById(R.id.songdetails);

           // dots = (ImageButton) view.findViewById(R.id.favmenu_button);
            imageView = (CircularImageView) view.findViewById(R.id.favsongCover);
            dots.setOnClickListener(this);
            layout.setOnClickListener(this);

            //albumCover = (ImageView) view.findViewById(R.id.album_artwork);
        }

        @Override
        public void onClick(View v) {
            final int id = v.getId();
            quickAction = new QuickAction(v.getContext(), QuickAction.VERTICAL);
            ActionItem play= new ActionItem(1,"Play",R.drawable.playicon);
            ActionItem playAll= new ActionItem(2,"Play All",R.drawable.playall);
            ActionItem queue= new ActionItem(3,"Add to Queue",R.drawable.queue);

            ActionItem favremove= new ActionItem(5,"Remove From Favorite",R.drawable.favremove);

            //add action items into QuickAction
            quickAction.addActionItem(play);
            quickAction.addActionItem(playAll);
            quickAction.addActionItem(queue);

            quickAction.addActionItem(favremove);


            quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionItem item) {
                    if(item.getActionId()==1){
                        s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }else if(item.getActionId()==2){
                        s.playAll(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }else if(item.getActionId()==3){
                        s.addToQueue(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }else if(item.getActionId()==5){
                        s.removeFromFavorite(getmodel(movietitle.getText().toString(),name.getText().toString()));
                    }
                }
            });


            if(R.id.menu_button == id){
                quickAction.show(v);

            }
            if(R.id.songdetails== id){
                s.play(getmodel(movietitle.getText().toString(),name.getText().toString()));
            }

        }
    }

    public favoriteFragmentSongAdapter(Context mContext, List<songWithTitle> songlist,favorites s) {
        this.mContext = mContext;
        this.songlist = songlist;
        this.s = s;


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




        holder.name.setText(FirstLetterUpperCase.convert(actualsong.getSongTitle()));
        //holder.name.setText(actualsong.getSongTitle());
        Glide.with(mContext).load(actualsong.getImages()).into(holder.imageView);

        holder.lyricist.setText(FirstLetterUpperCase.convert("Lyricist: " + actualsong.getLyricistName()));
        holder.movietitle.setText(FirstLetterUpperCase.convert("Movie: " + actualsong.getMovietitle()));
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

    public void setFilter(List<songWithTitle> songlists){
       songlist = new ArrayList<>();
        songlist.addAll(songlists);
        notifyDataSetChanged();
    }
}
