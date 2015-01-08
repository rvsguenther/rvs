package rvsguenther;

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
import java.util.NoSuchElementException;
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
	// Scanner für die Eingabe
	private static Scanner SystemEin = new Scanner( System.in );
	
	public static Chatteilnehmer findeTeilnehmer( String Name ) {
		// Während Teilnehmer in der Liste sind jeweils den ersten Teilnehmer entfernen,
		// auf Matching untersuchen und falls ja, zurückgeben
		List<Chatteilnehmer> liste = new ArrayList<Chatteilnehmer>();
		for(int i = 0; i <= Teilnehmerliste.size()-1; i++) {
			liste.add( Teilnehmerliste.get(i) );
		}
		while( !liste.isEmpty() ) {
			Chatteilnehmer AktuellerTeilnehmer = liste.remove(0);
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
				this.bindeServer(meinPort);
			}
			catch(IOException e) {
				System.err.println("Es ist ein Fehler aufgetreten.\nServer konnte nicht gestartet werden.\nProgramm wird beendet.");
				System.err.println(e.getLocalizedMessage());
				System.exit(-1);
			}
		}
		
		// Server an Port binden, falls Port in benutzung ist an anderen Port binden
		public void bindeServer( int port ) throws IOException {
			while( !this.Server.isBound() ) {
				try {
					this.Server.bind( new InetSocketAddress( "127.0.0.1", port ) );
				}
				catch( BindException e ) {
					System.err.println("Kann nicht an Adresse binden, da die Adresse bereits benutzt wird.");
					System.err.println("Bitte geben sie einen freien Port an, um an diesen zu binden.");
					System.err.print("[Port]: ");
					try {
						port = Integer.parseInt( SystemEin.nextLine() );
					}
					catch( NumberFormatException e1 ) {
						System.err.println("Port muss numerisch sein.");
					}
				}
				catch( IOException e ) {
					System.err.println( "Socket konnte nicht gebunden werden." );
					System.exit(-1);
				}
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
		private Scanner Eingaben;
		
		public Client( Socket Verbindung ) {
			this.Verbindung = Verbindung;
			if( this.Verbindung.isConnected() ) {
				try {
					this.Eingaben = new Scanner( new InputStreamReader( this.Verbindung.getInputStream() ) );
				} catch (IOException e) {
					System.err.println( "Kann Datenstrom nicht empfangen." );
				}
			}
		}
		
		public void setName( String Name ) {
			this.Name = Name;
		}
		
		public String getName() {
			return this.Name;
		}
		
		public boolean connected() {
			return this.Verbindung.isConnected();
		}
		
		// für die Verbindung zu anderen Clients
		public void connect( InetSocketAddress Adresse ) throws IOException {
			this.Verbindung.connect( Adresse );
			this.Eingaben = new Scanner( new InputStreamReader( this.Verbindung.getInputStream() ) );
		}
		
		// empfange einen Buffer der mit \n terminiert ist
		public String[] empfangeDaten() throws IOException {
			try {
				return Eingaben.nextLine().split(" ");
			}
			catch( NullPointerException e ) {
				return new String[0];
			}
		}
		
		// eine Line an den Empfänger senden
		public void sendeDaten( String Daten ) throws IOException {
			try {
				new PrintWriter( this.Verbindung.getOutputStream(), true ).println( Daten );
			}
			catch(IOException e) {
				System.err.println("Verbindung wurde zurückgesetzt.");
			}
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
		
		// Dauerschleife, welche Clientverbindungen annimmt und einen Thread für diese Startet
		public void run() {
			while( !_terminate ) {
				try {
					new clientthread( this.myServer.getClient() ).start();
				} catch (IOException e) {
					System.err.println( "Kann Clientverbindung nicht annehmen." );
				}
			}
		}
	}
	
	// In diesem Thread läuft die Clientverbindung, für jede Verbindung wird ein Thread erstellt
	// Die Threads werden vom Serverthread erstellt und beenden sich, wenn der Client die Verbindung schließt
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
		
		// Dauerschleife in der auf Eingaben gewartet wird, diese werden direkt verarbeitet
		public void run() {
			while( !_terminate ) {
				String[] Daten = null;
				try {
					Daten = this.myClient.empfangeDaten();
					switch( Daten[0] ) {
						case "x":
							try {
								this.myClient.close();
							} catch (IOException e) {
								System.err.println("Kann Clientverbindung nicht ordnungsgemäß schließen.");
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
							System.err.println( "Client sendet Fehler: "+Daten[1]);
						default:
							System.err.println( "Verstehe Client nicht." );
							break;	
					}
				}
				catch( ArrayIndexOutOfBoundsException e) {
					System.err.println("Keine Daten Empfangen.");
					System.err.println(e.getMessage());
				} catch( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void kommuniziere() throws IOException {
		System.out.print(">> ");
		String[] eingaben = SystemEin.nextLine().split(" ");
		switch( eingaben[0] ) {
			case "help":
				System.out.println("Type 'connect [adress]' to connect to a server.\nTry 'list' for a list of online users and 'message [name] [message]' to message a user.\nSet a name with 'name' [name], or exit with 'exit'.To show commands use 'help'.");
				break;
			case "list":
				myAdressClient.sendeDaten( "t" );
				String Answer2[] = myAdressClient.empfangeDaten();
				if( !Answer2[0].equals( "t" ) ) {
					System.err.println( "Server sendet verwirrende Antwort." );
					myAdressClient.close();
				}
				int numOfUsers1 = Integer.parseInt( Answer2[1] );
				Teilnehmerliste.clear();
				System.out.println("Angemeldet sind:");
				for(int j = 1; j <= numOfUsers1; j++) {
					String[] Teil1 = myAdressClient.empfangeDaten();
					System.out.println(Teil1[0]);
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
					System.err.println(Answer[0]+" "+Answer[1]);
					break;
				}
				myAdressClient.sendeDaten( "t" );
				String Answer1[] = myAdressClient.empfangeDaten();
				if( !Answer1[0].equals( "t" ) ) {
					System.err.println( "Server sendet verwirrende Antwort." );
					myAdressClient.close();
				}
				int numOfUsers = Integer.parseInt( Answer1[1] );
				Teilnehmerliste.clear();
				System.out.println("Angemeldet sind:");
				for(int j = 1; j <= numOfUsers; j++) {
					String[] Teil1 = myAdressClient.empfangeDaten();
					System.out.println(Teil1[0]);
					Teilnehmerliste.add( new Chatteilnehmer( Teil1[0], Teil1[1], Integer.parseInt( Teil1[2] ) ) );
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
			case "exit":
				SystemEin.close();
				System.exit(0);
			case "name":
				meinName = eingaben[1];
				System.out.println("Hallo "+meinName+"!");
				if( myAdressClient.connected() ) {
					myAdressClient.sendeDaten( "n "+meinName+" "+new Integer( meinPort ).toString() );
					System.out.println("Name am Server geändert.");
				}
				break;
			default:
				System.out.println("To show commands use 'help'.");
				break;
			}
		}
	
	public static void main( String[] args ) throws NumberFormatException, UnknownHostException, IOException {
		try {
			Server myServer = new Server(meinPort);
			serverthread server = new serverthread( myServer );
			server.start();
			SystemEin = new Scanner( System.in );
		}
		catch(IOException e) {
			System.err.println("Fehler beim Programmstart.");
			System.err.println(e.getLocalizedMessage());
			System.exit(-1);
		}
		System.out.println("Welcome! To show commands use 'help' Please set a name first with 'name' [name].");
		while(true) {
			try {
				kommuniziere();
			}
			catch(NoSuchElementException e) {
			}
			catch(NullPointerException e) {
			}
			catch(NumberFormatException e) {
				System.err.println("Port muss numerisch sein.");
			}
			catch(IOException e) {
				System.err.println("Nicht verbunden.");
			}
		}
	}

}
