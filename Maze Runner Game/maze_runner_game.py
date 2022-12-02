"""
Maze Runner Game

Maze Runner is a game where a player tries to find treasure
in a maze without running into any monsters lurking in that maze

@author Peter Olson
"""

import turtle
import functools
import random
import time

# NOTE: cannot play sound in replit currently
# import os #for macs
# import winsound #for windows

# Set up window
window = turtle.Screen()
window.title("Maze Runner by Peter Olson")
window.bgcolor("black")
window.setup(width=0.3, height=0.5)
width = window.window_width()
height = window.window_height()
window.tracer(0)
turtle.hideturtle()
turtle.penup()
turtle.color("white")
turtle.goto(0, -width//1.7)

# Settings
DEFAULT_TEXT_FILE = "maze_1.txt"
MAZE_TEXT_FILE = "maze_2.txt"
BLOCK_STR = OB = 'X'
PLAYER_STR = 'C'
TREASURE_STR = 'T'
MONSTER_STR = 'M'
EMPTY_STR = '.'
ALIVE = 0
DEAD = 1
WIN = 2
PLAYER_STATE = ALIVE
LEFT = 0
UP = 1
RIGHT = 2
DOWN = 3
KEY_LEFT = "a"
KEY_UP = "w"
KEY_RIGHT = "d"
KEY_DOWN = "s"
MONSTER_SPEED_IN_SEC = 1  # in seconds

# Text outputs
game_end_list = ["YOU DIED. You were beaten to a pulp by a minotaur.",
                 "FATALITY. Human be squished. Better luck next time.",
                 "ded. u got rekd lol",
                 "R.I.P. Here lies you, who could not live through a dinky maze.",
                 "Wasted. A radioactive baboon bashed your brains in.",
                 "You're dead. Can I have your salad?",
                 "All hail, king of the losers.",
                 "Ermergerd yer ded :(",
                 "Unlucky bro. That monster came out of nowhere!",
                 "You ran into a monster. It ate you. GG."]
game_win_list = ["YOU WIN! The hidden treasure is yours. Now to get back out...",
                 "Winner winner, chicken dinner.",
                 "The treasure... it's so shiny! Good job homie.",
                 "You reach the treasure out of breath. It's a box of Captain Crunch.",
                 "Hey!! Wow! That's really good! You like won, man!",
                 "Mission accomplished. Treasure achieved. Heading back to base.",
                 "Level 1 complete. Now onto level 2...",
                 "Congrats! You didn't die and you now have your very own yellow triangle.",
                 "Stoopid monsters. Mazes are for purple circles."]


# Classes
class Cell:
    def __init__(self, x, y, turtle_obj):
        self.x = x
        self.y = y
        self.turtle_obj = turtle_obj


class Player(Cell):
    def __init__(self, x, y, turtle_player, state):
        super().__init__(x, y, turtle_player)
        self.state = state


class Monster(Cell):
    def __init__(self, x, y, turtle_player, direction=UP):
        super().__init__(x, y, turtle_player)
        self.direction = direction


# Functions
def make_grid(size, data_type):
    return [[data_type] * size for _ in range(size)]


def create_maze(file=DEFAULT_TEXT_FILE):
    cell_maze = [[]]

    file_object = open(file, "r")
    row = 0
    for line in file_object:
        cell_list = line.split(" ")
        for cell in cell_list:
            cell_maze[row].append(cell.strip())
        row += 1
        new_list = []
        cell_maze.append(new_list)

    del cell_maze[len(cell_maze) - 1]
    file_object.close()

    return cell_maze


def print_maze_debug(maze):
    for row in maze:
        for col in row:
            print(col, end=' ')
        print("", end="\n")


# Set up grid objects
grid = create_maze(MAZE_TEXT_FILE)
# print_maze_debug(grid)
cell_width = width / len(grid[0])
cell_height = height / len(grid)


def print_maze(block_list):
    for row in range(0, len(block_list)):
        for col in range(0, len(block_list[row])):
            cell = block_list[row][col]
            if cell is not None:
                img = cell.turtle_obj
                loc_x = height//2 - cell.x * cell_height
                loc_y = cell.y * cell_width - width//2
                # print("(", loc_x, ",", loc_y, ")", sep='')
                img.goto(loc_y, loc_x)


def create_objects(maze):
    image_grid = make_grid(len(maze), None)
    player = None
    monsters = []
    treasure = None
    for row in range(0, len(maze)):
        for col in range(0, len(maze[row])):
            if maze[row][col] == BLOCK_STR:
                create_block(row, col, image_grid, "grey", "grey")
            elif maze[row][col] == EMPTY_STR:
                # create_block(row, col, image_grid, "grey")
                image_grid[row][col] = None
            elif maze[row][col] == PLAYER_STR:
                player = create_player(row, col)
                image_grid[row][col] = None
            elif maze[row][col] == MONSTER_STR:
                monster = create_monster(row, col)
                monsters.append(monster)
                image_grid[row][col] = None
            elif maze[row][col] == TREASURE_STR:
                treasure = create_treasure(row, col)
                image_grid[row][col] = None

    return image_grid, player, monsters, treasure


def create_block(row, col, image_grid, color="grey", outline="black"):
    block = turtle.Turtle()  # create Turtle object
    block.speed('fastest')  # set speed of animation (to max)
    block.shape("square")
    block.color(color, outline)
    block.penup()  # No white lines as object moves
    block.pensize(int(cell_width))
    block_cell = Cell(row, col, block)
    image_grid[row][col] = block_cell


def create_player(row, col):
    player = turtle.Turtle()  # create Turtle object
    player.speed('fastest')  # set speed of animation (to max)
    player.shape("circle")
    player.color("purple")
    player.penup()  # No white lines as object moves
    player.pensize(int(cell_width))
    loc_x = height//2 - row * cell_height
    loc_y = col * cell_width - width//2
    player.goto(loc_y, loc_x)
    player_cell = Player(row, col, player, ALIVE)
    return player_cell


def create_monster(row, col):
    monster = turtle.Turtle()  # create Turtle object
    monster.speed('fastest')  # set speed of animation (to max)
    monster.shape("circle")
    monster.color("red")
    monster.penup()  # No white lines as object moves
    monster.pensize(int(cell_width))
    loc_x = height // 2 - row * cell_height
    loc_y = col * cell_width - width // 2
    monster.goto(loc_y, loc_x)
    monster_cell = Monster(row, col, monster)
    return monster_cell


def create_treasure(row, col):
    treasure = turtle.Turtle()  # create Turtle object
    treasure.speed('fastest')  # set speed of animation (to max)
    treasure.shape("triangle")
    treasure.color("yellow")
    treasure.penup()  # No white lines as object moves
    treasure.pensize(int(cell_width))
    loc_x = height // 2 - row * cell_height
    loc_y = col * cell_width - width // 2
    treasure.goto(loc_y, loc_x)
    treasure_cell = Cell(row, col, treasure)
    return treasure_cell


def get_neighbor(pos_x, pos_y, cell_grid):
    if pos_x < 0 or pos_x >= len(cell_grid) or \
            pos_y < 0 or pos_y >= len(cell_grid[0]):
        return OB
    elif cell_grid[pos_x][pos_y] == BLOCK_STR:
        return BLOCK_STR
    elif cell_grid[pos_x][pos_y] == MONSTER_STR:
        return MONSTER_STR
    elif cell_grid[pos_x][pos_y] == TREASURE_STR:
        return TREASURE_STR
    elif cell_grid[pos_x][pos_y] == PLAYER_STR:
        return PLAYER_STR
    else:
        return EMPTY_STR


block_grid, player_obj, monster_list, treasure_obj = create_objects(grid)
print_maze(block_grid)


# Key-binding functions for player movement
def move(player_ref, lurd_dir):
    px = player_ref.x
    py = player_ref.y
    dx, dy = 0, 0
    if lurd_dir == KEY_UP:
        dx = -1
    elif lurd_dir == KEY_DOWN:
        dx = 1
    elif lurd_dir == KEY_LEFT:
        dy = -1
    else:  # KEY_RIGHT
        dy = 1
    neighbor = get_neighbor(px+dx, py+dy, grid)
    if neighbor == MONSTER_STR:
        player_ref.state = DEAD
    elif neighbor == TREASURE_STR:
        player_ref.state = WIN
    elif neighbor == EMPTY_STR:
        player_ref.state = ALIVE
        grid[player_ref.x][player_ref.y] = EMPTY_STR
        loc_x = height // 2 - (px+dx) * cell_height
        loc_y = (py+dy) * cell_width - width // 2
        player_ref.turtle_obj.goto(loc_y, loc_x)
        player_ref.x = px + dx
        player_ref.y = py + dy
        grid[player_ref.x][player_ref.y] = PLAYER_STR
    else:  # Player tried to move onto block or off of grid
        pass


def move_monsters(enemy_list):
    for enemy in enemy_list:
        move_monster(enemy)


def get_enemy_neighbor(enemy):
    if enemy.direction == UP:
        neighbor = get_neighbor(enemy.x-1, enemy.y, grid)
    elif enemy.direction == DOWN:
        neighbor = get_neighbor(enemy.x+1, enemy.y, grid)
    elif enemy.direction == LEFT:
        neighbor = get_neighbor(enemy.x, enemy.y-1, grid)
    else:  # enemy.direction == RIGHT
        neighbor = get_neighbor(enemy.x, enemy.y+1, grid)

    return neighbor


# Rotate direction clockwise
def change_direction(enemy):
    if enemy.direction == LEFT:
        enemy.direction = UP
    elif enemy.direction == UP:
        enemy.direction = RIGHT
    elif enemy.direction == RIGHT:
        enemy.direction = DOWN
    else:  # enemy.direction == DOWN
        enemy.direction = LEFT


def update_direction(enemy, neighbor):
    if neighbor == PLAYER_STR:
        player_obj.state = DEAD
        return True
    elif neighbor == MONSTER_STR or \
            neighbor == BLOCK_STR or \
            neighbor == TREASURE_STR or \
            neighbor == OB:
        change_direction(enemy)
        return True
    else:  # neighbor == EMPTY_STR
        return False


def move_direction(enemy):
    enemy_x = enemy.x
    enemy_y = enemy.y
    dx = dy = 0
    if enemy.direction == UP:
        dx = -1
    elif enemy.direction == DOWN:
        dx = 1
    elif enemy.direction == LEFT:
        dy = -1
    else:  # enemy.direction == RIGHT
        dy = 1
    grid[enemy.x][enemy.y] = EMPTY_STR
    loc_x = height // 2 - (enemy_x + dx) * cell_height
    loc_y = (enemy_y + dy) * cell_width - width // 2
    enemy.turtle_obj.goto(loc_y, loc_x)
    enemy.x = enemy_x + dx
    enemy.y = enemy_y + dy
    grid[enemy.x][enemy.y] = MONSTER_STR


def move_monster(enemy):
    neighbor = get_enemy_neighbor(enemy)
    changed_direction = update_direction(enemy, neighbor)
    if not changed_direction:
        move_direction(enemy)


# Keyboard binding
window.listen()
window.onkeypress(functools.partial(move, player_obj, KEY_UP), KEY_UP)
window.onkeypress(functools.partial(move, player_obj, KEY_DOWN), KEY_DOWN)
window.onkeypress(functools.partial(move, player_obj, KEY_LEFT), KEY_LEFT)
window.onkeypress(functools.partial(move, player_obj, KEY_RIGHT), KEY_RIGHT)
window.listen()

# Main game loop - Note that most of the action is happening
# when the game is first set up, and through keyboard inputs
start_time = time.time()
while True:
    window.update()

    # Move monsters
    if time.time() - start_time > MONSTER_SPEED_IN_SEC:
        move_monsters(monster_list)
        start_time = time.time()

    # Check game status
    if player_obj.state == DEAD:
        turtle.write(game_end_list[random.randint(0, len(game_end_list)-1)],
                     align="center", font=('Arial', 24, 'normal'))
        player_obj.turtle_obj.reset()
        break
    elif player_obj.state == WIN:
        turtle.write(game_win_list[random.randint(0, len(game_win_list)-1)],
                     align="center", font=('Arial', 24, 'normal'))
        player_obj.turtle_obj.reset()
        break

turtle.done()
