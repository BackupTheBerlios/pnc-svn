package com.mathias.drawutils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

public abstract class Util {

	public static boolean isEmpty(Object s) {
		return (s == null || s.toString().length() == 0);
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static boolean isBlank(String s) {
		return (s == null || s.trim().length() == 0);
	}

	public static String join(String[] sa, char delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sa.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}
			if (sa[i] != null) {
				sb.append(sa[i]);
			}
		}
		return sb.toString();
	}

	public static String join(List<String> sa, char delimiter) {
		return join(sa.toArray(new String[sa.size()]), delimiter);
	}

	public static String[] split(String s, char delimiter) {
		List<String> list = splitToList(s, delimiter);
		return list.toArray(new String[list.size()]);
	}

	public static List<String> splitToList(String s, char delimiter) {
		ArrayList<String> tokens = new ArrayList<String>();

		if (isEmpty(s)) {
			return tokens;
		}

		int start = 0;
		int end = s.indexOf(delimiter);

		while (true) {
			if (end == -1) {
				tokens.add(s.substring(start));
				break;
			}
			tokens.add(s.substring(start, end));
			start = end + 1;
			end = s.indexOf(delimiter, start);
		}

		return tokens;
	}

	public static void centerFrame(JFrame frame) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - frame.getWidth()) / 2;
		int y = (screen.height - frame.getHeight()) / 2;
		frame.setLocation(x, y);
	}

	public static GridBagConstraints getGBC(int x, int y, boolean fill, Insets insets) {
		return getGBC(x, y, 1, 1, fill, insets);
	}

	public static GridBagConstraints getGBC(int x, int y, int w, int h, boolean fill, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.anchor = GridBagConstraints.WEST;

		if (fill) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
		}

		if (insets != null) {
			gbc.insets = insets;
		}

		return gbc;
	}

	/**
	 * log all levels with the console handler for the logger
	 * @param name
	 */
	public static void addConsoleHandler(String name){
		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(Level.ALL);
		Logger logger = Logger.getLogger(name);
		logger.addHandler(ch);
		logger.setUseParentHandlers(false);
	}
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public static Dimension center(Dimension appSize){
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension(ss.width/2-appSize.width/2, ss.height/2-appSize.height/2);
	}

	public static Font getFallbackFont(String name, int size) {
		Font font = null;
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			if(f.getName().equalsIgnoreCase(name)){
				font = f.deriveFont(Font.PLAIN, size);
				break;
			}
		}
		if(font == null){
			font = fonts[0].deriveFont(Font.PLAIN, size);
		}
		return font;
	}

	public static void drawString(Graphics2D g, Color color, Font font, String string, float x, float y){
		FontRenderContext frc = g.getFontRenderContext();
		g.setColor(color);
		TextLayout layout = new TextLayout(string, font, frc);
		layout.draw(g, x, y);
	}

	/**
	 * @param data
	 * @return checksum of data in hex (length: ?)
	 */
	public static String md5(String data) {
		return toHex(alg("MD5", data));
	}

	/**
	 * @param data
	 * @return checksum of data in hex (length: ?)
	 */
	public static String sha1(String data) {
		return toHex(alg("SHA1", data));
	}

	/**
	 * @param data
	 * @return checksum of data in hex (length: ?)
	 */
	public static byte[] alg(String alg, String data) {
		if (isEmpty(alg) || isEmpty(data)) {
			return null;
		}
		try {
			MessageDigest algorithm = MessageDigest.getInstance(alg);
			algorithm.reset();
			algorithm.update(data.getBytes());
			return algorithm.digest();
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("NoSuchAlgorithmException: "+alg);
		}
	}

	/**
	 * Converts a byte into it's hexadecimal representation.
	 * 
	 * @param b
	 *          the byte value.
	 * @return a string containing the hexadecimal representation of the byte
	 *         value.
	 */
	public static String toHex(byte b) {
		return toHex(new byte[] { b }, 0, 1);
	}

	/**
	 * Converts a short into it's hexadecimal representation.
	 * 
	 * @param s
	 *          the short value.
	 * @return a string containing the hexadecimal representation of the short
	 *         value.
	 */
	public static String toHex(short s) {
		byte[] data = new byte[2];
		data[0] = (byte)((s >>> 8) & 0xFF);
		data[1] = (byte)((s >>> 0) & 0xFF);

		return toHex(data, 0, 2);
	}

	/**
	 * Converts an int into it's hexadecimal representation.
	 * 
	 * @param i
	 *          the int value.
	 * @return a string containing the hexadecimal representation of the int
	 *         value.
	 */
	public static String toHex(int i) {
		byte[] data = new byte[4];
		data[0] = (byte)((i >>> 24) & 0xFF);
		data[1] = (byte)((i >>> 16) & 0xFF);
		data[2] = (byte)((i >>> 8) & 0xFF);
		data[3] = (byte)((i >>> 0) & 0xFF);

		return toHex(data, 0, 4);
	}

	/**
	 * Converts a number of bytes into their hexadecimal representation.
	 * 
	 * @param b
	 *          an array of bytes.
	 * @return a string containing the hexadecimal representation of the provided
	 *         bytes.
	 */
	public static String toHex(byte[] b) {
		return toHex(b, 0, b.length);
	}

	/**
	 * Converts a number of bytes into their hexadecimal representation.
	 * 
	 * @param b
	 *          the source array.
	 * @param offset
	 *          starting position in the source array.
	 * @param length
	 *          the number bytes to convert.
	 * @return a string containing the hexadecimal representation of the provided
	 *         bytes.
	 * @exception ArrayIndexOutOfBoundsException
	 *              if conversion would cause access of data outside array bounds.
	 * @exception IllegalArgumentException
	 *              if <code>b</code> is <code>null</code> or
	 *              <code>length</code> is less than zero.
	 */
	public static String toHex(byte[] b, int offset, int length) {
		if (b == null) {
			throw new IllegalArgumentException("source array is null");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length is < 0");
		}
		if (offset < 0) {
			throw new ArrayIndexOutOfBoundsException(offset);
		}

		int end = offset + length;

		if (end > b.length) {
			throw new ArrayIndexOutOfBoundsException(end);
		}

		StringBuffer sb = new StringBuffer(length * 2);

		for (int i = offset; i < end; i++) {
			sb.append(convertDigit((int)((b[i] >> 4) & 0x0f)));
			sb.append(convertDigit((int)(b[i] & 0x0f)));
		}
		return (sb.toString());
	}

	/**
	 * Convert the specified value (0 .. 15) to the corresponding hexadecimal
	 * digit.
	 * 
	 * @param value
	 *          Value to be converted.
	 * @return the hexadecimal representation of the value.
	 */
	private static char convertDigit(int value) {
		if (value >= 10) {
			return ((char)(value - 10 + 'A'));
		} else {
			return ((char)(value + '0'));
		}
	}

	public static String dirName(String path){
		int i = path.lastIndexOf(File.separatorChar);
		if(i != -1){
			return path.substring(0, i);
		}
		return path;
	}

	public static String fileName(String path){
		int i = path.lastIndexOf(File.separatorChar);
		if(i != -1){
			return path.substring(i+1, path.length());
		}
		return path;
	}

	public static byte[] concat(byte[] org, byte[] cat){
		byte[] data = new byte[org.length+cat.length];
		for (int i = 0; i < org.length; i++) {
			data[i] = org[i];
		}
		for (int i = 0; i < cat.length; i++) {
			data[org.length+i] = cat[i];
		}
		return data;
	}

	public static byte[] serialize(Object object) throws IOException{
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        ObjectOutput out = new ObjectOutputStream(bos) ;
        out.writeObject(object);
        out.close();
        return bos.toByteArray();
	}

	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException{
		// Deserialize from a byte array
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object obj = in.readObject();
        in.close();
		return obj;
	}

	/**
	 * Convert from hexadecimal string to byte array.
	 * <p>
	 * The input string is required to contain only valid hexadecimal characters,
	 * and the given length must be even.
	 * 
	 * @param s
	 *          the string to convert.
	 * @return a byte array containing the values corresponding to the hexadecimal
	 *         representation.
	 * @exception IllegalArgumentException
	 *              if <code>s</code> is <code>null</code>, the length is odd
	 *              or the string contains non-hexadecimal characters.
	 */
	public static byte[] toBytes(String s) {
		if (s == null) {
			throw new IllegalArgumentException("input string is null");
		} else {
			return toBytes(s, 0, s.length());
		}
	}

	/**
	 * Convert from hexadecimal string to byte array.
	 * <p>
	 * The input string is required to contain only valid hexadecimal characters,
	 * and the given length must be even.
	 * 
	 * @param s
	 *          the string to convert.
	 * @param offset
	 *          starting position in the string.
	 * @param length
	 *          the number characters to convert.
	 * @return a byte array containing the values corresponding to the hexadecimal
	 *         representation.
	 * @exception StringIndexOutOfBoundsException
	 *              if conversion would cause access of data outside string
	 *              bounds.
	 * @exception IllegalArgumentException
	 *              if <code>s</code> is <code>null</code>,
	 *              <code>length</code> is less than zero, the length is odd or
	 *              the string contains non-hexadecimal characters.
	 */
	public static byte[] toBytes(String s, int offset, int length) {
		if (s == null) {
			throw new IllegalArgumentException("input string is null");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length is < 0");
		}
		if (offset < 0) {
			throw new StringIndexOutOfBoundsException(offset);
		}

		int end = offset + length;

		if (end > s.length()) {
			throw new StringIndexOutOfBoundsException(end);
		}

		else if ((length % 2) == 1) {
			throw new IllegalArgumentException("length of input string is odd >" + s + "<");
		}

		byte[] bb = new byte[length / 2];
		char ch;
		byte b;
		int pos = 0;

		for (int i = offset; i < end; i += 2, pos += 1) {
			b = 0;

			ch = s.charAt(i);
			if ((ch >= '0') && (ch <= '9')) {
				b += ((ch - '0') * 16);
			} else if ((ch >= 'a') && (ch <= 'f')) {
				b += ((ch - 'a' + 10) * 16);
			} else if ((ch >= 'A') && (ch <= 'F')) {
				b += ((ch - 'A' + 10) * 16);
			} else {
				throw new IllegalArgumentException("invalid hex character >" + s + "<");
			}

			ch = s.charAt(i + 1);
			if ((ch >= '0') && (ch <= '9')) {
				b += (ch - '0');
			} else if ((ch >= 'a') && (ch <= 'f')) {
				b += (ch - 'a' + 10);
			} else if ((ch >= 'A') && (ch <= 'F')) {
				b += (ch - 'A' + 10);
			} else {
				throw new IllegalArgumentException("invalid hex character >" + s + "<");
			}

			bb[pos] = b;
		}

		return bb;
	}

	/**
	 * Checks if a <code>String</code> contains only digits.
	 * 
	 * @param s
	 *            the <code>String</code> to test.
	 * @return <code>true</code> if the value of the provided String contains
	 *         only digits; <code>false</code> otherwise.
	 */
	public static boolean isNumeric(String s) {
		if (isEmpty(s)) {
			return false;
		}

		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a right aligned string.
	 * <p>
	 * Aligns the string to the right of a field of the given length. If the
	 * length of the string is less than the given length, the space to the left
	 * of the string will be padded with the given character.
	 * <p>
	 * If the length of the string is longer than the given length, the input
	 * string will be returned unmodified.
	 * 
	 * @param s
	 *            the string.
	 * @param length
	 *            the length of the resulting string.
	 * @param c
	 *            the pad character.
	 * @return a right aligned string.
	 */
	public static String rightAlign(String s, int length, char c) {
		if (length <= 0) {
			throw new IllegalArgumentException("Length of field <= 0");
		}

		if (isEmpty(s)) {
			return fill(c, length);
		}

		if (s.length() < length) {
			StringBuffer sb = new StringBuffer(length);
			int len = length - s.length();
			for (int i = 0; i < len; i++) {
				sb.append(c);
			}
			sb.append(s);
			return sb.toString();
		} else {
			return s;
		}
	}

	/**
	 * Returns a left aligned string.
	 * <p>
	 * Aligns the string to the left of a field of the given length. If the
	 * length of the string is less than the given length, the space to the
	 * right of the string will be padded with the given character.
	 * <p>
	 * If the length of the string is longer than the given length, the input
	 * string will be returned unmodified.
	 * 
	 * @param s
	 *            the string.
	 * @param length
	 *            the length of the resulting string.
	 * @param c
	 *            the pad character.
	 * @return a left aligned string.
	 */
	public static String leftAlign(String s, int length, char c) {
		if (length <= 0) {
			throw new IllegalArgumentException("Length of field <= 0");
		}

		if (isEmpty(s)) {
			return fill(c, length);
		}

		if (s.length() < length) {
			StringBuffer sb = new StringBuffer(length);
			int len = length - s.length();
			sb.append(s);
			for (int i = 0; i < len; i++) {
				sb.append(c);
			}
			return sb.toString();
		} else {
			return s;
		}
	}

	/**
	 * Creates a new <code>String</code> of the given length.
	 * <p>
	 * All characters in the string will be initialized to the provided
	 * character.
	 * 
	 * @param c
	 *            the character.
	 * @param s
	 *            the length.
	 * @return a string of given length, filled with the given character.
	 */
	public static String fill(char c, int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("Length of field <= 0");
		}

		StringBuffer sb = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Checks if a <code>String</code> is a valid hexadecimal string.
	 * 
	 * @param s
	 *            the <code>String</code> to test.
	 * @return <code>true</code> if the value of the provided String contains
	 *         only hexadecimal characters; <code>false</code> otherwise.
	 * @throws IllegalArgumentException
	 *             if the <code>String</code> contains an odd number of
	 *             characters.
	 */
	public static boolean isHex(String s) {
		if (isEmpty(s)) {
			return false;
		}

		if (s.length() % 2 != 0) {
			throw new IllegalArgumentException("Length of hex string is odd");
		}

		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch < '0' && ch > '9') && (ch < 'a' && ch > 'f')
					&& (ch < 'A' && ch > 'F')) {
				return false;
			}
		}
		return true;
	}

}
