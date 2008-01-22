package com.mathias.hemoroids;

import java.awt.*;

public class GunShip extends Ship
{
	static final int INITIAL_AMMO = 1500;
	static final int LOW_AMMO = 200;

	int ammo;
	public boolean nogun=true;
	public boolean semigun=false;
	public boolean shotgun=false;
	public boolean snipergun=false;
	public boolean autogun=false;
	NoGun cannon;
	NoGun cannon2;
//	NoGun cannon3;

	GunShip()
	{
		cannon=new NoGun();
		cannon2=new NoGun();
//		cannon3=new NoGun();
	}
	public void init()
	{
		nogun=true;
		semigun=true;
		shotgun=false;
		snipergun=false;
		autogun=false;
		cannon=new SemiGun(-12,-4);
		cannon2=new SemiGun(12,-4);
//		cannon3=new SemiGun(0,-8);
		ammo=INITIAL_AMMO;
		super.init();
	}
	public void update()
	{
		int nshots;
		if(Keys.z && active && ammo>0 && counter==0)
		{
			nshots=cannon.fire();
			nshots+=cannon2.fire();
//			ammo-=cannon3.fire();
			if(ammo>LOW_AMMO && ammo-nshots<=LOW_AMMO)
				Audio.lowammo.play();
			ammo-=nshots;
		}
		super.update();
		cannon.update(this);
		cannon2.update(this);
//		cannon3.update(this);
	}
  public int isColliding(Flying f)
	{
		int dam;
		if((dam=cannon.isColliding(f))!=0)
			return dam;
		if((dam=cannon2.isColliding(f))!=0)
			return dam;
//		if((dam=cannon3.isColliding(f))!=0)
//			return dam;
		return super.isColliding(f);
	}
	public void draw(Graphics g)
	{
		cannon.draw(g);
		cannon2.draw(g);
//		cannon3.draw(g);
		super.draw(g);
	}
	public void keyDown()
	{
		if(!active)
			return;
		//
		super.keyDown();
		//
		if(Keys.n0 && nogun)
		{
			Audio.mount.play();
			cannon=new NoGun();
			cannon2=new NoGun();
//			cannon3=new NoGun();
		}
		//
		if(Keys.n1 && semigun)
		{
			Audio.mount.play();
			cannon=new SemiGun(-12,-4);
			cannon2=new SemiGun(12,-4);
//			cannon3=new SemiGun(0,-8);
		}
		//
		if(Keys.n2 && shotgun)
		{
			Audio.mount.play();
			cannon=new ShotGun(-12,-4);
			cannon2=new ShotGun(12,-4);
//			cannon3=new ShotGun(0,-8);
		}
		//
		if(Keys.n3 && snipergun)
		{
			Audio.mount.play();
			cannon=new SniperGun(-12,-8);
			cannon2=new SniperGun(12,-8);
//			cannon3=new SniperGun(0,-8);
		}
		//
		if(Keys.n4 && autogun)
		{
			Audio.mount.play();
			cannon=new AutoGun(-12,-4);
			cannon2=new AutoGun(12,-4);
//			cannon3=new AutoGun(0,-8);
		}
		//
		if(Keys.x)
		{
			if(cannon.active && cannon2.active)
			{
				Audio.mount.play();
				cannon2.active=false;
			}
			else
			if(cannon.active && !cannon2.active)
			{
				Audio.mount.play();
				cannon.active=false;
				cannon2.active=true;
			}
			else
			if(!cannon.active && cannon2.active)
			{
				Audio.mount.play();
				cannon.active=true;
				cannon2.active=true;
			}
		}
	}
	public void stop()
	{
		cannon.stop();
		cannon2.stop();
//		cannon3.stop();
		super.stop();
	}
	public void addWepond1(NoGun gun)
	{
		cannon=gun;
		cannon.active=true;
	}
	public void explode()
	{
		cannon.explode();
		cannon2.explode();
		super.explode();
	}
}
