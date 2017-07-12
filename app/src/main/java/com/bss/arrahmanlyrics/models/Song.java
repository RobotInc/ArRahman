package com.bss.arrahmanlyrics.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by mohan on 5/20/17.
 */

public class Song implements Parcelable{
    String MovieTitle;
    String songTitle;
    String lyricistNames;
    String ulr;


    public Song(String MovieTitle, String songTitle,String lyricistNames, String ulr) {
        this.MovieTitle = MovieTitle;
        this.songTitle = songTitle;
        this.lyricistNames = lyricistNames;
        this.ulr = ulr;
    }

    protected Song(Parcel in) {
        MovieTitle = in.readString();
        songTitle = in.readString();
        lyricistNames = in.readString();
        ulr = in.readString();
    }


    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getLyricistNames() {
        return lyricistNames;
    }

    public void setLyricistNames(String lyricistNames) {
        this.lyricistNames = lyricistNames;
    }

    public String getUlr() {
        return ulr;
    }

    public void setUlr(String ulr) {
        this.ulr = ulr;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MovieTitle);
        dest.writeString(songTitle);
        dest.writeString(lyricistNames);
        dest.writeString(ulr);
    }

    public String getMovieTitle() {
        return MovieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        MovieTitle = movieTitle;
    }

    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Movie",MovieTitle);
        result.put("Song",songTitle);
        result.put("Lyricist", lyricistNames);
        result.put("URL", ulr);


        return result;
    }
}
