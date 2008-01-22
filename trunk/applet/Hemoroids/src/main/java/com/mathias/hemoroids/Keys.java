package com.mathias.hemoroids;
import java.awt.*;

public final class Keys
{
	public static boolean ctrl =false;
	public static boolean left =false;
	public static boolean right=false;
	public static boolean up   =false;
	public static boolean down =false;
	public static boolean bksp =false;
	public static boolean tab  =false;
	public static boolean enter=false;
	public static boolean esc  =false;
	public static boolean space=false;
	public static boolean h  =false;
	public static boolean p  =false;
	public static boolean m  =false;
	public static boolean d  =false;
	public static boolean s  =false;
	public static boolean t  =false;
	public static boolean x  =false;
	public static boolean z  =false;
	public static boolean n0 =false;
	public static boolean n1 =false;
	public static boolean n2 =false;
	public static boolean n3 =false;
	public static boolean n4 =false;
	public static boolean n5 =false;
	public static boolean n6 =false;
	public static boolean n7 =false;
	public static boolean n8 =false;
	public static boolean n9 =false;

	public static void updateDown(int key)
  {
		switch(key)
		{
			case Event.LEFT: left=true;break;
			case Event.RIGHT: right=true;break;
			case Event.DOWN: down=true;break;
			case Event.UP: up=true;break;
      case 8: bksp=true;break;
      case 9: tab=true;break;
      case 10: enter=true;break;
      case 27: esc=true;break;
      case 32: space=true;break;
      case 104: h=true;break;
      case 112: p=true;break;
      case 109: m=true;break;
      case 100: d=true;break;
      case 115: s=true;break;
			case 116: t=true;break;
      case 120: x=true;break;
      case 122: z=true;break;
      case 48: n0=true;break;
      case 49: n1=true;break;
      case 50: n2=true;break;
      case 51: n3=true;break;
      case 52: n4=true;break;
      case 53: n5=true;break;
      case 54: n6=true;break;
      case 55: n7=true;break;
      case 56: n8=true;break;
      case 57: n9=true;break;
		}
  }
	public static void updateUp(int key)
  {
		switch(key)
		{
			case Event.LEFT: left=false;break;
			case Event.RIGHT: right=false;break;
			case Event.DOWN: down=false;break;
			case Event.UP: up=false;break;
      case 8: bksp=false;break;
      case 9: tab=false;break;
      case 10: enter=false;break;
      case 27: esc=false;break;
      case 32: space=false;break;
      case 104: h=false;break;
      case 112: p=false;break;
      case 109: m=false;break;
      case 100: d=false;break;
      case 115: s=false;break;
			case 116: t=false;break;
      case 120: x=false;break;
      case 122: z=false;break;
      case 48: n0=false;break;
      case 49: n1=false;break;
      case 50: n2=false;break;
      case 51: n3=false;break;
      case 52: n4=false;break;
      case 53: n5=false;break;
      case 54: n6=false;break;
      case 55: n7=false;break;
      case 56: n8=false;break;
      case 57: n9=false;break;
		}
  }
}

