import Module_Pygame as mod
import copy

def copier(objet):
    return copy.deepcopy(objet)

# ----------------- BALLS -----------------

class Ball:
    def __init__(self):
        self.coord = None
        self.rayon = None
    def init_slider(self, coord, rayon):
        self.set_coord(coord)
        self.rayon = rayon
    def set_coord(self, coord):
        self.coord = coord
    def draw(self):
        mod.circle(self.coord[0], self.coord[1], 0.9 * self.rayon, "#AC7C5E")
        mod.circle(self.coord[0], self.coord[1], 0.9 * self.rayon, "#5E5E5E", round(self.rayon/6))

# ----------------- SLIDES ------------------

UP = "UP"
DOWN = "DOWN"
LEFT = "LEFT"
RIGHT = "RIGHT"

class Slide:
    global UP, DOWN, LEFT, RIGHT
    def __init__(self, type_s : str = None, directions = [], isFinal = False):
        if type_s != None:
            if isinstance(type_s, list):
                directions = type_s
            else:
                directions = self.set_directions(type_s)
        self.directions = directions
        self.directions.sort()
        self.provenances = self.set_provenances()
        self.provenances.sort()
        self.goal = isFinal
        self.coord = None
        self.taille = None
    
    def init_draw(self, coord_centre, taille_case):
        self.coord = coord_centre
        self.taille = taille_case
    
    def draw(self):
        mod.rect_t(self.coord[0]-self.taille/2, self.coord[1] - self.taille/2, self.taille+1, self.taille+1, "#777777")
        color = "#222222"
        taille = 3/4
        t = taille * self.taille+1
        if UP in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-self.taille/2
            mod.rect_t(x, y, t, (self.taille+t)/2+1, color)
        if DOWN in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-t/2
            mod.rect_t(x, y, t, (self.taille+t)/2+1, color)
        if LEFT in self.directions:
            x, y = self.coord[0] - self.taille/2, self.coord[1]-t/2
            mod.rect_t(x, y, (self.taille+t)/2+1, t, color)
        if RIGHT in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-t/2
            mod.rect_t(x, y, (self.taille+t)/2+1, t, color)
        if self.goal:
            mod.circle(self.coord[0], self.coord[1], self.taille/3, "#333333")
            mod.circle(self.coord[0], self.coord[1], self.taille/3, "#777777", round(self.taille/3/6))
    
    def canGoTo(self, direction):
        return direction in self.directions
    
    def canComeFrom(self, direction):
        return direction in self.provenances
    
    def set_directions(self, t):
        if t in [UP, DOWN, LEFT, RIGHT]:
            l = [t]
        else:
            v = [UP, DOWN]
            h = [LEFT, RIGHT]
            l = []
            if t == "CROSS":
                l = v + h
            elif t == "VERT":
                l = v
            elif t == "HORI":
                l = h
            elif t[0] == "T":
                if t[1:] in [UP, DOWN]:
                    l = h
                if t[1:] in [LEFT, RIGHT]:
                    l = v
                l += [t[1:]]
            elif t[0] == "C":
                arr = t.split("_")
                arr.pop(0)
                l = [arr[0], arr[1]]
        return l
    
    def set_provenances(self):
        l = []
        if self.canGoTo(DOWN):
            l.append(UP)
        if self.canGoTo(UP):
            l.append(DOWN)
        if self.canGoTo(RIGHT):
            l.append(LEFT)
        if self.canGoTo(LEFT):
            l.append(RIGHT)
        return l

CROSS = Slide("CROSS")
VERT = Slide("VERT")
HORI = Slide("HORI")
T_UP = Slide("TUP")
T_DOWN = Slide("TDOWN")
T_LEFT = Slide("TLEFT")
T_RIGHT = Slide("TRIGHT")
C_UL = Slide("C_UP_LEFT")
C_UR = Slide("C_UP_RIGHT")
C_DL = Slide("C_DOWN_LEFT")
C_DR = Slide("C_DOWN_RIGHT")
S_L = Slide("LEFT")
S_R = Slide("RIGHT")
S_U = Slide("UP")
S_D = Slide("DOWN")

# ----------------- BOUTONS -----------------

class Bouton:
    global UP, DOWN, LEFT, RIGHT
    def __init__(self, coord, taille, sens):
        self.coord = coord
        self.taille = taille
        self.sens = sens # UP, DOWN, LEFT, RIGHT
        self.pressed = False
        self.clicked = False
    
    def update(self, mouse_coord, left_click_pushed, left_click_pressed):
        self.pressed = False
        self.clicked = False
        if left_click_pressed:
            if abs(mouse_coord[0] - self.coord[0]) < self.taille/2 and abs(mouse_coord[1] - self.coord[1]) < self.taille/2:
                self.pressed = True
                if left_click_pushed:
                    self.clicked = True
    
    def draw(self):
        x, y = self.coord
        mod.rect_t(x-self.taille/2, y-self.taille/2, self.taille, self.taille, "#65492C")
        fact = 0.45
        col = "#A67C4C"
        if self.pressed:
            col = mod.transition("#A67C4C", "#000000", 20)
        if self.sens == UP:
            mod.rect_t(x-fact*self.taille/2, y-fact*self.taille/2, fact*self.taille, (fact+1)*self.taille/2+1, col)
        if self.sens == DOWN:
            mod.rect_t(x-fact*self.taille/2, y-self.taille/2, fact*self.taille, (fact+1)*self.taille/2, col)
        if self.sens == RIGHT:
            mod.rect_t(x-self.taille/2, y-fact*self.taille/2, (fact+1)*self.taille/2, fact*self.taille, col)
        if self.sens == LEFT:
            mod.rect_t(x-fact*self.taille/2, y-fact*self.taille/2, (fact+1)*self.taille/2+1, fact*self.taille, col)

