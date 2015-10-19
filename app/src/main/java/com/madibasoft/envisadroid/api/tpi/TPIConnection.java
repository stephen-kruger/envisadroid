package com.madibasoft.envisadroid.api.tpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.madibasoft.envisadroid.api.EnvisaException;
import com.madibasoft.envisadroid.api.Session;

public class TPIConnection {
	private Socket socket = null;
	private OutputStreamWriter out;
	private BufferedReader in;

	public void write(String stringToWrite) throws EnvisaException {
		Session.logd("Sending command "+stringToWrite);
		try {
			out.write(stringToWrite);
			out.flush();
		} catch (Exception e) {
			throw new EnvisaException(e, "Write to socket failed.");
		}
	}

	public String readLine() throws Exception {
		if (in.ready())
			return in.readLine();
		else 
			return null;
	}

	//	public String read() throws EnvisaException {
	//		String line = "";
	//
	//		try {
	//			if (in.ready()) {
	//				line = readLine();
	//			}
	//			return line;
	//		}
	//		catch (SocketException e) {
	//			if (e.getMessage().equalsIgnoreCase("socket closed")) {
	//				throw new EnvisaException(e, "Socket closed... shutting down continuous read.");
	//			}
	//			else {
	//				throw new EnvisaException(e, "Continuous read of socket in new thread failed.");
	//			}
	//		}
	//		catch (Exception e) {
	//			throw new EnvisaException(e, "Continuous read of socket in new thread failed.");
	//		}
	//	}

	public String readBlocking() throws EnvisaException {
		String line = "";

		try {
			line = readLine();
			return line;
		}
		catch (SocketException e) {
			if (e.getMessage().equalsIgnoreCase("socket closed")) {
				throw new EnvisaException(e, "Socket closed... shutting down continuous read.");
			}
			else {
				throw new EnvisaException(e, "Continuous read of socket in new thread failed.");
			}
		}
		catch (Exception e) {
			throw new EnvisaException(e, "Continuous read of socket in new thread failed.");
		}
	}

	boolean open(String server, int port, int timeout) throws IOException {
		Session.logi("Opening connection to "+server+":"+port);
		socket = new Socket();
		socket.setSoLinger(false, timeout);
		socket.setSoTimeout(timeout);
		SocketAddress socketAddress = new InetSocketAddress(server, port);
		socket.connect(socketAddress, timeout);
		out = new OutputStreamWriter(socket.getOutputStream(), "US-ASCII");
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		Session.logi("Connection opened to "+server+":"+port);
		return true;
	}

	public boolean close() {
		try {
			if (out!=null) {
				Session.logi("Closing down existing connection");
				out.close();
			}
			if (in!=null) {
				in.close();
			}
			if (socket!=null) {
				socket.close();
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
		}
		return true;
	}

	public boolean ready() throws IOException {
		return in.ready();
	}

}
