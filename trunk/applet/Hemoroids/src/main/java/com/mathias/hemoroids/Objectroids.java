package com.mathias.hemoroids;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;

public class Objectroids extends Applet implements Runnable
{
  // Constants
  static final int DELAY = 50;             // Milliseconds between screen updates.

  static final int MIN_ROCKS =  0;          // asteroids and explosions.
  static final int MIN_ROCK_SPEED = 4;
  static final int MAX_ROCK_SPEED = 20;

  static final int BIG_POINTS    =  25;     // Points for shooting different objects.
  static final int SMALL_POINTS  =  50;
  static final int UFO_POINTS    = 2500;
  static final int MISSLE_POINTS = 500;

  static final int NEW_SHIP_POINTS = 25000;  // Number of points needed to earn a new ship.
  static final int NEW_UFO_POINTS  = 2750;  // Number of points between flying saucers.
	static final int LEVEL_COUNTDOWN = 50;
	static final int TIME_BONUS = 2000;
	static final int SHIPS_START = 0;
//	static final int CODE = 19;//+12345

  static boolean playing = false;
  static boolean paused = false;
  static boolean gothighscore = false;
  static boolean xlife = false;

  // Values for screen
  static int width;
  static int height;
  // Values for the offscreen image.
  Image offImage;
  Graphics offGraphics;

  // Thread control variables.
  Thread loopThread;

  // Sprite objects.
  MissileShip skepp;
	Asteroids aster;
	Ufo saucer;
	Stars strs;
	SpaceShop spshop;
	Target targ;

	int time;
	static int ships;
	int score;
	int highscore;
	int highscorecode;
	int highscorecolor;
	int counter;
	int level;

