package com.bss.arrahmanlyrics.activites;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.EnglishLyrics;
import com.bss.arrahmanlyrics.Fragments.OtherLyrics;
import com.bss.arrahmanlyrics.Fragments.songList;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.utils.ArtworkUtils;
import com.bss.arrahmanlyrics.utils.Helper;
import com.bss.arrahmanlyrics.utils.MediaPlayerService;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.hanks.library.SmallBang;

public class lyricsActivity extends AppCompatActivity implements ImageView.OnClickListener, songList.OnSongSelectedListener, MediaPlayerService.ServiceCallbacks {
    DatabaseReference imageRef;
    DatabaseReference songRef;
    ViewPager lyricsPager;
    //List<String> songList;
    HashMap<String, Object> values;
    HashMap<String, String> links;
    SectionsPagerAdapter section;
    private ImageView play, next, prev, shuffle, favorite;
    public SeekBar bar;
    public TextView currentDur, totalDur;
    String movieName, songTitle;
    List<Song> passedList;
    EnglishLyrics enLyrics;
    OtherLyrics oLyrics;
    songList songListFragment;
    HashMap<String, Object> manualSong;
    boolean toggleFavorite = false;
    ImageView cover;
    Toolbar toolbar;
    ImageView left, right;
    SmallBang bang;
    EditText searchBar;
    boolean isSetDetails = false;
    LinearLayout topView;
    public MediaPlayerService player;
    public boolean serviceBound = false;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
    public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";
    public static final String Broadcast_PLAY = "com.bss.arrahmanlyrics.activites.Play";
    public static final String Broadcast_PAUSE = "com.bss.arrahmanlyrics.activites.Pause";
    public static final String Broadcast_NEXT = "com.bss.arrahmanlyrics.activites.Next";
    public static final String Broadcast_Prev = "com.bss.arrahmanlyrics.activites.Previous";
    public static final String Broadcast_Shuffle = "com.bss.arrahmanlyrics.activites.Shuffle";
    public static final String Broadcast_UnShuffle = "com.bss.arrahmanlyrics.activites.UnShuffle";

