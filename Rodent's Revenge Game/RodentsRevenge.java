
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Runs the Rodents Revenge game loop and methods
 *
 * @author Mr Olson
 * @version 092822
 */
public class RodentsRevenge {
    
    /*This is a global variable. It's scope is local to the entire class...
      this means that this variable is accessible anywhere within this class*/
    private String[][] grid;
    private final int TOTAL_ROWS, TOTAL_COLS;
    
    private String LEVEL_ONE_FILE = "levelOne.txt";
    
    private final String ERROR  = "";
    
    private final String EMPTY  = "E";
    private final String BLOCK  = "B";
    private final String CAT    = "A";
    private final String MOUSE  = "M";
    private final String CHEESE = "C";
    
    private final int DEFAULT_LIFE_STARTING_TOTAL = 3;
    private final int CHEESE_POINTS = 100;
    
    private int totalLives;
    private int totalScore = 0;
    private int mouseX, mouseY;
    private boolean isMouseTurn = true;
    
    private ArrayList<Integer> catX = new ArrayList<Integer>();
    private ArrayList<Integer> catY = new ArrayList<Integer>();
    private ArrayList<Boolean> catStuck = new ArrayList<Boolean>();
    
    private Scanner sc = new Scanner(System.in);
    
    /**
     * Default constructor. Creates a level based on the LEVEL_ONE_FILE file
     * and begins the game loop
     * 
     * Create the game object grid, an n x n sized 2D array of Strings
     */
    public RodentsRevenge() {
        setGridDimensions( getScanner( LEVEL_ONE_FILE ) );
        TOTAL_ROWS = grid.length;
        TOTAL_COLS = grid[0].length;
        
        setNewLevel( getScanner( LEVEL_ONE_FILE ) );

        totalLives = DEFAULT_LIFE_STARTING_TOTAL;
        
        runGame();
    }
    
    /**
     * Creates a level based on the levelData text file.
     * Create the game object grid, an n x n sized 2D array of Strings.
     * Begins the game loop.
     * 
     * @param levelDataTextFile The text file containing the level data
     */
    public RodentsRevenge( String levelDataTextFile ) {
        setGridDimensions( getScanner( levelDataTextFile ) );
        TOTAL_ROWS = grid.length;
        TOTAL_COLS = grid[0].length;
        
        setNewLevel( getScanner( levelDataTextFile ) );

        totalLives = DEFAULT_LIFE_STARTING_TOTAL;
        
        runGame();
    }

    /**
     * Sets the the size of the grid
     * 
     * @param fileScanner The scanner that is reading the text file containing
     *                    the level data
     */
    private void setGridDimensions( Scanner fileScanner ) {
        int totalRows = 0;
        int totalColumns = 0;
        while( fileScanner.hasNextLine() ) {
            String line = fileScanner.nextLine();
            String[] elements = line.split(" ");
            totalColumns = elements.length;
            totalRows++;
        }

        grid = new String[totalRows][totalColumns];
        
        fileScanner.close();
    }
    
    /**
     * Sets the grid's data based on text file input
     * 
     * @param fileScanner The scanner that is reading the text file containing
     *                    the level data
     */
    private void setNewLevel( Scanner fileScanner ) {
        int currentRow = 0;
        while( fileScanner.hasNextLine() ) {
            String line = fileScanner.nextLine();
            String[] elements = line.split(" ");
            for( int currentCol = 0; currentCol < TOTAL_COLS; currentCol++ ) {
                grid[currentRow][currentCol] = elements[currentCol];
                if(        elements[currentCol].equals(MOUSE) ) {
                    mouseX = currentRow;
                    mouseY = currentCol;
                } else if( elements[currentCol].equals(CAT) ) {
                    catX.add( currentRow );
                    catY.add( currentCol );
                    catStuck.add( false );
                }
            }
            currentRow++;
        }
        
        fileScanner.close();
    }
    