# ----------------- ENIGME ------------------

class Enigme:
    global UP, DOWN, LEFT, RIGHT
    def __init__(self, coord, taille, balls : list, goals : list, slides : list):
        """
        :params:
        - balls : la liste des positions initiles des boules
        - slides : la matrice contenant les différentes directions possibles des sliders en fonction de leur position (Slide)
        """
        assert len(slides) == len(slides[0]) and len(balls) == len(goals)
        self.taille = len(slides)
        self.coord = coord
        self.taille_tot = taille
        self.taille_case = self.taille_tot/self.taille
        self.goals = goals
        self.init_slides(slides)
        self.nb_balls = len(balls)
        self.init_balls(balls)
        self.debut = copier(self.balls)
        self.init_boutons()
        self.win = False
    
    def updCoord(self):
        for j in range(self.taille):
            for i in range(self.taille):
                if self.balls[j][i] != None:
                    x, y = self.coord[0]-self.taille_tot/2 + (i+1/2) * self.taille_case, self.coord[1]-self.taille_tot/2 + (j+1/2) * self.taille_case
                    self.balls[j][i].set_coord((x, y))
    
    def update(self, mouse_coord, left_click_pushed, left_click_pressed):
        if not self.win:
            change = False
            change = self.updBouton(mouse_coord, left_click_pushed, left_click_pressed)
            if change:
                self.updCoord()
            if self.verif_win():
                self.win = True
    
    def draw(self):
        for j in range(self.taille):
            for i in range(self.taille):
                self.slides[j][i].draw()
        for j in range(self.taille):
            for i in range(self.taille):
                if self.balls[j][i] != None:
                    self.balls[j][i].draw()
        for j in range(self.taille+2):
            for i in range(self.taille+2):
                if self.boutons[j][i] != None:
                    self.boutons[j][i].draw()
    
    def updBouton(self, mouse_coord, left_click_pushed, left_click_pressed):
        change = False
        for j in range(self.taille+2):
            for i in range(self.taille+2):
                if self.boutons[j][i] != None:
                    self.boutons[j][i].update(mouse_coord, left_click_pushed, left_click_pressed)
                    if self.boutons[j][i].clicked:
                        self.move(i, j)
                        change = True
        return change
    
    def move(self, i_b, j_b):
        # Attention ici i_b et j_b sont les coordonnées du bouton cliqué, ainsi la colonne 0 de balls est la colonne 1 de bouton
        if (i_b == 0):
            # Vers la gauche
            j = j_b-1
            for i in range(1, self.taille):
                b = self.balls[j][i]
                decal = 0
                while i-decal > 0 and "LEFT" in self.slides[j][i-decal].directions and self.balls[j][i-decal-1] == None:
                    decal += 1
                if decal != 0:
                    self.balls[j][i-decal] = b
                    self.balls[j][i] = None
        if (i_b == self.taille+1):
            # Vers la droite
            j = j_b-1
            for i2 in range(0, self.taille-1):
                i = self.taille-2-i2
                b = self.balls[j][i]
                decal = 0
                while i+decal < self.taille-1 and "RIGHT" in self.slides[j][i+decal].directions and self.balls[j][i+decal+1] == None:
                    decal += 1
                if decal != 0:
                    self.balls[j][i+decal] = b
                    self.balls[j][i] = None
        if (j_b == 0):
            # Vers le haut
            i = i_b-1
            for j in range(1, self.taille):
                b = self.balls[j][i]
                decal = 0
                while j-decal > 0 and "UP" in self.slides[j-decal][i].directions and self.balls[j-decal-1][i] == None:
                    decal += 1
                if decal != 0:
                    self.balls[j-decal][i] = b
                    self.balls[j][i] = None
        if (j_b == self.taille+1):
            # Vers le bas
            i = i_b-1
            for j2 in range(0, self.taille-1):
                j = self.taille-2-j2
                b = self.balls[j][i]
                decal = 0
                while j+decal < self.taille-1 and "DOWN" in self.slides[j+decal][i].directions and self.balls[j+decal+1][i] == None:
                    decal += 1
                if decal != 0:
                    self.balls[j+decal][i] = b
                    self.balls[j][i] = None
    
    def verif_win(self):
        count = 0
        for j in range(self.taille):
            for i in range(self.taille):
                b = self.balls[j][i]
                if b != None and self.slides[j][i].goal:
                    count += 1
        return count == self.nb_balls
    
    def reset(self):
        self.win = False
        self.balls = copier(self.debut)
        self.update((0, 0), False, False)
    
    def init_boutons(self):
        self.boutons = [[None for i in range(self.taille+2)] for j in range(self.taille+2)]
        for j in range(self.taille+2):
            for i in range(self.taille+2):
                sens = None
                if (i == 0 and (j != 0) and (j != self.taille+1)):
                    sens = LEFT
                if (j == 0 and (i != 0) and (i != self.taille+1)):
                    sens = UP
                if (i == self.taille+1 and (j != 0) and (j != self.taille+1)):
                    sens = RIGHT
                if (j == self.taille+1 and (i != 0) and (i != self.taille+1)):
                    sens = DOWN
                if sens != None:
                    x, y = self.coord[0]-(self.taille_tot+2*self.taille_case)/2 + (i+1/2) * self.taille_case, self.coord[1]-(self.taille_tot + 2*self.taille_case)/2 + (j+1/2) * self.taille_case
                    self.boutons[j][i] = Bouton((x, y), self.taille_case, sens)
    
    def init_balls(self, balls):
        self.balls = [[None for i in range(self.taille)] for j in range(self.taille)]
        for s in balls:
            self.balls[s[1]][s[0]] = Ball()
            self.balls[s[1]][s[0]].rayon = self.taille_case/3
        self.updCoord()
    
    def init_slides(self, slides):
        self.slides = slides
        for j in range(self.taille):
            for i in range(self.taille):
                self.slides[j][i] = copier(self.slides[j][i])
                if (i, j) in self.goals:
                    self.slides[j][i].goal = True
                else:
                    self.slides[j][i].goal = False
                if j == 0 and UP in self.slides[j][i].directions:
                    self.slides[j][i].directions.remove(UP)
                if j == 0 and DOWN in self.slides[j][i].provenances:
                    self.slides[j][i].provenances.remove(DOWN)
                if i == 0 and LEFT in self.slides[j][i].directions:
                    self.slides[j][i].directions.remove(LEFT)
                if i == 0 and RIGHT in self.slides[j][i].provenances:
                    self.slides[j][i].provenances.remove(RIGHT)
                if j == self.taille-1 and DOWN in self.slides[j][i].directions:
                    self.slides[j][i].directions.remove(DOWN)
                if j == self.taille-1 and UP in self.slides[j][i].provenances:
                    self.slides[j][i].provenances.remove(UP)
                if i == self.taille-1 and RIGHT in self.slides[j][i].directions:
                    self.slides[j][i].directions.remove(RIGHT)
                if i == self.taille-1 and LEFT in self.slides[j][i].provenances:
                    self.slides[j][i].provenances.remove(LEFT)
                x, y = self.coord[0]-self.taille_tot/2 + (i+1/2) * self.taille_case, self.coord[1]-self.taille_tot/2 + (j+1/2) * self.taille_case
                self.slides[j][i].init_draw((x, y), self.taille_case)
        

