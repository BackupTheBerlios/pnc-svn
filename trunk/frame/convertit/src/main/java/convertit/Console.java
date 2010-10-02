package convertit;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
				ConvertItUtil.handleHex2Text(p1);
			}else if("text2hex".startsWith(cmd)){
				ConvertItUtil.handleText2Hex(p1);
			}else if("dec2bin".startsWith(cmd)){
				// TODO
			}else if("dec2oct".startsWith(cmd)){
				// TODO
			}else if("hex2dec".startsWith(cmd)){
				// TODO
			}else if("alg".startsWith(cmd)){
				ConvertItUtil.handleAlg(p1, p2);
			}else if("md5".startsWith(cmd)){
				ConvertItUtil.handleMd5(p1);
			}else if("sha1".startsWith(cmd)){
				ConvertItUtil.handleSha1(p1);
			}else if("des".startsWith(cmd)){
				// TODO
			}else if("3descbc".startsWith(cmd)){
				// TODO
			}else if("luhncheckdigit".startsWith(cmd)){
				ConvertItUtil.handleLuhnCheckDigit(p1);
			}else if("xor".startsWith(cmd)){
				ConvertItUtil.handleXor(p1);
			}else if("pad".startsWith(cmd)){
				ConvertItUtil.handlePad(p1, p2, p3, p4);
			}else if("nibleswap".startsWith(cmd)){
				ConvertItUtil.handleNibleSwap(p1);
			}else if("byteswap".startsWith(cmd)){
				ConvertItUtil.handleByteSwap(p1);
			}else if("dirname".startsWith(cmd)){
				Log.log(Util.dirName(p1));
			}else if("filename".startsWith(cmd)){
				Log.log(Util.fileName(p1));
			}else if("date".startsWith(cmd)){
				ConvertItUtil.handleDate(p1);
			}else if("upper".startsWith(cmd)){
				Log.log(p1.toUpperCase());
			}else if("lower".startsWith(cmd)){
				Log.log(p1.toLowerCase());
			}else {
				Log.log("Unknown command: "+cmd);
			}
		}else{
			Log.log("No command!");
		}
	}

	private void handleHelp(String cmd){
		if(cmd == null){
			Log.log("ConvertIt");
			Log.log("    quit");
			Log.log("    help");
			Log.log("    text2hex <text>");
			Log.log("    hex2text <hex>");
			Log.log("    alg <alg> <data>");
			Log.log("    md5 <data>");
			Log.log("    sha1 <data>");
			Log.log("    nibleswap <hex>");
			Log.log("    byteswap <hex>");
			Log.log("    pad <text> <length> <char> <right|left>");
			Log.log("    dirname <file>");
			Log.log("    filename <file>");
			Log.log("    xor <data>");
			Log.log("    date <num>");
			Log.log("    upper <text>");
			Log.log("    lower <text>");
//		}else if("text2hex".startsWith(cmd)){
//			log("text2hex <text>");
//		}else if("pad".startsWith(cmd)){
//			log("pad <text> <length> <char> <right|left>");
		}else{
			Log.log("No help for: "+cmd);
		}
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
