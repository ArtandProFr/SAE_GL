import Module_Pygame as mod
import copy

def copier(objet):
    return copy.deepcopy(objet)

# ----------------- SLIDERS -----------------

RED = "ROUGE"
BLUE = "BLEU"
YELLOW = "JAUNE"

COLORS = {RED : "#B30707", BLUE : "#0710B3", YELLOW : "#B37007"}

class Slider:
    global COLORS
    def __init__(self, color):
        self.state = None
        self.color = color
        self.coord = None
        self.rayon = None
    def init_slider(self, coord, rayon):
        self.set_coord(coord)
        self.rayon = rayon
    def set_coord(self, coord):
        self.coord = coord
    def draw(self):
        col = "#615340"
        if self.state:
            col = "#FAB60C"
        mod.circle(self.coord[0], self.coord[1], self.rayon, col)
        mod.circle(self.coord[0], self.coord[1], self.rayon, COLORS[self.color], round(self.rayon/6))
        
SL_RED = Slider(RED)
SL_BLUE = Slider(BLUE)
SL_YELLOW = Slider(YELLOW)

# ----------------- SLIDES ------------------

UP = "UP"
DOWN = "DOWN"
LEFT = "LEFT"
RIGHT = "RIGHT"

class Slide:
    global UP, DOWN, LEFT, RIGHT
    def __init__(self, type_s : str = None, directions = []):
        if type_s != None:
            if isinstance(type_s, list):
                directions = type_s
            else:
                directions = self.set_directions(type_s)
        self.directions = directions
        self.directions.sort()
        self.provenances = self.set_provenances()
        self.provenances.sort()
        self.coord = None
        self.taille = None
    
    def init_draw(self, coord_centre, taille_case):
        self.coord = coord_centre
        self.taille = taille_case
    
    def draw(self):
        mod.rect_t(self.coord[0]-self.taille/2, self.coord[1] - self.taille/2, self.taille+1, self.taille+1, "#777777")
        color = "#222222"
        
        taille = 1/3
        t = taille * self.taille+1
        if UP in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-self.taille/2
            mod.rect_t(x, y, t, (self.taille+t)/2, color)
        if DOWN in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-t/2
            mod.rect_t(x, y, t, (self.taille+t)/2, color)
        if LEFT in self.directions:
            x, y = self.coord[0] - self.taille/2, self.coord[1]-t/2
            mod.rect_t(x, y, (self.taille+t)/2, t, color)
        if RIGHT in self.directions:
            x, y = self.coord[0] - t/2, self.coord[1]-t/2
            mod.rect_t(x, y, (self.taille+t)/2, t, color)
    
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

# ----------------- ENIGME ------------------