  public void init()
  {
		int i;
		//sounds
		Audio.loadSounds(this);
    //get size of screen
    Dimension d = new Dimension(950, 550); // size();
//    Graphics g = getGraphics(); // Find the size of the screen and set the values for sprites.
    width = d.width;
    height = d.height;
    Flying.width = d.width;
    Flying.height = d.height;
    // Create the offscreen graphics context, if no good one exists.
    offImage = createImage(width, height);
    offGraphics = offImage.getGraphics();
		//objects
		strs=new Stars(d.width,d.height);
		spshop=new SpaceShop();
		skepp=new MissileShip();
		aster=new Asteroids();
		saucer=new Ufo();
		targ=new Target();

		highscore=0;
		endGame();
  }
  public void initGame()
  {
		level=0;
		counter=LEVEL_COUNTDOWN;
		time=LEVEL_COUNTDOWN+1;
		score=0;
		ships=SHIPS_START;
		Stone.Speed=MIN_ROCK_SPEED;
		aster.Stones=MIN_ROCKS;
    playing=true;
    skepp.init();
		aster.stop();
		gothighscore=false;
  }
	public void nextLevel()
	{
		targ.stop();
		counter=LEVEL_COUNTDOWN;
		level++;
		score+=time;
		time+=TIME_BONUS;
		aster.stop();
		if(Stone.Speed>MAX_ROCK_SPEED)
			Stone.Speed+=2;
		else
			aster.Stones++;
		aster.init();
    playing=true;
	}
  public void endGame()
  {
    playing = false;
    skepp.stop();
		aster.stop();
		Stone.Speed=8;
		aster.init();
  }
  public void start()
  {
    if(loopThread == null)
    {
      loopThread = new Thread(this);
      loopThread.start();
    }
  }
  public void stop()
  {
    if(loopThread != null)
    {
      loopThread.stop();
      loopThread = null;
    }
  }
  public void run()
  {
    long startTime = System.currentTimeMillis();
    while (true)
    {
			if(!spshop.shopping && !paused)
			{
				if(playing)
					time--;
				//random shit
				if(time%700==0 && level>1)
				{
					Audio.shopenter.play();
					spshop.fadein();
				}
				if(time%300==0)
					spshop.fadeout();
				if(time%1200==0 && level>2)
					saucer.init(skepp);
//				if(time%500==0)
//					saucer.stop();
				//update
				spshop.update();
				strs.update();
				skepp.update();
				aster.update();
				saucer.update(level-2);
				targ.update();
				if(!aster.busy()&&playing)
				{
					if(--counter<=0)
						nextLevel();
				}
				if(spshop.isColliding(skepp)!=0)
				{
					spshop.shop(score);
					skepp.deltaX=0;
					skepp.deltaY=0;
				}
				if(skepp.isColliding(saucer)!=0)
				{
					score += UFO_POINTS;
					saucer.stop();
				}
				aster.isColliding(skepp.shield);
				saucer.isColliding(skepp.shield);
				if(aster.isColliding(skepp))
				{
					score+=(int)(Math.random()*100);
					if(score>highscore)
					{
						Audio.highscore.play();
						highscore=score;
						gothighscore=true;
					}
					if(score>=NEW_SHIP_POINTS && !xlife)
					{
						Audio.xlife.play();
						ships++;
						xlife=true;
					}
				}
			}
			//draw objects
      repaint();
			//sleep
      try {
        startTime += DELAY;
        Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
      }
      catch (InterruptedException e) {break;}
    }
  }
  public boolean keyDown(Event e, int key)
  {
		//update keys
    Keys.updateDown(key);
		//update objects
		if(key=='m' && targ.active)
			skepp.matchSpeed(targ.currentF);
		if(key=='t')
		{
			if(aster.index==0 && saucer.active && targ.currentF!=saucer)
				targ.lock(saucer);
			else
				targ.lock(aster.getNext());
		}
		if(key=='h')
			if(saucer.active)
				targ.lock(saucer);
		if(!spshop.shopping)
			skepp.keyDown(targ.currentF);
		score-=spshop.keyDown(skepp);
    //start the game, if not already in progress.
    if(Keys.enter && !playing)
      initGame();
		//pause
    if(key=='p')
		{
      paused=!paused;
			Audio.pause(paused);
		}
		//
		if(Keys.enter && skepp.dead)
			if(ships<=0)
			{
				if(gothighscore)
					highscorecode=score * 19 + 12345;
				initGame();
			}
			else
			{
				ships--;
				skepp.init();
			}
    return true;
  }
  public boolean keyUp(Event e, int key)
  {
    Keys.updateUp(key);
		skepp.keyUp();
    return true;
  }
  public void paint(Graphics g)
  {
    update(g);
  }
  public void update(Graphics g)
  {
		int i;
    // Fill in background
    offGraphics.setColor(Color.black);
    offGraphics.fillRect(0, 0, width, height);
    //stars.
		strs.draw(offGraphics);
		//draw sprites
		skepp.draw(offGraphics);
		aster.draw(offGraphics);
		saucer.draw(offGraphics);
		targ.draw(offGraphics);
		//info
		spshop.draw(offGraphics);
		drawGameInfo(offGraphics);
    // Copy the off screen buffer to the screen.
    g.drawImage(offImage, 0, 0, this);
  }
	public void drawGameInfo(Graphics offGraphics)
	{
		int row=100;
		int c=255*counter/LEVEL_COUNTDOWN;
		if(counter<LEVEL_COUNTDOWN && counter>0)
		{
			offGraphics.setColor(new Color(c,c,c));
	    offGraphics.drawString("Level "+(level+1), width/2-20, height/2-30);
		}
		offGraphics.setColor(Color.yellow);
		if(paused)
			offGraphics.drawString("Game paused!", width/2-20, height/2);
		if(skepp.counter>0 && !skepp.shield.active)
			offGraphics.drawString("Initializing...", width/2-20, height/2+60);
    if(!playing)
		{
      offGraphics.drawString(" H E M O R O I D S", width/2-30, height/2);
      offGraphics.drawString("press enter to Start", width/2-30, height/2+40);
    }
		else
			if(skepp.dead)
				if(ships>0)
					offGraphics.drawString("press enter for new ship", width/2, height/2);
				else
				{
					offGraphics.drawString("Game Over!", width/2, height/2);
					offGraphics.drawString("press enter to start a new game...", width/2-20, height/2+20);
					offGraphics.drawString("Code: "+(score * 19 + 12345), width/2-20, height/2+40);
				}

    offGraphics.drawString("Lives:      " + ships, width-100, row);row+=30;
    offGraphics.drawString("Ammo:       " + skepp.ammo, width-100, row);row+=30;
    offGraphics.drawString("Missiles:   " + skepp.missiles, width-100, row);row+=30;
    offGraphics.drawString("Shilds:     " + skepp.shields, width-100, row);row+=30;
    offGraphics.drawString("Hyperjumps: " + skepp.hyperjumps, width-100, row);row+=30;
    offGraphics.drawString("Level:      " + level, width-100, row);row+=30;
    offGraphics.drawString("Score:      " + score, width-100, row);row+=30;
    offGraphics.drawString("Time:       " + time, width-100, row);row+=30;
		if(gothighscore)
		{
			//offGraphics.setColor(new Color(highscorecolor,highscorecolor+255/3,highscorecolor+255*2/3));
			//highscorecolor+=3;
		}
    offGraphics.drawString("Highscore:  " + highscore, width-100, row);row+=30;
		if(!gothighscore)
			offGraphics.drawString("Code:       " + highscorecode, width-100, row);row+=30;
	}
}

