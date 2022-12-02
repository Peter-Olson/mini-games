
import java.util.Scanner;

/**
 * Rodents Revenge
 * 
 * Based on the 1990 game from Windows
 * v1 Operating w/o GUI
 *
 * @author Mr Olson
 * @version 092822
 */
public class GameStart {
    
    private static Scanner userInput = new Scanner(System.in);
    
    public static void main( String[] args ) {
        String response = "";
        clearConsole();
        do {
            RodentsRevenge gameObject = new RodentsRevenge("levelOneSmall.txt");
            
            SOPln("Would you like to play again?");
            response = userInput.nextLine().toLowerCase().trim();
        } while( response.contains("y") || response.contains("again") ||
                 response.contains("ok") );
        
        clearConsole();
                 
        SOPln("Would you like to see the high-scores?");
        response = userInput.nextLine().toLowerCase().trim();
        //print high scores
    }
    
    /**
     * Clear the console
     */
    private static void clearConsole() {
        System.out.print('\u000C'); //Clear terminal
    }
    
    private static void SOPln( String str ) {
        System.out.println( str );
    }
}
