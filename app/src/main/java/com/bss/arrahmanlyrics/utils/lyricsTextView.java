package com.bss.arrahmanlyrics.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mohan on 6/29/17.
 */


public class lyricsTextView extends TextView {

	public lyricsTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public lyricsTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public lyricsTextView(Context context) {
		super(context);
		init();
	}

	public void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "english.ttf");
		setTypeface(tf, 1);

	}
}

