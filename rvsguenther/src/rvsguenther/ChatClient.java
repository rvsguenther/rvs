package rvsguenther;

import java.util.Scanner;
import java.util.concurrent.*;

import chatserver.ChatServer;

public class ChatClient {
	
	public static class Server {
		
	}
	
	public static class Client {
		
	}
	
	public static class clientthread extends Thread {
		
		private boolean _terminate = false;
		private Server myServer;
		
		public clientthread( Server myServer ) {
			this.myServer = myServer;
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
	
	public static void main( String[] args ) {
		Server myServer = new Server();
		Client myClient = new Client();
		
		serverthread server = new serverthread( myClient );
		clientthread client = new clientthread( myServer );
		server.start();
		client.start();
		
		Scanner S = new Scanner(System.in);
		System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
		while(true) {
			String[] eingaben = S.next().split(" ");
			switch( eingaben[0] ) {
				case "help":
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
				case "list":
					System.out.println(test.get(i++));
					break;
				default:
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
			}

		}
	}

}
