package com.mathias.bellatetris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mathias.bellatetris.server.Server;

public class StoreHighscore {

	private static final int SOCKET_TIMEOUT = 5000;

	private static final int CONNECT_TIMEOUT = 5000;

	private InetSocketAddress mAddress;

	private Socket mSocket;

	private PrintWriter mWriter;

	private BufferedReader mReader;

	public StoreHighscore(String host, int port) {
		mAddress = new InetSocketAddress(host, port);
	}

	public void sendHighScore(String name, long score) throws IOException {
		try {
			openSocket();

			mWriter.println(Server.CMD_SET + name + "," + score + ","
					+ Server.getCheckSum(name, score));
			mWriter.flush();

			String result = mReader.readLine();

			if (result == null || !result.equalsIgnoreCase(Server.CMD_OK)) {
				System.err.println("Error: "+result);
			}
		} finally {
			closeSocket();
		}
	}
	
	public List<String> fetchHighScore() throws IOException {
		List<String> highScore = new ArrayList<String>();
		try {
			openSocket();

			mWriter.println(Server.CMD_GET);
			mWriter.flush();

			while(true){
				String res = mReader.readLine();
				if(res == null){
					break;
				}
				highScore.add(res);
			}
		} finally {
			closeSocket();
		}
		return highScore;
	}

	private void openSocket() throws IOException {
		mSocket = new Socket();
		mSocket.connect(mAddress, CONNECT_TIMEOUT);

		mWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
		mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

		mSocket.setSoTimeout(SOCKET_TIMEOUT);
	}

	private void closeSocket() {
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
