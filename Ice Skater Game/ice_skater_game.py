'''
5.	Write a function that takes in a grid of Strings. The String values
 will either have “.” for empty, “X” for a block, “O” for the ice skater,
  or “T” for the treasure. The function should take in input from the
   user, accepting the ‘W’, ‘A’, ‘S’, or ‘D’ keys, for the upward, left,
    down, or right directions, respectively. The ice skater will move
     in the direction entered, until they hit an ‘X’ or the edge of the
      grid. When the ice skater ends up on the “T” square, tell the user
       that they found the treasure and then end the function. The grid
        should be reprinted each time that the ice skater moves, but you
do not need to reprint for each square that they move, only their final
 position once they run into an ‘X’ or the edge of the grid
'''

'''
Pseudocode:
* Create a grid -> made outside the function
--> Function name: grid_maker()
* Taking in and analyzing user input
--> Function name: get_user_input()
* Move ice skater in a staight line up,down,left,or right
--> Function name: move_skater()
* Ice skater stops when they hit a block, or a treasure, or the edge of
the grid
--> Function name: also in move_skater()
* Update and print our grid
--> Function name: print_grid()
* End function / say 'you win!' when the ice skater finds the treasure
--> Function name: end_game()

Application Structure

grid_maker()

loop: continue skating until find treasure
    print_grid()
    get_user_input()
    move_skater()
end_game()
'''


def ice_skater_game(grid):
    found_treasure = False
    while not found_treasure:
        x, y = print_grid(grid)
        direction = get_user_input()
        found_treasure, x, y = move_skater(x, y, direction, grid)
    end_game()


def print_grid(grid):
    x, y = 0, 0
    for row in range(0, len(grid)):
        for col in range(0, len(grid[row])):
            if grid[row][col] == 'O':
                x = row
                y = col
            if grid[row][col] == '.':
                print(" ", end='')
            else:
                print(grid[row][col], end='')
        print('', end="\n")
    return x, y


def get_user_input():
    direction = input("What direction? Enter W,A,S,or D")
    return direction


def move_skater(x, y, direction, grid):
    # These conditions check to make sure the ice skater is not
    # outside the bounds of the grid, or inside a block tile,
    # or inside a treasure

    # Remove player position
    grid[x][y] = '.'

    while x >= 0 and y >= 0 and x < len(grid) and \
               y < len(grid[0]) and grid[x][y] != 'X':
        if direction == 'W':
            x -= 1
        elif direction == 'S':
            x += 1
        elif direction == 'A':
            y -= 1
        else:
            y += 1
        # Check if found treasure
        if x >= 0 and y >= 0 and x < len(grid) and \
               y < len(grid[0]) and grid[x][y] == 'T':
            return True, x, y
    # We ended in an invalid position, so go back one step
    if direction == 'W':
        x += 1
    elif direction == 'S':
        x -= 1
    elif direction == 'A':
        y += 1
    else:
        y -= 1

    # Update position in grid
    grid[x][y] = 'O'

    return False, x, y  # Did not find the treasure


def end_game():
    print("You found the treasure!")


grid = [['.', '.', '.', '.', 'X'],
        ['X', '.', '.', '.', '.'],
        ['.', '.', 'O', '.', '.'],
        ['.', 'X', '.', 'T', '.'],
        ['.', '.', '.', '.', '.']]
ice_skater_game(grid)

