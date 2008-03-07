package com.mathias.bellatetris.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.mathias.drawutils.Util;

public class Server {
	
	private static final int PORT = 7200;
	
	public static final String CMD_SET = "[SET]";

	public static final String CMD_GET = "[GET]";

	public static final String CMD_OK = "[OK]";

	public static final String CMD_ERROR = "[ERROR]";

	private List<HighscoreItem> highscore;

	private HighscoreItemDao hsDao;
	
	private String allow;

	public Server(){
		hsDao = new HighscoreItemDao();
		
		highscore = hsDao.getHighscores();

		allow = System.getProperty("allow").trim();
		int port = Integer.parseInt(System.getProperty("port", ""+PORT).trim());
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(true){
				new Connection(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Connection extends Thread {
		private Socket socket;
		
		public Connection(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try {
				String clientip = socket.getInetAddress().getHostAddress();
				if(allow != null && clientip.indexOf(allow) == -1){
					System.err.println("Illegal access from: "+clientip);
					return;
				}
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				String res = reader.readLine();
				System.out.println("Incoming: "+res);
				
				if(res != null && res.indexOf(CMD_GET) != -1){
					System.out.println("GET: "+res);
					Collections.sort(highscore);
					for (int i = 0; i < 10 && i < highscore.size(); i++) {
						HighscoreItem hs = highscore.get(i);
						System.out.println("sends: "+hs);
						writer.write(hs.name+ ","+ hs.score+"\n");
					}
					writer.flush();
				}else if(res != null && res.indexOf(CMD_SET) != -1){
					System.out.println("SET: "+res);
					String[] tokens = Util.split(res.substring(CMD_SET.length()), ',');
					if(validate(tokens)){
						HighscoreItem hs = new HighscoreItem(tokens[0], Long
								.parseLong(tokens[1]), socket.getInetAddress()
								.getHostName());
						hsDao.saveHighscore(hs);
						highscore.add(hs);
						writer.write(CMD_OK);
					}else{
						writer.write(CMD_ERROR);
					}
					writer.flush();
					for (HighscoreItem s : highscore) {
						System.out.println("Highscore list item: "+s);
					}
				}else{
					System.out.println("Unknown command: "+res);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private boolean validate(String[] tokens){
		if(tokens != null && tokens.length == 3){
			String name = tokens[0];
			long score;
			try{
				score = Long.parseLong(tokens[1]);
			}catch(NumberFormatException e){
				e.printStackTrace();
				System.err.println("NumberFormatException for score: "+tokens[1]);
				return false;
			}
			String checksum = tokens[2];
			if(Util.isEmpty(name)){
				System.err.println("Name is empty!");
				return false;
			}
			if(!getCheckSum(name, score).equals(checksum)){
				System.err.println("Wrong check sum!");
				return false;
			}
			return true;
		}
		System.err.println("Wrong amount of tokens: "+tokens);
		return false;
	}
	
	public static String getCheckSum(String name, long score){
		return Util.md5sum(name+score);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Server();
	}

}
