import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
   ConnectFour.java
   
   Project 3 Code for APCSP by Mr. Olson
   
   This class allows for a user to play ConnectFour within the console
   
   The user can play ConnectFour against another player (operating on the
   same computer), or can play against an AI.
   
   Additionally, this program can simulate games from text files
   
   --------------------------------------------------------------------------------------------------------------------------------------------------
   Method Branch Structure
   
   Main
   - runConnectFour( int gameMode ) -> runConnectFour( int gameMode, int currentGameNumber ) -- returns boolean            : Play a game of ConnectFour in the designed game mode
      - getLines( String fileLoc )                         -- returns String[]   : Retrieves the contents from a file
      - getFileMoves( String fileLoc, int lineNumber )     -- returns String     : Gets the game moves for the given line number
      - printBoard( int[][] board )                                              : Prints the current state of the board
      - getAIMove( int[][] board )                         -- returns int        : Gets the next move for the AI
      - addToken( int col, int turnNumber, int[][] board ) -- returns int[][]    : Adds a token to the board at the specified column
      - winLossOrDraw( int[][] board )                     -- returns int        : Determines whether the current board has a win, loss, or draw condition
         - checkWinOrLoss( int[][] board )                 -- returns int        : Determines whether the current board has a win or loss condition
            - checkHorizontals( int[][] board )            -- returns int        : Determines whether there is a win or loss in the horizontal direction
            - checkVerticals( int[][] board )              -- returns int        : Determines whether there is a win or loss in the vertical direction
            - checkLeftDiagonals( int[][] board )          -- returns int        : Determines whether there is a win or loss in the left diagonal direction
            - checkRightDiagonals( int[][] board )         -- returns int        : Determines whether there is a win or loss in the right diagonal direction
         - checkIfDraw( int[][] board )                    -- returns int        : Determines whether the current board has a draw condition
      - addGameToFile( ArrayList<Integer> gameMoves )                            : Adds the list of game moves to a file
         - writeToFile( String[] lines, String fileLoc )                         : Write lines to a file
      - recordWinLossOrDraw( int gameState )                                     : Adds the win, loss, or draw to the recordbook
         - getLines( String fileLoc )                      -- returns String[]   : Retrieves the contents from a file
         - clearFile( String fileLoc )                                           : Erases the contents of a file
         - writeToFile( String[] lines, String fileLoc )                         : Write lines to a file
   - runConnectFourPlayer() -> runConnectFour( int gameMode )                    : Play a game of ConnectFour against another player
   - runConnectFourAI()     -> runConnectFour( int gameMode )                    : Play a game of ConnectFour against the AI
   - runConnectFourFile()   -> runConnectFour( int gameMode )                    : Play a game of ConnectFour from a File
   - runConnectFourRandom() -> runConnectFour( int gameMode )                    : Play a game of ConnectFour using Random moves
   - printWinsAndLosses()                                                        : Display the history of wins, losses, and draws
      - getLines( String fileLoc )                         -- returns String[]   : Retrieves the contents from a file
   - resetWinsAndLosses()                                                        : Reset the history of wins, losses, and draws
      - clearFile( String fileLoc )                                              : Erases the contents of a file
      - writeToFile( String[] lines, String fileLoc )                            : Writes lines to a file
   
   @author Peter Olson
   @version 1.0
*/
public class ConnectFour {

   /*
      The file that contains the total number of wins and losses. This allows for the total wins and losses
      to be remembered across multiple games. Wins and losses that are calculated from synthetically run
      games that are stored in files (which are used for unit testing) are not recorded
   */
   private static final String WIN_LOSS_FILE_LOC = "./winLoss.txt";
   private static final String CONNECT_FOUR_GAMES = "./connectFourGames.txt"; //contains the moves which can be interpreted to run a game of ConnectFour
   private static final boolean ALLOW_RESET_WINS_AND_LOSSES = true; //@@CHOOSE to allow the ability to reset wins and losses
   
   private static final int TOTAL_ROWS = 6;
   private static final int TOTAL_COLS = 7;
   private static final int TOTAL_TO_SCORE = 4; //consecutive tokens horizontally, vertically, or diagonally
   
   //set colors and shapes of tokens
   private static final char PLAYER_1_TOKEN = 'O'; //@@CHOOSE token shape
   private static final char PLAYER_2_TOKEN = 'X';
   private static final String PLAYER_1_COLOR = "RED"; //@@CHOOSE player color
   private static final String PLAYER_2_COLOR = "YELLOW";
   //private static final String ANSI_PLAYER_1 = ANSI_RED; //@@CHOOSE player color
   //private static final String ANSI_PLAYER_2 = ANSI_YELLOW; 

   private static final int PLAYER_1_TOKEN_VAL = 1; //This represents the value stored in the board, which is replaced by PLAYER_1_TOKEN
   private static final int PLAYER_2_TOKEN_VAL = 2; //This represents the value stored in the board, which is replaced by PLAYER_2_TOKEN

   //Set the states of whether PLAYER_1 has won, lost, or tied, or if the game is not over yet
   private static final int GAME_CONTINUE = 0;
   private static final int WIN  = 1;
   private static final int LOSE = 2;
   private static final int DRAW = 3;
   
   //Set the gamemode state
   private static final int PLAYER = 0;
   private static final int AI     = 1;
   private static final int FILE   = 2;
   private static final int RANDOM = 3;
   
