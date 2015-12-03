/**
 * Auteurs  : Marc Pellet et David Villa
 * Labo 	: 02 - PRR
 * 
 * DESCRIPTION :
 * 
 * Cette classe gère les tâches applicatives pouvant être executée par un utilisateur.
 * 
 * Pour le moment deux tâches sont implémentées.
 * 
 * La première consiste à récupérer et afficher la valeur entière partagée entre les managers
 * des différents sites (valeur en section critique). Cette opération ne doit pas être bloquante.
 * Cela signifie donc que si plusieurs utilisateurs sont connectés, cette opération doit toujours
 * pouvoir s'exécutée même si d'autres utilisateurs sont en attentes d'exclusion mutuelle ou en 
 * cours de modification de la valeur.
 * 
 * La seconde consiste à modifier la valeur entière partagée entre les managers des différents 
 * sites (valeur en section critique). Cette opération nécessite l'obtention d'une exclusion 
 * mutuelle entre les autres utilisateurs d'un même site ainsi qu'entre les managers des différents
 * sites. La valeur en section critique sera affichée avant et après modification.
 * 
 * INITIALISATION :
 * 
 * L'état de la classe est consultable grâce à la méthode isReady(). 
 * 
 * Afin d'avoir accès aux différentes tâches disponibles, l'utilisateur doit définir manuellement 
 * un port sur lequel écouter et envoyer des datagrammes (UDP).
 * 
 * De plus il doit connaitre et renseigner manuellement le port d'écoute du manager local au site
 * auquel l'utilisateur appartient.
 * 
 * Il est a noté que ce système n'est pas forcement le plus pratique, ni le plus intuitif pour un
 * utilisateur lambda. Mais ceci est dicté par le besoin de pouvoir exécuter plusieurs managers 
 * sur la même machine afin de pouvoir tester le bon fonctionnement du laboratoire.
 * 
 * Cette initialisation peut être effectuée avant l'instanciation de la classe, les données initalisées
 * sont alors fournies en argument au constructeur, ou après à l'aide de la méthode init().
 * 
 * UTILISATION :
 * 
 * Une fois le datagramme socket initialisé et le port du manager du site renseigné, les différentes
 * opérations sont accessibles à l'utilisateur.
 * 
 * l'utilisateur se verra proposer de choisir entre les différentes opérations disponibles.
 * 
 * Après chaque opération, l'utilisateur se verra proposer l'option de choisir une nouvelle opération
 * ou de quitter le programme.
 * 
 * CONCEPTION :
 * 
 * Type de message :
 * Chaque opération nécessite l'échange de différents types de message entre l'utilisateur et le manager 
 * (une requête et la réponse associée).
 * 
 * Nous avons donc définit les messages suivants :
 * Opération d'affichage :
 * GET_VALUE : aucun paramètre - signifie au manager que l'utilisateur souhaite obtenir la valeur
 * VALUE     : int value - reponse du serveur contenant la valeur demandée
 * 
 * Opération de modification :
 * UPDATE_VALUE  : int newValue - signifie au manager que l'utilisateur souhaite modifie la valeur
 * 						et la remplacer par la valeur 'newValue'
 * VALUE         : int value - reponse du serveur contenant la valeur avant modification
 * VALUE_UPDATED : int updatedValue - réponse du serveur contenant la valeur modifiée. Cette réponse
 * 						est envoyée uniquement lorsque l'opération est terminée
 * Remarques :
 * Nous n'utilisons pas de message entre client/client car même s'il doit y avoir une exclusion mutuelle
 * entre ces derniers pour la modification de la valeur en section critique. Cette exclusion mutuelle est 
 * directement gérée par le manager qui a, lui, connaissance de tous les utilisateurs connectés et gère 
 * l'exclusion mutuelle à l'aide d'une file d'attente.
 * 
 * De plus comme l'opération de modification est bloquante tant qu'elle ne s'est pas exécutée, il n'y a pas
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
	 * Constructeur vide, initialisation nécessaire avant de pouvoir utiliser les opérations
	 */
	public TaskManager(){
		socket = null;
		managerPort = -1;
	}

	/**
	 * Constructeur permettant d'initialiser le socket lors de l'instanciation de l'objet.
	 * 
	 * @param socket - DatagramSocket : socket utilisé par le client
	 * @param managerPort - int : port utilisé par le manager du site
	 */
	public TaskManager(DatagramSocket socket, int managerPort){
		this.socket = socket;
		this.managerPort = managerPort;
	}

	/**
	 * Méthode se chargeant de l'initialisation du socket et du port d'écoute du manager du site.
	 * Durant son exécution, il sera demandé à l'utilisateur de fournir le port d'écoute du manager
	 * du site ainsi que le port que l'utilisateur souhaite utiliser pour le socket.
	 *
	 * @throws SocketException - erreur lors de la création du socket.
	 */
	public void init() throws SocketException{
		int port;
		System.out.println("Veuillez renseigner le port sur lequel vous voulez travailler : ");
		port = new Integer(Services.scan.nextLine());
		socket = new DatagramSocket(port);
		System.out.println("Vous avez choisi le port suivant : " + port);

		System.out.println("Veuillez maintenant entrer le port d'écoute du manager du site : ");
		managerPort = new Integer(Services.scan.nextLine());
		System.out.println("Vous avez indiqué le port manager suivant : " + managerPort);
	}

	/**
	 * Retourne l'état de l'objet. A savoir si ce dernier est initialisé ou non.
	 * 
	 * @return boolean : etat de l'objet
	 */
	public boolean isReady(){
		return socket != null && managerPort != -1;
	}

	/**
	 * Méthode gérant l'opération permettant de demander au manager de nouve fournir la 
	 * valeur en section critique et de l'afficher.
	 * 
	 * @throws UnknownHostException - en cas de nom de serveur non-reconnu
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
	public void showValue() throws UnknownHostException, IOException{
		if (isReady()){
			// création et envoi du message
			String query = Message.MSG_GET_VALUE;
			sendDatagram(query);

			// reception de la réponse du manager (valeur courante de la section critique)
			String response = responseRecieved();
			//int value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur courante : " + value);
			System.out.println(response);
		} else {
			System.out.println("Datagram socket pas initialisé...");
		}
	}

	/**
	 * Méthode gérant l'opération permettant de mettre à jour la valeur en section critique.
	 * En plus de mettre à jour cette valeur, elle affiche la valeur avant modification puis la
	 * même valeur après modification.
	 * 
	 * @param newValue - int : nouvelle valeur que l'on souhaite donner à la valeur en 
	 * 		section critique
	 * @throws UnknownHostException - en cas de nom de serveur non-reconnu
	 * @throws IOException - en cas d'erreur lors de l'envoi ou la reception d'un message
	 */
	public void updateValue(int newValue) throws UnknownHostException, IOException{
		if (isReady()){
			// creation et envoi du message
			String query = Message.MSG_UPDATE_VALUE + ":" + newValue;
			sendDatagram(query);

			// reception de la 1ere réponse du manager (valeur avant modification)
			String response = responseRecieved();
			//int value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur avant modification : " + value);
			System.out.println(response);

			// reception de la 2ème réponse du manager (valeur après modification)
			response = responseRecieved();
			//value = Integer.parseInt(Services.extractToken(msg, ":", 1));
			//System.out.println("[Client] valeur apres modification : " + value);
			System.out.println(response);
		} else {
			System.out.println("Datagram socket pas initialisé...");
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
	 * Setter pour le port d'écoute du manager
	 * 
	 * @param port - int : port d'écoute du manager
	 */
	public void setManagerPort(int port){
		managerPort = port;
	}

	/**
	 * Getter pour le port d'écoute du manager
	 * 
	 * @return int : port d'écoute du manager
	 */
	public int getManagerPort(){
		return managerPort;
	}

	/**
	 * Méthode gérant l'envoi de message au manager
	 * 
	 * @param msg - String : Message à envoyer au manager
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
	 * Méthode gérant la réception de message du manager
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
