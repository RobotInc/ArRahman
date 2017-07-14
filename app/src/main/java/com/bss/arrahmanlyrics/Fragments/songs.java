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
import com.bss.arrahmanlyrics.adapter.mainFragmentSongAdapter;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.CustomLayoutManager;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
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
 * {@link songs.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link songs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class songs extends Fragment implements SearchView.OnQueryTextListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	FastScrollRecyclerView songlistView;
	List<songWithTitle> songlist;
	List<songWithTitle> filtered;
	ArrayList<Song> passedList;
	HashMap<String, Object> values;
	DatabaseReference songref;
	mainFragmentSongAdapter adapter;
	ProgressDialog dialog;
	SearchView searchView;

	private OnFragmentInteractionListener mListener;

	public songs() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment songs.
	 */
	// TODO: Rename and change types and number of parameters
	public static songs newInstance(String param1, String param2) {
		songs fragment = new songs();
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
		//return inflater.inflate(R.layout.fragment_songs, container, false);
		View rootView = inflater.inflate(R.layout.fragment_songs, container, false);
		setHasOptionsMenu(true);

		songlist = new ArrayList<>();
		passedList = new ArrayList<>();
		adapter = new mainFragmentSongAdapter(getContext(), songlist);
		songlistView = (FastScrollRecyclerView) rootView.findViewById(R.id.songPlayList);
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
				bundle.putParcelableArrayList("list", passedList);
				bundle.putString("selectedSong", song.getSongTitle());
				bundle.putString("Title", song.getMovietitle());
				//bundle.putString("trackNo",song.getTrackNo());
				Toast.makeText(getContext(), song.getMovietitle(), Toast.LENGTH_SHORT).show();
				Intent i = new Intent(getContext(), lyricsActivity.class);


				i.putExtras(bundle);
				startActivity(i);


			}
		}));
		values = ((MainActivity)getActivity()).getValues();
		prepareSongList();

		/*songref = FirebaseDatabase.getInstance().getReference();

		songref.child("AR Rahman").child("Tamil").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();

				prepareSongList();
				adapter.notifyDataSetChanged();
				dialog.hide();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});*/

		return rootView;
	}

	private void prepareSongList() {
		songlist.clear();
		List<songWithTitle> list = new ArrayList<>();
		SortedSet<String> trackNos = new TreeSet<>();
		for (String albums : values.keySet()) {
			HashMap<String, Object> songs = (HashMap<String, Object>) values.get(albums);
			byte[] image = getImage(String.valueOf(songs.get("IMAGE")));
			for (String song : songs.keySet()) {
				if (!song.equals("IMAGE")) {
					HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
					songWithTitle newSong = new songWithTitle(albums, song, oneSong.get("Lyricist").toString(), image, String.valueOf(oneSong.get("Download")));
					list.add(newSong);
					trackNos.add(song);
				}

			}
		}


		for (String Track : trackNos) {
			for (songWithTitle songNo : list) {
				if (songNo.getSongTitle().equals(Track)) {
					songlist.add(songNo);
				}
			}

		}
		filtered = songlist;
		setPlayList();
		adapter.notifyDataSetChanged();

	}

	private void setPlayList() {
		passedList.clear();
		for (String movie : values.keySet()) {
			HashMap<String, Object> songs = (HashMap<String, Object>) values.get(movie);
			for (String song : songs.keySet()) {


				if (!song.equals("IMAGE")) {

					HashMap<String, Object> oneSong = (HashMap<String, Object>) songs.get(song);
					Song passingSongList = new Song(movie, song, String.valueOf(oneSong.get("Lyricist")), String.valueOf(oneSong.get("Download")));
					passedList.add(passingSongList);
				}
			}
		}
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
		filtered = filterAlbum(songlist,newText);
		if(TextUtils.isEmpty(newText)){
			adapter.setFilter(songlist);
		}else {
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
	public void onPause() {
		super.onPause();
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.songmenu, menu);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
		searchView.setOnQueryTextListener(this);
		searchView.setQueryHint("Search songs");

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
