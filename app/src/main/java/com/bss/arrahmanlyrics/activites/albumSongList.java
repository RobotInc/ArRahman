package com.bss.arrahmanlyrics.activites;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.adapter.AlbumSongAdapter;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.models.slideSong;
import com.bss.arrahmanlyrics.models.songWithTitle;
import com.bss.arrahmanlyrics.utils.ArtworkUtils;
import com.bss.arrahmanlyrics.utils.CustomLayoutManager;
import com.bss.arrahmanlyrics.utils.DividerItemDecoration;
import com.bss.arrahmanlyrics.utils.FirstLetterUpperCase;
import com.bss.arrahmanlyrics.utils.Helper;
import com.bss.arrahmanlyrics.utils.MediaPlayerService;
import com.bss.arrahmanlyrics.utils.RecyclerItemClickListener;
import com.bss.arrahmanlyrics.utils.SquareImageView;
import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.SortedSet;
import java.util.TreeSet;

import static android.R.color.holo_orange_light;

public class albumSongList extends AppCompatActivity implements View.OnClickListener {
    AlbumSongAdapter adapter;
    ArrayList<slideSong> list;
    ArrayList<Song> passedList;
    DatabaseReference songRef;
    HashMap<String, Object> values;
    Toolbar toolbar;
    SquareImageView artwork;
    FloatingActionButton fab;
    FastScrollRecyclerView rc;
    ImageView back;
    TextView songTitle;
    TextView movieTitle;
    public MediaPlayerService player;
    public boolean serviceBound = false;
    ImageView smallplay, smallback, eqToggle;
    TextView movietitle, songtitle;
    SeekBar seekbar;
    LinearLayout layout;
    Handler mhandler = new Handler();
    boolean isSetDetails = false;
    boolean playListSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_song_list);
        toolbar = (Toolbar) findViewById(R.id.albumtoolbar);

        toolbar.setTitle(FirstLetterUpperCase.convert(getIntent().getExtras().getString("Title")));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.holo_orange_light));
        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "english.ttf");
        // setSupportActionBar(toolbar);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDarkDarkTheme));
        smallplay = (ImageView) findViewById(R.id.small_toggle);
        smallplay.setOnClickListener(this);
        smallback = (ImageView) findViewById(R.id.img_bottom_slideone);
        movietitle = (TextView) findViewById(R.id.small_title);
        songtitle = (TextView) findViewById(R.id.small_song);
        seekbar = (SeekBar) findViewById(R.id.small_seekbar);
        passedList = getIntent().getExtras().getParcelableArrayList("list");
        list = new ArrayList<>();
        adapter = new AlbumSongAdapter(getApplicationContext(), list, albumSongList.this);
        rc = (FastScrollRecyclerView) findViewById(R.id.albumsongrv);
        rc.setAdapter(adapter);
        final CustomLayoutManager customLayoutManager = new CustomLayoutManager(getApplicationContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rc.setLayoutManager(customLayoutManager);
        artwork = (SquareImageView) findViewById(R.id.album_artwork);
        rc.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 75, true));
        fab = (FloatingActionButton) findViewById(R.id.play_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;


                if (!playListSet) {
                    StorageUtil storageUtil = new StorageUtil(getApplicationContext());
                    storageUtil.storeAudio((ArrayList<Song>) passedList);
                    storageUtil.storeAudioIndex(0);
                    Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
                    sendBroadcast(setplaylist);
                    playListSet = true;
                }
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudioIndex(index);

                Intent broadcastIntent = new Intent(lyricsActivity.Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                Toast.makeText(getApplicationContext(), "Current playlist replaced with" + FirstLetterUpperCase.convert(getIntent().getExtras().getString("Title")) + " album", Toast.LENGTH_SHORT).show();

                isSetDetails = false;


            }
        });
        layout = (LinearLayout) findViewById(R.id.smallview);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Song selectedAlbum = filtered.get(position);
                Bundle songs = new Bundle();
                songs.putParcelableArrayList("list", new StorageUtil(getApplicationContext()).loadAudio());
                //songs.putSerializable("map",(HashMap<String,Object>)values.get(selectedAlbum.getName()));
                //songs.putString("Title", selectedAlbum.getName());
                //songs.putString("selectedSong","");

                //songs.putByteArray("image",albumList.get(position).getThumbnail());

                try {
                    Intent intent = new Intent(getApplicationContext(), lyricsActivity.class);
                    intent.putExtras(songs);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.i("Exception", e.getMessage());
                }
            }
        });
        songRef = FirebaseDatabase.getInstance().getReference();
        songRef.child("AR Rahman").child("Tamil").child(getIntent().getExtras().getString("Title")).addValueEventListener(new ValueEventListener() {
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
       /* rc.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                slideSong song = list.get(position);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list", passedList);
                bundle.putString("selectedSong", song.getSongName());
                bundle.putString("Title", getIntent().getExtras().getString("Title"));
                //bundle.putString("trackNo",song.getTrackNo());


                ArrayList<Song> oneSong = new ArrayList<Song>();
                for (Song songs : passedList) {
                    if (songs.getMovieTitle().equalsIgnoreCase(getIntent().getExtras().getString("Title")) && songs.getSongTitle().equalsIgnoreCase(song.getSongName())) {
                        oneSong.add(songs);

                    }
                }
                StorageUtil storageUtil = new StorageUtil(getApplicationContext());
                storageUtil.storeAudio((ArrayList<Song>) oneSong);
                storageUtil.storeAudioIndex(0);
                Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
                sendBroadcast(setplaylist);
                StorageUtil storage = new StorageUtil(getApplicationContext());
                storage.storeAudioIndex(0);
                Intent broadcastIntent = new Intent(lyricsActivity.Broadcast_PLAY_NEW_AUDIO);
                sendBroadcast(broadcastIntent);
                isSetDetails = false;
            }
        }));*/


        adapter.notifyDataSetChanged();
        mhandler.post(runnable);
    }

    private void prepareSongList() {
        list.clear();
        List<slideSong> beforeList = new ArrayList<>();
        SortedSet<String> trackNos = new TreeSet<>();
        for (String song : values.keySet()) {
            if (!song.equalsIgnoreCase("image")) {
                HashMap<String, Object> songMap = (HashMap<String, Object>) values.get(song);
                slideSong songwith = new slideSong(song, String.valueOf(songMap.get("Track NO")), String.valueOf(songMap.get("Lyricist")), getImage(String.valueOf(values.get("IMAGE"))));
                beforeList.add(songwith);
                trackNos.add(String.valueOf(songMap.get("Track NO")));

            } else {

                setBackground(String.valueOf(values.get("IMAGE")));
            }

        }

        for (String track : trackNos) {
            for (slideSong song : beforeList) {
                if (song.getTrackNo().equalsIgnoreCase(track)) {
                    list.add(song);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void setBackground(String imageString) {
        try {
            ImageView cover = (ImageView) findViewById(R.id.album_artwork);
            back = (ImageView) findViewById(R.id.smallback);
            songTitle = (TextView) findViewById(R.id.small_song);
            movieTitle = (TextView) findViewById(R.id.small_title);


            Bitmap bitmap = getBitmap(imageString);
            cover.setImageBitmap(bitmap);
            Palette.from(bitmap).maximumColorCount(16).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    // Get the "vibrant" color swatch based on the bitmap

                    if (palette.getDarkVibrantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDarkVibrantSwatch().getRgb());
                        getWindow().setStatusBarColor(palette.getDarkVibrantSwatch().getRgb());
                        toolbar.setTitleTextColor(palette.getDarkVibrantSwatch().getTitleTextColor());
                        fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantSwatch().getRgb()));
                        back.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantSwatch().getRgb()));
                        movieTitle.setTextColor(palette.getDarkVibrantSwatch().getTitleTextColor());
                        songTitle.setTextColor(palette.getDarkVibrantSwatch().getBodyTextColor());
                        //rc.setBackgroundColor(palette.getDarkVibrantSwatch().getTitleTextColor());


                    } else if (palette.getDarkMutedSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDarkMutedSwatch().getRgb());
                        getWindow().setStatusBarColor(palette.getDarkMutedSwatch().getRgb());
                        toolbar.setTitleTextColor(palette.getDarkMutedSwatch().getTitleTextColor());

                        fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkMutedSwatch().getRgb()));
                        back.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkMutedSwatch().getRgb()));
                        movieTitle.setTextColor(palette.getDarkMutedSwatch().getTitleTextColor());
                        songTitle.setTextColor(palette.getDarkMutedSwatch().getBodyTextColor());
                        //rc.setBackgroundColor(palette.getDarkMutedSwatch().getTitleTextColor());

                    } else if (palette.getDominantSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getDominantSwatch().getRgb());
                        getWindow().setStatusBarColor(palette.getDominantSwatch().getRgb());
                        toolbar.setTitleTextColor(palette.getDominantSwatch().getTitleTextColor());

                        fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDominantSwatch().getRgb()));
                        back.setBackgroundTintList(ColorStateList.valueOf(palette.getDominantSwatch().getRgb()));
                        movieTitle.setTextColor(palette.getDominantSwatch().getTitleTextColor());
                        songTitle.setTextColor(palette.getDominantSwatch().getBodyTextColor());
                        //rc.setBackgroundColor(palette.getDominantSwatch().getTitleTextColor());

                    } else if (palette.getMutedSwatch() != null) {
                        toolbar.setBackgroundColor(palette.getMutedSwatch().getRgb());
                        getWindow().setStatusBarColor(palette.getMutedSwatch().getRgb());
                        toolbar.setTitleTextColor(palette.getMutedSwatch().getTitleTextColor());

                        fab.setBackgroundTintList(ColorStateList.valueOf(palette.getMutedSwatch().getRgb()));
                        back.setBackgroundTintList(ColorStateList.valueOf(palette.getMutedSwatch().getRgb()));
                        movieTitle.setTextColor(palette.getMutedSwatch().getTitleTextColor());
                        songTitle.setTextColor(palette.getMutedSwatch().getBodyTextColor());
                        //rc.setBackgroundColor(palette.getMutedSwatch().getTitleTextColor());

                    }


                }
            });
            //setPalettes(blured);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }


    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            //player.setCallbacks(albumSongList.this);
            setDetails();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public Bitmap getBitmap(String imageString) {
        if (imageString.equals(null)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            return bitmap;
        }
        byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmap;
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


	/*@Override
    public void updateUi() {
		if (player != null) {
			if (player.isPlaying()) {
				Log.i("udateUI","called update ui");
				seekbar.setMax(player.getDuration());
				Song song = player.getActiveAudio();
				songtitle.setText(song.getSongTitle());
				movietitle.setText(song.getMovieTitle());
				smallplay.setImageResource(android.R.drawable.ic_media_pause);
				setBackground(song);


			}
		}
	}*/

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                int position = player.getCurrrentDuration();
                seekbar.setProgress(position);
                setDetails();

            }
            mhandler.postDelayed(runnable, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.setCallbacks(null);
        }
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
    protected void onStop() {
        super.onStop();
        Log.i("testing", "am in stop");
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            //player.setCallbacks(null);
        }
    }

    private void setDetails() {
        if (player != null) {
            if (player.isPlaying()) {
                if (!isSetDetails) {
                    seekbar.setMax(player.getDuration());
                    Song song = player.getActiveAudio();
                    songtitle.setText(song.getSongTitle());
                    movietitle.setText(song.getMovieTitle());
                    smallplay.setImageResource(android.R.drawable.ic_media_pause);
                    setBackground(song);
                    isSetDetails = true;


                }


            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.small_toggle: {
                if (player == null) return;
                if (player.isPlaying()) {
                    Intent pause = new Intent(MainActivity.Broadcast_PAUSE);
                    sendBroadcast(pause);
                    smallplay.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    Intent playsong = new Intent(MainActivity.Broadcast_PLAY);
                    sendBroadcast(playsong);
                    smallplay.setImageResource(android.R.drawable.ic_media_pause);
                }
                break;
            }


        }

    }

    public void setBackground(Song song) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AR Rahman").child("Tamil");
        ref.child(String.valueOf(song.getMovieTitle())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> movie = (HashMap<String, Object>) dataSnapshot.getValue();
                Bitmap bitmap = Helper.getBitmap(String.valueOf(movie.get("IMAGE")));

                smallback.setImageBitmap(bitmap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void play(slideSong song) {
        String movieTitle = getIntent().getExtras().getString("Title");
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", passedList);
        bundle.putString("selectedSong", song.getSongName());
        bundle.putString("Title", getIntent().getExtras().getString("Title"));
        //bundle.putString("trackNo",song.getTrackNo());


        ArrayList<Song> oneSong = new ArrayList<Song>();
        for (Song songs : passedList) {
            if (songs.getMovieTitle().equalsIgnoreCase(movieTitle) && songs.getSongTitle().equalsIgnoreCase(song.getSongName())) {
                oneSong.add(songs);

            }
        }
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.storeAudio((ArrayList<Song>) oneSong);
        storageUtil.storeAudioIndex(0);
        Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
        sendBroadcast(setplaylist);
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(0);
        Intent broadcastIntent = new Intent(lyricsActivity.Broadcast_PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);
        isSetDetails = false;
        Toast.makeText(getApplicationContext(), song.getSongName(), Toast.LENGTH_SHORT).show();

/*
        Intent i = new Intent(getContext(), lyricsActivity.class);


        i.putExtras(bundle);
        startActivity(i);*/

    }

    public void playAll(slideSong songwith) {
        String movieTitle = getIntent().getExtras().getString("Title");
        int index = 0;

        for (Song song : passedList) {
            if (song.getMovieTitle().equalsIgnoreCase(movieTitle) && song.getSongTitle().equalsIgnoreCase(songwith.getSongName())) {
                index = passedList.indexOf(song);
            }
        }
        if (!playListSet) {
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            storageUtil.storeAudio((ArrayList<Song>) passedList);
            storageUtil.storeAudioIndex(index);
            Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
            sendBroadcast(setplaylist);
            playListSet = true;
        }
        StorageUtil storage = new StorageUtil(getApplicationContext());
        storage.storeAudioIndex(index);

        Intent broadcastIntent = new Intent(lyricsActivity.Broadcast_PLAY_NEW_AUDIO);
        sendBroadcast(broadcastIntent);


        isSetDetails = false;


    }

    public void addToQueue(slideSong songwith) {
        String movieTitle = getIntent().getExtras().getString("Title");
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());

        ArrayList<Song> list = storageUtil.loadAudio();
        for (Song song : list) {
            if (song.getMovieTitle().equalsIgnoreCase(movieTitle) && song.getSongTitle().equalsIgnoreCase(songwith.getSongName())) {
                Toast.makeText(getApplicationContext(), song.getSongTitle() + " is already exist", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (Song song : passedList) {
            if (song.getMovieTitle().equalsIgnoreCase(movieTitle) && song.getSongTitle().equalsIgnoreCase(songwith.getSongName())) {
                list.add(song);
                StorageUtil newUtil = new StorageUtil(getApplicationContext());
                newUtil.storeAudio((ArrayList<Song>) list);
                Intent setplaylist = new Intent(lyricsActivity.Broadcast_NEW_ALBUM);
                sendBroadcast(setplaylist);
                Toast.makeText(getApplicationContext(), song.getSongTitle() + " is added to queue", Toast.LENGTH_SHORT).show();
                break;

            }
        }


    }

    public void addToFavorite(slideSong songwith){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String movieTitle = getIntent().getExtras().getString("Title");

        if(mainApp.getSp().addToFavorite(movieTitle, songwith.getSongName(), user)){
            Toast.makeText(getApplicationContext(), "Added to Favorite", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "Already Exist", Toast.LENGTH_SHORT).show();
        }

    }
    public void removeFromFavorite(slideSong songwith){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String movieTitle = getIntent().getExtras().getString("Title");
        if(mainApp.getSp().removeFromFavorite(movieTitle, songwith.getSongName(), user)){
            Toast.makeText(getApplicationContext(), "Removed from Favorite", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "Add to Favorites First", Toast.LENGTH_SHORT).show();
        }

    }

}