    /**
     * @STUDENT
     * 
     * Runs the game loop
     */
    public void runGame() {
        printGrid();
        while( totalLives != 0 ) {
            moveMouse();
            if( !isMouseTurn ) {
                moveCats();
                checkCats();
                clearConsole();
                updateScore();
                printGrid();
                
                isMouseTurn = true;
            }
        }
        sc.close();
    }
    
    /**
     * @STUDENT
     * 
     * Wait for the player to enter a direction for the mouse to move
     */
    private void moveMouse() {
        SOPln("Enter w,a,s,or d");
        String direction = sc.nextLine();
        if(        direction.equals("a") ) {
            isMouseTurn = false;
            moveMouse("left");
        } else if( direction.equals("d") ) {
            isMouseTurn = false;
            moveMouse("right");
        } else if( direction.equals("w") ) {
            isMouseTurn = false;
            moveMouse("up");
        } else if( direction.equals("s") ) {
            isMouseTurn = false;
            moveMouse("down");
        }
    }
    
    /**
     * @STUDENT
     * 
     * Try to move the mouse using WASD
     * 
     * @param direction The direction the mouse will be moved. Either "left",
     *                  "right", "up", or "down"
     */
    private void moveMouse( String direction ) {
        if(      direction.equals("left") )
            tryLeft(  getNeighbor(mouseX, mouseY, 0,-1) );
        else if( direction.equals("right") )
            tryRight( getNeighbor(mouseX, mouseY, 0, 1) );
        else if( direction.equals("up") )
            tryUp(    getNeighbor(mouseX, mouseY,-1, 0) );
        else if( direction.equals("down") )
            tryDown(  getNeighbor(mouseX, mouseY, 1, 0) );
    }
    
    /**
     * @STUDENT
     * 
     * Gets the neighboring element in the grid
     * 
     * @param x The current x position
     * @param y The current y position
     * @param dx The change in x from the current x position
     * @param dy The change in y from the current y position
     * @return String Returns EMPTY, BLOCK, CAT, CHEESE, MOUSE, or ERROR if the
     *                dx / dy values entered are invalid, or if there is no
     *                valid neighbor
     */
    private String getNeighbor( int x, int y, int dx, int dy ) {
        if( dx > 1 || dx < -1 || dy > 1 || dy < -1 )
            return ERROR;
        
        int neighborX = x + dx;
        int neighborY = y + dy;
        
        if( neighborX < 0 || neighborX >= TOTAL_ROWS )
            return ERROR;
        if( neighborY < 0 || neighborY >= TOTAL_COLS )
            return ERROR;
        
        return grid[neighborX][neighborY];
    }
    
    /**
     * @STUDENT
     * 
     * Try to move the mouse to the left
     * 
     * @param neighbor Either EMPTY, BLOCK, CAT, CHEESE, or MOUSE. If the
     *                 neighbor is MOUSE, then the movement failed (either
     *                 the mouse is at the edge of the grid, or
     *                 'getNeighbor(...)' was called incorrectly)
     */
    private void tryLeft( String neighbor ) {
        if( neighbor.equals( ERROR ) )
            return;
        
        if( neighbor.equals( EMPTY ) )
            moveMouse(0,-1);
        
        if( neighbor.equals( CAT ) ) {
            moveMouseCenter();
            killMouse();
        }
            
        if( neighbor.equals( CHEESE ) ) {
            moveMouse(0,-1);
            eatCheese();
        }
        
        if( neighbor.equals( BLOCK ) )
            moveBlocks("left");
    }
    
