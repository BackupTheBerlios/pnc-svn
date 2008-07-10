package com.mathias.games.dogfight.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mathias.games.dogfight.Player;
import com.mathias.games.dogfight.server.Server;
import com.mathias.games.dogfight.server.ServerImpl;

public class ClientImpl implements Client {

//	private static final int SOCKET_TIMEOUT = 5000;
//	private static final int CONNECT_TIMEOUT = 5000;

	private Socket mSocket;

	private PrintWriter mWriter;

	private BufferedReader mReader;

	public List<Player> update(Player player) throws IOException {
		mWriter.println(ServerImpl.CMD_UPDATE+player.name+","+player.angle+","+player.x+","+player.y);
		mWriter.flush();
		
		List<Player> pls = new ArrayList<Player>();

		String res = mReader.readLine();
		while(res != null && res.charAt(0) != Server.CMD_EOS){
			System.out.println("res: "+res);
			String[] split = res.split(",");
			String name = split[1];
			double angle = Double.parseDouble(split[2]);
			int x = Integer.parseInt(split[3]);
			int y = Integer.parseInt(split[4]);
			pls.add(new Player(name, angle, x, y));
			res = mReader.readLine();
		}
		return pls;
	}

	public void connect(String host, int port) throws IOException {
		mSocket = new Socket();
		mSocket.connect(new InetSocketAddress(host, port)/*, CONNECT_TIMEOUT*/);

		mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
		mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

//		mSocket.setSoTimeout(SOCKET_TIMEOUT);
	}

	public void disconnect() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// Do nothing...
			}
			mSocket = null;
			mWriter = null;
			mReader = null;
		}
	}

}
