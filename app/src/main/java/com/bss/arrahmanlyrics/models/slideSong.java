package com.bss.arrahmanlyrics.models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.mainApp;

/**
 * Created by mohan on 5/20/17.
 */

public class slideSong {
    String songName;
    String trackNo;
    String lyricistNames;
    byte[] bitmap;


    public slideSong(String songName, String trackNo, String lyricistNames, byte[] bitmap) {
        this.songName = songName;
        this.trackNo = trackNo;
        this.lyricistNames = lyricistNames;
        this.bitmap = bitmap;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(String trackNo) {
        this.trackNo = trackNo;
    }

    public String getLyricistNames() {
        return lyricistNames;
    }

    public void setLyricistNames(String lyricistNames) {
        this.lyricistNames = lyricistNames;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }


}
