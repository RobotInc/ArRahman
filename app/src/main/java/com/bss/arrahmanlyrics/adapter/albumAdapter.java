package com.bss.arrahmanlyrics.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.Album;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohan on 6/15/17.
 */

public class albumAdapter extends RecyclerView.Adapter<albumAdapter.MyViewHolder> {
    private List<Album> albumList;
    private Context Context;

    public albumAdapter(Context Context, List<Album> albumList) {
        this.Context = Context;
        this.albumList = albumList;
    }

    @Override
    public albumAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_grid_view, parent, false);
        RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        return (MyViewHolder) holder;
    }

    @Override
    public void onBindViewHolder(final albumAdapter.MyViewHolder holder, int position) {
        Album album = albumList.get(position);

//        final Typeface font = Typeface.createFromAsset(Context.getAssets(), "Timber.ttf");
      //  holder.title.setTypeface(font);
        holder.title.setText(FirstLetterUpperCase.convert(album.getName()));
        holder.count.setText(album.getNumOfSongs() + " songs");
        Glide.with(Context).load(album.getThumbnail()).into(holder.thumbnail);
        try {
            Palette.from(album.getImage()).maximumColorCount(16).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    // Get the "vibrant" color swatch based on the bitmap

                   /* if (palette.getVibrantSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getVibrantSwatch().getRgb());
                        holder.title.setTextColor(palette.getVibrantSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getVibrantSwatch().getBodyTextColor());

                    }else if (palette.getLightVibrantSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getLightVibrantSwatch().getRgb());
                        holder.title.setTextColor(palette.getLightVibrantSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getLightVibrantSwatch().getBodyTextColor());
                        //holder.count.setTextColor(palette.getMutedSwatch().getBodyTextColor());

                    }else*/
                   if (palette.getDarkVibrantSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
                        holder.title.setTextColor(palette.getDarkVibrantSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getDarkVibrantSwatch().getBodyTextColor());

                    }else if (palette.getLightMutedSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getLightMutedSwatch().getRgb());
                        holder.title.setTextColor(palette.getLightMutedSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getLightMutedSwatch().getBodyTextColor());
                        //holder.count.setTextColor(palette.getMutedSwatch().getBodyTextColor());
                    }  else if (palette.getDarkMutedSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getDarkMutedSwatch().getRgb());
                        holder.title.setTextColor(palette.getDarkMutedSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getDarkMutedSwatch().getBodyTextColor());

                    }  else if (palette.getDominantSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getDominantSwatch().getRgb());
                        holder.title.setTextColor(palette.getDominantSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getDominantSwatch().getBodyTextColor());

                    } else if (palette.getMutedSwatch() != null) {
                        holder.backgroundColor.setBackgroundColor(palette.getMutedSwatch().getRgb());
                        holder.title.setTextColor(palette.getMutedSwatch().getTitleTextColor());
                        holder.count.setTextColor(palette.getMutedSwatch().getBodyTextColor());

                    }


                }
            });
        } catch (Exception e) {
            Log.i("exception:", album.getName());
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;
        public RelativeLayout textLayout;
        public LinearLayout backgroundColor;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.Title);
            count = (TextView) view.findViewById(R.id.TotalSongs);
            thumbnail = (ImageView) view.findViewById(R.id.album_artwork);
            backgroundColor = (LinearLayout) view.findViewById(R.id.backgroundColor);

        }
    }

    public void setFilter(List<Album> albumLists){
	   albumList = new ArrayList<>();
	    albumList.addAll(albumLists);
	    notifyDataSetChanged();
    }
}
