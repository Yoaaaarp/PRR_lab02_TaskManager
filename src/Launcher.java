/**
 * Auteurs  : Marc Pellet et David Villa
 * Labo 	: 02 - PRR
 * 
 * DESCRIPTION : 
 * 
 * Classe principale du programme utilisateur.
 * 
 * Cette dernière s'occupe de créer et initialiser le TaskManager.
 * 
 * Une fois le TaskManager opérationnel, il sera proposer à l'utilisateur trois opérations.
 * - quitter le programme
 * - afficher la valeur en section critique
 * - modifier la valeur en section critique
 * 
 * Ce choix sera reproposé après chaque exécution des deux dernières opérations jusqu'à ce
 * que l'utilisateur décide de quitter le programme.
 * 
 */
import java.util.Scanner;

public class Launcher {
	public static void main(String[] args) {
		// initialisation du scanner pour partage au sein du programme
		Services.scan = new Scanner(System.in);
		// instanciation du gestionnaire de tâches
		TaskManager tm = new TaskManager();
		
		try {
			// initialisation du socket et du port d'écoute du manager
			tm.init();
			
			boolean running = true;
			int choice;
			// permet l'exécution d'opération tant que l'utilisateur le désire
			while(running){
				// l'utilisateur doit choisir l'opération désirée
				choice = choiceFromMenu();
				switch(choice){
				case 0 : // ce choix quitte le programme
					running = false;
					System.out.println("Merci d'avoir utilisé notre programme!");
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
			System.out.println("Choisissez une opération parmis les suivantes : ");
			System.out.println("0 - Quitter le programme");
			System.out.println("1 - Afficher la valeur en section critique");
			System.out.println("2 - Modifier la valeur en section critique");
			choice = new Integer(Services.scan.nextLine());
		} while(choice < 0 || choice > 2);
		return choice;
	}
}
