package rvsguenther;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.*;

import chatserver.ChatServer;

public class ChatClient {
	
	public static class Server {
		
	}
	
	public static class Client {
		
		private Socket Adressbuchverbindung = new Socket();
		
		public void connect( String ip, int port ) throws UnknownHostException, IOException {
			this.Adressbuchverbindung.connect( new InetSocketAddress( ip, port ) );
		}	
		
		
	}
	
	public static class serverthread extends Thread {
		
		private boolean _terminate = false;
		private Client myClient;
		
		public serverthread( Client myClient ) {
			this.myClient = myClient;
		}
		
		MyThread() {
			_terminate = false;
		}
		
		private void terminate() {
			_terminate = true;
		}
		
		public void run() {
			while( !_terminate ) {

			}
		}
	}
	
	public static void main( String[] args ) throws NumberFormatException, UnknownHostException, IOException {
		Server myServer = new Server();
		Client myClient = new Client();
		
		serverthread server = new serverthread( myClient );
		server.start();
		
		Scanner S = new Scanner(System.in);
		System.out.println("Welcome! To show commands use 'help', type 'connect [ip:port]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
		while(true) {
			String[] eingaben = S.next().split(" ");
			switch( eingaben[0] ) {
				case "help":
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
				case "list":
					System.out.println();
					break;
				case "connect":
					System.out.println(eingaben.length);
					String[] address = eingaben[1].split(":");
					myClient.connect(address[0], new Integer( null ).valueOf( address[1] ));
				default:
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
			}

		}
	}

}
