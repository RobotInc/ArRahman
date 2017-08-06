package com.bss.arrahmanlyrics.activites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;

import com.bss.arrahmanlyrics.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by mohan on 7/13/17.
 */

public class about extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		String userEmail = user.getEmail();
		String des = "We all Know how magical AR Rahman's tunes are! And it's pleasure to sing along with the help of lyrics. So we made this musical app as a tribute to AR Rahman. Explore and feel the Music";
		Element versionElement = new Element();
		versionElement.setTitle("Version 1.2");
		Element telegram = new Element();
		telegram.setTitle("Chat with us in Telegram");
		telegram.setValue("https://t.me/beyonity");
		telegram.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://t.me/beyonity";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		Element Facebook = new Element();
		Facebook.setTitle("Like us on Facebook");
		Facebook.setValue("https://t.me/beyonity");
		Facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "https://www.facebook.com/beyonityss";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		Facebook.setIconDrawable(R.drawable.about_icon_facebook);
		Element copyRights = new Element();
		copyRights.setTitle("© BSS 2017");
		telegram.setIconDrawable(R.drawable.telegram);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("About AR Rahman Lyrics");
		Element userelement = new Element();
		userelement.setTitle(userEmail);
		userelement.setGravity(Gravity.CENTER);
		View aboutPage = new AboutPage(getApplicationContext())
				.isRTL(false)
				.setImage(R.mipmap.ic_launcher)
				.setDescription(des)
				.addItem(versionElement)
				.addGroup("You can easily Reach Us by")
				.addItem(Facebook)
				.addItem(telegram)
				.addItem(copyRights)
				.addGroup("Logged In User")
				.addItem(userelement)
				.create();


		setContentView(aboutPage);
	}
}
