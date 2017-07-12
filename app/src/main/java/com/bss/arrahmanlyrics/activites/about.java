package com.bss.arrahmanlyrics.activites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bss.arrahmanlyrics.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by mohan on 7/13/17.
 */

public class about extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Element versionElement = new Element();
		versionElement.setTitle("Version 1.01");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("About AR Rahman Lyrics");
		View aboutPage = new AboutPage(getApplicationContext())
				.isRTL(false)
				.setImage(R.mipmap.ic_launcher)
				.addItem(versionElement)
				.addGroup("Connect with us")
				.addEmail("elmehdi.sakout@gmail.com")
				.addWebsite("http://medyo.github.io/")
				.addFacebook("the.medy")
				.addTwitter("medyo80")
				.addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
				.addPlayStore("com.ideashower.readitlater.pro")
				.addGitHub("medyo")
				.addInstagram("medyo80")

				.create();
		setContentView(aboutPage);
	}
}