   private static final int NO_FILE = -1; //For game modes that do not use a File for game move input

   private static Scanner scanner = new Scanner( System.in );

   /**
      Runs the game and allows the user to select the mode of operator
      
      @param args The argument given to this program
      @see runConnectFourPlayer()
      @see runConnectFourAI()
      @see runConnectFourFile()
      @see runConnectFourRandom()
      @see printWinsAndLosses()
      @see resetWinsAndLosses()
   */
   public static void main( String[] args ) {
      
      boolean isRunning = true;
      int gameState = GAME_CONTINUE; //used to keep track of total wins
      
      //Show that resetting wins and losses is available or not
      String resetMenuItem = "\tf. Reset wins and losses\n";
      if( !ALLOW_RESET_WINS_AND_LOSSES ) resetMenuItem = "";
      
      do {
      
         SOPln("Welcome to Mr. Olson's ConnectFour Game!\n\n" +
               "What would you like to do?");
         
         SOPln("\ta. Play against friend\n" +
               "\tb. Play against computer\n" +
               "\tc. Run a game from a file\n" +
               "\td. Run a random game\n" +
               "\te. View total wins and losses\n" +
               resetMenuItem +
               "\tq. Quit");
         
         String response = scanner.nextLine();
         response.toUpperCase();
         char choice;
         
         //Get character response
         if( response.length() > 1 ) {
            if(      response.contains("FRIEND") || response.contains("A.") )   choice = 'a';
            else if( response.contains("COMPUTER") || response.contains("B.") ) choice = 'b';
            else if( response.contains("FILE") || response.contains("C.") )     choice = 'c';
            else if( response.contains("RANDOM") || response.contains("D.") )   choice = 'd';
            else if( response.contains("VIEW") || response.contains("E.") )     choice = 'e';
            else if( ALLOW_RESET_WINS_AND_LOSSES &&
                   (response.contains("RESET") || response.contains("F.")) )    choice = 'f';
            else if( !ALLOW_RESET_WINS_AND_LOSSES &&
                   (response.contains("RESET") || response.contains("F.")) )    choice = '0';
            else if( response.contains("QUIT") || response.contains("Q.") )     choice = 'q';
            else { SOPln("Invalid response. Please enter a single letter, according to the menu choices above.\n"); choice = '0'; }
         } else {
            choice = response.charAt(0);
         }
         
         //call corresponding program, or quit
         switch( choice ) {
            case 'a':
               gameState = runConnectFourPlayer(); //gameState is currently not used here
               break;
            case 'b':
               gameState = runConnectFourAI(); //or here
               break;
            case 'c':
               runConnectFourFile();
               break;
            case 'd':
               gameState = runConnectFourRandom(); //or here
               break;
            case 'e':
               printWinsAndLosses();
               break;
            case 'f':
               resetWinsAndLosses();
               break;
            case '0':
               break;
            case 'q':
            default:
               isRunning = false;
               SOPln("\nGoodbye!");
               break;
         }
      
      } while( isRunning);
      
   }
   
   /**
      Play a game of ConnectFour. The gameMode will determine how the moves are played.
      
      @param gameMode Determines which type of ConnectFour game is being played.
                      Games can be played player vs player using the gameMode PLAYER
                      Games can be played player vs AI using the gamemode AI
                      Games can be played AI vs AI from a File using the gamemode FILE
                      Games can be played AI vs AI using random moves using the gamemode RANDOM
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see runConnectFour( int gameMode, int currentGameNumber )
      @see runConnectFour()
   */
   private static int runConnectFour( int gameMode ) {
      int gameState = GAME_CONTINUE;
      if( gameMode == FILE ) {
         SOPln("Enter the game number you would like to run from the file or\n" +
               "enter zero (0) if you would like to test run all the games in the File");
         int option = scanner.nextInt(); scanner.nextLine();
         if( option != 0 ) {
            try {
               gameState = runConnectFour( gameMode, option );
            } catch( IllegalGameStateException e ) {
               SOPln( e.getMessage() );
            }
         } else {
            int totalGames = getLines( CONNECT_FOUR_GAMES ).length;
            ++option;
            while( option <= totalGames ) {
               try {
                  gameState = runConnectFour( gameMode, option++ );
               } catch( IllegalGameStateException e ) {
                  SOPln( e.getMessage() );
               }
            }
         }
      } else {
         try {
            gameState = runConnectFour( gameMode, NO_FILE );
         } catch( IllegalGameStateException e ) {
            SOPln( e.getMessage() );
         }
      }
      
      return gameState;
   }
   
