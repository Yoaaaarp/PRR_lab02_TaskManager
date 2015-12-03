/**
 * Auteurs  : Marc Pellet et David Villa
 * Labo 	: 02 - PRR
 */

import java.util.Scanner;

public class Services {
	public static Scanner scan;
	public static String extractToken(String str, String delim, int index) {
		String[] temp = str.split(delim);
		return temp[index];
	}
}
