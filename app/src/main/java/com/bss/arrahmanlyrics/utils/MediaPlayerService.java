package com.bss.arrahmanlyrics.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.activites.MainActivity;
import com.bss.arrahmanlyrics.activites.MainActivity;

import com.bss.arrahmanlyrics.activites.lyricsActivity;
import com.bss.arrahmanlyrics.mainApp;
import com.bss.arrahmanlyrics.models.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.bss.arrahmanlyrics.R;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Created by mohan on 7/16/17.
 */

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener, CacheListener {
    private int resumePosition;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    boolean shuffleOn = true;
    private TextView currentDur, TotalDur;
    //path to the audio file
    private String mediaFile;
    // Binder given to clients
    Equalizer equalizer;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private final IBinder iBinder = new LocalBinder();
    HashMap<Song, Integer> songCount = new HashMap<>();
    private boolean mLostAudioFocus = false;
    private boolean mIsDucked = false;
    private boolean audioLost = false;
    //List of available Audio files
    private ArrayList<Song> audioList;
    NotificationCompat.Builder notificationBuilder;
    private int audioIndex = -1;
    private Song activeAudio; //an object of the currently playing audio
    public static final String ACTION_PLAY = "com.bss.arrahmanlyrics.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.bss.arrahmanlyrics.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.bss.arrahmanlyrics.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.bss.arrahmanlyrics.ACTION_NEXT";
    public static final String ACTION_STOP = "com.bss.arrahmanlyrics.ACTION_STOP";
    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    public ServiceCallbacks callbacks;
    public mainActivityCallback maincallback;
    private int iVolume;

    private final static int INT_VOLUME_MAX = 100;
    private final static int INT_VOLUME_MIN = 0;
    private final static float FLOAT_VOLUME_MAX = 1;
    private final static float FLOAT_VOLUME_MIN = 0;
    private albumActivityCallback albumcallback;

    public void stopEverything() {
        stopForeground(true);
        stopSelf();

    }

    public interface ServiceCallbacks {
        void updateUi();


    }

    public interface mainActivityCallback {
        void update();


    }

    public interface albumActivityCallback {
        void updateui();


    }

    private void initMediaPlayer() {

        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(setProxyUrl(activeAudio.getUlr()));

        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //stopMedia();
        //stop the service
        //stopSelf();
        skipToNext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        register_setNewalbum();
        register_play();
        register_pause();
        register_next();
        register_prev();
        register_shuffle();
        register_unShuffle();
        register_eqToggle();
        register_addToQueue();
        register_removeFromQueue();
        registerheadsetState();
    }

    //Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("prepare", "prepared");
        if (callbacks != null) {
            Log.i("prepare", "not null");
            callbacks.updateUi();


        }
        if (maincallback != null) {
            Log.i("prepare", "not null");
            maincallback.update();


        }
        if (albumcallback != null) {
            Log.i("prepare", "not null");
            albumcallback.updateui();
        }

        playMedia();

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:

                if (mediaPlayer.isPlaying()) {
                    pauseMedia();

                    //service.stopSelf();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.3f, 0.3f);
                    mIsDucked = true;
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    pauseMedia();
                    mLostAudioFocus = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:

                if (mIsDucked) {
                    float devicevolume = getDeviceVolume();
                    mediaPlayer.setVolume(devicevolume, devicevolume);
                    mIsDucked = false;
                } else if (mLostAudioFocus) {
                    // If we temporarily lost the audio focus we can resume playback here
                    if (mediaPlayer.isPlaying()) {
                        playMedia();
                    }
                    mLostAudioFocus = false;
                }
                break;
            default:
                Log.d("Audio Focus", "Unknown focus");
        }
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private void playMedia() {

        if (!mediaPlayer.isPlaying()) {
            fadeIn(1000);
            equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
            equalizer.setEnabled(true);


            //mediaPlayer.start();

        }


    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {

            mediaPlayer.stop();

        }
    }

    private void pauseMedia() {

        if (mediaPlayer.isPlaying()) {
            fadeOut(1000);

            //mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }


    }

    private void resumeMedia() {

        if (!mediaPlayer.isPlaying()) {
            fadeInResume(1000);

            //mediaPlayer.seekTo(resumePosition);
            //mediaPlayer.start();

        }


    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*try {
            //Load data from SharedPreferences

			StorageUtil storage = new StorageUtil(getApplicationContext());
			audioList = storage.loadAudio();
			audioIndex = storage.loadAudioIndex();

			if (audioIndex != -1 && audioIndex < audioList.size()) {
				//index is in a valid range
				activeAudio = audioList.get(audioIndex);
			} else {
				stopSelf();
			}
		} catch (NullPointerException e) {
			stopSelf();
		}*/

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                //initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            //buildNotification(PlaybackStatus.PAUSED);
        }
        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        return START_STICKY_COMPATIBILITY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        Log.i("musicS", "called");
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }


        Log.i("musicS", "after notification");
        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(setNewAlbum);
        unregisterReceiver(play);
        unregisterReceiver(pause);
        unregisterReceiver(next);
        unregisterReceiver(prev);
        unregisterReceiver(shuffle);
        unregisterReceiver(unShuffle);
        unregisterReceiver(eqToggle);
        unregisterReceiver(addToQueue);
        unregisterReceiver(removeFromQueue);
        unregisterReceiver(headsetListener);


        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }
    private BroadcastReceiver headsetListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) && isPlaying()) {

                pauseMedia();



            }

        }
    };
    public void registerheadsetState() {
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetListener, receiverFilter);
    }
    private BroadcastReceiver setNewAlbum = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            if (shuffleOn) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = getShuffledList(storage.loadAudio());
                audioIndex = storage.loadAudioIndex();
            } else {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = storage.loadAudio();
                audioIndex = storage.loadAudioIndex();
            }
            songCount.clear();
            for (Song song : audioList) {
                songCount.put(song, 0);
            }


        }
    };

    private void register_setNewalbum() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_NEW_ALBUM);
        registerReceiver(setNewAlbum, filter);
    }

    private BroadcastReceiver addToQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            if (shuffleOn) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = getShuffledList(storage.loadAudio());

            } else {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = storage.loadAudio();

            }
            for (Song song : audioList) {
                if (!songCount.containsKey(song))
                    songCount.put(song, 0);
            }


        }
    };

    private void register_addToQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_ADDTOQUEUE);
        registerReceiver(addToQueue, filter);
    }

    private BroadcastReceiver removeFromQueue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            if (shuffleOn) {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = getShuffledList(storage.loadAudio());

            } else {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = storage.loadAudio();

            }
            if (songCount.containsKey(activeAudio)) {
                songCount.remove(activeAudio);
            }


        }
    };

    private void register_removeFromQueue() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_REMOVEFROMQUERE);
        registerReceiver(removeFromQueue, filter);
    }


    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            if (mLostAudioFocus) {
                mLostAudioFocus = false;
            }
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                pauseMedia();
                                ongoingCall = true;
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {

                                ongoingCall = false;
                                resumeMedia();

                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = new StorageUtil(getApplicationContext()).loadAudio().get(audioIndex);

            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            if (mediaPlayer != null) {

                mediaPlayer.reset();
            }

            initMediaPlayer();
            if (songCount.containsKey(activeAudio)) {
                int value = songCount.get(activeAudio);
                songCount.put(activeAudio, value + 1);
            }

            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
            StorageUtil setsong = new StorageUtil(getApplicationContext());

        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        //updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                if (mediaPlayer == null) return;
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
                if (callbacks != null) {
                    callbacks.updateUi();
                }
                if (maincallback != null) {
                    maincallback.update();
                }
                if (albumcallback != null) {
                    Log.i("prepare", "not null");
                    albumcallback.updateui();
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                if (mediaPlayer == null) return;
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                notificationBuilder.setOngoing(false);
                if (callbacks != null) {
                    callbacks.updateUi();
                }
                if (maincallback != null) {
                    maincallback.update();
                }
                if (albumcallback != null) {
                    Log.i("prepare", "not null");
                    albumcallback.updateui();
                }
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                if (mediaPlayer == null) return;
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                if (mediaPlayer == null) return;
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);

            }

            @Override
            public void onStop() {
                super.onStop();
                if (mediaPlayer == null) return;
                removeNotification();
                //Stop the service
                stopSelf();
                if (callbacks != null) {
                    callbacks.updateUi();
                }
                if (maincallback != null) {
                    maincallback.update();
                }
                if (albumcallback != null) {
                    Log.i("prepare", "not null");
                    albumcallback.updateui();
                }
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher); //replace with medias albumArt
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getMovieTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getSongTitle())
                .build());
    }

    private void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else if (audioIndex >= audioList.size()) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
            //get next in playlist


        } else {
            activeAudio = audioList.get(++audioIndex);
        }


        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();


    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();


    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher); //replace with your own image

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a new Notification
        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(Intent.CATEGORY_APP_MUSIC)
                .setPriority(Notification.PRIORITY_DEFAULT)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))

                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentIntent(pendInt)
                .setContentTitle(activeAudio.getMovieTitle())
                .setContentInfo(activeAudio.getSongTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationBuilder.setOngoing(true);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationBuilder.setOngoing(false);
        }
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        //notificationManager.cancelAll();
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private String setProxyUrl(String url) throws IOException {
        HttpProxyCacheServer proxy = mainApp.getProxy(getApplicationContext());
        proxy.registerCacheListener(this, url);
        String proxyUrl = proxy.getProxyUrl(url);
        Log.d("proxy", "Use proxy url " + proxyUrl + " instead of original url " + url);
        return proxyUrl;

    }

    public void stopCacheListener() {
        mainApp.getProxy(getApplicationContext()).unregisterCacheListener(this);
    }

    public int getDuration() {
        if (mediaPlayer == null)
            return 0;
        return mediaPlayer.getDuration();
    }

    public int getCurrrentDuration() {
        if (mediaPlayer == null) {
            return 0;
        }

        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int duration) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(duration);
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        } else if (mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setMainCallbacks(mainActivityCallback callbacks) {
        this.maincallback = callbacks;
    }

    public void setMainCallbacks(albumActivityCallback callbacks) {
        this.albumcallback = callbacks;
    }

    public Song getActiveAudio() {
        return activeAudio;
    }

    private BroadcastReceiver play = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer == null) return;
            resumeMedia();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };

    private void register_play() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY);
        registerReceiver(play, filter);
    }

    private BroadcastReceiver pause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer == null) return;
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);

        }
    };

    private void register_pause() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PAUSE);
        registerReceiver(pause, filter);
    }

    private BroadcastReceiver next = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer == null) return;
            skipToNext();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };

    private void register_next() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_NEXT);
        registerReceiver(next, filter);
    }

    private BroadcastReceiver prev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer == null) return;
            skipToPrevious();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };

    private void register_prev() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_Prev);
        registerReceiver(prev, filter);
    }

    private BroadcastReceiver shuffle = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioList = getShuffledList(new StorageUtil(getApplicationContext()).loadAudio());
            shuffleOn = true;

        }
    };

    private void register_shuffle() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_Shuffle);
        registerReceiver(shuffle, filter);
    }

    private BroadcastReceiver unShuffle = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            audioList = new StorageUtil(getApplicationContext()).loadAudio();
            //Log.i("audioindex", activeAudio.getMovieTitle()+" "+activeAudio.getSongTitle());

            for (Song song : audioList) {
                //Log.i("audioindex", activeAudio.getMovieTitle()+" "+activeAudio.getSongTitle());
                Log.i("audioindex", song.getMovieTitle() + " " + song.getSongTitle());
                if (song.getMovieTitle().equalsIgnoreCase(activeAudio.getMovieTitle()) && song.getSongTitle().equalsIgnoreCase(activeAudio.getSongTitle())) {
                    audioIndex = audioList.indexOf(song);
                    Log.i("audioindex", String.valueOf(audioList.indexOf(song)));
                    break;
                }

            }
            shuffleOn = false;

        }
    };

    private void register_unShuffle() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_UnShuffle);
        registerReceiver(unShuffle, filter);
    }

    private BroadcastReceiver eqToggle = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (equalizer.getEnabled() == true) {
                equalizer.setEnabled(false);

            } else {
                equalizer.setEnabled(true);

            }


        }
    };

    private void register_eqToggle() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_EQTOGGLE);
        registerReceiver(eqToggle, filter);
    }

    private ArrayList<Song> getShuffledList(ArrayList<Song> list) {
        ArrayList<Song> shuffleList = new ArrayList<>();
        if (list != null) {

            Random r = new Random();
            List<Integer> randomsNos = new ArrayList<>();
            for (int a = 0; a < list.size(); a++) {
                int b = 0;
                do {
                    b = r.nextInt(list.size());
                    if (!randomsNos.contains(b)) {
                        randomsNos.add(b);
                    }
                } while (randomsNos.contains(b) && randomsNos.size() != list.size());

            }

            for (int values : randomsNos) {
                shuffleList.add(list.get(values));
            }

            return shuffleList;
        }
        return null;

    }

    public void fadeOut(final int duration) {

        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = duration;
            private float volume = 0.0f;

            @Override
            public void run() {
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                // can call h again after work!
                time -= 100;
                volume = (deviceVolume * time) / duration;
                mediaPlayer.setVolume(volume, volume);
                if (time > 0)
                    h.postDelayed(this, 100);
                else {
                    mediaPlayer.pause();
                    if (callbacks != null) {
                        callbacks.updateUi();
                    }
                    if (maincallback != null) {
                        maincallback.update();
                    }
                    if (albumcallback != null) {
                        Log.i("prepare", "not null");
                        albumcallback.updateui();
                    }

                    //_player.release();
                }
            }
        }, 100); // 1 second delay (takes millis)


    }

    public void fadeIn(final int duration) {
        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {

                mediaPlayer.start();
                if (maincallback != null) {
                    maincallback.update();
                }
                // can call h again after work!
                time += 100;
                volume = (deviceVolume * time) / duration;
                mediaPlayer.setVolume(volume, volume);
                if (time < duration)
                    h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)


    }

    public void fadeInResume(final int duration) {
        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(resumePosition);
                    mediaPlayer.start();
                    if (callbacks != null) {
                        callbacks.updateUi();

                    }
                    if (maincallback != null) {
                        maincallback.update();
                    }
                    if (albumcallback != null) {
                        Log.i("prepare", "not null");
                        albumcallback.updateui();
                    }
                }

                // can call h again after work!
                time += 100;
                volume = (deviceVolume * time) / duration;
                mediaPlayer.setVolume(volume, volume);
                if (time < duration)
                    h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)

    }


    public float getDeviceVolume() {
        int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return (float) volumeLevel / maxVolume;
    }

    public boolean isShuffleOn() {
        return shuffleOn;
    }


}