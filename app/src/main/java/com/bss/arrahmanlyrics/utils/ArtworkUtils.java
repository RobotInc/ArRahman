package com.bss.arrahmanlyrics.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.utils.bitmap;


import java.io.File;



/*
 * Created by Coolalien on 6/28/2016.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ArtworkUtils {


    private static ArtworkUtils sInstance;
    private Context mcontext;

    public ArtworkUtils(Context context) {
        this.mcontext = context;
    }

    public static void init(Context context) {
        sInstance = new ArtworkUtils(context);
    }

    public static ArtworkUtils getInstance() {
        return sInstance;
    }




    public static AsyncTask<String, Void, String> getBlurArtwork(Context context, int radius, Bitmap bitmap, ImageView imageView, float scale) {
        BlurArtwork blurArtwork = new BlurArtwork(context, radius, bitmap, imageView, scale);
        return blurArtwork.execute("Executed");
    }

    public static void blurPreferances(Context context, Bitmap blurBitmap, ImageView imageView) {

        switch ("0") {
            case "0":
                getBlurArtwork(context, radius(), blurBitmap, imageView, 1.0f);
                break;
            case "1":
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.8f);
                break;
            case "2":
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.6f);
                break;
            case "3":
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.4f);
                break;
            case "4":
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.2f);
                break;
            default:
                getBlurArtwork(context, 25, blurBitmap, imageView, 0.2f);
                break;
        }
    }


    public static int radius() {
        int radius = 1;
        String blurView = "0";
        switch (blurView) {
            case "0":
                radius = 5;
                return radius;
            case "1":
                radius = 10;
                return radius;
            case "2":
                radius = 15;
                return radius;
            case "3":
                radius = 20;
                return radius;
            case "4":
                radius = 25;
                return radius;
        }
        return radius;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

}

