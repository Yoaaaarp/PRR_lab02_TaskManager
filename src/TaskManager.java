/**
 * Auteurs  : Marc Pellet et David Villa
 * Labo 	: 02 - PRR
 * 
 * DESCRIPTION :
 * 
 * Cette classe g�re les t�ches applicatives pouvant �tre execut�e par un utilisateur.
 * 
 * Pour le moment deux t�ches sont impl�ment�es.
 * 
 * La premi�re consiste � r�cup�rer et afficher la valeur enti�re partag�e entre les managers
 * des diff�rents sites (valeur en section critique). Cette op�ration ne doit pas �tre bloquante.
 * Cela signifie donc que si plusieurs utilisateurs sont connect�s, cette op�ration doit toujours
 * pouvoir s'ex�cut�e m�me si d'autres utilisateurs sont en attentes d'exclusion mutuelle ou en 
 * cours de modification de la valeur.
 * 
 * La seconde consiste � modifier la valeur enti�re partag�e entre les managers des diff�rents 
 * sites (valeur en section critique). Cette op�ration n�cessite l'obtention d'une exclusion 
 * mutuelle entre les autres utilisateurs d'un m�me site ainsi qu'entre les managers des diff�rents
 * sites. La valeur en section critique sera affich�e avant et apr�s modification.
 * 
 * INITIALISATION :
 * 
 * L'�tat de la classe est consultable gr�ce � la m�thode isReady(). 
 * 
 * Afin d'avoir acc�s aux diff�rentes t�ches disponibles, l'utilisateur doit d�finir manuellement 
 * un port sur lequel �couter et envoyer des datagrammes (UDP).
 * 
 * De plus il doit connaitre et renseigner manuellement le port d'�coute du manager local au site
 * auquel l'utilisateur appartient.
 * 
 * Il est a not� que ce syst�me n'est pas forcement le plus pratique, ni le plus intuitif pour un
 * utilisateur lambda. Mais ceci est dict� par le besoin de pouvoir ex�cuter plusieurs managers 
 * sur la m�me machine afin de pouvoir tester le bon fonctionnement du laboratoire.
 * 
 * Cette initialisation peut �tre effectu�e avant l'instanciation de la classe, les donn�es initalis�es
 * sont alors fournies en argument au constructeur, ou apr�s � l'aide de la m�thode init().
 * 
 * UTILISATION :
 * 
 * Une fois le datagramme socket initialis� et le port du manager du site renseign�, les diff�rentes
 * op�rations sont accessibles � l'utilisateur.
 * 
 * l'utilisateur se verra proposer de choisir entre les diff�rentes op�rations disponibles.
 * 
 * Apr�s chaque op�ration, l'utilisateur se verra proposer l'option de choisir une nouvelle op�ration
 * ou de quitter le programme.
 * 
 * CONCEPTION :
 * 
 * Type de message :
 * Chaque op�ration n�cessite l'�change de diff�rents types de message entre l'utilisateur et le manager 
 * (une requ�te et la r�ponse associ�e).
 * 
 * Nous avons donc d�finit les messages suivants :
 * Op�ration d'affichage :
 * GET_VALUE : aucun param�tre - signifie au manager que l'utilisateur souhaite obtenir la valeur
 * VALUE     : int value - reponse du serveur contenant la valeur demand�e
 * 
 * Op�ration de modification :
 * UPDATE_VALUE  : int newValue - signifie au manager que l'utilisateur souhaite modifie la valeur
 * 						et la remplacer par la valeur 'newValue'
 * VALUE         : int value - reponse du serveur contenant la valeur avant modification
 * VALUE_UPDATED : int updatedValue - r�ponse du serveur contenant la valeur modifi�e. Cette r�ponse
 * 						est envoy�e uniquement lorsque l'op�ration est termin�e
 * Remarques :
 * Nous n'utilisons pas de message entre client/client car m�me s'il doit y avoir une exclusion mutuelle
 * entre ces derniers pour la modification de la valeur en section critique. Cette exclusion mutuelle est 
 * directement g�r�e par le manager qui a, lui, connaissance de tous les utilisateurs connect�s et g�re 
 * l'exclusion mutuelle � l'aide d'une file d'attente.
 * 
 * De plus comme l'op�ration de modification est bloquante tant qu'elle ne s'est pas ex�cut�e, il n'y a pas
 * besoin d'ajouter de message pour signifier que la section critique n'est pas encore disponible.
 * 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class TaskManager {
	private DatagramSocket socket;
	private int managerPort;

	/**
	 * Constructeur vide, initialisation n�cessaire avant de pouvoir utiliser les op�rations
	 */
	public TaskManager(){
		socket = null;
		managerPort = -1;
	}

	/**
	 * Constructeur permettant d'initialiser le socket lors de l'instanciation de l'objet.
	 * 
	 * @param socket - DatagramSocket : socket utilis� par le client
	 * @param managerPort - int : port utilis� par le manager du site
	 */
	public TaskManager(DatagramSocket socket, int managerPort){
		this.socket = socket;
		this.managerPort = managerPort;
	}

	/**
	 * M�thode se chargeant de l'initialisation du socket et du port d'�coute du manager du site.
	 * Durant son ex�cution, il sera demand� � l'utilisateur de fournir le port d'�coute du manager
	 * du site ainsi que le port que l'utilisateur souhaite utiliser pour le socket.
	 *
	 * @throws SocketException - erreur lors de la cr�ation du socket.
	 */
	public void init() throws SocketException{
		int port;
		System.out.println("Veuillez renseigner le port sur lequel vous voulez travailler : ");
		port = new Integer(Services.scan.nextLine());
		socket = new DatagramSocket(port);
		System.out.println("Vous avez choisi le port suivant : " + port);

		System.out.println("Veuillez maintenant entrer le port d'�coute du manager du site : ");
		managerPort = new Integer(Services.scan.nextLine());
		System.out.println("Vous avez indiqu� le port manager suivant : " + managerPort);
	}

	/**
	 * Retourne l'�tat de l'objet. A savoir si ce dernier est initialis� ou non.
	 * 
	 * @return boolean : etat de l'objet
	 */
	public boolean isReady(){
		return socket != null && managerPort != -1;
	}

	/**
	 * M�thode g�rant l'op�ration permettant de demander au manager de nouve fournir la 
	 * valeur en section critique et de l'afficher.
	 * 
	 * @throws UnknownHostException - en cas de nom de serveur non-reconnu
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
	public void showValue() throws UnknownHostException, IOException{
		if (isReady()){
			// cr�ation et envoi du message
			String query = Message.MSG_GET_VALUE;
			sendDatagram(query);

			// reception de la r�ponse du manager (valeur courante de la section critique)
			String response = responseRecieved();
			//int value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur courante : " + value);
			System.out.println(response);
		} else {
			System.out.println("Datagram socket pas initialis�...");
		}
	}

	/**
	 * M�thode g�rant l'op�ration permettant de mettre � jour la valeur en section critique.
	 * En plus de mettre � jour cette valeur, elle affiche la valeur avant modification puis la
	 * m�me valeur apr�s modification.
	 * 
	 * @param newValue - int : nouvelle valeur que l'on souhaite donner � la valeur en 
	 * 		section critique
	 * @throws UnknownHostException - en cas de nom de serveur non-reconnu
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
	public void updateValue(int newValue) throws UnknownHostException, IOException{
		if (isReady()){
			// creation et envoi du message
			String query = Message.MSG_UPDATE_VALUE + ":" + newValue;
			sendDatagram(query);

			// reception de la 1ere r�ponse du manager (valeur avant modification)
			String response = responseRecieved();
			//int value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur avant modification : " + value);
			System.out.println(response);

			// reception de la 2�me r�ponse du manager (valeur apr�s modification)
			response = responseRecieved();
			//value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur apres modification : " + value);
			System.out.println(response);
		} else {
			System.out.println("Datagram socket pas initialis�...");
		}
	}

	/**
	 * Setter pour le socket
	 * 
	 * @param socket - DatagramSocket : socket de l'utilisateur
	 */
	public void setDatagramSocket(DatagramSocket socket){
		this.socket = socket;
	}

	/**
	 * Getter pour le socket
	 * 
	 * @return DatagramSocket : le socket de l'utilisateur
	 */
	public DatagramSocket getDatagramSocket(){
		return socket;
	}

	/**
	 * Setter pour le port d'�coute du manager
	 * 
	 * @param port - int : port d'�coute du manager
	 */
	public void setManagerPort(int port){
		managerPort = port;
	}

	/**
	 * Getter pour le port d'�coute du manager
	 * 
	 * @return int : port d'�coute du manager
	 */
	public int getManagerPort(){
		return managerPort;
	}

	/**
	 * M�thode g�rant l'envoi de message au manager
	 * 
	 * @param msg - String : Message � envoyer au manager
	 */
	private void sendDatagram(String msg) throws UnknownHostException, IOException{
		byte[] queryBuffer = new byte[256];
		// creation du message
		InetAddress addr = InetAddress.getByName("David-PC");
		queryBuffer = msg.getBytes();
		DatagramPacket query = new DatagramPacket(queryBuffer, queryBuffer.length, addr, managerPort);

		// envoi du message au manager
		socket.send(query);
	}

	/**
	 * M�thode g�rant la r�ception de message du manager
	 * 
	 * @return String : Reponse du manager
	 */
	private String responseRecieved() throws IOException{
		byte[] responseBuffer = new byte[256];
		DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
		socket.receive(response);

		return new String(response.getData(), response.getOffset(), response.getLength());
	}
}
