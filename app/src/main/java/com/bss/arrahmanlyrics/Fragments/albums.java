package com.bss.arrahmanlyrics.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.activites.MainActivity;
import com.bss.arrahmanlyrics.activites.albumSongList;
import com.bss.arrahmanlyrics.activites.lyricsActivity;
import com.bss.arrahmanlyrics.adapter.albumAdapter;
import com.bss.arrahmanlyrics.models.Album;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.firebase.database.DatabaseReference;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link albums.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link albums#newInstance} factory method to
 * create an instance of this fragment.
 */
public class albums extends Fragment implements SearchView.OnQueryTextListener{
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private albumAdapter albumListAdapter;
	private FastScrollRecyclerView rv;
	private OnFragmentInteractionListener mListener;
	ProgressDialog dialog;

	DatabaseReference albumData;
	HashMap<String, Object> values;
	private List<Album> albumList;
	private List<Album> filtered;



	//private List<Album> filtedList;
	private ArrayList<Song> songWithTitleList;
	SearchView searchView;
	Toolbar toolbar;
	View rootView;


	public albums() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment albums.
	 */
	// TODO: Rename and change types and number of parameters
	public static albums newInstance(String param1, String param2) {
		albums fragment = new albums();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
		toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
		setHasOptionsMenu(true);

		albumList = new ArrayList<>();
		songWithTitleList = new ArrayList<>();
		//filtedList = new ArrayList<>();
		albumListAdapter = new albumAdapter(getContext(), albumList);
		rv = (FastScrollRecyclerView) rootView.findViewById(R.id.albumrv);
		rv.setAdapter(albumListAdapter);

		rv.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Album selectedAlbum = filtered.get(position);
				Bundle songs = new Bundle();
				songs.putParcelableArrayList("list", getFirstSongName(selectedAlbum.getName()));
				//songs.putSerializable("map",(HashMap<String,Object>)values.get(selectedAlbum.getName()));
				songs.putString("Title", selectedAlbum.getName());
				songs.putString("selectedSong","");

				//songs.putByteArray("image",albumList.get(position).getThumbnail());

				try {
					Intent intent = new Intent(getContext(), albumSongList.class);
					intent.putExtras(songs);
					startActivity(intent);
				} catch (Exception e) {
					Log.i("Exception", e.getMessage());
				}

			}
		}));
		loadGridView(2);
		values = ((MainActivity)getActivity()).getValues();
		prepareAlbums();
		/*
		albumData = FirebaseDatabase.getInstance().getReference();
		albumData.child("AR Rahman").child("Tamil").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();

				prepareAlbums();
				dialog.hide();


			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
*/

		//prepareAlbums();

		return rootView;
	}

   /* private void setBackgrounds() {
        View mainwindow = getActivity().findViewById(android.R.id.content).getRootView();
        mainwindow.setDrawingCacheEnabled(true);

        Bitmap image = mainwindow.getDrawingCache();
        Bitmap croppedBitmap = Bitmap.createBitmap(image, 0, (int) (image.getHeight() * 0.8), image.getWidth(), 40);
        try {
            Palette.from(croppedBitmap).maximumColorCount(1600000).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    // Get the "vibrant" color swatch based on the bitmap

                    if (palette.getDarkVibrantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
                    } else if (palette.getDominantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDominantSwatch().getRgb());
                    } else if (palette.getDarkMutedSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDarkMutedSwatch().getRgb());
                    } else if (palette.getVibrantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getVibrantSwatch().getRgb());

                    } else if (palette.getLightVibrantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getLightVibrantSwatch().getRgb());   //holder.count.setTextColor(palette.getMutedSwatch().getBodyTextColor());

                    } else if (palette.getLightMutedSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getLightMutedSwatch().getRgb());//holder.count.setTextColor(palette.getMutedSwatch().getBodyTextColor());
                    } else if (palette.getMutedSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getMutedSwatch().getRgb());
                    }


                }
            });


        } catch (Exception e) {

        }
    }*/

	private void prepareAlbums() {
		albumList.clear();
		List<Album> list = new ArrayList<>();

		SortedSet<String> trackNos = new TreeSet<>();
		for (String album : values.keySet()) {
			list.add(new Album(album, ((HashMap<String, Object>) values.get(album)).size() - 1,
					getImage(String.valueOf(((HashMap<String, Object>) values.get(album)).get("IMAGE"))), getBitmap(String.valueOf(((HashMap<String, Object>) values.get(album)).get("IMAGE")))));
			trackNos.add(album);
		}

		for(String tracks : trackNos){
			for(Album album : list){
				if(album.getName().equals(tracks)){
					albumList.add(album);
				}
			}
		}
		filtered = albumList;
		Log.i("albumFilter",String.valueOf(albumList.size()));
		albumListAdapter.notifyDataSetChanged();


	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}


	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		//Log.i("size",String.valueOf(filtedList.size()));
		filtered = new ArrayList<>();
		filtered = filterAlbum(albumList,newText);
		if(TextUtils.isEmpty(newText)){
			albumListAdapter.setFilter(albumList);
		}else {
			albumListAdapter.setFilter(filtered);
		}

		return true;
	}



	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.albummenu, menu);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.album_search));
		searchView.setOnQueryTextListener(this);
		searchView.setQueryHint("Search album");

		}

	private byte[] getImage(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] bitMapData = stream.toByteArray();
			return bitMapData;

		}
		byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
		//Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedString;
	}

	@Override
	public void onPause() {
		super.onPause();


	}

	public Bitmap getBitmap(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			return bitmap;
		}
		byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return bitmap;
	}

	private void loadGridView(int columns) {

		GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns);
		rv.setLayoutManager(layoutManager);

	}

	private ArrayList<Song> getFirstSongName(String Movie) {
		songWithTitleList.clear();
		HashMap<String, Object> movie = (HashMap<String, Object>) values.get(Movie);
		for (String song : movie.keySet()) {


			if (!song.equals("IMAGE")) {
				Log.e("onesong", String.valueOf(song));
				HashMap<String, Object> oneSong = (HashMap<String, Object>) movie.get(song);
				Song songs = new Song(Movie, song, String.valueOf(oneSong.get("Lyricist")),String.valueOf(oneSong.get("Download")));
				songWithTitleList.add(songs);
			}
		}
		return songWithTitleList;
	}

	public List<Album> filterAlbum(List<Album> listAlbums, String query) {
		query = query.toLowerCase();
		final List<Album> filteralbumlist = new ArrayList<>();
		for (Album album : listAlbums) {
			final String text = album.getName().toLowerCase();
			if (text.contains(query)) {
				filteralbumlist.add(album);
			}
		}
		return filteralbumlist;
	}

	@Override
	public void onResume() {
		super.onResume();

	}




}