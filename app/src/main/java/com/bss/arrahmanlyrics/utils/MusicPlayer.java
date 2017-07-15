package com.bss.arrahmanlyrics.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.camera2.TotalCaptureResult;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.EnglishLyrics;
import com.bss.arrahmanlyrics.Fragments.OtherLyrics;
import com.bss.arrahmanlyrics.Fragments.songList;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.songUlr;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by mohan on 6/26/17.
 */

public class MusicPlayer implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener, CacheListener {

	MediaPlayer player;
	List<Song> ulrs;
	int songIndex = 0;
	int ulrsIndex = 0;
	//HashMap<String, String> list;
	Context context;
	String currentPlayingSong;
	HashMap<String, Object> manualSong;
	String Movie;
	SeekBar bar;
	TextView totalDur;
	TelephonyManager tm;
	AudioManager audioManager;
	PhoneStateListener phoneStateListener;
	ProgressDialog dialog;
	ArrayList playedList = new ArrayList();
	ImageView playButton;
	int resumePosition;
	boolean shuffle = true, repeat = false;
	EnglishLyrics enLyrics;
	songList songListFragment;
	List<Song> randomList;
	List<Song> currentList;
	OtherLyrics oLyrics;
	ImageView cover, favorites;
	int positioninCurrentList = 0;
	boolean mIsDucked = false,mLostAudioFocus = false;
	int oldPosition = 0;

	private boolean ongoingCall = false;


