import Module_Pygame as mod
import math
import pygame

class Cadran:
    def __init__(self, coord_mid, rayon_ext, taille_bouton = 1/5, liste = [1, 3, 2, 1, 1, 2, 1, 3], taille_possib = 2, timer_lose = 0.6, fps = 60):
        self.coord = coord_mid
        self.liste = liste
        self.position = 0
        self.taille = len(self.liste)
        self.taille_possib = taille_possib
        self.angle_unite = 360/self.taille
        self.angle_jouable = self.taille_possib*self.angle_unite
        self.rayon_ext = rayon_ext
        self.rayon_int = self.rayon_ext * (1 - taille_bouton)
        self.angle_cadran = self.get_angle_cadran()
        self.fps = fps
        self.temps_lose = timer_lose * self.fps
        self.win = False
        self.reset()
    
    def verif_win(self):
        return not False in self.pushed
        
    def get_angle_cadran(self):
        l = []
        for i in range(self.taille):
            l.append((self.position + i) * self.angle_unite)
        return l
    
    def reset(self):
        self.position = 0
        self.pushed = [False for i in range(self.taille)]
        self.timer = -1
        self.changed = True
        self.win = False
    
    def get_pos_selectionne(self, angle):
        for i in range(self.taille):
            if self.angle_cadran[i] < angle < self.angle_cadran[(i+1)%self.taille]:
                return i
        return -1

    def update(self, mouse_coord, left_click):
        self.changed = False
        if self.timer >= 0:
            if self.timer >= self.temps_lose:
                self.reset()
            else:
                self.timer += 1
        elif left_click and not self.win:
            self.changed = True
            if self.rayon_int < mod.dist2(mouse_coord, self.coord) < self.rayon_ext:
                dx = mouse_coord[0] - self.coord[0]
                dy = mouse_coord[1] - self.coord[1]

                # atan2 renvoie l'angle en radians entre -pi et pi
                # On inverse dy car l'axe Y de pygame est inversé
                # Rayonnement sur 360° (0° en haut, sens horaire)
                angle = math.degrees(math.atan2(dx, -dy)) % 360

                a1 = self.angle_cadran[self.position]
                a2 = self.angle_cadran[self.position] + self.angle_jouable
                if not a1 < a2:
                    a1, a2 = a2, a1
                if a1 < angle < a2:
                    pos = self.get_pos_selectionne(angle)
                    if not self.pushed[pos]:
                        self.pushed[pos] = True
                        self.position += self.liste[pos]
                        self.position %= self.taille
                        if self.verif_win():
                            self.win = True
                        else:
                            hasFalse = False
                            indices_jouables = [(self.position + j) % self.taille for j in range(self.taille_possib)]
                            for i in indices_jouables:
                                if not self.pushed[i]:
                                    hasFalse = True
                                    break
                            if not hasFalse:
                                self.timer = 0
                else:
                    if self.get_pos_selectionne(angle) != -1:
                        self.reset()
    
    def draw(self):
        x, y = self.coord
        mod.circle(x, y, self.rayon_ext + 10, "#111111")
        for i in range(self.taille):
            a = self.angle_cadran[i]
            b = self.angle_cadran[(i+1)%self.taille]
            col = "#AA8844"
            if self.pushed[i]:
                col = mod.transition(col, "#000000", 50)
            mod.milieu_portion_cercle(self.coord, self.rayon_int, self.rayon_ext, 90-b, 90-a, col)
            
            mod.milieu_portion_cercle(self.coord, self.rayon_int, self.rayon_ext, 90-b, 90-a, mod.transition(col, "#000000", 25), width = 6)
            
            mid_ang = a + (self.angle_unite / 2)
            rad = math.radians(90 - mid_ang)
            dist_txt = (self.rayon_ext + self.rayon_int) / 2
            tx = self.coord[0] + dist_txt * math.cos(rad)
            ty = self.coord[1] - dist_txt * math.sin(rad) - 1/22 * self.rayon_ext
            mod.write_scale(tx, ty, str(self.liste[i]), "#AA0000", 50)

        for i in range(self.taille):
            a = self.angle_cadran[i]
            b = self.angle_cadran[(i+1)%self.taille]
            indices_jouables = [(self.position + j) % self.taille for j in range(self.taille_possib)]
            if i in indices_jouables:
                mod.portion_cercle(self.coord, self.rayon_ext, 90-b, 90-a, "#777777", width = 6)                
   
TX, TY = 1920, 1080
FPS = 60
FONT = None
FONT_SIZE = 30
TITLE = 'TEST CADRAN'
TOUCHES = {1 : {'Keys' : mod.get_keyboard_keys() + mod.get_controller_keys() + mod.get_mouse_keys(), 'Controller' : None, 'Mouse' : {'Coord' : (0, 0), 'Wheel' : 0}}}

CADRAN = Cadran((TX//2, TY//2), min(TX, TY)//3) # L'original

#CADRAN = Cadran((TX//2, TY//2), min(TX, TY)//3, liste = [1, 2, 3, 1, 2, 1]) # Plus simple

def init_prog(app):
    global CADRAN
    app.var["cadran"] = CADRAN

def update_prog(app):
    app.var["cadran"].update(app.mouse_coord, "MOUSE_LEFT" in app.is_pushed)
    if "SPACE" in app.is_pushed:
        app.var["cadran"].reset()
    if app.var["cadran"].changed:
        app.refresh = True
    
def draw_prog(app):
    app.var["cadran"].draw()
    
mod.App(init_prog, update_prog, draw_prog, TX, TY, FPS, TITLE, background = '#2A2A2A', touches_players = TOUCHES, auto_drawing = False)