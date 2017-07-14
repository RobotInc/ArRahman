package com.bss.arrahmanlyrics.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.activites.MainActivity;
import com.bss.arrahmanlyrics.activites.lyricsActivity;
import com.bss.arrahmanlyrics.adapter.favoriteFragmentSongAdapter;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.CustomLayoutManager;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link favorites.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link favorites#newInstance} factory method to
 * create an instance of this fragment.
 */
public class favorites extends Fragment implements SearchView.OnQueryTextListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;
	FastScrollRecyclerView songlistView;
	List<songWithTitle> songlist;

	List<songWithTitle> filtered;
	ArrayList<Song> favoriteList;
	HashMap<String, ArrayList<String>> favorites;
	HashMap<String, Object> values;
	DatabaseReference songref;
	favoriteFragmentSongAdapter adapter;
	ProgressDialog dialog;
	SearchView searchView;

	View view;

	public favorites() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment favorites.
	 */
	// TODO: Rename and change types and number of parameters
	public static favorites newInstance(String param1, String param2) {
		favorites fragment = new favorites();
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

		setHasOptionsMenu(true);



	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_favorites, container, false);

		songlist = new ArrayList<>();

		filtered = new ArrayList<>();
		values = ((MainActivity)getActivity()).getValues();
		favorites = new HashMap<>();
		favoriteList = new ArrayList<>();
		adapter = new favoriteFragmentSongAdapter(getContext(), songlist);
		songlistView = (FastScrollRecyclerView) view.findViewById(R.id.favoriteSongPlayList);
		songlistView.setAdapter(adapter);
		final CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
		customLayoutManager.setSmoothScrollbarEnabled(true);
		songlistView.setLayoutManager(customLayoutManager);

		songlistView.addItemDecoration(new DividerItemDecoration(getContext(), 75, true));
		songlistView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				songWithTitle song = filtered.get(position);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("list", favoriteList);
				bundle.putString("selectedSong", song.getSongTitle());
				bundle.putString("Title", song.getMovietitle());
				//bundle.putString("trackNo",song.getTrackNo());
				Toast.makeText(getContext(), song.getMovietitle(), Toast.LENGTH_SHORT).show();
				Intent i = new Intent(getContext(), lyricsActivity.class);


				i.putExtras(bundle);
				startActivity(i);


			}
		}));
		DatabaseReference userRef = FirebaseDatabase.getInstance().getReference();
		userRef.keepSynced(true);
		userRef.child(((MainActivity)getActivity()).user.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if(dataSnapshot.exists()){

					HashMap<String,Object> fav = (HashMap<String, Object>) dataSnapshot.getValue();
					HashMap<String,Object> movies = (HashMap<String, Object>) fav.get("Fav Songs");
					ArrayList<String> Favsonglist = new ArrayList<String>();
					favorites.clear();
					if (movies != null) {
						for (String movie : movies.keySet()) {

							HashMap<String, Object> songs = (HashMap<String, Object>) movies.get(movie);

							Favsonglist.clear();
							for (String song : songs.keySet()) {

								Favsonglist.add(song);
							}

							favorites.put(movie, (ArrayList<String>) Favsonglist.clone());
						}

						mainApp.getSp().setFavorites(favorites);
						songlist.clear();
						favoriteList.clear();
						filtered.clear();

						prepareSongList();

					}
				}else {
					songlist.clear();
					favoriteList.clear();
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


		adapter.notifyDataSetChanged();
		return view;

	}



	private void prepareSongList() {
		if(favoriteList !=null){
			favoriteList.clear();
		}


		if (favorites != null) {
			for (String movies : favorites.keySet()) {

				HashMap<String, Object> movieMap = (HashMap<String, Object>) values.get(movies);
				ArrayList<String> favoriteSongs = favorites.get(movies);


				for (String song : favoriteSongs) {
					HashMap<String, Object> songMap = (HashMap<String, Object>) movieMap.get(song);

					Song songModel = new Song(movies, song, String.valueOf(songMap.get("Lyricist")), String.valueOf(songMap.get("Download")));
					favoriteList.add(songModel);
				}
			}
		}
		songlist.clear();
		if (favoriteList != null) {
			for (Song song : favoriteList) {
				String moive = song.getMovieTitle();
				String title = song.getSongTitle();
				HashMap<String, Object> movieMap = (HashMap<String, Object>) values.get(moive);
				HashMap<String, Object> songMap = (HashMap<String, Object>) movieMap.get(title);
				songWithTitle songwith = new songWithTitle(moive, title, String.valueOf(songMap.get("Lyricist")), getImage(String.valueOf(movieMap.get("IMAGE"))), song.getUlr());
				songlist.add(songwith);

			}
		} else {
			Log.e("fav null", String.valueOf(favoriteList));
		}

		filtered = songlist;
		adapter.notifyDataSetChanged();
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
		filtered = new ArrayList<>();
		filtered = filterAlbum(songlist, newText);
		if (TextUtils.isEmpty(newText)) {
			adapter.setFilter(songlist);
		} else {
			adapter.setFilter(filtered);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.favoritesongmenu, menu);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.fav_song_search));
		searchView.setOnQueryTextListener(this);
		searchView.setQueryHint("Search songs");

	}

	public List<songWithTitle> filterAlbum(List<songWithTitle> listsongs, String query) {
		query = query.toLowerCase();
		final List<songWithTitle> filteralbumlist = new ArrayList<>();
		for (songWithTitle songs : listsongs) {
			final String text1 = songs.getSongTitle().toLowerCase();
			final String text2 = songs.getMovietitle().toLowerCase();
			final String text3 = songs.getLyricistName().toLowerCase();
			if (text1.contains(query) || text2.contains(query) || text3.contains(query)) {
				filteralbumlist.add(songs);
			}
		}
		return filteralbumlist;
	}

	@Override
	public void onPause() {
		super.onPause();
		//dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();



	}
	public void Toast(String message){
		Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			/*case R.id.feedback:
				Intent intent = new Intent(getContext(), feedback.class);
				startActivity(intent);
				return true;*/
			case R.id.about:
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle(R.string.title);
				builder.setMessage(R.string.description);
				builder.setPositiveButton(R.string.ok, null);
				builder.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}
}