   /**
      Play a game of ConnectFour. The gameMode will determine how the moves are played
      
      The gameMode 's available are PLAYER, AI, FILE, and RANDOM
      
      If PLAYER_1 wins, then a win is recorded to the overall win/loss record
      If PLAYER_1 loses, then a loss is recorded to the overall win/loss record
      
      @param gameMode Determines which type of ConnectFour game is being played.
                      Games can be played player vs player using the gameMode PLAYER
                      Games can be played player vs AI using the gamemode AI
                      Games can be played AI vs AI from a File using the gamemode FILE
                      Games can be played AI vs AI using random moves using the gamemode RANDOM
      @param currentGameNumber The current game number being played from a file. Only used for the FILE gameMode.
                               A game not using a File will have currentGameNumber NO_FILE.
                               A game running all games within the File will have currentGameNumber 0
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see recordWinOrLossOrDraw( boolean wonGame )
      @see getFileMoves( String fileLoc, int lineNumber )
      @see printBoard( int[][] board )
      @see getAIMove( int[][] board )
      @see addToken( int col, int turnNumber, int[][] board )
      @see winLossOrDraw( int[][] board )
      @see runConnectFourPlayer()
      @see runConnectFourAI()
      @see runConnectFourFile()
      @see runConnectFourRandom()
   */
   private static int runConnectFour( int gameMode, int currentGameNumber ) throws IllegalGameStateException {
      
      int[][] board = new int[ TOTAL_ROWS ][ TOTAL_COLS ];
      
      int turnNumber = 1;
      
      SOPln("The player that goes first will be PLAYER 1.\n" +
            "The player that goes second will be PLAYER 2.\n");
      
      SOPln("PLAYER 1's tokens will be " + PLAYER_1_COLOR + "." );
      SOPln("PLAYER 2's tokens will be " + PLAYER_2_COLOR + ".\n" );
      
      int gameState = GAME_CONTINUE;
      
      //Get the moves to read in
      String fileMoves = "";
      if( gameMode == FILE ) fileMoves = getFileMoves( CONNECT_FOUR_GAMES, currentGameNumber - 1 );
      
      Random random = new Random();
      
      ArrayList<Integer> gameMoves = new ArrayList<Integer>();
      
      //run game
      do {
         try {
            printBoard( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         
         //Get the column that the user puts the token into
         int col;
         do {
            if( turnNumber % 2 == 1 ) SOPln("PLAYER 1 (" + PLAYER_1_TOKEN + "'s), it is your turn. Choose 1-" + TOTAL_COLS + " to play your token.");
            else                      SOPln("PLAYER 2 (" + PLAYER_2_TOKEN + "'s), it is your turn. Choose 1-" + TOTAL_COLS + " to play your token.");
            
            //How the column is picked depends on the game mode
            if(      gameMode == PLAYER ) { col = scanner.nextInt(); scanner.nextLine(); }
            else if( gameMode == AI     ) { if( turnNumber % 2 == 1 ) { col = scanner.nextInt(); scanner.nextLine(); } else col = getAIMove( board ); }
            else if( gameMode == FILE   )   col = Integer.valueOf( fileMoves.split(",")[ turnNumber - 1 ] );
            else if( gameMode == RANDOM )   col = random.nextInt( TOTAL_COLS ) + 1;
            else {
               col = 0;
               throw new IllegalGameStateException("Illegal game mode state. Expected PLAYER, AI, FILE, or RANDOM.");
            }
            
            if( col > TOTAL_COLS || col < 1 ) SOPln("Invalid input. Please enter a column between 1 and " + TOTAL_COLS + ", inclusive.");
         } while( col > TOTAL_COLS || col < 1 );
         
         //Add the token to the column
         try {
            board = addToken( col, turnNumber, board );
            ++turnNumber;
            
            //Record moves into CONNECT_FOUR_GAMES file
            gameMoves.add( col );
            
            //Determine if a player has won, lost, or if there is a draw
            gameState = winLossOrDraw( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         
      } while( gameState == GAME_CONTINUE );
      
      //Write moves to CONNECT_FOUR_GAMES file
      addGameToFile( gameMoves );
      
      //Wrap up game results
      if( gameState == WIN ) {
         try {
            printBoard( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         SOPln("\nPLAYER 1 wins!\n");
         if( gameMode != FILE )
            recordWinLossOrDraw( WIN );
      } else if( gameState == LOSE ) {
         try {
            printBoard( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         SOPln("\nPLAYER 2 wins!\n");
         if( gameMode != FILE )
            recordWinLossOrDraw( LOSE );
      } else if( gameState == DRAW ) {
         try {
            printBoard( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         SOPln("\nThe game ends in a draw!\n");
         if( gameMode != FILE )
            recordWinLossOrDraw( DRAW );
      } else throw new IllegalGameStateException("Illegal game state: " + gameState);
      
      SOPln("Press enter to continue.");
      scanner.nextLine();
      
      return gameState;
   }
   
   /**
      Gets the next AI move for the current board
      
      @param board The current game board
      @return int The column chosen to play in
      @see runConnectFour( int gameMode, int currentGameNumber )
   */
   private static int getAIMove( int[][] board ) {
      
      return 0;
   }
   
   /**
      Gets the moves of ConnectFour game
      
      @param fileLoc The location of the file
      @param lineNumber The game moves of the stored ConnectFour game
      @return String The games moves of a ConnectFour game, delimited by commas
      @see getLines( String fileLoc )
      @see runConnectFour( int gameMode, int currentGameNumber )
   */
   private static String getFileMoves( String fileLoc, int lineNumber ) {
      return getLines( fileLoc )[ lineNumber ];
   }
   
   /**
      Adds the list of game moves to the CONNECT_FOUR_GAMES file
      
      @param moveList The list of moves from a single game
      @see getLines( String fileLoc )
      @see clearFile( String fileLoc )
      @see writeToFile( String[] lines, String fileLoc )
   */
   private static void addGameToFile( ArrayList<Integer> moveList ) {
      //Saves lines
      String[] fileLines = getLines( CONNECT_FOUR_GAMES );
      
      //Clear file
      clearFile( CONNECT_FOUR_GAMES );
      
      /* @@@@@ Write to file with added line of moves from last game @@@@@ */
      String[] newMoveSet = new String[ fileLines.length + 1 ];
      
      //Convert moveList to String
      String movesFromLastGame = "";
      int i = 0;
      int size = moveList.size();
      while( i < size ) movesFromLastGame += moveList.get(i++) + ",";
      movesFromLastGame = movesFromLastGame.substring( 0, movesFromLastGame.length() - 1 ); //remove last comma
      
      //Add moves to new list
      for( int j = 0; j < newMoveSet.length - 1; j++ ) {
         newMoveSet[j] = fileLines[j];
      }
      newMoveSet[ newMoveSet.length - 1 ] = movesFromLastGame;
      
      //Write to file
      writeToFile( newMoveSet, CONNECT_FOUR_GAMES );
      
      /* @@@@@ End write to file @@@@@@ */
   }
   
   /**
      Adds a token to the board
      
      @param col The column to add the token to. If the column selected is not valid, an IllegalBoardException is thrown
      @param turnNumber The current turn number of the game. This value is used to determine which player is playing the current token
      @param board The board to add the token to
      @return int[][] The updated board
   */
   private static int[][] addToken( int col, int turnNumber, int[][] board ) throws IllegalBoardException {
      //User should not consider column 1 as having index 0
      col--;
      
      if( col > TOTAL_COLS || col < 0 )
         throw new IllegalBoardException("Invalid column entry. Please enter a number between 1 and " + TOTAL_COLS + ", inclusive.");
   
      //Determine which player
      boolean isPlayer1Turn = turnNumber % 2 == 1 ? true : false;
      int value = isPlayer1Turn ? PLAYER_1_TOKEN_VAL : PLAYER_2_TOKEN_VAL;
      
      //Add token. If column is full, throw exception
      for( int i = 0; i < board.length; i++ ) {
         if( board[i][col] != 0 && i == 0 ) throw new IllegalBoardException("Column is full. Please pick a difference column.");
         else if( (board[i][col] == 0 && i < board.length - 1 && board[i+1][col] != 0) ||
                  (board[i][col] == 0 && i == board.length - 1) )
            board[i][col] = value;
      }
      
      return board;
   }
   
   /**
      Prints the ConnectFour board
      
      PLAYER_1's tokens are PLAYER_1_COLOR and use the PLAYER_1_TOKEN shape
      PLAYER_2's tokens are PLAYER_2_COLOR and use the PLAYER_2_TOKEN shape
      
      @param board The game board to print
      @see PLAYER_1_COLOR
      @see PLAYER_2_COLOR
      @see PLAYER_1_TOKEN
      @see PLAYER_2_TOKEN
   */
   private static void printBoard( int[][] board ) throws IllegalBoardException {
      final String LEFT_SPOT = "[";
      final String RIGHT_SPOT = "]";
   
      for( int row = 0; row < TOTAL_ROWS; row++ ) {
         for( int col = 0; col < TOTAL_COLS; col++ ) {
            if( board[row][col] == 0 )                       SOP( LEFT_SPOT + " " + RIGHT_SPOT );
            else if( board[row][col] == PLAYER_1_TOKEN_VAL ) SOP( LEFT_SPOT + PLAYER_1_TOKEN + RIGHT_SPOT );
            else if( board[row][col] == PLAYER_2_TOKEN_VAL ) SOP( LEFT_SPOT + PLAYER_2_TOKEN + RIGHT_SPOT );
            else                                             throw new IllegalBoardException("\n\nIllegal value found in board: position [" + row + ", " + col + "]\n");
         }
         SOPln();
      }
   }
   
   /**
      Determines whether a player has won, lost, or if there is a draw
      
      A player has won once there are four of their tokens in a row, in a column,
      or in a diagonal formation
      
      @param board The board to check
      @return int Returns GAME_CONTINUE, WIN, LOSE, or DRAW
      @see runConnectFourPlayer()
      @see checkWinOrLoss( int[][] board )
      @see checkIfDraw( int[][] board )
   */
   private static int winLossOrDraw( int[][] board ) {
      int winOrLoss = checkWinOrLoss( board );
      if( winOrLoss == GAME_CONTINUE ) return checkIfDraw( board ); //returns DRAW or GAME_CONTINUE
      else                             return winOrLoss == WIN ? WIN : LOSE;
   }
   
   /**
      Determines whether the game is a draw or if the game can continue
      
      @param board The board to check
      @return int Returns GAME_CONTINUE or DRAW
      @see winLossOrDraw( int[][] board )
   */
   private static int checkIfDraw( int[][] board ) {
      
      for( int row = 0; row < board.length; row++ ) {
         for( int col = 0; col < board[row].length; col++ ) {
            if( board[row][col] == 0 ) return GAME_CONTINUE;
         }
      }
      
      return DRAW;
   }
   
   /**
      Determines if PLAYER_1 has won or lost
      
      @param board The board to check
      @return int Returns WIN if PLAYER_1 wins, LOSE if PLAYER_1 lost, and GAME_CONTINUE if PLAYER_1 did not win or lose yet
      @see winLossOrDraw( int[][] board )
      @see checkHorizontals( int[][] board )
      @see checkVerticals( int[][] board )
      @see checkLeftDiagonals( int[][] board )
      @see checkRightDiagonals( int[][] board )
   */
   private static int checkWinOrLoss( int[][] board ) {
      
      int gameState = checkHorizontals( board );
      if( gameState == GAME_CONTINUE ) gameState = checkVerticals( board );
      else return gameState;
      if( gameState == GAME_CONTINUE ) gameState = checkLeftDiagonals( board );
      else return gameState;
      if( gameState == GAME_CONTINUE ) gameState = checkRightDiagonals( board );
      
      return gameState;
      
   }
   
   /**
      Determine whether the game has been won or lost in the horizontal direction
      
      @param board The board to be checked
      @return int Returns GAME_CONTINUE, WIN, or LOSE
      @see checkWinOrLoss( int[][] board )
   */
   private static int checkHorizontals( int[][] board ) {
      int player1Total = 0;
      int player2Total = 0;
   
      //Check horizontals
      for( int row = 0; row < board.length; row++ ) {
         for( int col = 0; col < board[row].length; col++ ) {
            //Add totals in a row
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this row
            if( col > TOTAL_COLS - TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
            
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
      }
      
      return GAME_CONTINUE;
   }
   
   /**
      Determine whether the game has been won or lost in the vertical direction
      
      @param board The board to be checked
      @return int Returns GAME_CONTINUE, WIN, or LOSE
      @see checkWinOrLoss( int[][] board )
   */
   private static int checkVerticals( int[][] board ) {
      
      int player1Total = 0;
      int player2Total = 0;
      
      //Check verticals
      for( int col = 0; col < board[0].length; col++ ) {
         for( int row = 0; row < board.length; row++ ) {
            //Adds totals in a column
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this col
            if( row >= TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
      }
      
      return GAME_CONTINUE;
   }
   
   /**
      Determine whether the game has been won or lost in the left diagonal direction
      
      @param board The board to be checked
      @return int Returns GAME_CONTINUE, WIN, or LOSE
      @see checkWinOrLoss( int[][] board )
   */
   private static int checkLeftDiagonals( int[][] board ) {
      
      int player1Total = 0;
      int player2Total = 0;
   
      //Check left diagonals starting at top left corner going down
      for( int pos = 0; pos < board.length; pos++ ) {
         for( int row = pos, col = 0; row < board.length && row >= 0 && col < board[0].length; row--, col++ ) {
            //Adds totals in diagonal
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this diagonal
            if( row < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
      }
   
      player1Total = 0;
      player2Total = 0;
   
      //Check left diagonals starting at bottom left corner going right
      for( int pos = 0; pos < board[0].length; pos++ ) {
         for( int row = board.length - 1, col = pos; row >= 0 && col < board[0].length; row--, col++ ) {
         
            //Adds totals in diagonal
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this diagonal
            if( row < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
         //Check for not winnable for width
         if( pos > TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
            break;
      }
      
      return GAME_CONTINUE;
   }
   
   /**
      Determine whether the game has been won or lost in the right diagonal direction
      
      @param board The board to be checked
      @return int Returns GAME_CONTINUE, WIN, or LOSE
      @see checkWinOrLoss( int[][] board )
   */
   private static int checkRightDiagonals( int[][] board ) {
      
      int player1Total = 0;
      int player2Total = 0;
   
      //Check right diagonals starting at top right corner going down
      for( int pos = 0; pos < board.length; pos++ ) {
         for( int row = pos, col = board[0].length - 1; row >= 0 && row < board.length && col >= 0; row--, col-- ) {
            //Adds totals in diagonal
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this diagonal
            if( row < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
         //Check for not winnable for width
         if( pos < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
            break;
      }
   
      //Reset
      player1Total = 0;
      player2Total = 0;
   
      //Check right diagonals starting at bottom right corner going left
      for( int pos = board[0].length - 1; pos >= 0; pos-- ) {
         for( int row = board.length - 1, col = pos; row >= 0 && col >= 0; row--, col-- ) {
            //Adds totals in diagonal
            if( board[row][col] == PLAYER_1_TOKEN_VAL ) {
               player1Total++;
               player2Total = 0;
            } else if( board[row][col] == PLAYER_2_TOKEN_VAL ) {
               player2Total++;
               player1Total = 0;
            } else {
               player1Total = player2Total = 0;
            }
            
            //Check win
            if(      player1Total >= TOTAL_TO_SCORE ) return WIN;
            else if( player2Total >= TOTAL_TO_SCORE ) return LOSE;
            
            //Check for not winnable for this diagonal
            if( row < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
               break;
         }
         
         //Reset
         player1Total = 0;
         player2Total = 0;
         
         //Check for not winnable for width
         if( pos < TOTAL_TO_SCORE && player1Total == 0 && player2Total == 0 )
            break;
      }
      
      return GAME_CONTINUE;
   }
   
   /**
      Play a game of ConnectFour against another player
      
      If PLAYER_1 wins, then a win is recorded to the overall win/loss record
      If PLAYER_1 loses, then a loss is recorded to the overall win/loss record
      
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see recordWinOrLossOrDraw( boolean wonGame )
      @see printBoard( int[][] board )
      @see addToken( int col, int turnNumber, int[][] board )
      @see winLossOrDraw( int[][] board )
   */
   private static int runConnectFourPlayer() {
      return runConnectFour( PLAYER );
   }
   
   /**
      Play a game of ConnectFour against the computer
      
      If PLAYER_1 wins, then a win is recorded to the overall win/loss record
      If PLAYER_1 loses, then a loss is recorded to the overall win/loss record
      
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see runConnectFour( int gameMode )
      @see AI The game mode for playing against the computer
   */
   private static int runConnectFourAI() {
      return runConnectFour( AI );
   }
   
   /**
      Run a game of ConnectFour from CONNECT_FOUR_GAMES text file
      
      The format of the text file contains the moves for PLAYER_1 and PLAYER_2
      The pattern shows the column of the move made by PLAYER_1, followed by
      the move made by PLAYER_2, delimited by commas. The subsequent pairs of moves
      by PLAYER_1 and PLAYER_2 continue until the last move is reached, which should be the
      resulting move that determines that the game has resulted in a win/loss or a draw
      
      Each total set of moves of a game should be on one line. There can be a maximum of
      TOTAL_ROWS x TOTAL_COLS moves, and neither player is allowed to pass.
      
      Eg. 1,2,3,4,7,5,1,6,2,3,4,5,6,4,1,1,6,6,5,5  --> should be a win for PLAYER_2
      
      @see int Either WIN, LOSE, or DRAW
      @see runConnectFour( int gameMode )
      @see FILE The game mode for running games from a File
   */
   private static int runConnectFourFile() {
      return runConnectFour( FILE );
   }
   
   /**
      Run a game of ConnectFour given a Unit Test game format
      
      This runs similarly to runConnectFourFile(), but allows a given String to be used instead of
      reading from a file. This is a stripped-down version of runConnectFour( int gameState )
   
      Play a game of ConnectFour. The gameMode will determine how the moves are played
      
      The gameMode 's available are PLAYER, AI, FILE, and RANDOM
      
      If PLAYER_1 wins, then a win is recorded to the overall win/loss record
      If PLAYER_1 loses, then a loss is recorded to the overall win/loss record
      
      @param filesMoves The String of fileMoves to run
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see recordWinOrLossOrDraw( boolean wonGame )
      @see getFileMoves( String fileLoc, int lineNumber )
      @see printBoard( int[][] board )
      @see getAIMove( int[][] board )
      @see addToken( int col, int turnNumber, int[][] board )
      @see winLossOrDraw( int[][] board )
      @see runConnectFourPlayer()
      @see runConnectFourAI()
      @see runConnectFourFile()
      @see runConnectFourRandom()
   */
   public static int runConnectFourFileTest( String fileMoves ) throws IllegalGameStateException {
      
      int[][] board = new int[ TOTAL_ROWS ][ TOTAL_COLS ];
      
      int turnNumber = 1;
      
      int gameState = GAME_CONTINUE;
      
      Random random = new Random();
      ArrayList<Integer> gameMoves = new ArrayList<Integer>();
      
      //run game
      do {
         
         //Get the column that the user puts the token into
         int col;
         do {
            //Get column entry
            col = Integer.valueOf( fileMoves.split(",")[ turnNumber - 1 ] );
            
            if( col > TOTAL_COLS || col < 1 ) SOPln("Invalid input. Please enter a column between 1 and " + TOTAL_COLS + ", inclusive.");
         } while( col > TOTAL_COLS || col < 1 );
         
         //Add the token to the column
         try {
            board = addToken( col, turnNumber, board );
            ++turnNumber;
            
            //Record moves into CONNECT_FOUR_GAMES file
            gameMoves.add( col );
            
            //Determine if a player has won, lost, or if there is a draw
            gameState = winLossOrDraw( board );
         } catch( IllegalBoardException e ) {
            SOPln( e.getMessage() );
         }
         
      } while( gameState == GAME_CONTINUE );
      
      //Wrap up game results
      if     ( gameState == WIN )  SOPln("\nPLAYER 1 wins!\n");
      else if( gameState == LOSE ) SOPln("\nPLAYER 2 wins!\n");
      else if( gameState == DRAW ) SOPln("\nThe game ends in a draw!\n");
      else throw new IllegalGameStateException("Illegal game state: " + gameState);
      
      return gameState;
   }
   
   /**
      Runs a game of ConnectFour using random moves for each computer playing
      against one another
      
      @return int Returns WIN if PLAYER_1 won, LOSE if PLAYER_1 lost, and DRAW if PLAYER_1 tied PLAYER_2
      @see runConnectFour( int gameMode )
      @see RANDOM The game mode for running games randomly
   */
   private static int runConnectFourRandom() {
      return runConnectFour( RANDOM );
   }
   
   /**
      Print the current total wins and losses
      
      @see resetWinsAndLosses()
      @see WIN_LOSS_FILE_LOC Contains the total number of wins and losses
   */
   private static void printWinsAndLosses() {
      String[] fileLines = getLines( WIN_LOSS_FILE_LOC );
      
      SOPln("Player 1 Record:\n");
      SOPln( fileLines[0] + "\n" + fileLines[1] + "\n" +fileLines[2] + "\n" );
   }
   
   /**
      Retrieve the lines of a File as an array of Strings
      
      @param fileLoc The path of the file
      @return String[] A list of the lines of the File
      @see printWinsAndLosses()
      @see recordWinLossOrDraw( int gameState )
   */
   private static String[] getLines( String fileLoc ) {
      ArrayList<String> fileLines = new ArrayList<String>();
      
      File winLossFile = new File( fileLoc );
      Scanner reader = null;
      try {
         reader = new Scanner( winLossFile );
      } catch( FileNotFoundException e ) {
         e.printStackTrace();
      }
      
      while( reader.hasNextLine() )
         fileLines.add( reader.nextLine() );
      
      reader.close();
      
      return fileLines.toArray( new String[ fileLines.size() ] );
   }
   
   /**
      Reset the current number of wins and losses to zero
      
      @see printWinsAndLosses()
      @see WIN_LOSS_FILE_LOC Contains the total number of wins and losses
      @see PrintWriter.println( String str )
      @see writeToFile( String[] lines, String fileLoc )
   */
   private static void resetWinsAndLosses() {
      //Empty text file
      clearFile( WIN_LOSS_FILE_LOC );
   
      //Write back to file with zero wins, losses, and draws
      String[] lines = { "Wins: 0", "Losses: 0", "Draws: 0" };
      writeToFile( lines, WIN_LOSS_FILE_LOC );
      
      SOPln("\nBoo.\n");
   }
   
   /**
      Clear the contents of a file
      
      @param fileLoc The path of the file
      @see Files.newBufferedWriter( Path path, Charset cs, OpenOptions... options )
      @see Paths.get( String path )
      @see PrintWriter.println( String str )
   */
   private static void clearFile( String fileLoc ) {
      try {
         Files.newBufferedWriter( Paths.get( fileLoc ), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING );
      } catch( IOException e ) {
         e.printStackTrace();
      }
   }
   
   /**
      Adds a win, loss, or tie to the WIN_LOSS_FILE_LOC text file
      
      @param gameState Contains WIN, LOSE, or DRAW
      @see getLines( String fileLoc )
      @see clearFile( String fileLoc )
      @see writeToFile( String[] lines, String fileLoc )
   */
   private static void recordWinLossOrDraw( int gameState ) {

      /*@@@@@ Scan file @@@@@@@@@@*/
      
      String winLine, lossLine, drawLine;
      String[] fileLines = getLines( WIN_LOSS_FILE_LOC );
      winLine  = fileLines[0];
      lossLine = fileLines[1];
      drawLine = fileLines[2];
      
      String winStr =  winLine.split(" ")[1];  winStr.trim();
      String lossStr = lossLine.split(" ")[1]; lossStr.trim();
      String drawStr = drawLine.split(" ")[1]; drawStr.trim();
      
      int currentWins =   Integer.valueOf( winStr );
      int currentLosses = Integer.valueOf( lossStr );
      int currentDraws =  Integer.valueOf( drawStr );
      
      if(      gameState == WIN )  currentWins++;
      else if( gameState == LOSE ) currentLosses++;
      else                         currentDraws++;
      
      /*@@@@@@ End scan file section @@@@@@@@@@@*/
      
      /*@@@@@ Overwrite file @@@@@@ */
      
      //Empty text file
      clearFile( WIN_LOSS_FILE_LOC );
      
      //Write to file
      String[] lines = { "Wins: "   + currentWins, "Losses: " + currentLosses, "Draws: "  + currentDraws };
      writeToFile( lines, WIN_LOSS_FILE_LOC );
      
      /*@@@@@@ End overwrite file section @@@@@@@@ */
   }
   
   /**
      Write the set of lines to the given file
      
      @param lines The lines to write to the file
      @param fileLoc The location of the file
      @see recordWinLossOrDraw( int gameState )
      @see resetWinsAndLosses()
      @see addGameToFile( ArrayList<Integer> moveList )
   */
   private static void writeToFile( String[] lines, String fileLoc ) {
   
      try {
         PrintWriter writer = new PrintWriter( fileLoc, "UTF-8" );
         for( int i = 0; i < lines.length; i++ ) {
            writer.println( lines[i] );
         }
         writer.close();
      } catch( IOException e ) {
         e.printStackTrace();
      }
      
   }
   
   /**
      A faster, shorter wrapper class to print statements to the console
      
      @param str The string to print using System.out.println( String str )
   */
   private static void SOPln( String str ) {
      System.out.println( str );
   }
   
   /**
      A faster, shorter wrapper class to print statements to the console
   */
   private static void SOPln() {
      System.out.println();
   }
   
   /**
      A faster, shorter wrapper class to print statements to the console,
      sans line break
      
      @param str The string to print using System.out.print( String str )
   */
   private static void SOP( String str ) {
      System.out.print( str );
   }

   /**
      An exception triggered whenever the board is in an illegal state
      
      This can occur when an illegal character/number is added to the board
      
      @see printBoard( int[][] board )
   */
   private static class IllegalBoardException extends Exception {
   
      /**
         Creates an IllegalBoardException object
         
         @param str The message to print
      */
      public IllegalBoardException( String str ) {
         super( str );
      }
   
   }
   
   /**
      An exception triggered whenever the game is in an illegal state
      
      This can occur if the gameState is not currently in the state of
      GAME_CONTINUES, WIN, LOSE, or DRAW
      
      @see recordWinLoseOrDraw( int gameState )
   */
   private static class IllegalGameStateException extends Exception {
      
      /**
         Creates an IllegalGameStateException object
         
         @param str The message to print
      */
      public IllegalGameStateException( String str ) {
         super( str );
      }
      
   }

   /**
      Adds console color adjustments
      
      eg.
        System.out.print(Color.BLACK_BOLD);
        System.out.println("Black_Bold");
        System.out.print(Color.RESET);

        System.out.print(Color.YELLOW);
        System.out.print(Color.BLUE_BACKGROUND);
        System.out.println("YELLOW & BLUE");
        System.out.print(Color.RESET);

        System.out.print(Color.YELLOW);
        System.out.println("YELLOW");
        System.out.print(Color.RESET);
   */
   enum Color {
    //Color end string, color reset
    RESET("\033[0m"),

    // Regular Colors. Normal color, no bold, background color etc.
    BLACK("\033[0;30m"),    // BLACK
    RED("\033[0;31m"),      // RED
    GREEN("\033[0;32m"),    // GREEN
    YELLOW("\033[0;33m"),   // YELLOW
    BLUE("\033[0;34m"),     // BLUE
    MAGENTA("\033[0;35m"),  // MAGENTA
    CYAN("\033[0;36m"),     // CYAN
    WHITE("\033[0;37m"),    // WHITE

    // Bold
    BLACK_BOLD("\033[1;30m"),   // BLACK
    RED_BOLD("\033[1;31m"),     // RED
    GREEN_BOLD("\033[1;32m"),   // GREEN
    YELLOW_BOLD("\033[1;33m"),  // YELLOW
    BLUE_BOLD("\033[1;34m"),    // BLUE
    MAGENTA_BOLD("\033[1;35m"), // MAGENTA
    CYAN_BOLD("\033[1;36m"),    // CYAN
    WHITE_BOLD("\033[1;37m"),   // WHITE

    // Underline
    BLACK_UNDERLINED("\033[4;30m"),     // BLACK
    RED_UNDERLINED("\033[4;31m"),       // RED
    GREEN_UNDERLINED("\033[4;32m"),     // GREEN
    YELLOW_UNDERLINED("\033[4;33m"),    // YELLOW
    BLUE_UNDERLINED("\033[4;34m"),      // BLUE
    MAGENTA_UNDERLINED("\033[4;35m"),   // MAGENTA
    CYAN_UNDERLINED("\033[4;36m"),      // CYAN
    WHITE_UNDERLINED("\033[4;37m"),     // WHITE

    // Background
    BLACK_BACKGROUND("\033[40m"),   // BLACK
    RED_BACKGROUND("\033[41m"),     // RED
    GREEN_BACKGROUND("\033[42m"),   // GREEN
    YELLOW_BACKGROUND("\033[43m"),  // YELLOW
    BLUE_BACKGROUND("\033[44m"),    // BLUE
    MAGENTA_BACKGROUND("\033[45m"), // MAGENTA
    CYAN_BACKGROUND("\033[46m"),    // CYAN
    WHITE_BACKGROUND("\033[47m"),   // WHITE

    // High Intensity
    BLACK_BRIGHT("\033[0;90m"),     // BLACK
    RED_BRIGHT("\033[0;91m"),       // RED
    GREEN_BRIGHT("\033[0;92m"),     // GREEN
    YELLOW_BRIGHT("\033[0;93m"),    // YELLOW
    BLUE_BRIGHT("\033[0;94m"),      // BLUE
    MAGENTA_BRIGHT("\033[0;95m"),   // MAGENTA
    CYAN_BRIGHT("\033[0;96m"),      // CYAN
    WHITE_BRIGHT("\033[0;97m"),     // WHITE

    // Bold High Intensity
    BLACK_BOLD_BRIGHT("\033[1;90m"),    // BLACK
    RED_BOLD_BRIGHT("\033[1;91m"),      // RED
    GREEN_BOLD_BRIGHT("\033[1;92m"),    // GREEN
    YELLOW_BOLD_BRIGHT("\033[1;93m"),   // YELLOW
    BLUE_BOLD_BRIGHT("\033[1;94m"),     // BLUE
    MAGENTA_BOLD_BRIGHT("\033[1;95m"),  // MAGENTA
    CYAN_BOLD_BRIGHT("\033[1;96m"),     // CYAN
    WHITE_BOLD_BRIGHT("\033[1;97m"),    // WHITE

    // High Intensity backgrounds
    BLACK_BACKGROUND_BRIGHT("\033[0;100m"),     // BLACK
    RED_BACKGROUND_BRIGHT("\033[0;101m"),       // RED
    GREEN_BACKGROUND_BRIGHT("\033[0;102m"),     // GREEN
    YELLOW_BACKGROUND_BRIGHT("\033[0;103m"),    // YELLOW
    BLUE_BACKGROUND_BRIGHT("\033[0;104m"),      // BLUE
    MAGENTA_BACKGROUND_BRIGHT("\033[0;105m"),   // MAGENTA
    CYAN_BACKGROUND_BRIGHT("\033[0;106m"),      // CYAN
    WHITE_BACKGROUND_BRIGHT("\033[0;107m");     // WHITE

    private final String code;

    Color(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}

}