TX, TY = 1920, 1080
FPS = 60
FONT = None
FONT_SIZE = 30
TITLE = 'TEST ENIGME'
TOUCHES = {1 : {'Keys' : mod.get_keyboard_keys() + mod.get_controller_keys() + mod.get_mouse_keys(), 'Controller' : None, 'Mouse' : {'Coord' : (0, 0), 'Wheel' : 0}}}

SLIDES_DEFAULT = [[C_DR, HORI, C_DL, C_DR, C_DL],
          [T_RIGHT, T_DOWN, CROSS, CROSS, C_UL],
          [T_RIGHT, CROSS, T_LEFT, T_RIGHT, C_DL],
          [T_RIGHT, CROSS, T_LEFT, T_RIGHT, C_UL],
          [C_UR, T_UP, T_UP, T_UP, S_L]]

BALLS_DEFAULT = [(0, 0), (2, 2), (0, 4), (4, 4)]

GOALS_DEFAULT = [(2, 0), (1, 2), (2, 3), (4, 3)]

ENIGME = Enigme((TX//2, TY//2), 2*min(TX, TY)//3, BALLS_DEFAULT, GOALS_DEFAULT, SLIDES_DEFAULT)

def init_prog(app):
    global ENIGME
    app.var["enigme"] = ENIGME

def update_prog(app):
    if "SPACE" in app.is_pushed:
        app.var["enigme"].reset()
    app.var["enigme"].update(app.mouse_coord, "MOUSE_LEFT" in app.is_pushed, "MOUSE_LEFT" in app.is_pressed)
    
def draw_prog(app):
    app.var["enigme"].draw()
    
mod.App(init_prog, update_prog, draw_prog, TX, TY, FPS, TITLE, background = '#2A2A2A', touches_players = TOUCHES, auto_drawing = True)
        