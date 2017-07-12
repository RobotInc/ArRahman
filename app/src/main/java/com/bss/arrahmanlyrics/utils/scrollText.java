package com.bss.arrahmanlyrics.utils;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mohan on 7/13/17.
 */

public class scrollText extends android.support.v7.widget.AppCompatTextView {
	public scrollText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public scrollText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	public scrollText(Context context) {
		super(context);
	}
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		if(focused)
			super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	public void onWindowFocusChanged(boolean focused) {
		if(focused)
			super.onWindowFocusChanged(focused);
	}


	@Override
	public boolean isFocused() {
		return true;
	}
	public void init() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "english.ttf");
		setTypeface(tf, 1);

	}
}
