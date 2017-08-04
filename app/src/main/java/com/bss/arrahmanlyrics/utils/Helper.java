package com.bss.arrahmanlyrics.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.mainApp;

/**
 * Created by mohan on 7/18/17.
 */

public class Helper {

	public static String durationCalculator(long id) {
		String finalTimerString = "";
		String secondsString = "";
		String mp3Minutes = "";
		// Convert total duration into time

		int minutes = (int) (id % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((id % (1000 * 60 * 60)) % (1000 * 60) / 1000);

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}
		if (minutes < 10) {
			mp3Minutes = "0" + minutes;
		} else {
			mp3Minutes = "" + minutes;
		}
		finalTimerString = finalTimerString + mp3Minutes + ":" + secondsString;
		// return timer string
		return finalTimerString;
	}
	public static Bitmap getBitmap(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(mainApp.getContext().getResources(), R.mipmap.ic_launcher);
			return bitmap;
		}
		byte[] image = Base64.decode(imageString, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		return bitmap;
	}
}
