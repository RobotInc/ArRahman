package com.bss.arrahmanlyrics.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.mainApp;

import java.io.ByteArrayOutputStream;

/**
 * Created by mohan on 6/18/17.
 */

public class image {


    private static byte[] getImage(Context context, String imageString) {
        if (imageString.equals(null)) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitMapData = stream.toByteArray();
            return bitMapData;

        }
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        //Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedString;
    }
    public static Bitmap getBitmap(Context context, String imageString) {
        if (imageString.equals(null)) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            return bitmap;
        }
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmap;
    }
}
