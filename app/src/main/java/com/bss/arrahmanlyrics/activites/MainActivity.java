package com.bss.arrahmanlyrics.activites;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bss.arrahmanlyrics.Fragments.albums;
import com.bss.arrahmanlyrics.Fragments.favorites;
import com.bss.arrahmanlyrics.Fragments.songs;
import com.bss.arrahmanlyrics.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
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
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity
		implements GoogleApiClient.OnConnectionFailedListener {
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

	private InterstitialAd mInterstitialAd;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);


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

	}


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
						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=ADD YOUR APPS PACKAGE NAME")); // Add package name of your application
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
						if(task.isSuccessful()){
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


	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public HashMap<String, Object> getValues() {
		return values;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dialog.dismiss();
	}

	void initUI() {

		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading Database");
		dialog.show();
		data = FirebaseDatabase.getInstance().getReference();
		data.child("AR Rahman").child("Tamil").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				values = (HashMap<String, Object>) dataSnapshot.getValue();
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
				mViewPager = (ViewPager) findViewById(R.id.container);
				mViewPager.setOffscreenPageLimit(2);
				mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
						if (position == 2) {
							if (mInterstitialAd.isLoaded()) {
								mInterstitialAd.show();
							} else {
								Log.d("TAG", "The interstitial wasn't loaded yet.");
							}
						}
					}

					@Override
					public void onPageSelected(int position) {

					}

					@Override
					public void onPageScrollStateChanged(int state) {

					}
				});
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
	public void signinTry(){
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
				Intent aboutPage = new Intent(getApplicationContext(),about.class);
				startActivity(aboutPage);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu,menu);
		return true;
	}
}