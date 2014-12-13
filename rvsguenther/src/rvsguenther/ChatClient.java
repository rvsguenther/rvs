package rvsguenther;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.*;

import chatserver.ChatServer;

public class ChatClient {
	
	public static class Server {
		
		private ServerSocket Server;
		
		public Server() throws IOException {
			this.Server = new ServerSocket();
			this.Server.bind( new InetSocketAddress( "127.0.0.1", 1234 ) );
		}
		
		public Client getClient() throws IOException {
			Socket Verbindung = this.Server.accept();
			return new Client( Verbindung );
		}
		
	}
	
	public static class Client {
		
		private Socket Verbindung;
		private String Name;
		
		public Client( Socket Verbindung ) {
			this.Verbindung = Verbindung;
		}
		
		public void setName( String Name ) {
			this.Name = Name;
		}
		
		public String getName() {
			return this.Name;
		}
		
		public String[] empfangeDaten() throws IOException {
			return new BufferedReader( new InputStreamReader( this.Verbindung.getInputStream() ) ).readLine().split(" "); 
		}
		
		
	}
	
	public static class AdressClient {
		
		private Socket Adressbuchverbindung = new Socket();
		
		public void connect( String ip, int port ) throws UnknownHostException, IOException {
			this.Adressbuchverbindung.connect( new InetSocketAddress( ip, port ) );
		}	
		
		
	}
	
	public static class serverthread extends Thread {
		
		private boolean _terminate = false;
		private Server myServer;
		
		public serverthread( Server myServer ) {
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
				try {
					new clientthread( this.myServer.getClient() ).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class clientthread extends Thread {
		
		private boolean _terminate = false;
		private Client myClient;
		
		public clientthread( Client myClient ) {
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
				String[] Daten = null;
				try {
					Daten = this.myClient.empfangeDaten();
				} catch (IOException e) {
					e.printStackTrace();
				}
				switch( Daten[0] ) {
					case "x":
						this.terminate();
						break;
					case "n":
						this.myClient.setName( Daten[1] );
						break;
					case "m":
						System.out.println( myClient.getName()+": "+Daten[1]);
						break;
					case "e":
						System.out.println( "Client sendet Fehler: "+Daten[1]);
					default:
						System.out.println( "Verstehe Client nicht." );
						break;	
				}
			}
		}
	}
	
	public static void main( String[] args ) throws NumberFormatException, UnknownHostException, IOException {
		Server myServer = new Server();
		AdressClient myAdressClient = new AdressClient();
		
		serverthread server = new serverthread( myServer );
		server.start();
		
		Scanner S = new Scanner(System.in);
		System.out.println("Welcome! To show commands use 'help', type 'connect [ip:port]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
		while(true) {
			String[] eingaben = S.nextLine().split(" ");
			switch( eingaben[0] ) {
				case "help":
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
				case "list":
					System.out.println();
					break;
				case "connect":
					String[] address = eingaben[1].split(":");
					myAdressClient.connect(address[0], Integer.parseInt( address[1] ));
				default:
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
			}

		}
	}

}
