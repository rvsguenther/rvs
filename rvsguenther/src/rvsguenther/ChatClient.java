package rvsguenther;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import chatserver.ChatServer;

public class ChatClient {
	
	private static List<Chatteilnehmer> Teilnehmerliste = new ArrayList<Chatteilnehmer>();
	private static String meinName = "Chatteilnehmer";
	private static int meinPort = 1234;
	private static Socket Verbindung = new Socket();
	private static Client myAdressClient = new Client( Verbindung );
	
	public static Chatteilnehmer findeTeilnehmer( String Name ) {
		while( !Teilnehmerliste.isEmpty() ) {
			Chatteilnehmer AktuellerTeilnehmer = Teilnehmerliste.remove(0);
			if( AktuellerTeilnehmer.getName().equals( Name ) ) 
				return AktuellerTeilnehmer;
		} 
		return null;
	}
	
	public static class Chatteilnehmer {
		
		private String name ="Chatteilnehmer", IP = "127.0.0.1";
		private int Port = 1234;
		
		public Chatteilnehmer( String name, String IP, int Port )  {
			this.name = name;
			this.IP = IP;
			this.Port = Port;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getIP() {
			return this.IP;
		}
		
		public int getPort() {
			return this.Port;
		}
		
	}
	
	public static class Server {
		
		private ServerSocket Server;
		
		public Server() throws IOException {
			this.Server = new ServerSocket();
			this.Server.bind( new InetSocketAddress( "127.0.0.1", 1234 ) );
			//System.out.println("Server lauscht.");
		}
		
		public Client getClient() throws IOException {
			//System.out.println("Server wartet auf Verbindung.");
			Socket Verbindung = this.Server.accept();
			//System.out.println("Server bekommt Verbindung.");
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
		
		public void connect( InetSocketAddress Adresse ) throws IOException {
			this.Verbindung.connect( Adresse );
		}
		
		public String[] empfangeDaten() throws IOException {
			BufferedReader Reader = new BufferedReader( new InputStreamReader( this.Verbindung.getInputStream() ) );
			/*if( Reader.ready() )
				return Reader.readLine().split(" ");
			else
				return null;*/
			return Reader.readLine().split(" ");
		}
		
		public String[] readListe() throws IOException {
			List<String> Inhalt = new ArrayList<String>();
			BufferedReader Reader = new BufferedReader( new InputStreamReader( this.Verbindung.getInputStream() ) );
			String tmp = "";
			char r = 0;
			do {
			r = (char) Reader.read();
				if( r != '\n' && r!= '\r' )
					tmp += String.valueOf(r);
				else {
					Inhalt.add(tmp);
					tmp = "";
				}
			} while( Reader.ready() );
			return Inhalt.toArray(new String[0]);
		}
		
		public void sendeDaten( String Daten ) throws IOException {
			new PrintWriter( this.Verbindung.getOutputStream(), true ).println( Daten );
		}
		
		private void close() throws IOException {
			this.Verbindung.close();
		}
		
	}
	
	public static class serverthread extends Thread {
		
		private boolean _terminate = false;
		private Server myServer;
		
		public serverthread( Server myServer ) {
			this.myServer = myServer;
		}
		
		void MyThread() {
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
		
		void MyThread() {
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
						try {
							this.myClient.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
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
					myAdressClient.sendeDaten( "t" );
					String Answer2[] = myAdressClient.readListe();
					String[] Answer21 = Answer2[0].split(" ");
					if( !Answer21[0].equals( "t" ) ) {
						System.err.println( "Server sendet verwirrende Antwort." );
						myAdressClient.close();
					}
					int numOfUsers1 = Integer.parseInt( Answer21[1] );
					Teilnehmerliste.clear();
					for(int j = 1; j <= numOfUsers1; j++) {
						System.out.println( Answer2[j] );
						String[] Teil1 = Answer2[j].split(" ");
						Teilnehmerliste.add( new Chatteilnehmer( Teil1[0], Teil1[1], Integer.parseInt( Teil1[2] ) ) );
					}
					break;
				case "connect":
					String[] address = eingaben[1].split(":");
					myAdressClient.connect( new InetSocketAddress( address[0], Integer.parseInt( address[1] ) ) );
					myAdressClient.sendeDaten( "n "+meinName+" "+new Integer( meinPort ).toString() );
					String[] Answer = myAdressClient.empfangeDaten();
					if( Answer[0].equals( "s" ) )
						System.out.println( "Erfolgreich am Server angemeldet." );
					else {
						System.err.println( "Konnte nicht am Server anmelden." );
						myAdressClient.close();
						break;
					}
					myAdressClient.sendeDaten( "t" );
					Answer = myAdressClient.readListe();
					String[] Answer1 = Answer[0].split(" ");
					if( !Answer1[0].equals( "t" ) ) {
						System.err.println( "Server sendet verwirrende Antwort." );
						myAdressClient.close();
					}
					int numOfUsers = Integer.parseInt( Answer1[1] );
					Teilnehmerliste.clear();
					for(int j = 1; j <= numOfUsers; j++) {
						System.out.println( Answer[j] );
						String[] Teil = Answer[j].split(" ");
						Teilnehmerliste.add( new Chatteilnehmer( Teil[0], Teil[1], Integer.parseInt( Teil[2] ) ) );
					}
					break;
				case "message":
					Chatteilnehmer MeinBuddy = findeTeilnehmer( eingaben[1] );
					if( MeinBuddy == null ) {
						System.out.println( "Teilnehmer nicht gefunden." );
						break;
					}
					Socket Verbindung1 = new Socket();
					Verbindung1.connect( new InetSocketAddress( MeinBuddy.getIP(), MeinBuddy.getPort() ) );
					Client meinClient = new Client( Verbindung1 );
					meinClient.sendeDaten( "n "+meinName );
					meinClient.sendeDaten( "m "+eingaben[2] );
					meinClient.sendeDaten( "x byebye" );
					meinClient.close();
					break;
				default:
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					break;
			}

		}
	}

}