	public MusicPlayer(Context context) {
		this.context = context;

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);

		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		player.setOnBufferingUpdateListener(this);
		randomList = new ArrayList<>();
		currentList = new ArrayList<>();
		callStateListener();
		requestAudioFocus();

	}

	private boolean requestAudioFocus() {
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			//Focus gained
			return true;
		}
		//Could not gain focus
		return false;
	}

	private boolean removeAudioFocus() {
		return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
				audioManager.abandonAudioFocus(this);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {

		//double ratio = percent / 100.0;
		//int bufferingLevel = (int) (mp.getDuration() * ratio);

		//bar.setSecondaryProgress(bufferingLevel);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {

		next();


	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//checkFavoriteItem(Movie,currentPlayingSong);
		play();
		playButton.setImageResource(R.drawable.btnpause);

		setLyricsManually(Movie, currentPlayingSong);
		//Song song = currentList.get(currentList.indexOf(currentPlayingSong));
		//setBackground(song.getImage());
		dialog.dismiss();

	}

	public void setPlayList(List<Song> ulr) {

		this.ulrs = ulr;
		currentList.clear();
		randomList.clear();
		Random r = new Random();
		List<Integer> randomsNos = new ArrayList<>();
		for (int a = 0; a < ulr.size(); a++) {
			int b = 0;
			do {
				b = r.nextInt(ulr.size());
				if (!randomsNos.contains(b)) {
					randomsNos.add(b);
				}
			} while (randomsNos.contains(b) && randomsNos.size() != ulr.size());

		}

		for (int values : randomsNos) {
			randomList.add(ulr.get(values));
		}
		currentList = randomList;
		for (Song song : currentList) {
			Log.i("songlist", song.getSongTitle());
		}
	}

	public void setPlay(String name, String moiveName, SeekBar bar, TextView totalDur, Context presetContext, ImageView playButton, ImageView favorites, EnglishLyrics enLyrics, OtherLyrics oLyrics, songList songListFragment, ImageView cover) {
		String download = "";
		this.enLyrics = enLyrics;
		this.bar = bar;
		this.songListFragment = songListFragment;
		this.totalDur = totalDur;
		this.Movie = moiveName;
		this.oLyrics = oLyrics;
		this.cover = cover;
		this.playButton = playButton;
		this.favorites = favorites;
		for (Song ulr : currentList) {
			if (ulr.getSongTitle().equals(name)) {
				download = ulr.getUlr();
				positioninCurrentList = ulrs.indexOf(ulr);
				songIndex = currentList.indexOf(ulr);
				ulrsIndex = ulrs.indexOf(ulr);
				//song = ulr;


			}
		}


		try {
			player.reset();
			checkCachedState(download);

			player.setDataSource(setProxyUrl(download));
			Log.i("source", download);
			currentPlayingSong = name;
			//checkFavoriteItem(currentPlayingSong);
			context = presetContext;
			dialog = new ProgressDialog(presetContext);
			dialog.setMessage("Loading Song From database...");
			dialog.show();

			player.prepareAsync();
			//((TextView) this.enLyrics.getActivity().findViewById(R.id.song_title)).setText(name);
			//((TextView) this.enLyrics.getActivity().findViewById(R.id.album_title)).setText(Movie);
		} catch (IOException e) {
			//Log.e("error source", String.valueOf(download);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void play() {

		if (player.getDuration() > 0) {
			player.start();
			songListFragment.scrollTo(currentPlayingSong);
			bar.setMax((int) player.getDuration());

			bar.setProgress(getCurrentPosition());
			totalDur.setText(String.format("%02d : %02d ",
					TimeUnit.MILLISECONDS.toMinutes((long) player.getDuration()),
					TimeUnit.MILLISECONDS.toSeconds((long) player.getDuration()) -
							TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
									player.getDuration())))
			);


		} else {
			Toast.makeText(context, "Nothing to play", Toast.LENGTH_SHORT).show();
		}

	}

	public void pause() {
		if (player.isPlaying()) {
			player.pause();
			resumePosition = player.getCurrentPosition();
			playButton.setImageResource(R.drawable.btnplay);
		}

	}

	public void next() {

		int totalSongs = currentList.size();
		if (totalSongs > 0 && songIndex < totalSongs - 1) {
			Song song = currentList.get(songIndex + 1);
			changeSong(song.getUlr(), song.getSongTitle());
			ulrsIndex = ulrs.indexOf(song);
			//songListFragment.setEq(ulrsIndex,oldPosition);
			songIndex++;
			Movie = song.getMovieTitle();
			currentPlayingSong = song.getSongTitle();
			//checkFavoriteItem(song.getSongTitle());
			setLyricsManually(Movie, song.getSongTitle());

		} else if (songIndex == totalSongs - 1) {

			Song song = currentList.get(0);
			changeSong(song.getUlr(), song.getSongTitle());
			ulrsIndex = ulrs.indexOf(song);
			songIndex = 0;
			Movie = song.getMovieTitle();
			currentPlayingSong = song.getSongTitle();
			//checkFavoriteItem(song.getSongTitle());
			setLyricsManually(Movie, song.getSongTitle());

		}



	}

	private void changeSong(String download, String name) {
		try {

			player.reset();
			checkCachedState(download);

			player.setDataSource(setProxyUrl(download));
			//player.setDataSource(download);
			currentPlayingSong = name;
			dialog = new ProgressDialog(context);
			dialog.setMessage("Loading Song From database...");
			dialog.show();
			player.prepareAsync();
		} catch (IOException e) {
			//Log.e("error source", String.valueOf(download);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}

	public void previous() {

		int totalSongs = currentList.size();
		if (totalSongs > 0 && songIndex > 0) {
			Song song = currentList.get(songIndex - 1);
			changeSong(song.getUlr(), song.getSongTitle());
			ulrsIndex = ulrs.indexOf(song);
			songIndex--;
			Movie = song.getMovieTitle();
			currentPlayingSong = song.getSongTitle();
			//checkFavoriteItem(song.getSongTitle());
			setLyricsManually(Movie, song.getSongTitle());


		} else if (songIndex == 0) {
			Song song = currentList.get(totalSongs - 1);
			changeSong(song.getUlr(), song.getSongTitle());
			ulrsIndex = ulrs.indexOf(song);
			songIndex = totalSongs - 1;
			Movie = song.getMovieTitle();
			currentPlayingSong = song.getSongTitle();
			//checkFavoriteItem(song.getSongTitle());
			//setBackground(song.getImage());
			setLyricsManually(Movie, song.getSongTitle());

		}

/*		int totalSongs = ulrs.size();
		if (totalSongs > 0 && songIndex > 0) {
			Random r = new Random();
			int rIndex = songIndex;
			if (shuffle) {
				do {
					rIndex = r.nextInt(totalSongs - 1);
				} while (songIndex == rIndex);
			} else {
				rIndex += 1;
			}
			songUlr song = ulrs.get(rIndex);
			changeSong(song.getUrl(), song.getSongTitle());
			songIndex = rIndex;
			setLyricsManually(Movie, song.getSongTitle());
			((TextView) enLyrics.getActivity().findViewById(R.id.song_title)).setText(song.getSongTitle());
			((TextView) enLyrics.getActivity().findViewById(R.id.album_title)).setText(Movie);
		} else if (songIndex == totalSongs - 1) {
			Random r = new Random();
			int rIndex = songIndex;
			if (shuffle) {
				do {
					rIndex = r.nextInt(totalSongs - 1);
				}while (songIndex == rIndex);
			}else {
				rIndex = 0;
			}
			songUlr song = ulrs.get(rIndex);
			changeSong(song.getUrl(), song.getSongTitle());
			songIndex = rIndex;
			setLyricsManually(Movie, song.getSongTitle());
			((TextView) enLyrics.getActivity().findViewById(R.id.song_title)).setText(song.getSongTitle());
			((TextView) enLyrics.getActivity().findViewById(R.id.album_title)).setText(Movie);
		}*/

	}

	public void resume() {
		if (!player.isPlaying()) {
			player.seekTo(resumePosition);
			player.start();
		}
		playButton.setImageResource(R.drawable.btnpause);
	}

	public void shuffle() {
		if (shuffle) {
			shuffle = false;
			songIndex = ulrsIndex;
			currentList = ulrs;
		} else {
			shuffle = true;
			currentList = randomList;
		}

	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	public int getDuration() {
		return player.getDuration();
	}

	public void seekTo(int progress) {
		player.seekTo(progress);
	}

	public boolean isPlaying() {
		return player.isPlaying();

	}

	public void stop() {
		player.stop();
		playButton.setImageResource(R.drawable.btnplay);

	}


	@Override
	public void onAudioFocusChange(int focusChange) {
		String TAG = "Audio Manager";
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_LOSS:
				Log.d(TAG, "AudioFocus Loss");
				if (isPlaying()) {
					pause();
					//service.stopSelf();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				if (isPlaying()) {
					player.setVolume(0.3f, 0.3f);
					mIsDucked = true;
				}
				Log.d(TAG, "AudioFocus Loss Can Duck Transient");
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				Log.d("", "AudioFocus Loss Transient");
				if (isPlaying()) {
					pause();
					mLostAudioFocus = true;
				}
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				Log.d(TAG, "AudioFocus Gain");
				if (mIsDucked) {
					player.setVolume(1.0f, 1.0f);
					mIsDucked = false;
				} else if (mLostAudioFocus) {
					// If we temporarily lost the audio focus we can resume playback here
					if (player.isPlaying()) {
						play();
					}
					mLostAudioFocus = false;
				}
				break;
			default:
				Log.d(TAG, "Unknown focus");
		}
	}


	private void callStateListener() {
		// Get the telephony manager
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		//Starting listening for PhoneState changes
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					//if at least one call exists or the phone is ringing
					//pause the MediaPlayer
					case TelephonyManager.CALL_STATE_OFFHOOK:
					case TelephonyManager.CALL_STATE_RINGING:
						if (player != null) {
							pause();
							ongoingCall = true;
						}
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						// Phone idle. Start playing.
						if (player != null) {
							if (ongoingCall) {
								ongoingCall = false;
								resume();
							}
						}
						break;
				}
			}
		};
		// Register the listener with the telephony manager
		// Listen for changes to the device call state.
		tm.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CALL_STATE);
	}

	public void stopListener() {
		removeAudioFocus();
		//Disable the PhoneStateListener
		if (phoneStateListener != null) {
			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
	}


	public void setLyricsManually(final String albumname, final String songTitle) {
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
		ref.child("AR Rahman").child("Tamil").child(albumname).child(songTitle).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {

				manualSong = (HashMap<String, Object>) dataSnapshot.getValue();
				Log.i("Selected Song", String.valueOf(manualSong));
				((TextView) enLyrics.getActivity().findViewById(R.id.song_title)).setText(FirstLetterUpperCase.convert(songTitle));
				((TextView) enLyrics.getActivity().findViewById(R.id.album_title)).setText(FirstLetterUpperCase.convert(albumname));
				setLyrics(manualSong);
				checkFavoriteItem(Movie, currentPlayingSong);
				Log.i("Song Favorite", songTitle);


			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});

		DatabaseReference refImage = FirebaseDatabase.getInstance().getReference();
		refImage.child("AR Rahman").child("Tamil").child(albumname).child("IMAGE").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				setBackground(String.valueOf(dataSnapshot.getValue()));
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

	}

	void setLyrics(HashMap<String, Object> manualSong) {

		final StringBuilder builderEnglish = new StringBuilder();
		//builderEnglish.append(manualSong.get("English"));
		//builderEnglish.append(manualSong.get("EnglishOne"));

		//Typeface english = Typeface.createFromAsset(enLyrics.getActivity().getAssets(), "english.ttf");

		enLyrics.lyricsText.setText(String.valueOf(manualSong.get("English")));
		enLyrics.lyricsText2.setText(String.valueOf(manualSong.get("EnglishOne")));
		//enLyrics.lyricsText.setTypeface(english);

		final StringBuilder builderOther = new StringBuilder();
		//builderOther.append(manualSong.get("Others"));
		//builderOther.append(manualSong.get("OthersOne"));

		//Typeface tamil = Typeface.createFromAsset(oLyrics.getActivity().getAssets(), "english.ttf");

		oLyrics.lyricsText.setText(String.valueOf(manualSong.get("Others")));
		oLyrics.lyricsText2.setText(String.valueOf(manualSong.get("OthersOne")));
		//oLyrics.lyricsText.setTypeface(tamil);

	}

	public boolean getShuffle() {
		return shuffle;
	}

	public void setPlay(String name) {
		String download = "";

		for (Song ulr : ulrs) {
			if (ulr.getSongTitle().equals(name)) {
				download = ulr.getUlr();
				songIndex = ulrs.indexOf(ulr);

				Movie = ulr.getMovieTitle();
			}
		}


		try {
			player.reset();
			checkCachedState(download);

			player.setDataSource(setProxyUrl(download));
			//player.setDataSource(download);
			currentPlayingSong = name;
			//checkFavoriteItem(currentPlayingSong);
			dialog = new ProgressDialog(context);
			dialog.setMessage("Loading Song From database...");
			dialog.show();
			player.prepareAsync();
			((TextView) enLyrics.getActivity().findViewById(R.id.song_title)).setText(FirstLetterUpperCase.convert(name));
			((TextView) enLyrics.getActivity().findViewById(R.id.album_title)).setText(FirstLetterUpperCase.convert(Movie));
		} catch (IOException e) {
			//Log.e("error source", String.valueOf(download);
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}


	public void setBackground(String image) {
		try {

			Bitmap bitmap = getBitmap(image);
			ArtworkUtils.blurPreferances(context, bitmap, cover);
			//setPalettes(blured);
		} catch (Exception e) {
			Log.e("error", e.getMessage());
		}
	}

	public Bitmap getBitmap(String imageString) {
		if (imageString.equals(null)) {
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
			return bitmap;
		}
		byte[] image = Base64.decode(imageString, Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		return bitmap;
	}

	@Override
	public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
		double ratio = percentsAvailable / 100.0;
		int bufferingLevel = (int) (player.getDuration() * ratio);
		bar.setSecondaryProgress(bufferingLevel);

		Log.e("proxy", String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
	}

	private void checkCachedState(String url) {
		HttpProxyCacheServer proxy = mainApp.getProxy(context);
		boolean fullyCached = proxy.isCached(url);

		if (fullyCached) {
			double ratio = 100 / 100.0;
			int bufferingLevel = (int) (player.getDuration() * ratio);
			bar.setSecondaryProgress(bufferingLevel);
		}
	}

	private String setProxyUrl(String url) throws IOException {
		HttpProxyCacheServer proxy = mainApp.getProxy(context);
		proxy.registerCacheListener(this, url);
		String proxyUrl = proxy.getProxyUrl(url);
		Log.d("proxy", "Use proxy url " + proxyUrl + " instead of original url " + url);
		return proxyUrl;

	}

	public void stopCacheListener() {
		mainApp.getProxy(context).unregisterCacheListener(this);
	}

	public void addFavorites() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		mainApp.getSp().addFavorite(Movie, currentPlayingSong, user);


	}

	public void removeFavorites() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		mainApp.getSp().removeFavorite(Movie, currentPlayingSong, user);


	}

	public boolean checkFavoriteItem(String albumName, String songName) {

		//Song song = currentList.get(currentList.indexOf(Name));

		HashMap<String, ArrayList<String>> favorites = mainApp.getSp().getFavorites();
		if (favorites != null) {
			if (favorites.containsKey(albumName)) {
				if (favorites.get(albumName).contains(songName)) {
					this.favorites.setImageResource(R.drawable.ic_action_favorite_on);
					return true;
				}
			}

		}
		this.favorites.setImageResource(R.drawable.ic_action_favorite);
		return false;
	}

	public boolean checkFavoriteItem() {
		HashMap<String, ArrayList<String>> favorites = mainApp.getSp().getFavorites();
		if (favorites != null) {
			if (favorites.containsKey(Movie)) {
				if (favorites.get(Movie).contains(currentPlayingSong)) {

					return true;
				}
			}


		}

		return false;

	}

	public String getCurrentPlayingSong() {
		return currentPlayingSong;
	}

}