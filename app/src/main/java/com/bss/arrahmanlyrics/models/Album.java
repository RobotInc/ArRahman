package com.bss.arrahmanlyrics.models;

import android.graphics.Bitmap;

/**
 * Created by mohan on 6/15/17.
 */


/**
 * Created by mohan on 5/15/17.
 */
public class Album {
    private String name;
    private int numOfSongs;
    private byte[] thumbnail;
    private Bitmap image;


    public Album() {
    }

    public Album(String name, int numOfSongs, byte[] thumbnail, Bitmap image) {
        this.name = name;
        this.numOfSongs = numOfSongs;
        this.thumbnail = thumbnail;
        this.image = image;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumOfSongs() {
        return numOfSongs;
    }

    public void setNumOfSongs(int numOfSongs) {
        this.numOfSongs = numOfSongs;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}


