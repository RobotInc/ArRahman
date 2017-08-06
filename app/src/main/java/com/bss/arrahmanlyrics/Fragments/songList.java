package com.bss.arrahmanlyrics.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.activites.MainActivity;
import com.bss.arrahmanlyrics.activites.lyricsActivity;
import com.bss.arrahmanlyrics.adapter.fragmentSongAdapter;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.slideSong;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.BlurImage;
import com.bss.arrahmanlyrics.utils.CustomLayoutManager;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utils.SimpleDividerItemDecoration;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import es.claucookie.miniequalizerlibrary.EqualizerView;

//import static com.bss.arrahmanlyrics.activites.MainActivity.Broadcast_PLAY_NEW_AUDIO;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link songList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link songList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class songList extends Fragment implements SearchView.OnQueryTextListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	FastScrollRecyclerView songlistView;
	fragmentSongAdapter adapter;
	List<Song> passedList;
	List<songWithTitle> songsListArray;
	List<songWithTitle> filtered;
	lyricsActivity activity;
	boolean playListSet = false;
	SearchView searchView;
	HashMap<String, Object> values;
	DatabaseReference songref;
	OnSongSelectedListener mCallback;
	int presentpos = 0;
	private OnFragmentInteractionListener mListener;

    public void play(songWithTitle song) {
		int index = 0;
		for (Song songs : passedList) {
			if (songs.getMovieTitle().equalsIgnoreCase(song.getMovietitle()) && songs.getSongTitle().equalsIgnoreCase(song.getSongTitle())) {
				index = passedList.indexOf(songs);
			}
		}

		if (!playListSet) {
			StorageUtil storageUtil = new StorageUtil(getContext());
			storageUtil.storeAudio((ArrayList<Song>) passedList);
			storageUtil.storeAudioIndex(0);
			Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
			getActivity().sendBroadcast(setplaylist);
			playListSet = true;
		}
		mCallback.onSongSelected(index);

		Intent broadcastIntent = new Intent(lyricsActivity.Broadcast_PLAY_NEW_AUDIO);
		getActivity().sendBroadcast(broadcastIntent);
		((lyricsActivity)getActivity()).setisSetDetails(false);

    }

	public void removeFromQueue(songWithTitle songwith) {
		StorageUtil storageUtil = new StorageUtil(getContext());

		ArrayList<Song> list = storageUtil.loadAudio();
		for (Song song : list) {
			if (song.getMovieTitle().equalsIgnoreCase(songwith.getMovietitle()) && song.getSongTitle().equalsIgnoreCase(songwith.getSongTitle())) {
				list.remove(song);
				passedList.remove(song);
				StorageUtil newUtil = new StorageUtil(getContext());
				newUtil.storeAudio((ArrayList<Song>) list);
				songsListArray.remove(songwith);

				adapter.notifyDataSetChanged();
				Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
				getActivity().sendBroadcast(setplaylist);
				Toast.makeText(getContext(), song.getSongTitle() + " is removed from queue", Toast.LENGTH_SHORT).show();
				break;
			}
		}



	}


	public interface OnSongSelectedListener {
		public void onSongSelected(int position);
	}

	public songList() {
		// Required empty public constructor
	}


	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment songList.
	 */
	// TODO: Rename and change types and number of parameters
	public static songList newInstance(String param1, String param2) {
		songList fragment = new songList();
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
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View rootview = inflater.inflate(R.layout.fragment_song_list, container, false);
		setHasOptionsMenu(true);
		songlistView = (FastScrollRecyclerView) rootview.findViewById(R.id.albumPlayList);
		passedList = getActivity().getIntent().getExtras().getParcelableArrayList("list");
		playListSet = false;
		songsListArray = new ArrayList<>();
		adapter = new fragmentSongAdapter(getContext(), songsListArray,songList.this);
		songlistView.setAdapter(adapter);
		final CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
		customLayoutManager.setSmoothScrollbarEnabled(true);
		songlistView.setThumbColor(Color.parseColor("#a000ffae"));
		songlistView.setLayoutManager(customLayoutManager);


		songlistView.addItemDecoration(new DividerItemDecoration(getContext(), 75, true));


				songref = FirebaseDatabase.getInstance().getReference();

		songref.child("AR Rahman").child("Tamil").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();

				prepareSongList();
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
		adapter.notifyDataSetChanged();

		return rootview;
	}

	private void prepareSongList() {
		songsListArray.clear();

		for (Song song : passedList) {
			String moive = song.getMovieTitle();
			String title = song.getSongTitle();
			HashMap<String, Object> movieMap = (HashMap<String, Object>) values.get(moive);
			HashMap<String, Object> songMap = (HashMap<String, Object>) movieMap.get(title);
			songWithTitle songwith = new songWithTitle(moive, title, String.valueOf(songMap.get("Lyricist")), getImage(String.valueOf(movieMap.get("IMAGE"))), song.getUlr());
			songsListArray.add(songwith);

		}
		filtered = songsListArray;
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
		filtered = filterAlbum(songsListArray, newText);
		if (TextUtils.isEmpty(newText)) {
			adapter.setFilter(songsListArray);
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
		inflater.inflate(R.menu.main, menu);
		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.list_search));
		searchView.setOnQueryTextListener(this);

		searchView.setQueryHint("Search songs");


	}

	@Override
	public void onResume() {
		super.onResume();

	}

	public void scrollTo(String songTitle) {
		Log.e("size", String.valueOf(songsListArray.size()));
		for (songWithTitle song : songsListArray) {
			if (song.getSongTitle().equals(songTitle)) {
				int index = songsListArray.indexOf(song);
				songlistView.scrollToPosition(index);
				break;
			}
		}

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
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mCallback = (OnSongSelectedListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}
}
