package rvsguenther;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatClient {
	
	// Teilnehmerliste vom Adressbuchserver
	private static List<Chatteilnehmer> Teilnehmerliste = new ArrayList<Chatteilnehmer>();
	// Name des Teilnehmers
	private static String meinName = "Chatteilnehmer";
	// Port des Teilnehmers
	private static int meinPort = 1337;
	// Client für die Verbindung zum Adressbuchserver
	private static Client myAdressClient = new Client( new Socket() );
	
	public static Chatteilnehmer findeTeilnehmer( String Name ) {
		// Während Teilnehmer in der Liste sind jeweils den ersten Teilnehmer entfernen,
		// auf Matching untersuchen und falls ja, zurückgeben
		while( !Teilnehmerliste.isEmpty() ) {
			Chatteilnehmer AktuellerTeilnehmer = Teilnehmerliste.remove(0);
			if( AktuellerTeilnehmer.getName().equals( Name ) ) 
				return AktuellerTeilnehmer;
		} 
		// Teilnehmer nicht gefunden, null zurückgeben
		return null;
	}
	
	// Klasse für die Teilnehmer aus der Liste des Adressbuchservers
	public static class Chatteilnehmer {
		
		// 
		private String name ="Chatteilnehmer", IP = "127.0.0.1";
		private int Port = 1337;
		
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
	
	// Klasse für den Server
	public static class Server {
		
		private ServerSocket Server;
		
		public Server( int port ) throws IOException {
			try {
				this.Server = new ServerSocket();
				this.Server.bind( new InetSocketAddress( "127.0.0.1", port ) );
			}
			catch(BindException e) {
				System.err.println("Kann nicht an Adresse binden, da die Adresse bereits benutzt wird.\nProgramm wird beendet.");
				System.exit(-1);
			}
			catch(IOException e) {
				System.err.println("Es ist ein Fehler aufgetreten.\nProgramm wird beendet.");
				System.exit(-1);
			}
		}
		
		// blockiert bis Verbindung ankommt, gibt dann neuen Client mit Verbindung zurück
		public Client getClient() throws IOException {
			try {
				Socket Verbindung = this.Server.accept();
				return new Client( Verbindung );
			}
			catch(IOException e) {
				System.err.println("Konnte Clientverbindung nicht verarbeiten.");
				return null;
			}
		}
		
	}
	
	// eigentliche ChatClient Klasse
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
		
		// für die Verbindung zu anderen Clients
		public void connect( InetSocketAddress Adresse ) throws IOException {
			this.Verbindung.connect( Adresse );
		}
		
		// empfange einen Buffer der mit \n terminiert ist
		public String[] empfangeDaten() throws IOException {
			try {
				BufferedReader Reader = new BufferedReader( new InputStreamReader( this.Verbindung.getInputStream() ) );
				return Reader.readLine().split(" ");
			}
			catch(IOException e) {
				System.err.println("Verbindung wurde zurückgesetzt.");
				return null;
			}
		}
		
		// für den Empfang der Teilnehmerliste am Adressbuchserver
		public String[] readListe() throws IOException {
			try {
				// ArrayList für die Liste
				List<String> Inhalt = new ArrayList<String>();
				BufferedReader Reader = new BufferedReader( new InputStreamReader( this.Verbindung.getInputStream() ) );
				// Buffer char-weise ablesen und bei \n und \r den String in die Liste speichern,
				// dann String zurücksetzen und weiterlesen, solange gesendet wird
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
			catch(IOException e) {
				System.err.println("Verbindung wurde zurückgesetzt.");
				return null;
			}
		}
		
		// eine Line an den Empfänger senden
		public void sendeDaten( String Daten ) throws IOException {
			new PrintWriter( this.Verbindung.getOutputStream(), true ).println( Daten );
		}
		
		private void close() throws IOException {
			this.Verbindung.close();
		}
		
	}
	
	// Der Thread für die Serverfunktionalität
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
							//fehler
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
		Server myServer = new Server(meinPort);
		serverthread server = new serverthread( myServer );
		server.start();
		
		Scanner S = new Scanner(System.in);
		System.out.println("Welcome! To show commands use 'help', type 'connect [ip:port]' to connect to a server.\nTry 'list' for a list of online users and 'message [name] [message]' to message a user.\n'Exit' closes the programm.");
		System.out.print(">> ");
		while(true) {
			String[] eingaben = S.nextLine().split(" ");
			switch( eingaben[0] ) {
				case "help":
					System.out.println("Welcome! To show commands use 'help', type 'connect [adress]' to connect to a server. Try 'list' for a list of online users and 'message [name] [message]' to message a user.");
					System.out.print(">> ");
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