    /**
     * @STUDENT
     * 
     * Try to move the mouse to the right
     * 
     * @param neighbor Either EMPTY, BLOCK, CAT, CHEESE, or MOUSE. If the
     *                 neighbor is MOUSE, then the movement failed (either
     *                 the mouse is at the edge of the grid, or
     *                 'getNeighbor(...)' was called incorrectly)
     */
    private void tryRight( String neighbor ) {
        //delete this
        int[] newList = {1,4,7,25,-3};
        newList[0] = 27;
        newList[1] = newList[0];
        newList[0]++;
        int total = 0;
        for( int i = 0; i < newList.length; i++ ) {
            total += newList[i];
        }
        double average = (double)total / (double)newList.length;
        
        int[][] listGrid = new int[5][5];
        int[][] listGridOption2 = { {1, 2, 3, 4, 5},
                                    {6, 7, 8, 9, 10},
                                    {1, 2, 3, 4, 5},
                                    {4, 5, 6, 7, 8},
                                    {5, 5, 5, 5, 5} };
        
        if( neighbor.equals( MOUSE ) )
            return;
        
        if( neighbor.equals( EMPTY ) )
            moveMouse(0,1);
        
        if( neighbor.equals( CAT ) ) {
            moveMouseCenter();
            killMouse();
        }
            
        if( neighbor.equals( CHEESE ) ) {
            moveMouse(0,1);
            eatCheese();
        }
        
        if( neighbor.equals( BLOCK ) )
            moveBlocks("right");
    }
    
    /**
     * @STUDENT
     * 
     * Try to move the mouse up
     * 
     * @param neighbor Either EMPTY, BLOCK, CAT, CHEESE, or MOUSE. If the
     *                 neighbor is MOUSE, then the movement failed (either
     *                 the mouse is at the edge of the grid, or
     *                 'getNeighbor(...)' was called incorrectly)
     */
    private void tryUp( String neighbor ) {
        if( neighbor.equals( MOUSE ) )
            return;
        
        if( neighbor.equals( EMPTY ) )
            moveMouse(-1,0);
        
        if( neighbor.equals( CAT ) ) {
            moveMouseCenter();
            killMouse();
        }
            
        if( neighbor.equals( CHEESE ) ) {
            moveMouse(-1,0);
            eatCheese();
        }
        
        if( neighbor.equals( BLOCK ) )
            moveBlocks("up");
    }
    
    /**
     * @STUDENT
     * 
     * Try to move the mouse down
     * 
     * @param neighbor Either EMPTY, BLOCK, CAT, CHEESE, or MOUSE. If the
     *                 neighbor is MOUSE, then the movement failed (either
     *                 the mouse is at the edge of the grid, or
     *                 'getNeighbor(...)' was called incorrectly)
     */
    private void tryDown( String neighbor ) {
        if( neighbor.equals( MOUSE ) )
            return;
        
        if( neighbor.equals( EMPTY ) )
            moveMouse(1,0);
        
        if( neighbor.equals( CAT ) ) {
            moveMouseCenter();
            killMouse();
        }
            
        if( neighbor.equals( CHEESE ) ) {
            moveMouse(1,0);
            eatCheese();
        }
        
        if( neighbor.equals( BLOCK ) )
            moveBlocks("down");
    }
    
    /**
     * @STUDENT
     * 
     * Move mouse to new space
     * 
     * @param dx The change in x position
     * @param dy The change in y position
     */
    private void moveMouse( int dx, int dy ) {
        grid[mouseX][mouseY] = EMPTY;
        grid[mouseX+dx][mouseY+dy] = MOUSE;
        mouseX += dx;
        mouseY += dy;
    }
    
    /**
     * @STUDENT
     * 
     * Move the mouse onto the center square of the grid. The mouse will despawn from its
     * location and respawn in the center of the map (regardless of whether there is a block
     * there or not)
     */
    private void moveMouseCenter() {
        grid[mouseX][mouseY] = EMPTY;
        mouseX = TOTAL_ROWS/2;
        mouseY = TOTAL_COLS/2;
        grid[mouseX][mouseY] = MOUSE;
    }
    
    /**
     * @STUDENT
     * 
     * Kill the mouse and respawn in the central block
     */
    private void killMouse() {
        totalLives--;
    }
    
    /**
     * @STUDENT
     * 
     * Eat the cheese and gain points
     * 
     * @param dx The change in x direction
     * @param dy The change in y direction
     */
    private void eatCheese() {
        totalScore += CHEESE_POINTS;
    }
    
