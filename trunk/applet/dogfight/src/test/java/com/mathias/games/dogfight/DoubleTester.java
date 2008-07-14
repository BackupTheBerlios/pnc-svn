package com.mathias.games.dogfight;

import java.text.DecimalFormat;

public class DoubleTester {

	static final String ZEROES = "000000000000";
	static final String BLANKS = "            ";

	static String format(double val, int n, int w) {
		// rounding
		double incr = 0.5;
		for (int j = n; j > 0; j--)
			incr /= 10;
		val += incr;

		String s = Double.toString(val);
		int n1 = s.indexOf('.');
		int n2 = s.length() - n1 - 1;

		if (n > n2)
			s = s + ZEROES.substring(0, n - n2);
		else if (n2 > n)
			s = s.substring(0, n1 + n + 1);

		if (w > 0 & w > s.length())
			s = BLANKS.substring(0, w - s.length()) + s;
		else if (w < 0 & (-w) > s.length()) {
			w = -w;
			s = s + BLANKS.substring(0, w - s.length());
		}
		return s;
	}

	public static void main(String[] args) {
		double angle = -2.750000000432;
		DecimalFormat fmt = new DecimalFormat("####.00");
//		fmt.setMaximumFractionDigits(4);
		System.out.println(angle);
		System.out.println(String.format("%f", angle));
		System.out.println(fmt.format(angle));
		System.out.println(format(angle, 4, 4));
	}

}
