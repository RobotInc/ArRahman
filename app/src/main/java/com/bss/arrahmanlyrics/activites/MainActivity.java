package com.bss.arrahmanlyrics.activites;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.albums;
import com.bss.arrahmanlyrics.Fragments.favorites;
import com.bss.arrahmanlyrics.Fragments.songs;
import com.bss.arrahmanlyrics.R;
import com.bss.arrahmanlyrics.models.Album;
import com.bss.arrahmanlyrics.models.Song;
import com.bss.arrahmanlyrics.utils.ArtworkUtils;
import com.bss.arrahmanlyrics.utils.Helper;
import com.bss.arrahmanlyrics.utils.MediaPlayerService;

import com.bss.arrahmanlyrics.utils.StorageUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity
		implements GoogleApiClient.OnConnectionFailedListener, MediaPlayerService.ServiceCallbacks, View.OnClickListener {
	GoogleApiClient mGoogleApiClient;
	private FirebaseAuth mFirebaseAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	ImageView profileImage;
	TextView userName, userEmailId;
	public HashMap<String, Object> values = new HashMap<>();
	DatabaseReference data;
	Toolbar toolbar;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	ProgressDialog dialog;
	public FirebaseUser user;
	//-private AdView mAdView;
	private InterstitialAd mInterstitialAd;
	public MediaPlayerService player;
	public boolean serviceBound = false;
	ImageView smallplay,smallback,eqToggle;
	TextView movietitle, songtitle;
	SeekBar seekbar;
	private Handler mHandler = new Handler();
	Boolean isDetailSet = false;
	public static final String Broadcast_PLAY_NEW_AUDIO = "com.bss.arrahmanlyrics.activites.PlayNewAudio";
	public static final String Broadcast_NEW_ALBUM = "com.bss.arrahmanlyrics.activites.PlayNewAlbum";
	public static final String Broadcast_PLAY = "com.bss.arrahmanlyrics.activites.Play";
	public static final String Broadcast_PAUSE = "com.bss.arrahmanlyrics.activites.Pause";
	public static final String Broadcast_NEXT = "com.bss.arrahmanlyrics.activites.Next";
	public static final String Broadcast_Prev = "com.bss.arrahmanlyrics.activites.Previous";
	public static final String Broadcast_Shuffle = "com.bss.arrahmanlyrics.activites.Shuffle";
	public static final String Broadcast_UnShuffle = "com.bss.arrahmanlyrics.activites.UnShuffle";
	public static final String Broadcast_EQTOGGLE= "com.bss.arrahmanlyrics.activites.eqToggle";
	LinearLayout layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		smallplay = (ImageView) findViewById(R.id.small_toggle);

		smallback = (ImageView) findViewById(R.id.img_bottom_slideone);
		movietitle = (TextView) findViewById(R.id.small_title);
		songtitle = (TextView) findViewById(R.id.small_song);


		smallplay.setOnClickListener(this);


		seekbar = (SeekBar) findViewById(R.id.small_seekbar);


		mFirebaseAuth = FirebaseAuth.getInstance();
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

					}
				} /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		user = mFirebaseAuth.getCurrentUser();

		//	do{
		if (user != null) {
			initUI();
		} else {

			signIn();
		}
		//}while (!loginSuccess);


		MobileAds.initialize(getApplicationContext(),
				"ca-app-pub-2287984365462163~8036573131");

		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-2287984365462163/1279872339");
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				Log.i("Ads", "onAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				Log.i("Ads", "onAdFailedToLoad");
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when the ad is displayed.
				Log.i("Ads", "onAdOpened");
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
				Log.i("Ads", "onAdLeftApplication");
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the interstitial ad is closed.
				Log.i("Ads", "onAdClosed");
			}
		});
		/*mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.build();
		mAdView.loadAd(adRequest);
		mAdView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				// Code to be executed when an ad finishes loading.
				Log.i("Ads", "onAdLoaded");
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				Log.i("Ads", "onAdFailedToLoad");
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when an ad opens an overlay that
				// covers the screen.
				Log.i("Ads", "onAdOpened");
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
				Log.i("Ads", "onAdLeftApplication");
			}

			@Override
			public void onAdClosed() {
				// Code to be executed when when the user is about to return
				// to the app after tapping on an ad.
				Log.i("Ads", "onAdClosed");
			}
		});*/
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
		mHandler.post(runnable);

	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (player != null && player.isPlaying()) {
				int position = player.getCurrrentDuration();
				seekbar.setProgress(position);
				setDetails();

			}
			mHandler.postDelayed(runnable, 1000);
		}
	};


	@Override
	public void onBackPressed() {

		getWindow().closeAllPanels();
		AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(MainActivity.this);

		builder.setTitle("Thank You");
		builder.setMessage("Thank You For Using Our Application Please Give Us Your Suggestions and Feedback ");
		builder.setNegativeButton("RATE US",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.bss.arrahmanlyrics")); // Add package name of your application
						startActivity(intent);
						Toast.makeText(MainActivity.this, "Thank you for your Rating",
								Toast.LENGTH_SHORT).show();
					}
				});
		builder.setPositiveButton("QUIT",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						finish();
					}
				});

		builder.show();
	}


	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		signinTry();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

		if (requestCode == 1) {
			Log.e("test", String.valueOf(requestCode));
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

			handleSignInResult(result);
		} else {
			signinTry();
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {
		Log.d("Sign In", "handleSignInResult:" + result.isSuccess());
		if (result.isSuccess()) {
			// Signed in successfully, show authenticated UI.
			GoogleSignInAccount acct = result.getSignInAccount();
			Log.i("user Name", acct.getDisplayName());


			firebaseAuthWithGoogle(acct);


		} else {


			signinTry();
		}
	}

	private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
		Log.d("Sign in", "firebaseAuthWithGoogle:" + acct.getId());

		final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mFirebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						Log.d("sign in", "signInWithCredential:onComplete:" + task.isSuccessful());
						//String pic = acct.getPhotoUrl().toString();
						//Toast.makeText(getApplicationContext(),pic,Toast.LENGTH_SHORT).show();
						// Picasso.with(getApplicationContext()).load(pic).into(profileImage);
						// If sign in fails, display a message to the user. If sign in succeeds
						// the auth state listener will be notified and logic to handle the
						// signed in user can be handled in the listener.
						if (task.isSuccessful()) {
							user = mFirebaseAuth.getCurrentUser();
							initUI();

						}

						//userEmailId.setText(user.getEmail());
						if (!task.isSuccessful()) {
							Log.w("Sign in", "signInWithCredential", task.getException());
							signinTry();
						}
						// ...
					}
				});
	}

	@Override
	public void updateUi() {
		Log.i("callback","called update ui");
		if (player != null) {
			if (player.isPlaying()) {

				seekbar.setMax(player.getDuration());
				Song song = player.getActiveAudio();
				songtitle.setText(song.getSongTitle());
				movietitle.setText(song.getMovieTitle());
				smallplay.setImageResource(android.R.drawable.ic_media_pause);
				setBackground(song);


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
					Intent pause = new Intent(Broadcast_PAUSE);
					sendBroadcast(pause);
					smallplay.setImageResource(android.R.drawable.ic_media_play);
				} else {
					Intent playsong = new Intent(Broadcast_PLAY);
					sendBroadcast(playsong);
					smallplay.setImageResource(android.R.drawable.ic_media_pause);
				}
				break;
			}


		}

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

	public void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, 1);
	}


	@Override
	public void onStart() {
		super.onStart();
		Log.i("testing", "am in start");
		isDetailSet = false;
		if (!serviceBound) {
			Intent playerIntent = new Intent(this, MediaPlayerService.class);
			startService(playerIntent);
			bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
			Log.i("bounded", "service bounded");


		}


	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("testing", "am in resume");
		isDetailSet = false;


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

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("testing", "am in pause");

	}

	public HashMap<String, Object> getValues() {
		return values;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dialog.dismiss();
		if (serviceBound) {
			unbindService(serviceConnection);
			player.setCallbacks(null);
			//service is active
			//player.stopSelf();

		}

	}

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
			player.setCallbacks(MainActivity.this);
			setDetails();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceBound = false;
		}
	};

	private void setDetails() {
		if (player != null) {
			if (player.isPlaying()) {
				if(!isDetailSet){
					seekbar.setMax(player.getDuration());
					Song song = player.getActiveAudio();
					songtitle.setText(song.getSongTitle());
					movietitle.setText(song.getMovieTitle());
					smallplay.setImageResource(android.R.drawable.ic_media_pause);
					setBackground(song);
					isDetailSet = true;
					Log.i("CalledSet","called set details");

				}


			}
		}
	}


	void initUI() {

		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading Database");
		dialog.show();
		data = FirebaseDatabase.getInstance().getReference().child("AR Rahman").child("Tamil");
		data.keepSynced(true);
		data.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				mViewPager = (ViewPager) findViewById(R.id.container);
				mViewPager.setOffscreenPageLimit(2);

				mSectionsPagerAdapter.addFragment(new albums(), "Albums");
				mSectionsPagerAdapter.addFragment(new songs(), "Songs");
				mSectionsPagerAdapter.addFragment(new favorites(), "Favorite Songs");

				mViewPager.setAdapter(mSectionsPagerAdapter);
				// Set up the ViewPager with the sections adapter.

				TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
				tabLayout.setupWithViewPager(mViewPager);

				dialog.hide();

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


	}

	public void signinTry() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setTitle("Error While Connecting");
		builder.setMessage("oops Looks like network issues make sure your internet connection is on and try again... ");
		builder.setNegativeButton("Quit",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						System.exit(1);
					}
				});
		builder.setPositiveButton("Try again",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
					                    int which) {
						signIn();
					}
				});

		builder.show();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.nav_log:
				mFirebaseAuth.signOut();
				Auth.GoogleSignInApi.signOut(mGoogleApiClient);
				signIn();
				return true;
			/*case R.id.feedback:
				Intent intent = new Intent(getApplicationContext(), feedback.class);
				startActivity(intent);
				return true;*/
			case R.id.about:
				Intent aboutPage = new Intent(getApplicationContext(), about.class);
				startActivity(aboutPage);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public void setBackground(Song song) {
		HashMap<String, Object> movie = (HashMap<String, Object>) values.get(song.getMovieTitle());




			Bitmap bitmap = Helper.getBitmap(String.valueOf(movie.get("IMAGE")));
			smallback.setImageBitmap(bitmap);


	}

	public void setIsDetailSet(Boolean value){
		isDetailSet = value;

	}
}