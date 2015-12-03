/**
 * Auteurs  : Marc Pellet et David Villa
 * Labo 	: 02 - PRR
 * 
 * DESCRIPTION : 
 * 
 * Classe principale du programme utilisateur.
 * 
 * Cette derni�re s'occupe de cr�er et initialiser le TaskManager.
 * 
 * Une fois le TaskManager op�rationnel, il sera proposer � l'utilisateur trois op�rations.
 * - quitter le programme
 * - afficher la valeur en section critique
 * - modifier la valeur en section critique
 * 
 * Ce choix sera repropos� apr�s chaque ex�cution des deux derni�res op�rations jusqu'� ce
 * que l'utilisateur d�cide de quitter le programme.
 * 
 */
import java.util.Scanner;

public class Launcher {
	public static void main(String[] args) {
		// initialisation du scanner pour partage au sein du programme
		Services.scan = new Scanner(System.in);
		// instanciation du gestionnaire de t�ches
		TaskManager tm = new TaskManager();
		
		try {
			// initialisation du socket et du port d'�coute du manager
			tm.init();
			
			boolean running = true;
			int choice;
			// permet l'ex�cution d'op�ration tant que l'utilisateur le d�sire
			while(running){
				// l'utilisateur doit choisir l'op�ration d�sir�e
				choice = choiceFromMenu();
				switch(choice){
				case 0 : // ce choix quitte le programme
					running = false;
					System.out.println("Merci d'avoir utilis� notre programme!");
					break;
				case 1 : // ce choix permet d'afficher la valeur en section critique
					tm.showValue();
					break;
				case 2 : // ce choix permet de modifier la valeur en section critique
					System.out.println("Veuillez entrer la nouvelle valeur : ");
					Scanner scan = new Scanner(System.in);
					tm.updateValue(new Integer(scan.nextLine()));
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Services.scan.close();
	}

	private static int choiceFromMenu(){
		int choice;
		do{
			System.out.println("Choisissez une op�ration parmis les suivantes : ");
			System.out.println("0 - Quitter le programme");
			System.out.println("1 - Afficher la valeur en section critique");
			System.out.println("2 - Modifier la valeur en section critique");
			choice = new Integer(Services.scan.nextLine());
		} while(choice < 0 || choice > 2);
		return choice;
	}
}
