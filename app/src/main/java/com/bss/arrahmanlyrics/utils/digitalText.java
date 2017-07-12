package com.bss.arrahmanlyrics.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mohan on 6/29/17.
 */


public class digitalText extends android.support.v7.widget.AppCompatTextView {

	public digitalText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public digitalText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public digitalText(Context context) {
		super(context);
		init();
	}

	public void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "digital.ttf");
		setTypeface(tf, 1);

	}
}