    /**
     * @STUDENT
     * 
     * Move blocks, if possible. Blocks cannot be moved if a cat is blocking
     * the stack, or if the blocks are pushing against the edge of the grid
     * 
     * @param direction Either "left", "right", "up", or "down"
     */
    private void moveBlocks( String direction ) {
        if(      direction.equals("left") )  moveBlocksLeft();
        else if( direction.equals("right") ) moveBlocksRight();
        else if( direction.equals("up") )    moveBlocksUp();
        else if( direction.equals("down") )  moveBlocksDown();
    }
    
    /**
     * @STUDENT
     * 
     * Move blocks to the left, if possible. Blocks cannot be moved if a
     * cat is blocking the stack, or if the blocks are pushing against
     * the edge of the grid
     */
    private void moveBlocksLeft() {
        int dy = -1;
        while( grid[mouseX][mouseY+dy].equals(BLOCK) )
            if( mouseY + --dy < 0 )
                return;
        
        if( grid[mouseX][mouseY+dy].equals(EMPTY) ||
            grid[mouseX][mouseY+dy].equals(CHEESE) ) {
            grid[mouseX][mouseY+dy] = BLOCK;
            grid[mouseX][mouseY-1]  = MOUSE;
            grid[mouseX][mouseY]    = EMPTY;
            mouseY--;
        }
    }
    
    /**
     * @STUDENT
     * 
     * Move blocks to the right, if possible. Blocks cannot be moved if a
     * cat is blocking the stack, or if the blocks are pushing against
     * the edge of the grid
     */
    private void moveBlocksRight() {
        int dy = 1;
        while( grid[mouseX][mouseY+dy].equals(BLOCK) )
            if( mouseY + ++dy >= TOTAL_COLS )
                return;
        
        if( grid[mouseX][mouseY+dy].equals(EMPTY) ||
            grid[mouseX][mouseY+dy].equals(CHEESE) ) {
            grid[mouseX][mouseY+dy] = BLOCK;
            grid[mouseX][mouseY+1]  = MOUSE;
            grid[mouseX][mouseY]    = EMPTY;
            mouseY++;
        }
    }
    
    /**
     * @STUDENT
     * 
     * Move blocks up, if possible. Blocks cannot be moved if a
     * cat is blocking the stack, or if the blocks are pushing against
     * the edge of the grid
     */
    private void moveBlocksUp() {
        int dx = -1;
        while( grid[mouseX+dx][mouseY].equals(BLOCK) )
            if( mouseX + --dx < 0 )
                return;
        
        if( grid[mouseX+dx][mouseY].equals(EMPTY) ||
            grid[mouseX+dx][mouseY].equals(CHEESE) ) {
            grid[mouseX+dx][mouseY] = BLOCK;
            grid[mouseX-1][mouseY]  = MOUSE;
            grid[mouseX][mouseY]    = EMPTY;
            mouseX--;
        }
    }
    
    /**
     * @STUDENT
     * 
     * Move blocks up, if possible. Blocks cannot be moved if a
     * cat is blocking the stack, or if the blocks are pushing against
     * the edge of the grid
     */
    private void moveBlocksDown() {
        int dx = 1;
        while( grid[mouseX+dx][mouseY].equals(BLOCK) )
            if( mouseX + ++dx >= TOTAL_ROWS )
                return;
        
        if( grid[mouseX+dx][mouseY].equals(EMPTY) ||
            grid[mouseX+dx][mouseY].equals(CHEESE) ) {
            grid[mouseX+dx][mouseY] = BLOCK;
            grid[mouseX+1][mouseY]  = MOUSE;
            grid[mouseX][mouseY]    = EMPTY;
            mouseX++;
        }
    }
    
    /**
     * Try to move all the cats on screen
     */
    private void moveCats() {
        int totalCats = catX.size();
        for( int rep = 0; rep < totalCats; rep++ )
            moveCat(rep);
    }
    
