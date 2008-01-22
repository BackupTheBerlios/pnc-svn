package com.mathias.luminies;

public class ScoreCalculator {

	public static void main(String[] args) {
		for(double i = 1; i < 20; i++){
			int score = Double.valueOf(i*25*(i/4)).intValue();
			System.out.println("Score for "+i+": "+score);
		}
	}
}
