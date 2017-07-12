package com.bss.arrahmanlyrics.models;

/**
 * Created by mohan on 6/28/17.
 */

public class songUlr {
	String songTitle;
	String url;

	public songUlr(String songTitle, String url) {
		this.songTitle = songTitle;
		this.url = url;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