    /**
     * Try to move a cat
     * 
     * @param catIndex The cat's position in the cat lists
     */
    private void moveCat( int catIndex ) {
        int catX = this.catX.get(catIndex);
        int catY = this.catY.get(catIndex);
        String direction = findDirection( catX, catY );
        
        //Set lists for random delta movement
        final double SWAP_CHANCE = 0.5;
        int[] randomDelta = {1,-1};
        randomSwap(randomDelta, SWAP_CHANCE);
        int[] randomUL = {-1,0};
        randomSwap(randomUL, SWAP_CHANCE);
        int[] randomUR = {-1,0};
        randomSwap(randomUR, SWAP_CHANCE);
        randomUR[1] *= -1;
        int[] randomDL = {-1,0};
        randomSwap(randomDL, SWAP_CHANCE);
        randomDL[0] *= -1;
        int[] randomDR = {1,0};
        randomSwap(randomDR, SWAP_CHANCE);
        int[] randomULDR = {1,-1};
        randomSwap(randomULDR, SWAP_CHANCE);
        randomULDR[1] *= -1;
        
        int catDx = 0;
        int catDy = 0;
        
        if( direction.equals("left") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY, 0,-1),               //L
                getNeighbor(catX,catY,randomDelta[0],-1),   //UL/DL
                getNeighbor(catX,catY,randomDelta[1],-1),   //UL/DL
                getNeighbor(catX,catY,randomDelta[0], 0),   //U/D
                getNeighbor(catX,catY,randomDelta[1], 0)    //U/D
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = 0;              catDy = -1; }
                    else if( rep == 1 ) { catDx = randomDelta[0]; catDy = -1; }
                    else if( rep == 2 ) { catDx = randomDelta[1]; catDy = -1; }
                    else if( rep == 3 ) { catDx = randomDelta[0]; catDy =  0; }
                    else                { catDx = randomDelta[1]; catDy =  0; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("right") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY, 0, 1),               //R
                getNeighbor(catX,catY,randomDelta[0], 1),   //UR/DR
                getNeighbor(catX,catY,randomDelta[1], 1),   //UR/DR
                getNeighbor(catX,catY,randomDelta[0], 0),   //U/D
                getNeighbor(catX,catY,randomDelta[1], 0)    //U/D
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = 0;              catDy = 1; }
                    else if( rep == 1 ) { catDx = randomDelta[0]; catDy = 1; }
                    else if( rep == 2 ) { catDx = randomDelta[1]; catDy = 1; }
                    else if( rep == 3 ) { catDx = randomDelta[0]; catDy = 0; }
                    else                { catDx = randomDelta[1]; catDy = 0; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("up") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,-1,0),                 //U
                getNeighbor(catX,catY,-1,randomDelta[0]),    //UL/UR
                getNeighbor(catX,catY,-1,randomDelta[1]),    //UL/UR
                getNeighbor(catX,catY, 0,randomDelta[0]),    //L/R
                getNeighbor(catX,catY, 0,randomDelta[1])     //L/R
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = -1; catDy = 0;              }
                    else if( rep == 1 ) { catDx = -1; catDy = randomDelta[0]; }
                    else if( rep == 2 ) { catDx = -1; catDy = randomDelta[1]; }
                    else if( rep == 3 ) { catDx = 0;  catDy = randomDelta[0]; }
                    else                { catDx = 0;  catDy = randomDelta[1]; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("down") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,1, 0),               //D
                getNeighbor(catX,catY,1,randomDelta[0]),   //DL/DR
                getNeighbor(catX,catY,1,randomDelta[1]),   //DL/DR
                getNeighbor(catX,catY,0,randomDelta[0]),   //L/R
                getNeighbor(catX,catY,0,randomDelta[1])    //L/R
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = 1; catDy = 0;              }
                    else if( rep == 1 ) { catDx = 1; catDy = randomDelta[0]; }
                    else if( rep == 2 ) { catDx = 1; catDy = randomDelta[1]; }
                    else if( rep == 3 ) { catDx = 0; catDy = randomDelta[0]; }
                    else                { catDx = 0; catDy = randomDelta[1]; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("UL") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,-1,-1),                         //UL
                getNeighbor(catX,catY,randomUL[0],   randomUL[1]),    //U/L
                getNeighbor(catX,catY,randomUL[1],   randomUL[0]),    //U/L
                getNeighbor(catX,catY,randomDelta[0],randomDelta[1]), //UR/DL
                getNeighbor(catX,catY,randomDelta[1],randomDelta[0])  //UR/DL
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = -1;             catDy = -1;             }
                    else if( rep == 1 ) { catDx = randomUL[0];    catDy = randomUL[1];    }
                    else if( rep == 2 ) { catDx = randomUL[1];    catDy = randomUL[0];    }
                    else if( rep == 3 ) { catDx = randomDelta[0]; catDy = randomDelta[1]; }
                    else                { catDx = randomDelta[1]; catDy = randomDelta[0]; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("UR") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,-1,1),                             //UR
                getNeighbor(catX,catY,randomUR[0],     randomUR[1]),     //U/R
                getNeighbor(catX,catY,randomUR[1]*-1,  randomUR[0]*-1),  //U/R
                getNeighbor(catX,catY,randomULDR[0],   randomULDR[1]),   //UL/DR
                getNeighbor(catX,catY,randomULDR[0]*-1,randomULDR[1]*-1) //UL/DR
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = -1;               catDy = 1;                }
                    else if( rep == 1 ) { catDx = randomUR[0];      catDy = randomUR[1];      }
                    else if( rep == 2 ) { catDx = randomUR[1]*-1;   catDy = randomUR[0]*-1;   }
                    else if( rep == 3 ) { catDx = randomULDR[0];    catDy = randomULDR[1];    }
                    else                { catDx = randomULDR[0]*-1; catDy = randomULDR[1]*-1; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else if( direction.equals("DL") ) {
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,1,-1),                             //DL
                getNeighbor(catX,catY,randomDL[0],     randomUR[1]),     //D/L
                getNeighbor(catX,catY,randomDL[1]*-1,  randomUR[0]*-1),  //D/L
                getNeighbor(catX,catY,randomULDR[0],   randomULDR[1]),   //UL/DR
                getNeighbor(catX,catY,randomULDR[0]*-1,randomULDR[1]*-1) //UL/DR
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = -1;               catDy = 1;                }
                    else if( rep == 1 ) { catDx = randomDL[0];      catDy = randomDL[1];      }
                    else if( rep == 2 ) { catDx = randomDL[1]*-1;   catDy = randomDL[0]*-1;   }
                    else if( rep == 3 ) { catDx = randomULDR[0];    catDy = randomULDR[1];    }
                    else                { catDx = randomULDR[0]*-1; catDy = randomULDR[1]*-1; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        } else { /*if( direction.equals("DR") ) {*/
            //Precedence order for pathfinding
            String[] neighbors = {
                getNeighbor(catX,catY,1,1),                           //DR
                getNeighbor(catX,catY,randomDR[0],   randomDR[1]),    //D/R
                getNeighbor(catX,catY,randomDR[1],   randomDR[0]),    //D/R
                getNeighbor(catX,catY,randomDelta[0],randomDelta[1]), //UR/DL
                getNeighbor(catX,catY,randomDelta[1],randomDelta[0])  //UR/DL
            };
            for( int rep = 0; rep < neighbors.length; rep++ ) {
                if( neighbors[rep].equals(EMPTY) || neighbors[rep].equals(MOUSE) ) {
                    this.catStuck.set(catIndex, false);
                    //Set deltas
                    if(      rep == 0 ) { catDx = 1;              catDy = 1;              }
                    else if( rep == 1 ) { catDx = randomDR[0];    catDy = randomDR[1];    }
                    else if( rep == 2 ) { catDx = randomDR[1];    catDy = randomDR[0];    }
                    else if( rep == 3 ) { catDx = randomDelta[0]; catDy = randomDelta[1]; }
                    else                { catDx = randomDelta[1]; catDy = randomDelta[0]; }
                    
                    moveCat( catIndex, catX, catY, catDx, catDy );
                    if( neighbors[rep].equals(MOUSE) ) {
                        moveMouseCenter();
                        killMouse();
                    }
                    return;
                }
            }
        }
        
        //No move was found... cat is stuck
        this.catStuck.set(catIndex, true);
    }
    
    /**
     * Move cat to new location
     * 
     * @param catIndex The index of the cat in the list
     * @param catX The x position of the cat
     * @param catY The y position of the cat
     * @param catDx The change in x position of the cat
     * @param catDy The change in y position of the cat
     */
    private void moveCat( int catIndex, int catX, int catY, int catDx, int catDy ) {
        grid[catX+catDx][catY+catDy] = CAT;
        grid[catX][catY] = EMPTY;
        this.catX.set(catIndex, catX + catDx);
        this.catY.set(catIndex, catY + catDy);
    }
    
    /**
     * Randomize an int array and return it
     * 
     * @param list The list to shuffle
     */
    private void shuffle( int[] list ) {
        for( int rep = list.length - 1; rep > 0; rep-- ) {
            //Pseudorandom index
            int index = ((int)(Math.random()*10*((double)(rep+1)))) % list.length;
            //Swap
            int temp = list[index];
            list[index] = list[rep];
            list[rep] = temp;
        }
    }
    
    /**
     * Randomly swap elements with other elements in the list, or keep them where they are
     * 
     * @param list The array to perform swaps on
     * @param swapChance The chance that an element is swapped or remains unswapped
     */
    private void randomSwap( int[] list, double swapChance ) {
         for( int rep = 0; rep < list.length; rep++ ) {
             double randomValue = Math.random();
             if( randomValue < swapChance )
                 swap(list, rep);
         }
    }
    
    /**
     * Swap two values in a list
     * 
     * @param list The list to swap
     * @param rep The index of the value to swap
     */
    private void swap( int[] list, int index ) {
        Random random = new Random();
        int otherIndex = 0;
        do { //Get random index not equal to the index passed in
            otherIndex = random.nextInt( list.length );
        } while( otherIndex == index );
        
        //Make swap
        int temp = list[index];
        list[index] = list[otherIndex];
        list[otherIndex] = temp;
    }
    
    /**
     * Find the direction the mouse is in from the current position
     * 
     * @param catX The x coordinate of the cat
     * @param catY The y coordinate of the cat
     * @return String The direction of the mouse from the cat
     */
    private String findDirection( int catX, int catY ) {
        final double ACUTE_MARGIN  = 22.5;
        final double OBTUSE_MARGIN = 67.5;
        
        int x_dif = mouseX - catX;
        int y_dif = mouseY - catY;
        if( x_dif > 0 ) { //mouse is right
            if( y_dif > 0 ) { //mouse is down
                double angle = getAngle( (double)y_dif, (double)x_dif );
                if(      Math.abs(angle) <= ACUTE_MARGIN )
                    return "down";
                else if( Math.abs(angle) > ACUTE_MARGIN &&
                         Math.abs(angle) < OBTUSE_MARGIN )
                    return "DR"; //down-right diagonal, or (1,1) dx,dy
                else
                    return "right";
            } else if( y_dif < 0 /*mouse is up*/) {
                double angle = getAngle( (double)y_dif, (double)x_dif );
                if(      Math.abs(angle) <= ACUTE_MARGIN )
                    return "up";
                else if( Math.abs(angle) > ACUTE_MARGIN &&
                         Math.abs(angle) < OBTUSE_MARGIN )
                    return "UR"; //up-right diagonal, or (-1,1) dx,dy
                else
                    return "right";
            } else { //mouse is directly to the right
                return "right";
            }
        } else if( x_dif < 0 ) { //mouse is left
            if( y_dif > 0 ) { //mouse is down
                double angle = getAngle( (double)y_dif, (double)x_dif );
                if(      Math.abs(angle) <= ACUTE_MARGIN )
                    return "down";
                else if( Math.abs(angle) > ACUTE_MARGIN &&
                         Math.abs(angle) < OBTUSE_MARGIN )
                    return "DL"; //down-left diagonal, or (1,-1) dx,dy
                else
                    return "left";
            } else if( y_dif < 0 /*mouse is up*/) {
                double angle = getAngle( (double)y_dif, (double)x_dif );
                if(      Math.abs(angle) <= ACUTE_MARGIN )
                    return "up";
                else if( Math.abs(angle) > ACUTE_MARGIN &&
                         Math.abs(angle) < OBTUSE_MARGIN )
                    return "UL"; //up-left diagonal, or (-1,-1) dx,dy
                else
                    return "left";
            } else { //mouse is directly to the left
                return "left";
            }
        } else if( y_dif > 0 ) { //mouse is directly down
            return "down";
        } else { //mouse is directly up
            return "up";
        }
    }
    
    /**
     * Gets the angle of the triangle made between the two segments and the hypotenuse
     * of a triangle using arctan (trig)
     * 
     * @param legOpp The length of the opposite side of the triangle
     * @param legAdj The length of the adjacent side of the triangle
     * @return double The angle between the adjacent side and the hypotenuse
     */
    private double getAngle( double legOpp, double legAdj ) {
        return Math.toDegrees( Math.atan( legOpp / legAdj ) );
    }
    
    /**
     * Check and see if all the cats are stuck or not. If they are, turn them to cheese
     * and update the score
     */
    private void checkCats() {
        boolean allCatsStuck = areCatsStuck();
        
        if( !allCatsStuck )
            return;
        
        //Turn cats to cheese
        int totalCats = this.catStuck.size();
        for( int cat = 0; cat < totalCats; cat++ )
            grid[this.catX.get(cat)][this.catY.get(cat)] = CHEESE;
        
        this.catX.clear();
        this.catY.clear();
        this.catStuck.clear();
    }
    
    /**
     * Finds whether all the cats are stuck or not
     * 
     * @return boolean True if all the cats are stuck, false otherwise
     */
    private boolean areCatsStuck() {
        int totalCats = this.catStuck.size();
        for( int cat = 0; cat < totalCats; cat++ ) {
            if( !this.catStuck.get(cat) )
                return false;
        }
        
        return true;
    }
    
    /**
     * @STUDENT
     * 
     * Update the score in the console
     */
    private void updateScore() {
        
    }
    
    /**
     * @STUDENT
     * 
     * Prints the grid's contents
     */
    private void printGrid() {
        for( int row = 0; row < TOTAL_ROWS; row++ ) {
            for( int col = 0; col < TOTAL_COLS; col++ ) {
                if( grid[row][col].equals( EMPTY ) )
                    SOP("  ");
                else
                    SOP( grid[row][col] + " " );
            }
            SOPln();
        }
    }
    
    /**
     * Gets a Scanner object that is reading a text file
     * 
     * @param textFileName The name of the text file that contains the level
     *                     data
     * @return Scanner The Scanner object that is reading the text file
     */
    private Scanner getScanner( String textFileName ) {
        File file = new File( textFileName );
        Scanner scanner = null; /*'null' is an object that is instantiated,
                                  but has no values set yet*/
        try {
            scanner = new Scanner( file ); //reading the level data
        } catch( FileNotFoundException e ) {
            e.printStackTrace();
        }
        
        return scanner;
    }
    
    /**
     * Clear the console
     */
    private void clearConsole() {
        System.out.print('\u000C'); //Clear terminal
    }
    
    private void SOPln( String str ) {
        System.out.println( str );
    }
    private void SOPln() {
        System.out.println("");
    }
    private void SOP( String str ) {
        System.out.print( str );
    }
    private void SOP() {
        System.out.print(""); //For debugging
    }
    
}
