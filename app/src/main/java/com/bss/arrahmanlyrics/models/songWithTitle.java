package com.bss.arrahmanlyrics.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mohan on 6/30/17.
 */

public class songWithTitle implements Parcelable {
	String Movietitle;
	String songTitle;
	String lyricistName;
	byte[] images;
	String ulr;

	public songWithTitle(String movietitle, String songTitle, String lyricistName, byte[] images,String ulr) {
		Movietitle = movietitle;
		this.songTitle = songTitle;
		this.lyricistName = lyricistName;
		this.images = images;
		this.ulr = ulr;
	}

	protected songWithTitle(Parcel in) {
		Movietitle = in.readString();
		songTitle = in.readString();
		lyricistName = in.readString();
		images = in.createByteArray();
		ulr = in.readString();
	}

	public static final Creator<songWithTitle> CREATOR = new Creator<songWithTitle>() {
		@Override
		public songWithTitle createFromParcel(Parcel in) {
			return new songWithTitle(in);
		}

		@Override
		public songWithTitle[] newArray(int size) {
			return new songWithTitle[size];
		}
	};

	public String getUlr() {
		return ulr;
	}

	public void setUlr(String ulr) {
		this.ulr = ulr;
	}

	public String getMovietitle() {
		return Movietitle;
	}

	public void setMovietitle(String movietitle) {
		Movietitle = movietitle;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getLyricistName() {
		return lyricistName;
	}

	public void setLyricistName(String lyricistName) {
		this.lyricistName = lyricistName;
	}

	public byte[] getImages() {
		return images;
	}

	public void setImages(byte[] images) {
		this.images = images;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(Movietitle);
		dest.writeString(songTitle);
		dest.writeString(lyricistName);
		dest.writeByteArray(images);
		dest.writeString(ulr);
	}
}
