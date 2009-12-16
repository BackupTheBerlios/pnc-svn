package convertit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import com.mathias.drawutils.Util;

public class Console {

	public Console() {
	}

	public void handleCommand(String[] cmds) {
		if(cmds != null && cmds.length > 0){
			String cmd = cmds[0];
			String p1 = (cmds.length > 1 ? cmds[1] : null);
			String p2 = (cmds.length > 2 ? cmds[2] : null);
			String p3 = (cmds.length > 3 ? cmds[3] : null);
			String p4 = (cmds.length > 4 ? cmds[4] : null);
			if("help".startsWith(cmd)){
				handleHelp(p1);
			}else if("quit".startsWith(cmd)){
				System.exit(0);
			}else if("hex2text".startsWith(cmd)){
				handleHex2Text(p1);
			}else if("text2hex".startsWith(cmd)){
				handleText2Hex(p1);
			}else if("dec2bin".startsWith(cmd)){
				// TODO
			}else if("dec2oct".startsWith(cmd)){
				// TODO
			}else if("hex2dec".startsWith(cmd)){
				// TODO
			}else if("alg".startsWith(cmd)){
				handleAlg(p1, p2);
			}else if("md5".startsWith(cmd)){
				handleMd5(p1);
			}else if("sha1".startsWith(cmd)){
				handleSha1(p1);
			}else if("des".startsWith(cmd)){
				// TODO
			}else if("3descbc".startsWith(cmd)){
				// TODO
			}else if("luhncheckdigit".startsWith(cmd)){
				handleLuhnCheckDigit(p1);
			}else if("xor".startsWith(cmd)){
				handleXor(p1);
			}else if("pad".startsWith(cmd)){
				handlePad(p1, p2, p3, p4);
			}else if("nibleswap".startsWith(cmd)){
				handleNibleSwap(p1);
			}else if("byteswap".startsWith(cmd)){
				handleByteSwap(p1);
			}else if("dirname".startsWith(cmd)){
				log(Util.dirName(p1));
			}else if("filename".startsWith(cmd)){
				log(Util.fileName(p1));
			}else if("date".startsWith(cmd)){
				handleDate(p1);
			}else {
				log("Unknown command: "+cmd);
			}
		}else{
			log("No command!");
		}
	}

	private void handleLuhnCheckDigit(String p1) {
		if(Util.isEmpty(p1) || !Util.isNumeric(p1)){
			log("Invalid input");
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
			log(res);
		}
	}

	private void handleNibleSwap(String p1) {
		if(!Util.isEmpty(p1) && Util.isHex(p1)){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < p1.length(); i+=2) {
				sb.append(p1.charAt(i+1));
				sb.append(p1.charAt(i));
			}
			log(sb.toString());
		}
	}

	private void handleByteSwap(String p1) {
		if(!Util.isEmpty(p1) && Util.isHex(p1) && p1.length() % 4 == 0){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < p1.length(); i+=4) {
				sb.append(p1.charAt(i+2));
				sb.append(p1.charAt(i+3));
				sb.append(p1.charAt(i));
				sb.append(p1.charAt(i+1));
			}
			log(sb.toString());
		}
	}

	private void handleXor(String p1) {
		if(!Util.isEmpty(p1) && Util.isNumeric(p1)){
			long val = Long.parseLong(p1);
			log(""+~val);
		}
	}

	private void handlePad(String p1, String p2, String p3, String p4) {
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
			log(Util.rightAlign(p1, length, c));
		}else{
			log(Util.leftAlign(p1, length, c));
		}
	}

	private void handleAlg(String alg, String data) {
		if(!Util.isEmpty(alg) && !Util.isEmpty(data)){
			log(Util.toHex(Util.alg(alg, data)));
		}
	}
	
	private void handleMd5(String data) {
		if(!Util.isEmpty(data)){
			log(Util.md5(data));
		}
	}
	
	private void handleSha1(String data) {
		if(!Util.isEmpty(data)){
			log(Util.sha1(data));
		}
	}
	
	private void handleText2Hex(String data){
		if(!Util.isEmpty(data)){
			log(Util.toHex(data.getBytes()));
		}
	}
	
	private void handleHex2Text(String data){
		if(!Util.isEmpty(data) && Util.isHex(data)){
			byte[] bytes = Util.toBytes(data);
			log(new String(bytes));
		}
	}

	private void handleDate(String p1) {
		if(!Util.isEmpty(p1) && Util.isNumeric(p1)){
			try{
				log(""+new Date(Long.parseLong(p1)));
			}catch(Exception e){
				log("Could not parse: "+p1);
			}
		}
	}

	private void handleHelp(String cmd){
		if(cmd == null){
			log("ConvertIt");
			log("    quit");
			log("    help");
			log("    text2hex <text>");
			log("    hex2text <hex>");
			log("    alg <alg> <data>");
			log("    md5 <data>");
			log("    sha1 <data>");
			log("    nibleswap <hex>");
			log("    byteswap <hex>");
			log("    pad <text> <length> <char> <right|left>");
			log("    dirname <file>");
			log("    filename <file>");
			log("    xor <data>");
			log("    date <num>");
//		}else if("text2hex".startsWith(cmd)){
//			log("text2hex <text>");
//		}else if("pad".startsWith(cmd)){
//			log("pad <text> <length> <char> <right|left>");
		}else{
			log("No help for: "+cmd);
		}
	}

	private static String[] copyArray(String[] sa, int from) {
		int newLength = sa.length - from;
		String[] copy = new String[newLength];
		System.arraycopy(sa, from, copy, 0, newLength);
		return copy;
	}
	
	private static void log(String msg){
		System.out.println(msg);
	}

	public void inputLoop() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));

			while (true) {
				System.out.print("CONVERIT>");
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				handleCommand(line.trim().split("\\s+"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args != null && args.length > 0){
			new Console().handleCommand(args);
		}else{
			new Console().inputLoop();
		}
	}

}
