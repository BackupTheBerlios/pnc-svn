package convertit.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * The <code>Str</code> class provides a number of static utility methods for
 * a number of <code>String</code> manipulations.
 * 
 * @author Jan Svensson
 * @version 1.0
 */
public class Str {

	/**
	 * Checks if a <code>String</code> is empty.
	 * 
	 * @param s
	 *          the <code>String</code> to test.
	 * @return <code>true</code> if the provided String is <code>null</code>
	 *         or has a length of 0; <code>false</code> otherwise.
	 */
	public static boolean isEmpty(String s) {
		return (s == null || s.length() == 0);
	}

	/**
	 * Checks if a <code>String</code> has a non-blank value.
	 * 
	 * @param s
	 *          the <code>String</code> to test.
	 * @return <code>true</code> if the provided String is <code>null</code>
	 *         ,has a length of 0 or contains only whitespace; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isBlank(String s) {
		return (s == null || s.length() == 0 || s.trim().length() == 0);
	}

	/**
	 * Checks if a <code>String</code> contains only digits.
	 * 
	 * @param s
	 *          the <code>String</code> to test.
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
	 * Checks if a <code>String</code> is a valid hexadecimal string.
	 * 
	 * @param s
	 *          the <code>String</code> to test.
	 * @return <code>true</code> if the value of the provided String contains
	 *         only hexadecimal characters; <code>false</code> otherwise.
	 * @throws IllegalArgumentException
	 *           if the <code>String</code> contains an odd number of
	 *           characters.
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
			if ((ch < '0' && ch > '9') && (ch < 'a' && ch > 'f') && (ch < 'A' && ch > 'F')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the value of the provided <code>String</code> is a member of
	 * the provided array.
	 * 
	 * @param s
	 *          the <code>String</code> to test.
	 * @param a
	 *          the array of values.
	 * @return <code>true</code> if the the provided String is not empty and
	 *         it's value matches any of the members in the provided array;
	 *         <code>false</code> otherwise.
	 */
	public static boolean isMember(String s, String[] a) {
		if (!isEmpty(s) && a != null && a.length > 0) {
			for (int i = 0; i < a.length; i++) {
				if (s.equals(a[i])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if any of the strings provided in an array is a member of another
	 * array.
	 * <p>
	 * Checks if the union of the provided arrays is non-zero, if any of the
	 * values in the arrays is null, the value is not used in the union.
	 * 
	 * @param a
	 *          array of Strings.
	 * @param b
	 *          array of Strings.
	 * @return <code>true</code> if the union of the given arrays is non-zero;
	 *         <code>false</code> otherwise.
	 */
	public static boolean containsMember(String[] a, String[] b) {
		if (a != null && b != null && a.length != 0 && b.length != 0) {
			for (int i = 0; i < a.length; i++) {
				String s1 = a[i];
				if (s1 != null) {
					for (int j = 0; j < b.length; j++) {
						if (s1.equals(b[j])) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Concatenates all elements in the provided array into one string.
	 * <p>
	 * The <code>null</code> value will be represented by the empty string.
	 * 
	 * @param a
	 *          array of Strings.
	 * @return a string that is the concatenation of all strings in the provided
	 *         array.
	 */
	public static String join(String[] a) {
		if (a == null || a.length == 0) {
			return "";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < a.length; i++) {
				String s = a[i];
				if (s != null) {
					sb.append(s);
				}
			}
			return sb.toString();
		}
	}

	/**
	 * Concatenates all elements in the provided array into one string using the
	 * given char as delimiter.
	 * <p>
	 * <code>null</code> values will be represented by the empty string.
	 * 
	 * @param a
	 *          array of Strings.
	 * @param delimiter
	 *          delimiter character.
	 * @return a string that is the concatenation of all strings in the provided
	 *         array delimited by <code>delimiter</code>.
	 */
	public static String join(String[] a, char delimiter) {
		if (a == null || a.length == 0) {
			return "";
		} else if (a.length == 1) {
			return a[0];
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < a.length; i++) {
				if (i != 0) {
					sb.append(delimiter);
				}
				String s = a[i];
				if (s != null) {
					sb.append(s);
				}
			}
			return sb.toString();
		}
	}

	/**
	 * Returns a string with all occurences of the given character removed.
	 * 
	 * @param s
	 *          a <code>String</code>
	 * @param remove
	 *          the character to be removed.
	 * @return a string with all occurences of the given character removed.
	 */
	public static String squeeze(String s, char remove) {
		if (isEmpty(s)) {
			return s;
		}

		if (s.indexOf(remove) == -1) {
			return s;
		}

		StringBuffer sb = new StringBuffer(s.length());
		char ch;

		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			if (ch != remove) {
				sb.append(ch);
			}
		}

		return sb.toString();
	}

	/**
	 * Returns an array containing all non-empty strings from the provided array.
	 * 
	 * @param s
	 *          an array of strings
	 * @return an array containing all non-empty strings from the provided array.
	 */
	public static String[] squeeze(String[] a) {
		if (a == null || a.length == 0) {
			return a;
		}

		int count = 0;
		for (int i = 0; i < a.length; i++) {
			if (isEmpty(a[i])) {
				count++;
			}
		}

		if (count > 0) {
			String[] tmp = new String[a.length - count];
			int pos = 0;
			for (int i = 0; i < a.length; i++) {
				if (!isEmpty(a[i])) {
					tmp[pos++] = a[i];
				}
			}
			return tmp;
		} else {
			return a;
		}
	}

	/**
	 * Splits a string around matches of the given character.
	 * <p>
	 * The array returned by this method contains each substring of this string
	 * that is terminated by another substring that matches the given character or
	 * is terminated by the end of the string. The substrings in the array are in
	 * the order in which they occur in this string. If the character does not
	 * match character in the input then the resulting array has just one element,
	 * namely this string.
	 * 
	 * @param s
	 *          the <code>String</code> to split.
	 * 
	 * @param c
	 *          the character to split around.
	 * 
	 * @return the array of strings computed by splitting this string around
	 *         matches of the given character
	 */
	public static String[] split(String s, char c) {
		return split(s, c, -1, false);
	}

	/**
	 * Splits a string around matches of the given character.
	 * <p>
	 * The array returned by this method contains each substring of this string
	 * that is terminated by another substring that matches the given character or
	 * is terminated by the end of the string. The substrings in the array are in
	 * the order in which they occur in this string. If the character does not
	 * match character in the input then the resulting array has just one element,
	 * namely this string.
	 * <p>
	 * The <tt>limit</tt> parameter controls the number of times the character
	 * is applied and therefore affects the length of the resulting array. If the
	 * limit <i>n</i> is greater than zero then the pattern will be applied at
	 * most <i>n</i> - 1 times, the array's length will be no greater than <i>n</i>,
	 * and the array's last entry will contain all input beyond the last matched
	 * delimiter. If <i>n</i> is non-positive then the pattern will be applied as
	 * many times as possible and the array can have any length.
	 * 
	 * @param s
	 *          the <code>String</code> to split.
	 * 
	 * @param c
	 *          the character to split around.
	 * 
	 * @param limit
	 *          the result threshold, as described above
	 * 
	 * @return the array of strings computed by splitting this string around
	 *         matches of the given character
	 */
	public static String[] split(String s, char c, int limit) {
		return split(s, c, limit, false);
	}

	/**
	 * Splits a string around matches of the given character.
	 * <p>
	 * The array returned by this method contains each substring of this string
	 * that is terminated by another substring that matches the given character or
	 * is terminated by the end of the string. The substrings in the array are in
	 * the order in which they occur in this string. If the character does not
	 * match character in the input then the resulting array has just one element,
	 * namely this string.
	 * <p>
	 * The <tt>limit</tt> parameter controls the number of times the character
	 * is applied and therefore affects the length of the resulting array. If the
	 * limit <i>n</i> is greater than zero then the pattern will be applied at
	 * most <i>n</i> - 1 times, the array's length will be no greater than <i>n</i>,
	 * and the array's last entry will contain all input beyond the last matched
	 * delimiter. If <i>n</i> is non-positive then the pattern will be applied as
	 * many times as possible and the array can have any length.
	 * <p>
	 * The <tt>squeeze</tt> parameter controls if empty elements should be
	 * removed from the resulting array. The <i>squeeze</i> is applied after the
	 * array of strings is computed, so if the limit is greater than zero and some
	 * of the computed strings are empty, the resulting array may contain fewer
	 * elements than the limit specifies.
	 * 
	 * @param s
	 *          the <code>String</code> to split.
	 * 
	 * @param c
	 *          the character to split around.
	 * 
	 * @param limit
	 *          the result threshold, as described above
	 * 
	 * @param squeeze
	 *          if empty elements should be removed from the result.
	 * 
	 * @return the array of strings computed by splitting this string around
	 *         matches of the given character
	 */
	public static String[] split(String s, char c, int limit, boolean squeeze) {
		if (isEmpty(s)) {
			return new String[0];
		}

		// Count occurences of delimiter
		int count = count(s, c);

		if (limit > 0) {
			// Adjust to occurences of delimiter
			limit -= 1;

			if (count > limit) {
				count = limit;
			}
		}

		String[] data = new String[count + 1];
		int idx = 0;
		int pos = 0;
		int end = 0;

		for (idx = 0; idx < count; idx++) {
			end = s.indexOf(c, pos);
			data[idx] = s.substring(pos, end);
			pos = end + 1;
		}
		data[idx] = s.substring(pos);

		if (squeeze) {
			data = squeeze(data);
		}

		return data;
	}

	/**
	 * Counts the number of occurences of a character in a string.
	 * 
	 * @param s
	 *          the <code>String</code> to check.
	 * @param c
	 *          the character to count.
	 * @return the number of occurences of the character in the string.
	 */
	public static int count(String s, char c) {
		int count = 0;
		int index = -1;
		while (true) {
			index = s.indexOf(c, index + 1);
			if (index == -1) {
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * Creates a new <code>String</code> of the given length.
	 * <p>
	 * All characters in the string will be initialized to the provided character.
	 * 
	 * @param c
	 *          the character.
	 * @param s
	 *          the length.
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
	 *          the string.
	 * @param length
	 *          the length of the resulting string.
	 * @param c
	 *          the pad character.
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
	 * Aligns the string to the left of a field of the given length. If the length
	 * of the string is less than the given length, the space to the right of the
	 * string will be padded with the given character.
	 * <p>
	 * If the length of the string is longer than the given length, the input
	 * string will be returned unmodified.
	 * 
	 * @param s
	 *          the string.
	 * @param length
	 *          the length of the resulting string.
	 * @param c
	 *          the pad character.
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

	public static String[] getCSVEntries(String s) {
		return split(s, ',', -1, true);
	}

	public static String[] append(String s, String[] a) {
		if (a == null) {
			return new String[] { s };
		} else {
			String[] tmp = new String[a.length + 1];
			System.arraycopy(a, 0, tmp, 0, a.length);
			tmp[a.length] = s;
			return tmp;
		}
	}

	public static String md5sum(String data){
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(data.getBytes());
			byte digest[] = algorithm.digest();
			
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < digest.length; i++) {
				String hex = Integer.toHexString(digest[i] & 0xff);
				if(hex.length() == 1){
					hexString.append("0");
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("NoSuchAlgorithmException: MD5");
		}
	}
	
	/**
	 * Returns a key value pair string for the fields in an object
	 * @param o
	 * Object to get the member fields from
	 * @return
	 * Key value string delimited by brackets
	 */
	public static String toString(Object o){
		StringBuffer sb = new StringBuffer();
		
		if(o == null){
			return "null";
		}
		
		if(o instanceof List){
			//System.out.println("it's a list");
			List l = (List)o;
			//1.4 way
			for(Iterator i = l.iterator(); i.hasNext(); ){
				sb.append(toString(i.next()));
			}
			return sb.toString();
		}
		
		Field[] fields = o.getClass().getDeclaredFields();
		for(int i = 0; i < fields.length; i++){
			try {
				if(Modifier.STATIC != (fields[i].getModifiers() & Modifier.STATIC)){
					if(Modifier.PUBLIC == (fields[i].getModifiers() & Modifier.PUBLIC)){
						//System.out.println("it's public: "+fields[i].getName());
						sb.append("["+fields[i].getName()+"="+fields[i].get(o)+"]");
					}else if(Modifier.PRIVATE == (fields[i].getModifiers() & Modifier.PRIVATE)){
						//System.out.println("it's private: "+fields[i].getName());
						Method[] methods = o.getClass().getMethods();
						for(int j = 0; j < methods.length; j++){
							if(methods[j].getName().equalsIgnoreCase("get"+fields[i].getName())){
								try{
									Object ret = methods[j].invoke(o, new Object[0]).toString();
									sb.append("["+fields[i].getName()+"="+ret+"]");
								}catch(NullPointerException e){
									sb.append("["+fields[i].getName()+"=null]");
								}
							}
						}
					}else{
						sb.append("["+fields[i].getName()+"="+"?"+"]");
					}
				}else{
					sb.append("["+fields[i].getName()+"="+o+"]");
				}
			} catch (IllegalArgumentException e) {
				sb.append("[["+fields[i].getName()+"="+o+"]: "+e.getMessage()+"]");
			} catch (IllegalAccessException e) {
				sb.append("[["+fields[i].getName()+"="+o+"]: "+e.getMessage()+"]");
			} catch (InvocationTargetException e) {
				sb.append("[["+fields[i].getName()+"="+o+"]: "+e.getMessage()+"]");
			} catch(Exception e){
				sb.append("[["+fields[i].getName()+"="+o+"]: "+e.getMessage()+"]");
			}
		}
		return sb.toString();
	}

	/**
	 * Removes last character if it is a return char (\n) 
	 * @param str
	 * String to chomp
	 * @return
	 * Chomped string
	 */
	public static String chomp(String str){
		if(!isEmpty(str) && str.charAt(str.length()-1) == '\n'){
			return str.substring(0, str.length()-1);
		}else{
			return str;
		}
	}

	public static Properties textParser(String inp){
		Properties p = new Properties();
		String[] lines = split(inp, '\n');
		for (int i = 0; i < lines.length; i++) {
			String[] keyvalue = split(lines[i], '=');
			if(keyvalue.length == 2){
				p.put(keyvalue[0].trim(), keyvalue[1].trim());
			}
		}
		return p;
	}
	
	public static String readStream(InputStream inp) throws IOException{
		StringBuffer sb = new StringBuffer();
		BufferedInputStream bis = new BufferedInputStream(inp);
		int ch = -1;
		while ((ch = bis.read()) != -1) {
			sb.append((char) ch);
		}
		bis.close();
		return sb.toString();
	}
}