class Enigme:
    global UP, DOWN, LEFT, RIGHT, SL_RED, SL_BLUE, SL_YELLOW
    def __init__(self, coord, taille, sliders : list, slides : list):
        """
        :params:
        - sliders : la liste des positions de sliders initiaux et de leur couleur (tuples : (x, y, Slider), avec Slider le modèle à copier)
        - slides : la matrice contenant les différentes directions possibles des sliders en fonction de leur position (Slide)
        """
        assert len(slides) == len(slides[0])
        self.taille = len(slides)
        self.coord = coord
        self.taille_tot = taille
        self.taille_case = self.taille_tot/self.taille
        self.init_slides(slides)
        self.init_sliders(sliders)
        self.debut = copier(self.sliders)
        self.win = False
        self.selection = None
    
    def goodCoord(self):
        for j in range(self.taille):
            for i in range(self.taille):
                if self.sliders[j][i] != None:
                    x, y = self.coord[0]-self.taille_tot/2 + (i+1/2) * self.taille_case, self.coord[1]-self.taille_tot/2 + (j+1/2) * self.taille_case
                    self.sliders[j][i].set_coord((x, y))
    
    def update(self, mouse_coord, left_click, left_release):
        if not self.win:
            if self.selection == None and left_click:
                for j in range(self.taille):
                    for i in range(self.taille):
                        s = self.sliders[j][i]
                        if s != None and mod.dist2(mouse_coord, s.coord) < s.rayon:
                            self.selection = (i, j)
            if self.selection != None and self.sliders[self.selection[1]][self.selection[0]] != None:
                i, j = self.selection
                sl = self.sliders[j][i]
                slide = self.slides[j][i]
                
                # Position centrale de la case actuelle
                orig_x = self.coord[0] - self.taille_tot/2 + (i + 0.5) * self.taille_case
                orig_y = self.coord[1] - self.taille_tot/2 + (j + 0.5) * self.taille_case
                
                diff_x = mouse_coord[0] - orig_x
                diff_y = mouse_coord[1] - orig_y
                
                # Contrainte de mouvement selon les directions du rail
                new_x, new_y = orig_x, orig_y
                
                # Priorité à l'axe où la souris tire le plus
                if abs(diff_x) > abs(diff_y):
                    if (diff_x > 0 and RIGHT in slide.directions and i < self.taille - 1 and self.sliders[j][i+1] == None) or (diff_x < 0 and LEFT in slide.directions and i > 0 and self.sliders[j][i-1] == None):
                        new_x = mouse_coord[0]
                else:
                    if (diff_y > 0 and DOWN in slide.directions and j < self.taille - 1 and self.sliders[j+1][i] == None) or (diff_y < 0 and UP in slide.directions and j > 0 and self.sliders[j-1][i] == None):
                        new_y = mouse_coord[1]

                if mod.dist2((new_x, new_y), (orig_x, orig_y)) < self.taille_case:
                    sl.set_coord((new_x, new_y))
            
                    # 3. CHANGEMENT DE CASE (Saut vers une case adjacente)
                    for dj, di in [(-1, 0), (1, 0), (0, -1), (0, 1)]: # Haut, Bas, Gauche, Droite
                        ni, nj = i + di, j + dj
                        if 0 <= ni < self.taille and 0 <= nj < self.taille:
                            if self.sliders[nj][ni] is None: # Case vide
                                dest_x = self.coord[0] - self.taille_tot/2 + (ni + 0.5) * self.taille_case
                                dest_y = self.coord[1] - self.taille_tot/2 + (nj + 0.5) * self.taille_case
                                
                                # Si le slider dépasse la moitié de la case suivante
                                if mod.dist2(sl.coord, (dest_x, dest_y)) < (self.taille_case / 2):
                                    self.sliders[nj][ni] = sl
                                    self.sliders[j][i] = None
                                    self.selection = (ni, nj)
                                    break
                
            if self.selection != None and left_release and self.sliders[self.selection[1]][self.selection[0]] != None:
                i, j = self.selection
                x, y = self.coord[0]-self.taille_tot/2 + (i+1/2) * self.taille_case, self.coord[1]-self.taille_tot/2 + (j+1/2) * self.taille_case
                self.sliders[j][i].set_coord((x, y))
                self.selection = None
                
            for j in range(self.taille):
                for i in range(self.taille):
                    if self.sliders[j][i] != None:
                        self.upd_slider(i, j)
            if self.verif_win():
                self.win = True
                self.goodCoord()
    
    def draw(self):
        for j in range(self.taille):
            for i in range(self.taille):
                self.slides[j][i].draw()
        for j in range(self.taille):
            for i in range(self.taille):
                if self.sliders[j][i] != None:
                    self.sliders[j][i].draw()
    
    def upd_slider(self, i, j):
        self.sliders[j][i].state = True
        col = self.sliders[j][i].color
        b = False
        for x in range(max(0, i-1), min(i+2, self.taille)):
            for y in range(max(0, j-1), min(j+2, self.taille)):
                if (x, y) != (i, j) and self.sliders[y][x] != None and self.sliders[y][x].color != col:
                    self.sliders[j][i].state = False
                    b = True
                    break
            if b:
                break
    
    def verif_win(self):
        for j in range(self.taille):
            for i in range(self.taille):
                s = self.sliders[j][i]
                if s != None and s.state == False:
                    return False
        return True
    
    def reset(self):
        self.win = False
        self.selection = None
        self.sliders = copier(self.debut)
        # On force une mise à jour des états (couleurs)
        for j in range(self.taille):
            for i in range(self.taille):
                if self.sliders[j][i] is not None:
                    self.upd_slider(i, j)
                
    
    def init_sliders(self, sliders):
        self.sliders = [[None for i in range(self.taille)] for j in range(self.taille)]
        for s in sliders:
            if len(s) == 3 and s[0] >= 0 and s[0] < self.taille and s[1] >= 0 and s[1] < self.taille and isinstance(s[2], Slider) and s[2].color != None:
                x, y = self.coord[0]-self.taille_tot/2 + (s[0]+1/2) * self.taille_case, self.coord[1]-self.taille_tot/2 + (s[1]+1/2) * self.taille_case
                self.sliders[s[1]][s[0]] = copier(s[2])
                self.sliders[s[1]][s[0]].init_slider((x, y), self.taille_case/3)
    
    def init_slides(self, slides):
        self.slides = slides
        for j in range(self.taille):
            for i in range(self.taille):
                self.slides[j][i] = copier(self.slides[j][i])
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

SLIDES_DEFAULT = [[C_DR, HORI, HORI, S_L, S_D, S_D, S_D],
          [S_U, C_DR, T_DOWN, S_L, VERT, VERT, S_U],
          [S_D, T_RIGHT, C_UL, C_DR, T_LEFT, T_RIGHT, S_L],
          [VERT, S_U, S_R, T_UP, C_UL, S_U, S_D],
          [T_RIGHT, S_L, S_D, S_R, HORI, HORI, C_UL],
          [S_U, S_D, S_U, S_D, C_DR, T_DOWN, C_DL],
          [S_R, T_UP, HORI, C_UL, C_UR, T_UP, C_UL]]

SLIDERS_DEFAULT = [(1, 0, SL_BLUE), (4, 0, SL_RED),
           (0, 1, SL_RED), (1, 1, SL_BLUE), (3, 1, SL_YELLOW), (6, 1, SL_YELLOW),
           (2, 2, SL_RED), (4, 2, SL_YELLOW), (6, 2, SL_RED),
           (6, 3, SL_BLUE),
           (1, 4, SL_RED), (2, 4, SL_YELLOW),
           (4, 5, SL_YELLOW), (5, 5, SL_RED), (6, 5, SL_YELLOW),
           (0, 6, SL_BLUE)]

ENIGME = Enigme((TX//2, TY//2), 2*min(TX, TY)//3, SLIDERS_DEFAULT, SLIDES_DEFAULT)

def init_prog(app):
    global ENIGME
    app.var["enigme"] = ENIGME

def update_prog(app):
    if "SPACE" in app.is_pushed:
        app.var["enigme"].reset()
    app.var["enigme"].update(app.mouse_coord, "MOUSE_LEFT" in app.is_pushed, "MOUSE_LEFT" in app.is_released)
    
def draw_prog(app):
    app.var["enigme"].draw()
    
mod.App(init_prog, update_prog, draw_prog, TX, TY, FPS, TITLE, background = '#2A2A2A', touches_players = TOUCHES, auto_drawing = True)
        