package com.mathias.hemoroids;

import java.awt.*;

public class SpaceShop extends Flying
{
	static final int SEMIGUN_PRICE = 1000;
	static final int SHOTGUN_PRICE = 4000;
	static final int SNIPERGUN_PRICE = 8000;
	static final int AUTOGUN_PRICE = 15000;
	static final int PHOTON_PRICE = 500; //100 st
	static final int HYPERJUMP_PRICE = 200;
	static final int SHIELD_PRICE = 100;
	static final int MISSILE_PRICE = 100;
	static final int XLIFE_PRICE = 80000;

	public boolean shopping;
	private int money;

	SpaceShop()
	{
		shopping=false;
		cr=255;
		cg=0;
		cb=0;
		deltaX=Math.random()*6-3;
		deltaY=Math.random()*6-3;
		shape.addPoint(50,-10);
		shape.addPoint(15,-30);
		shape.addPoint(-20,-10);
		shape.addPoint(-10,-10);
		shape.addPoint(-10,30);
		shape.addPoint(40,30);
		shape.addPoint(40,-10);
		buildSprite();
		active=false;
	}
	public int keyDown(MissileShip skepp)
	{
		if(shopping)
		{
			if(Keys.n0)
			{
				Audio.shop.play();
				shopping=false;
				active=false;
			}
			if(Keys.n1)
			{
				if(!skepp.semigun)
					if(pay(SEMIGUN_PRICE))
					{
						Audio.shop.play();
						skepp.semigun=true;
						return SEMIGUN_PRICE;
					}
			}
			if(Keys.n2)
			{
				if(!skepp.shotgun)
					if(pay(SHOTGUN_PRICE))
					{
						Audio.shop.play();
						skepp.shotgun=true;
						return SHOTGUN_PRICE;
					}
			}
			if(Keys.n3)
			{
				if(!skepp.snipergun)
					if(pay(SNIPERGUN_PRICE))
					{
						Audio.shop.play();
						skepp.snipergun=true;
						return SNIPERGUN_PRICE;
					}
			}
			if(Keys.n4)
			{
				if(!skepp.autogun)
					if(pay(AUTOGUN_PRICE))
					{
						Audio.shop.play();
						skepp.autogun=true;
						return AUTOGUN_PRICE;
					}
			}
			if(Keys.n5)
			{
				if(pay(PHOTON_PRICE))
				{
					Audio.shop.play();
					skepp.ammo+=100;
					return PHOTON_PRICE;
				}
			}
			if(Keys.n6)
			{
				if(pay(HYPERJUMP_PRICE))
				{
					Audio.shop.play();
					skepp.hyperjumps++;
					return HYPERJUMP_PRICE;
				}
			}
			if(Keys.n7)
			{
				if(pay(SHIELD_PRICE))
				{
					Audio.shop.play();
					skepp.shields++;
					return SHIELD_PRICE;
				}
			}
			if(Keys.n8)
			{
				if(pay(MISSILE_PRICE))
				{
					Audio.shop.play();
					skepp.missiles++;
					return MISSILE_PRICE;
				}
			}
			if(Keys.n9)
			{
				if(pay(XLIFE_PRICE))
				{
					Audio.shop.play();
					Objectroids.ships++;
					return XLIFE_PRICE;
				}
			}
		}
		return 0;
	}
	public void draw(Graphics g)
	{
		int x,y;
		if(!shopping)
		{
			super.draw(g);
			if(active)
			{
				g.setColor(Color.blue);
				g.drawString("Space",(int)(currentX+width/2),(int)(currentY+height/2));
				g.drawString("Shop",(int)(currentX+width/2),(int)(currentY+height/2+20));
			}
		}
		else
		{
			x=20;
			y=20;
			g.setColor(Color.green);
			g.drawString("Guns",x,y);y+=20;
			setBuyColor(SEMIGUN_PRICE,g);
			g.drawString("(1) SemiAutomaticPhotonBlaster $ "+Integer.toString(SEMIGUN_PRICE),x,y);y+=20;
			setBuyColor(SHOTGUN_PRICE,g);
			g.drawString("(2) ShotGunPhotonBlaster $ "+Integer.toString(SHOTGUN_PRICE),x,y);y+=20;
			setBuyColor(SNIPERGUN_PRICE,g);
			g.drawString("(3) SniperPhotonBlaster $ "+Integer.toString(SNIPERGUN_PRICE),x,y);y+=20;
			setBuyColor(AUTOGUN_PRICE,g);
			g.drawString("(4) FullyAutomaticPhotonBlaster $ "+Integer.toString(AUTOGUN_PRICE),x,y);y+=20;y+=20;
			g.setColor(Color.green);
			g.drawString("Ammo",x,y);y+=20;
			setBuyColor(PHOTON_PRICE,g);
			g.drawString("(5) PhotonsMag [100] $ "+Integer.toString(PHOTON_PRICE),x,y);y+=20;y+=20;
			g.setColor(Color.green);
			g.drawString("Misc",x,y);y+=20;
			setBuyColor(HYPERJUMP_PRICE,g);
			g.drawString("(6) HyperJump $ "+Integer.toString(HYPERJUMP_PRICE),x,y);y+=20;
			setBuyColor(SHIELD_PRICE,g);
			g.drawString("(7) Shield $ "+Integer.toString(SHIELD_PRICE),x,y);y+=20;
			setBuyColor(MISSILE_PRICE,g);
			g.drawString("(8) Missile [1] $ "+Integer.toString(MISSILE_PRICE),x,y);y+=20;
			setBuyColor(XLIFE_PRICE,g);
			g.drawString("(9) X-tra life $ "+Integer.toString(XLIFE_PRICE),x,y);y+=20;y+=20;
			g.setColor(Color.green);
			g.drawString("(0) Exit SpaceShop!",x,y);y+=20;
		}
	}
	public void shop(int m)
	{
		money=m;
		shopping=true;
	}
	private boolean pay(int price)
	{
		if(money>=price)
		{
			money-=price;
			return true;
		}
		return false;
	}
	public void setBuyColor(int price,Graphics g)
	{
			if(money>=price)
				g.setColor(Color.green);
			else
				g.setColor(Color.red);
	}
}