    TextView song_title, album_title;

//MusicPlayer mainApp.getPlayer();

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyrics2);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        toolbar = (Toolbar) findViewById(R.id.lyricstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //().setStatusBarColor(Color.parseColor("#a000ffae"));
        links = new HashMap<>();
        bang = SmallBang.attach2Window(this);
        lyricsPager = (ViewPager) findViewById(R.id.lyricsPager);
        //slidingpanel = (SlidingPaneLayout) findViewById(R.id.slidingpanelayout);
        //songlistView = (RecyclerView) findViewById(R.id.fastsonglist);
        enLyrics = new EnglishLyrics();
        oLyrics = new OtherLyrics();
        songListFragment = new songList();

        section = new SectionsPagerAdapter(getSupportFragmentManager());
        section.addFragment(songListFragment, "Song List");
        section.addFragment(enLyrics, "English Lyrics");
        section.addFragment(oLyrics, "Other Lyrics");


        topView = (LinearLayout) findViewById(R.id.topView);
        topView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                songListFragment.scrollTo(player.getActiveAudio().getSongTitle());
                return false;

            }
        });
        //songList = new ArrayList<>();
        passedList = new StorageUtil(getApplicationContext()).loadAudio();


        left = (ImageView) findViewById(R.id.swifeleft);
        right = (ImageView) findViewById(R.id.swiferight);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int getItemNo = lyricsPager.getCurrentItem();
                if (getItemNo > 0) {
                    lyricsPager.setCurrentItem(getItemNo - 1);
                }

            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int getItemNo = lyricsPager.getCurrentItem();
                if (getItemNo < 2) {
                    lyricsPager.setCurrentItem(getItemNo + 1);
                }

            }
        });

        values = new HashMap<>();
        lyricsPager.setAdapter(section);
        lyricsPager.setCurrentItem(0);
        lyricsPager.setOffscreenPageLimit(3);
        lyricsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    left.setVisibility(View.VISIBLE);
                    right.setVisibility(View.VISIBLE);

                } else if (position == 0) {
                    right.setVisibility(View.VISIBLE);
                    left.setVisibility(View.INVISIBLE);

                } else {
                    left.setVisibility(View.VISIBLE);
                    right.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (getIntent().getExtras().getString("Title") != null) {
            movieName = getIntent().getExtras().getString("Title");
        } else {
            movieName = "";
        }

        Log.e("selected Movie", movieName);
//		Log.e("selected song", getIntent().getExtras().getString("selectedSong"));


        play = (ImageView) findViewById(R.id.playPause);
        next = (ImageView) findViewById(R.id.forward);
        prev = (ImageView) findViewById(R.id.backward);
        shuffle = (ImageView) findViewById(R.id.shuffle_song);
        favorite = (ImageView) findViewById(R.id.favorite);
        favorite.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        bar = (SeekBar) findViewById(R.id.custombar);
        cover = (ImageView) findViewById(R.id.blurArtwork);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    player.seekTo(progress);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (player != null) {
            if (player.isPlaying()) {
                Log.i("seekbar", String.valueOf(player.getDuration()));
                bar.setMax(player.getDuration());
            }
        }
        currentDur = (TextView) findViewById(R.id.currentDur);
        totalDur = (TextView) findViewById(R.id.totalDur);
        song_title = (TextView) findViewById(R.id.song_title);
        album_title = (TextView) findViewById(R.id.album_title);
        album_title.setText(movieName);

        //songListAdapter = new SlideSongAdapter(getApplicationContext(), songlist);
        //songlistView.setAdapter(songListAdapter);
        //CustomLayoutManager customLayoutManager = new CustomLayoutManager(getApplicationContext());
        //customLayoutManager.setSmoothScrollbarEnabled(true);
        // songlistView.setLayoutManager(customLayoutManager);
        // songlistView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        /*imageRef = FirebaseDatabase.getInstance().getReference();
		imageRef.child("AR Rahman").child("Tamil").child(getIntent().getExtras().getString("Title")).child("IMAGE").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String imageString = String.valueOf(dataSnapshot.getValue());


				setBackground(imageString);


			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});*/
		/*songRef = FirebaseDatabase.getInstance().getReference();
		songRef.child("AR Rahman").child("Tamil").child(getIntent().getExtras().getString("Title")).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();

				preparePlaylist();
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});*/
        if (passedList != null) {
            if(passedList.size()>0) {
                songTitle = ((Song) passedList.get(0)).getSongTitle();
                preparePlaylist();
            }else {
                finish();
                Toast.makeText(getApplicationContext(),"nothing to showup, play or add songs to queue",Toast.LENGTH_SHORT).show();
            }
        }else {
            finish();
            Toast.makeText(getApplicationContext(),"nothing to showup, play or add songs to queue",Toast.LENGTH_SHORT).show();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.post(runnable);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int position = player.getCurrrentDuration();
                bar.setProgress(position);
                currentDur.setText(Helper.durationCalculator(position));

            }
            mHandler.postDelayed(runnable, 1000);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            player.setCallbacks(lyricsActivity.this);
            setDetails();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void preparePlaylist() {
		/*List<songUlr> ulrs = new ArrayList<>();
		songList.clear();
		SortedSet<String> sorted = new TreeSet<>();
		for (String songs : values.keySet()) {
			if (!songs.equals("IMAGE")) {
				songList.add(songs);
				sorted.add(songs);
			}

		}

		for(String sortedName:sorted){
			HashMap<String, Object> oneSong = (HashMap<String, Object>) values.get(sortedName);
			songUlr url = new songUlr(sortedName,String.valueOf(oneSong.get("Download")));
			ulrs.add(url);
		}*/

		/*mainApp.getPlayer().setPlayList(passedList);
		mainApp.getPlayer().setPlay(songTitle,movieName, bar, totalDur,lyricsActivity.this,play,favorite,enLyrics,oLyrics,songListFragment,cover);*/
        setLyricsManually(movieName, songTitle);

        //play.setImageResource(R.drawable.ic_action_pause);
        Log.e("duration", String.valueOf(mainApp.getPlayer().getDuration()));
        //bar.setMax((int) (player.getDuration()));
        //bar.setProgress(((MainActivity)getApplicationContext()).player.getCurrrentDuration());
		/*totalDur.setText(String.format("%02d : %02d ",
				TimeUnit.MILLISECONDS.toMinutes((long) ((MainActivity)getApplicationContext()).player.getDuration()),
				TimeUnit.MILLISECONDS.toSeconds((long) mainApp.getPlayer().getDuration()) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
								mainApp.getPlayer().getDuration())))
		);

		//myHandler.postDelayed(UpdateSongTime, 100);
	}


	/*   private void setLyrics() {
		   final StringBuilder builderEnglish = new StringBuilder();
		   builderEnglish.append(selectedSong.get("English"));
		   builderEnglish.append(selectedSong.get("EnglishOne"));
		   final StringBuilder builderOther = new StringBuilder();
		   builderOther.append(selectedSong.get("Others"));
		   builderOther.append(selectedSong.get("OthersOne"));

		   Typeface english = Typeface.createFromAsset(getAssets(),"english.ttf");
		   title.setText(getIntent().getExtras().getString("SongTitle"));


		   lyricist.setText(getIntent().getExtras().getString("lyricist"));


		   lyricsText.setText(String.valueOf(builderOther));
		   lyricsText.setTypeface(english);
	   }*/
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_down);
        super.onPause();


    }

    @Override
    protected void onStart() {
        super.onStart();
        isSetDetails = false;
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_forward);

    }

    public void setBackground(String imageString) {
        try {
            ImageView cover = (ImageView) findViewById(R.id.blurArtwork);
            Bitmap bitmap = getBitmap(imageString);
            ArtworkUtils.blurPreferances(getApplicationContext(), bitmap, cover);
            //setPalettes(blured);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }


    }

	/*   public void setPalettes(Bitmap bitmap) {

		   try {
			   Palette.from(bitmap).maximumColorCount(160000000).generate(new Palette.PaletteAsyncListener() {

				   @Override
				   public void onGenerated(Palette palette) {
					   LinearLayout layout = (LinearLayout) findViewById(R.id.dragPanel);
					   playPauseView = (PlayPauseView) findViewById(R.id.btn_play);
					   // Get the "vibrant" color swatch based on the bitmap
						if (palette.getDarkVibrantSwatch() != null) {
						   layout.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
							getWindow().setStatusBarColor(palette.getDarkVibrantSwatch().getRgb());
						totalDur.setText(Helper.durationCalculator(player.getDuration()));
				bar.setMax(player.getDuration());	marqueeTextView.setTextColor(ColorStateList.valueOf(palette.getDarkVibrantSwatch().getBodyTextColor()));
						   playPauseView.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantSwatch().getTitleTextColor()));


					   } else if (palette.getDarkMutedSwatch() != null) {
							layout.setBackgroundColor(palette.getDarkMutedSwatch().getRgb());
							getWindow().setStatusBarColor(palette.getDarkMutedSwatch().getRgb());
							marqueeTextView.setTextColor(ColorStateList.valueOf(palette.getDarkMutedSwatch().getBodyTextColor()));
							playPauseView.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkMutedSwatch().getTitleTextColor()));
					   } else if (palette.getDominantSwatch() != null) {
							layout.setBackgroundColor(palette.getDominantSwatch().getRgb());
							getWindow().setStatusBarColor(palette.getDominantSwatch().getRgb());
							marqueeTextView.setTextColor(ColorStateList.valueOf(palette.getDominantSwatch().getBodyTextColor()));
							playPauseView.setBackgroundTintList(ColorStateList.valueOf(palette.getDominantSwatch().getTitleTextColor()));
					   } else if (palette.getMutedSwatch() != null) {
							layout.setBackgroundColor(palette.getMutedSwatch().getRgb());
							getWindow().setStatusBarColor(palette.getMutedSwatch().getRgb());
							marqueeTextView.setTextColor(ColorStateList.valueOf(palette.getMutedSwatch().getBodyTextColor()));
							playPauseView.setBackgroundTintList(ColorStateList.valueOf(palette.getMutedSwatch().getTitleTextColor()));
					   }


				   }
			   });
		   } catch (Exception e) {
			   Log.i("exception:", e.getMessage());
		   }
	   }*/


    public Bitmap getBitmap(String imageString) {
        if (imageString.equals(null)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            return bitmap;
        }
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playPause: {
                if (player == null) return;
                if (player.isPlaying()) {
                    Intent pause = new Intent(Broadcast_PAUSE);
                    sendBroadcast(pause);
                    play.setImageResource(R.drawable.btnplay);
                } else {
                    Intent playsong = new Intent(Broadcast_PLAY);
                    sendBroadcast(playsong);
                    play.setImageResource(R.drawable.btnpause);
                }

                break;
            }
            case R.id.backward: {
                if (player == null) return;
                Intent prev = new Intent(Broadcast_Prev);
                sendBroadcast(prev);
                isSetDetails = false;
                break;
            }
            case R.id.forward: {
                if (player == null) return;
                Intent next = new Intent(Broadcast_NEXT);
                sendBroadcast(next);
                isSetDetails = false;
                break;
            }
            case R.id.shuffle_song: {
                if (player == null) return;
                if (player.isShuffleOn()) {
                    Intent shuffleOff = new Intent(Broadcast_UnShuffle);
                    sendBroadcast(shuffleOff);
                    bang.bang(v);
                    shuffle.setImageResource(R.drawable.shuffle);
                } else {
                    Intent shuffleon = new Intent(Broadcast_Shuffle);
                    sendBroadcast(shuffleon);
                    bang.bang(v);
                    shuffle.setImageResource(R.drawable.shuffleon);
                }
                break;
            }
            case R.id.favorite: {
                if (player != null) {
                    if (player.isPlaying()) {
                        if (checkFavoriteItem()) {
                            removeFavorites();
                            bang.bang(v);
                            favorite.setImageResource(R.drawable.ic_action_favorite);
                            //mainApp.getPlayer().removeFavorites();
                        } else {
                            addFavorites();
                            bang.bang(v);
                            favorite.setImageResource(R.drawable.ic_action_favorite_on);
                            //mainApp.getPlayer().addFavorites();

                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onSongSelected(int position) {
        playAudio(position);
    }

    @Override
    public void updateUi() {
        totalDur.setText(Helper.durationCalculator(player.getDuration()));
        bar.setMax(player.getDuration());
        Song song = player.getActiveAudio();
        song_title.setText(song.getSongTitle());
        album_title.setText(song.getMovieTitle());
        play.setImageResource(R.drawable.btnpause);
        setLyricsManually(song.getMovieTitle(), song.getSongTitle());
        if (player.isShuffleOn()) {
            shuffle.setImageResource(R.drawable.shuffleon);
        } else {
            shuffle.setImageResource(R.drawable.shuffle);
        }
        songListFragment.scrollTo(song.getSongTitle());
        checkFavoriteItem(song.getMovieTitle(), song.getSongTitle());


    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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

    /*private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (((MainActivity)getApplicationContext()).player. {
                int startTime = mainApp.getPlayer().getCurrentPosition();
                currentDur.setText(String.format("%02d : %02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime)))
                );
                if (startTime == mainApp.getPlayer().getDuration()) {
                    bar.setProgress(0);
                } else {
                    bar.setProgress(startTime);
                }
            }


            myHandler.postDelayed(this, 100);

        }
    };*/
    public void setLyricsManually(String albumname, String songTitle) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("AR Rahman").child("Tamil").child(albumname).child(songTitle).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                manualSong = (HashMap<String, Object>) dataSnapshot.getValue();
                //Log.i("Selected Song", String.valueOf(manualSong));
                if (manualSong != null) {
                    setLyrics(manualSong);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void setLyrics(HashMap<String, Object> manualSong) {

        final StringBuilder builderEnglish = new StringBuilder();
        builderEnglish.append(manualSong.get("English"));
        builderEnglish.append(manualSong.get("EnglishOne"));


        enLyrics.lyricsText.setText(String.valueOf(builderEnglish));
        enLyrics.lyricsText2.setText(String.valueOf(builderEnglish));

        final StringBuilder builderOther = new StringBuilder();
        builderOther.append(manualSong.get("Others"));
        builderOther.append(manualSong.get("OthersOne"));


        oLyrics.lyricsText.setText(String.valueOf(builderOther));
        oLyrics.lyricsText2.setText(String.valueOf(builderOther));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.setCallbacks(null);
            //service is active
            //player.stopSelf();

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    public void playAudio(int audioIndex, ArrayList<Song> audioList) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);


            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent playlist = new Intent(Broadcast_NEW_ALBUM);
            sendBroadcast(playlist);
			/*Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
			sendBroadcast(broadcastIntent);*/
        }
    }

    public void playAudio(int audioIndex) {
        //Check is service is active

        //Store the new audioIndex to SharedPreferences
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(audioIndex);
        //`tLog.i("servicebound:","true");

        //Service is active
        //Send a broadcast to the service -> PLAY_NEW_AUDIO


    }

    public void setDetails() {
        if (player != null) {
            if (player.isPlaying()) {
                if (!isSetDetails) {
                    totalDur.setText(Helper.durationCalculator(player.getDuration()));
                    bar.setMax(player.getDuration());
                    Song song = player.getActiveAudio();
                    song_title.setText(song.getSongTitle());
                    album_title.setText(song.getMovieTitle());
                    play.setImageResource(R.drawable.btnpause);
                    setLyricsManually(song.getMovieTitle(), song.getSongTitle());
                    if (player.isShuffleOn()) {
                        shuffle.setImageResource(R.drawable.shuffleon);
                    } else {
                        shuffle.setImageResource(R.drawable.shuffle);
                    }
                    songListFragment.scrollTo(song.getSongTitle());
                    checkFavoriteItem(song.getMovieTitle(), song.getSongTitle());
                    isSetDetails = true;
                }
            }
        }


    }

    public void addFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mainApp.getSp().addFavorite(player.getActiveAudio().getMovieTitle(), player.getActiveAudio().getSongTitle(), user);


    }

    public void removeFavorites() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mainApp.getSp().removeFavorite(player.getActiveAudio().getMovieTitle(), player.getActiveAudio().getSongTitle(), user);


    }

    public boolean checkFavoriteItem(String albumName, String songName) {

        //Song song = currentList.get(currentList.indexOf(Name));

        HashMap<String, ArrayList<String>> favorites = mainApp.getSp().getFavorites();
        if (favorites != null) {
            if (favorites.containsKey(albumName)) {
                if (favorites.get(albumName).contains(songName)) {
                    favorite.setImageResource(R.drawable.ic_action_favorite_on);
                    return true;
                }
            }

        }
        favorite.setImageResource(R.drawable.ic_action_favorite);
        return false;
    }

    public boolean checkFavoriteItem() {
        HashMap<String, ArrayList<String>> favorites = mainApp.getSp().getFavorites();
        if (favorites != null) {
            if (favorites.containsKey(player.getActiveAudio().getMovieTitle())) {
                if (favorites.get(player.getActiveAudio().getMovieTitle()).contains(player.getActiveAudio().getSongTitle())) {

                    return true;
                }
            }


        }

        return false;

    }

    public void setisSetDetails(boolean value) {
        isSetDetails = false;
    }
}