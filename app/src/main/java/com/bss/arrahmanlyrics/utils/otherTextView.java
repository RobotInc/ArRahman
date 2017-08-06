package com.bss.arrahmanlyrics.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;


/**
 * Created by mohan on 6/29/17.
 */


public class otherTextView extends android.support.v7.widget.AppCompatTextView {

	public otherTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public otherTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public otherTextView(Context context) {
		super(context);
		init();
	}

	public void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Catamaran.ttf");
		setTypeface(tf, 1);

	}
}

