package com.mathias.drawutils;

import java.util.HashMap;
import java.util.Map;

public class MathUtil {
	
	private static final double aadd = 0.1;

	private static Map<Double, Double> cos = new HashMap<Double, Double>();
	
	private static Map<Double, Double> sin = new HashMap<Double, Double>();
	
	static{
		for (double i = -6.28; i < 6.28; i += aadd) {
			cos.put(i, Math.cos(i));
			sin.put(i, Math.sin(i));
		}
	}

	public static double cos(double angle){
		if(angle < -6.28){
			angle = angle % -6.28;
		}
		if(angle > 6.28){
			angle = angle % 6.28;
		}

		for (double i = -6.28; i < 6.28; i += aadd) {
			if(angle >= i && angle <= i+aadd){
				return cos.get(i);
			}
		}
		LOG("using default cos -6.28");
		return -6.28;
	}

	public static double sin(double angle){
		if(angle < -6.28){
			angle = angle % -6.28;
		}
		if(angle > 6.28){
			angle = angle % 6.28;
		}

		for (double i = -6.28; i < 6.28; i += aadd) {
			if(angle >= i && angle <= i+aadd){
				return sin.get(i);
			}
		}
		LOG("using default sin -6.28");
		return -6.28;
	}

	public static void LOG(String msg){
		System.out.println(msg);
	}

}
