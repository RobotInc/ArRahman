package com.bss.arrahmanlyrics.utils;

/**
 * Created by mohan on 7/5/17.
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.favorites;
import com.bss.arrahmanlyrics.mainApp;

import com.bss.arrahmanlyrics.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class favoritesReadWrite {
	FirebaseUser user;
	DatabaseReference userRef;
	HashMap<String, Object> movies;
	HashMap<String, ArrayList<String>> Favorites;
	ArrayList<String> songlist;


	public static final String PREFS_NAME = "PRODUCT_APP";
	public static final String FAVORITES = "Product_Favorite";

	public favoritesReadWrite() {
		super();


	}

	/* This four methods are used for maintaining favorites.
	public void saveFavorites(List<Song> favorites) {
		/*SharedPreferences settings;
		Editor editor;

		settings = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		editor = settings.edit();

		Gson gson = new Gson();
		String jsonFavorites = gson.toJson(favorites);

		editor.putString(FAVORITES, jsonFavorites);

		editor.commit();
		HashMap<String, Object> list = new HashMap<>();
		list.put("Fav Songs", favorites);
		userRef.child(user.getUid()).updateChildren(list);


	}*/

	public void addFavorite(String movieTitle, String songTitle, FirebaseUser user) {
		if (Favorites != null) {
			if (Favorites.containsKey(movieTitle)) {
				if (Favorites.get(movieTitle).contains(songTitle)) {
					return;
				}
			}
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put(songTitle, songTitle);
		DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
		userRef.child(user.getUid()).child("Fav Songs").child(movieTitle).updateChildren(map);

	}


	public void removeFavorite(String movieTitle, String songTitle, FirebaseUser user) {
		Log.e("removeFavorite", songTitle + " " + movieTitle);
		if (Favorites != null) {
			if (Favorites.containsKey(movieTitle)) {
				if (Favorites.get(movieTitle).contains(songTitle)) {
					HashMap<String, Object> map = new HashMap<>();
					map.put(songTitle, songTitle);
					DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
					userRef.child(user.getUid()).child("Fav Songs").child(movieTitle).child(songTitle).removeValue(new DatabaseReference.CompletionListener() {
						@Override
						public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

						}
					});
				}
			}
		}

	}

	public HashMap<String, ArrayList<String>> getFavorites() {
	/*	SharedPreferences settings;
		List<Song> favorites;

		settings = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);

		if (settings.contains(FAVORITES)) {
			String jsonFavorites = settings.getString(FAVORITES, null);
			Gson gson = new Gson();
			Song[] favoriteItems = gson.fromJson(jsonFavorites,
					Song[].class);

			favorites = Arrays.asList(favoriteItems);
			favorites = new ArrayList<Song>(favorites);
		} else
			return null;

		return (ArrayList<Song>) favorites;*/
		return Favorites;
	}

	public void setFavorites(HashMap<String, ArrayList<String>> Favorites) {

		this.Favorites = Favorites;


	}


}