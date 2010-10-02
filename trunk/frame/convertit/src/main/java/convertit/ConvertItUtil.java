package convertit;

import java.util.Date;

import com.mathias.drawutils.Util;

public class ConvertItUtil {

	public static void handleLuhnCheckDigit(String p1) {
		if(Util.isEmpty(p1) || !Util.isNumeric(p1)){
			Log.log("Invalid input");
		}else{
			int sum = 0;
			for(int i = 0; i < p1.length(); i++) {
				char a = p1.charAt(i);
				int v = (a - '0');
				if((i & 1) == 1){
					v *= 2;
				}
				if(v > 9){
					sum += v - 10 + 1;
				}else{
					sum += v;
				}
			}
			sum %= 10;
			if(sum != 0){
				sum = 10 - sum;
			}
			String res = p1+sum;
			if((res.length() % 2) != 0){
				res += 'F';
			}
			Log.log(res);
		}
	}

	public static void handleNibleSwap(String p1) {
		if(!Util.isEmpty(p1) && Util.isHex(p1)){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < p1.length(); i+=2) {
				sb.append(p1.charAt(i+1));
				sb.append(p1.charAt(i));
			}
			Log.log(sb.toString());
		}
	}

	public static void handleByteSwap(String p1) {
		if(!Util.isEmpty(p1) && Util.isHex(p1) && p1.length() % 4 == 0){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < p1.length(); i+=4) {
				sb.append(p1.charAt(i+2));
				sb.append(p1.charAt(i+3));
				sb.append(p1.charAt(i));
				sb.append(p1.charAt(i+1));
			}
			Log.log(sb.toString());
		}
	}

	public static void handleXor(String p1) {
		if(!Util.isEmpty(p1) && Util.isNumeric(p1)){
			long val = Long.parseLong(p1);
			Log.log(""+~val);
		}
	}

	public static void handlePad(String p1, String p2, String p3, String p4) {
		//pad arne 10 c left
		char c = '0';
		boolean right = true;
		int length = 0;
		if(!Util.isEmpty(p4) && "left".equalsIgnoreCase(p4)){
			right = false;
		}
		if(!Util.isEmpty(p3)){
			c = p3.charAt(0);
		}
		if(!Util.isEmpty(p2) && Util.isNumeric(p2)){
			length = Integer.parseInt(p2);
		}
		if(right){
			Log.log(Util.rightAlign(p1, length, c));
		}else{
			Log.log(Util.leftAlign(p1, length, c));
		}
	}

	public static void handleAlg(String alg, String data) {
		if(!Util.isEmpty(alg) && !Util.isEmpty(data)){
			Log.log(Util.toHex(Util.alg(alg, data)));
		}
	}
	
	public static void handleMd5(String data) {
		if(!Util.isEmpty(data)){
			Log.log(Util.md5(data));
		}
	}
	
	public static void handleSha1(String data) {
		if(!Util.isEmpty(data)){
			Log.log(Util.sha1(data));
		}
	}
	
	public static void handleText2Hex(String data){
		if(!Util.isEmpty(data)){
			Log.log(Util.toHex(data.getBytes()));
		}
	}
	
	public static void handleHex2Text(String data){
		if(!Util.isEmpty(data) && Util.isHex(data)){
			byte[] bytes = Util.toBytes(data);
			Log.log(new String(bytes));
		}
	}

	public static void handleDate(String p1) {
		if(!Util.isEmpty(p1) && Util.isNumeric(p1)){
			try{
				Log.log(""+new Date(Long.parseLong(p1)));
			}catch(Exception e){
				Log.log("Could not parse: "+p1);
			}
		}
	}

}
