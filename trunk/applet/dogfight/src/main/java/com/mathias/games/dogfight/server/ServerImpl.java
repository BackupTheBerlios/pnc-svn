package com.mathias.games.dogfight.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.Player;

public class ServerImpl implements Server {
	
	private Map<String, Player> players = new HashMap<String, Player>();

	public ServerImpl(){
		System.out.println("Starting server!");
		int port = Integer.parseInt(System.getProperty("port", ""+PORT).trim());
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while(true){
				new ServerConnection(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public class ServerConnection extends Thread {

		private Socket socket;
		
		public ServerConnection(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			System.out.println("Incoming connection!");
			try {
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				do{
					String res = reader.readLine();
					if(res != null && res.charAt(0) == Server.CMD_UPDATE){
//						System.out.println("UPDATE: "+res);
						Player ply = Player.deserialize(res.substring(1));
						if(ply == null){
							System.out.println("ERROR for "+res);
							break;
						}
						Player player = players.get(ply.name);
						if(player == null){
							players.put(ply.name, ply);
						}
						for (Player p : players.values()) {
							if(!p.name.equals(ply.name)){
								writer.write(p.serialize()+"\n");
							}
						}
						writer.write(CMD_EOS+"\n");
						writer.flush();
					}else{
						Util.sleep(10);
						System.out.println("sleep, Unknown command: "+res);
					}
				}while(true);
			} catch (IOException e) {
				System.out.println(e.getMessage());
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e2) {
						System.out.println(e.getMessage());
					}
				}
				return;
			}
		}
	}

	public static void main(String[] args) {
		new ServerImpl();
	}

}
