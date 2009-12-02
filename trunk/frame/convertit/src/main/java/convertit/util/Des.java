package convertit.util;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Des {
	private Cipher ecipher;
	private Cipher dcipher;

	public Des(byte[] key) {
		this(new SecretKeySpec(key, 0, 64, "DES"));
	}

	public Des(String key) {
		this(new SecretKeySpec(key.getBytes(), 0, 64, "DES"));
	}

	public Des(SecretKey key) {
		try {
			ecipher = Cipher.getInstance("DES");
			dcipher = Cipher.getInstance("DES");
			ecipher.init(Cipher.ENCRYPT_MODE, key);
			dcipher.init(Cipher.DECRYPT_MODE, key);

		} catch (javax.crypto.NoSuchPaddingException e) {
		} catch (java.security.NoSuchAlgorithmException e) {
		} catch (java.security.InvalidKeyException e) {
		}
	}

	public String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return new String(enc);
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (Exception e) {
		}
		return null;
	}

	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
			byte[] dec = str.getBytes();

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (Exception e) {
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			// Generate a temporary key. In practice, you would save this key.
			// See also e464 Encrypting with DES Using a Pass Phrase.
			SecretKey key = KeyGenerator.getInstance("DES").generateKey();

			// Create encrypter/decrypter class
			Des encrypter = new Des(key);

			// Encrypt
			String encrypted = encrypter.encrypt("Don't tell anybody!");

			// Decrypt
			String decrypted = encrypter.decrypt(encrypted);
			System.out.println("decrypted="+decrypted);
		

			//
			// Create encrypter/decrypter class
			int len = 56;
			byte[] bkey = new byte[len];
			for (int i = 0; i < len; i++) {
				bkey[i] = 0;
			}
			
			encrypter = new Des(new DESKeySpec("arnealka".getBytes()).getKey());
			

			// Encrypt
			encrypted = encrypter.encrypt("Don't tell anybody!");

			// Decrypt
			decrypted = encrypter.decrypt(encrypted);
			System.out.println("decrypted="+decrypted);
		} catch (Exception e) {
			System.out.println("Exception: "+e.getMessage());
		}
	}
	public static byte[] addParity(byte[] in) {
        byte[] result = new byte[8];
    
        // Keeps track of the bit position in the result
        int resultIx = 1;
    
        // Used to keep track of the number of 1 bits in each 7-bit chunk
        int bitCount = 0;
    
        // Process each of the 56 bits
        for (int i=0; i<56; i++) {
            // Get the bit at bit position i
            boolean bit = (in[6-i/8]&(1<<(i%8))) > 0;
    
            // If set, set the corresponding bit in the result
            if (bit) {
                result[7-resultIx/8] |= (1<<(resultIx%8))&0xFF;
                bitCount++;
            }
    
            // Set the parity bit after every 7 bits
            if ((i+1) % 7 == 0) {
                if (bitCount % 2 == 0) {
                    // Set low-order bit (parity bit) if bit count is even
                    result[7-resultIx/8] |= 1;
                }
                resultIx++;
                bitCount = 0;
            }
            resultIx++;
        }
        return result;
    }

}
