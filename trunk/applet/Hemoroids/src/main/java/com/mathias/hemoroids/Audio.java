package com.mathias.hemoroids;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public final class Audio {
	// Sound clips.
	public static AudioClip crash;

	public static AudioClip explosion;

	public static AudioClip fire;

	public static AudioClip missile;

	public static AudioClip saucer;

	public static AudioClip thrusters;

	public static AudioClip warp;

	public static AudioClip xlife;

	public static AudioClip lowammo;

	public static AudioClip shop;

	public static AudioClip mount;

	public static AudioClip shield;

	public static AudioClip highscore;

	public static AudioClip shopenter;

	public static AudioClip lock;

	// Flags for looping sound clips.
	public static boolean crashPlaying = false;

	public static boolean explosionPlaying = false;

	public static boolean firePlaying = false;

	public static boolean missilePlaying = false;

	public static boolean saucerPlaying = false;

	public static boolean thrustersPlaying = false;

	public static boolean warpPlaying = false;

	public static boolean xlifePlaying = false;

	public static boolean lowammoPlaying = false;

	public static boolean shopPlaying = false;

	public static boolean mountPlaying = false;

	public static boolean shieldPlaying = false;

	public static boolean highscorePlaying = false;

	public static boolean shopenterPlaying = false;

	public static boolean lockPlaying = false;

	public static void loadSounds(Applet app) {
		// Load all sound clips by playing and immediately stopping them.
		try {
			crash = app
					.getAudioClip(new URL(app.getDocumentBase(), "crash.au"));
			explosion = app.getAudioClip(new URL(app.getDocumentBase(),
					"explosion.au"));
			fire = app.getAudioClip(new URL(app.getDocumentBase(), "fire.au"));
			missile = app.getAudioClip(new URL(app.getDocumentBase(),
					"missile.au"));
			saucer = app.getAudioClip(new URL(app.getDocumentBase(),
					"saucer.au"));
			thrusters = app.getAudioClip(new URL(app.getDocumentBase(),
					"thrusters.au"));
			warp = app.getAudioClip(new URL(app.getDocumentBase(), "warp.au"));
			xlife = app
					.getAudioClip(new URL(app.getDocumentBase(), "xlife.au"));
			lowammo = app.getAudioClip(new URL(app.getDocumentBase(),
					"lowammo.au"));
			shop = app.getAudioClip(new URL(app.getDocumentBase(), "shop.au"));
			mount = app
					.getAudioClip(new URL(app.getDocumentBase(), "mount.au"));
			shield = app.getAudioClip(new URL(app.getDocumentBase(),
					"shield.au"));
			highscore = app.getAudioClip(new URL(app.getDocumentBase(),
					"highscore.au"));
			shopenter = app.getAudioClip(new URL(app.getDocumentBase(),
					"shopenter.au"));
			lock = app.getAudioClip(new URL(app.getDocumentBase(), "lock.au"));
		} catch (MalformedURLException e) {
		}

		crash.play();
		crash.stop();
		explosion.play();
		explosion.stop();
		fire.play();
		fire.stop();
		missile.play();
		missile.stop();
		saucer.play();
		saucer.stop();
		thrusters.play();
		thrusters.stop();
		warp.play();
		warp.stop();
		xlife.play();
		xlife.stop();
		lowammo.play();
		lowammo.stop();
		shop.play();
		shop.stop();
		mount.play();
		mount.stop();
		shield.play();
		shield.stop();
		highscore.play();
		highscore.stop();
		shopenter.play();
		shopenter.stop();
		lock.play();
		lock.stop();
	}

	public static void pause(boolean paused) {
		if (paused) {
			if (missilePlaying)
				missile.loop();
			if (saucerPlaying)
				saucer.loop();
			if (thrustersPlaying)
				thrusters.loop();
		} else {
			if (missilePlaying)
				missile.stop();
			if (saucerPlaying)
				saucer.stop();
			if (thrustersPlaying)
				thrusters.stop();
		}
	}
}
