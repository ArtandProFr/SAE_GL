import Module_Pygame as mod
import copy

def copier(a):
    return copy.deepcopy(a)

TX, TY = 1920, 1080
FPS = 60
FONT = None
FONT_SIZE = 30
TITLE = 'TEST ENIGME GATE'
TOUCHES = {1 : {'Keys' : mod.get_keyboard_keys() + mod.get_controller_keys() + mod.get_mouse_keys(), 'Controller' : None, 'Mouse' : {'Coord' : (0, 0), 'Wheel' : 0}}}

GRILLE = (107, 68)
K = min(TX/GRILLE[0], TY/GRILLE[1])
NTX, NTY = GRILLE[0] * K, GRILLE[1] * K
DTX = (TX - NTX)/2
DTY = (TY - NTY)/2

TPTX = NTX/GRILLE[0]
TPTY = NTY/GRILLE[1]
assert TPTX == TPTY
TPT = TPTX
PT_CASE = 9

T_BUTTON = (5, 5)
T_LIGHT = (2, 2)
T_NOT = (4, 4)
T_AND = (5, 4) # Triangle Bleu (Ici en vertical)
T_XOR = (5, 4) # Demi-cercle Vert (Ici en vertical)

LINEW = TPT//7
POINTW = 1.5 * LINEW
T_WIRE = 1.8*POINTW
W_SPEED = 45/3600
W_LENGTH = 1/15

class Button:
    def __init__(self, coord = None, c_in = None, sens = "RIGHT", rayon = T_BUTTON[0]*TPT//2):
        self.shown = False
        self.pushing_state = False
        self.input = c_in
        self.coord = coord
        self.rayon = rayon
        self.sens = sens
        self.anchors_in = [[]]
        self.anchors_out = [self.coord, ()]
        self.place(self.coord)
        self.update()
    
    def connect(self, conn_in = None):
        if conn_in != None:
            self.input = conn_in
            
    def size(self, rayon = None):
        if rayon:
            self.rayon = rayon
    
    def place(self, coord = None):
        self.coord = coord
        r = 0
        if self.sens == "LEFT":
            r = 180
        elif self.sens == "UP":
            r = 90
        elif self.sens == "DOWN":
            r = -90
        if coord:
            anch = mod.rotation((self.coord[0] - self.rayon, self.coord[1]), self.coord, r)
            self.anchors_in = [[anch, self.coord]]
            self.anchors_out = [self.coord, mod.rotation(anch, self.coord, 180)]

    def update(self, mouse_coord = None, left_click = None):
        if mouse_coord and left_click and self.coord:
            if left_click and mod.dist2(mouse_coord, self.coord) < self.rayon:
                self.pushing_state = not self.pushing_state
        self.state = (self.input and self.pushing_state and self.input.state) or (not self.input and self.pushing_state)
        
                
    def draw(self):
        if self.coord and self.rayon:
            l = 2/9
            x, y = self.coord
            d = self.rayon//12
            angle_vue = (1.5, 1)
            dx = angle_vue[0] * d
            dy = angle_vue[1] * d
            reduc = 0.25
            mod.circle(x+dx, y+dy, int((1+l) * self.rayon), mod.transition("#AAAAAA", "#000000", 20))
            mod.circle(x, y, int((1+l) * self.rayon), "#AAAAAA")
            if self.state == None:
                col = "#888888"
            else:
                col = "#6E3500"
                if self.state:
                    col = "#DB6A00"
                    dx, dy = dx*reduc, dy*reduc
            if self.pushing_state and not self.state:
                col = mod.transition(col, "#000000", 50)
                dx, dy = dx*reduc, dy*reduc
            mod.circle(x, y, self.rayon, mod.transition(col, "#000000", 20))
            mod.circle(x-dx, y-dy, self.rayon, col)
        elif not self.shown:
            self.shown = True
            print("- Button is not visible")

class Light:
    def __init__(self, coord = None, c_in = None, sens = "RIGHT", rayon = T_LIGHT[0]*TPT//2):
        self.shown = False
        self.coord = coord
        self.input = c_in
        self.state = False
        self.rayon = rayon
        self.sens = sens
        self.anchors_in = [[]]
        self.anchors_out = ()
        self.place(self.coord)
        self.update()
    
    def connect(self, conn_in = None):
        if conn_in != None:
            self.input = conn_in
    
    def size(self, rayon = None):
        if rayon:
            self.rayon = rayon
        
    def place(self, coord = None):
        self.coord = coord
        r = 0
        if self.sens == "LEFT":
            r = 180
        elif self.sens == "UP":
            r = 90
        elif self.sens == "DOWN":
            r = -90
        if coord:
            anch = mod.rotation((self.coord[0] - 2 * self.rayon, self.coord[1]), self.coord, r)
            self.anchors_in = [[anch, self.coord]]
            self.anchors_out = [self.coord, mod.rotation(anch, self.coord, 180)]
    
    def update(self):
        if self.input:
            self.state = self.input.state
        else:
            self.state = False
    
    def draw(self):
        if self.coord and self.rayon:
            l = 1/6
            x, y  = self.coord
            if self.state == None:
                col = "#888888"
            else:
                col = "#6E6200"
                if self.state:
                    col = "#DBB300"
            mod.circle(x, y, (1 + l) * self.rayon, mod.transition(col, "#000000", 20))
            mod.circle(x, y, self.rayon, col)
        elif not self.shown:
            self.shown = True
            print("- Light is not visible")

class Gate:
    def __init__(self, sort = None, coord = None, sens = "RIGHT", c_in = None, taille = T_NOT[0]*TPT//2):
        self.shown = False
        self.type = sort
        self.coord = coord
        if c_in == None:
            self.inputs = []
        else:
            self.inputs = c_in
        self.taille = taille
        self.sens = sens
        self.state = False
        self.anchors_in = [[]]
        self.anchors_out = ()
        self.place(self.coord)
        self.update()
    
    def set_sens(self, sens = "RIGHT"):
        self.sens = sens
    
    def connect(self, c_in = None):
        if c_in:
            if not isinstance(c_in, list):
                c_in = [c_in]
            for i in c_in:
                self.inputs.append(i)
    
    def disconnect(self, c_in = None):
        if c_in:
            if not isinstance(c_in, list):
                c_in = [c_in]
            for i in c_in:
                self.inputs.remove(i)
    
    def size(self, taille = None):
        if taille:
            self.taille = taille
        
    def place(self, coord = None):
        self.coord = coord
        r = 0
        if self.sens == "LEFT":
            r = 180
        elif self.sens == "UP":
            r = 90
        elif self.sens == "DOWN":
            r = -90
        if coord:
            if self.type == "NOT":
                anch = mod.rotation((self.coord[0] - self.taille, self.coord[1]), self.coord, r)
                self.anchors_in = [[anch, self.coord]]
                self.anchors_out = [self.coord, mod.rotation(anch, (self.coord[0] + self.taille, self.coord[1]), 180)]
            elif self.type == "XOR":
                t = self.taille #*1.3
                # 1. On crée le triangle de BASE (toujours pointé vers la DROITE)
                p1 = (self.coord[0] + t, self.coord[1])
                p2 = mod.rotation(p1, self.coord, 120)
                p3 = mod.rotation(p1, self.coord, -120)
                anch_in1 = mod.rotation((p2[0], (p2[1]+p1[1])/2), self.coord, r)
                anch_in12 = mod.rotation((p2[0]-t, (p2[1]+p1[1])/2), self.coord, r)
                anch_in2 = mod.rotation((p2[0], (p3[1]+p1[1])/2), self.coord, r)
                anch_in22 = mod.rotation((p2[0]-t, (p3[1]+p1[1])/2), self.coord, r)
                anch_out = p1
                self.anchors_in = [[anch_in12, anch_in1], [anch_in22, anch_in2]]
                self.anchors_out = [self.coord, anch_out]
            elif self.type == "AND":
                t = self.taille # * 1.3
                p1 = (self.coord[0] - 4 * t / (3 * 3.1415926), self.coord[1])
                
                # Angles de base du demi-cercle pointé vers la droite
                angle_debut = -90
                angle_fin = 90

                # 2. On détermine l'angle de rotation global selon le sens
                rot = 0
                if self.sens == "LEFT":
                    rot = 180
                elif self.sens == "UP":
                    rot = 90  # Sur un écran, 90° pivote vers le haut
                elif self.sens == "DOWN":
                    rot = -90   # Et -90° pivote vers le bas

                # 3. On applique la rotation sur les CENTRES si nécessaire
                if rot != 0:
                    p1 = mod.rotation(p1, self.coord, rot)
                p2 = (p1[0], p1[1] - t)
                p3 = (p1[0], p1[1] + t)
                anch_in1 = mod.rotation((p2[0], (p2[1]+p1[1])/2), self.coord, r)
                anch_in12 = mod.rotation((p2[0]-1.3*t, (p2[1]+p1[1])/2), self.coord, r)
                anch_in2 = mod.rotation((p2[0], (p3[1]+p1[1])/2), self.coord, r)
                anch_in22 = mod.rotation((p2[0]-1.3*t, (p3[1]+p1[1])/2), self.coord, r)
                anch_out = mod.eloigner(p1, self.coord, t)
                self.anchors_in = [[anch_in12, anch_in1], [anch_in22, anch_in2]]
                self.anchors_out = [self.coord, anch_out]
    
    def validity(self):
        l = len(self.inputs)
        if l == 0 or (self.type == "NOT" and l > 1) or (l == 1 and self.type in ["AND", "XOR"]):
            return False
        if (self.type == "NOT" and l == 1) or (l == 2 and self.type in ["AND", "XOR"]):
            return True
        if not self.type in ["NOT", "XOR", "AND"]:
            return False
        return None
    
    def update(self):
        if self.inputs:
            if self.type == "NOT":
                self.state = not self.inputs[0].state
            elif self.type == "AND":
                temp = True
                for i in self.inputs:
                    if not i.state:
                        temp = False
                        break
                self.state = temp
            elif self.type == "XOR":
                temp = False
                for i in self.inputs:
                    if i.state and temp:
                        temp = False
                        break
                    if i.state and not temp:
                        temp = True
                self.state = temp
        else:
            self.state = False
    
    def draw(self):
        if self.coord and self.taille:
            x, y = self.coord
            l = 0.1
            if self.type == "NOT":
                col = "#FF0000"
            elif self.type == "AND":
                col = "#00FF00"
            elif self.type == "XOR":
                col = "#0000FF"
            if not self.validity():
                col = "#222222"
            if self.type == "NOT":
                mod.circle(x, y, (1+l) * self.taille, mod.transition(col, "#000000", 20))
                mod.circle(x, y, self.taille, col)
            elif self.type == "XOR":
                t = self.taille #*1.3
                # 1. On crée le triangle de BASE (toujours pointé vers la DROITE)
                p1 = (self.coord[0] + t, self.coord[1])
                p2 = mod.rotation(p1, self.coord, 120)
                p3 = mod.rotation(p1, self.coord, -120)
                
                # 2. On applique la rotation globale selon le SENS de la porte logique
                angle_sens = 0
                if self.sens == "DOWN":
                    angle_sens = -90
                elif self.sens == "LEFT":
                    angle_sens = 180
                elif self.sens == "UP":
                    angle_sens = 90
                    
                if angle_sens != 0:
                    p1 = mod.rotation(p1, self.coord, angle_sens)
                    p2 = mod.rotation(p2, self.coord, angle_sens)
                    p3 = mod.rotation(p3, self.coord, angle_sens)
                el = t*1.1
                pc1 = mod.eloigner(p1, self.coord, el)
                pc2 = mod.eloigner(p2, self.coord, el)
                pc3 = mod.eloigner(p3, self.coord, el)
                mod.triangle(pc1, pc2, pc3, mod.transition(col, "#000000", 20))
                mod.triangle(p1, p2, p3, col)
            elif self.type == "AND":
                t = self.taille # * 1.3
                el = t + 7
                # 1. On calcule la base UNIQUE (Modèle par défaut : RIGHT)
                # Centre du demi-cercle principal
                p1 = (self.coord[0] - 4 * t / (3 * 3.1415926), self.coord[1])
                # Centre du demi-cercle de l'ombre (légèrement décalé vers la gauche)
                pc1 = (p1[0] - (el - t) / 2, p1[1])
                
                # Angles de base du demi-cercle pointé vers la droite
                angle_debut = -90
                angle_fin = 90

                # 2. On détermine l'angle de rotation global selon le sens
                rot = 0
                if self.sens == "LEFT":
                    rot = 180
                elif self.sens == "UP":
                    rot = 90  # Sur un écran, 90° pivote vers le haut
                elif self.sens == "DOWN":
                    rot = -90   # Et -90° pivote vers le bas

                # 3. On applique la rotation sur les CENTRES si nécessaire
                if rot != 0:
                    p1 = mod.rotation(p1, self.coord, rot)
                    pc1 = mod.rotation(pc1, self.coord, rot)

                # 4. On dessine en ajoutant la rotation aux angles de l'arc
                mod.portion_cercle(pc1, el, angle_debut + rot, angle_fin + rot, mod.transition(col, "#000000", 20))
                mod.portion_cercle(p1, t, angle_debut + rot, angle_fin + rot, col)
                
                
        elif not self.shown:
            self.shown = True
            print("- {} Gate is not visible".format(self.type))

class Wire:
    def __init__(self, c_in = None, c_out = None, courbes = [], num_input = 0, width = T_WIRE, speed = W_SPEED, taille = W_LENGTH):
        self.input = c_in
        self.output = c_out
        if courbes != []:
            self.courbes = [mod.Courbe_Bezier(self.input.anchors_out)] + courbes + [mod.Courbe_Bezier(self.output.anchors_in[num_input])]
        else:
            self.courbes = [mod.Courbe_Bezier(self.input.anchors_out + self.output.anchors_in[num_input])]
        self.circuit = mod.Circuit(self.courbes)
        self.circuit.initialiser_circuit(10**3, 1)
        self.circuit.set_modifier(False)
        self.long = self.circuit.longueur
        self.width = width
        self.travel = 0
        self.speed = speed
        self.taille_travel = taille
    
    def show(self):
        print(self.input, self.output)
        print(self.courbes)
        for c in self.courbes:
            print(c.points)
        print(self.long)
    
    def update(self, app):
        if self.input and self.input.state:
            self.travel += 60 * app.dt * self.speed
            self.travel %= 1
        else:
            self.travel = 0

    def draw(self):
        col = "#06192E"
        prec = 200
        travel = []
        for p in range(prec):
            l = p/prec
            c = self.circuit.get_coord(l, relative = True)
            if c:
                x, y = c
                if (self.input and self.input.state) and (self.travel - self.taille_travel/2) < l < (self.travel + self.taille_travel/2):
                    travel.append([x, y, 1-abs(l - self.travel)/(self.taille_travel/2)])
                    continue
                mod.circle(x, y, self.width, col)
        for p in travel:
            mod.circle(p[0], p[1], self.width, mod.transition("#FFFFFF", col, p[2]*50))
        
class Grid:
    global DTX, DTY, TPT, PT_CASE, LINEW, POINTW
    def __init__(self, tx = 0, ty = 0, dx = 0, dy = 0):
        self.tx = tx
        self.ty = ty
        self.dx = DTX + TPT * dx
        self.dy = DTY + TPT * dy
        self.objets = []
        self.input = None
        self.state_component = None
        self.state = False
        self.wires = []
    
    def init_wire(self):
        for o in self.objets:
            if isinstance(o, Gate):
                for i in range(len(o.inputs)):
                    if i < 2:
                        c = o.inputs[i]
                        if isinstance(c, Grid):
                            c = c.state_component
                        self.add_wire(Wire(c, o, num_input = i))
                """
                for i in o.inputs:
                    if not isinstance(i, Grid):
                        self.add_wire(Wire(i, o))
                """
            elif o.input and not isinstance(o.input, Grid):
                self.add_wire(Wire(o.input, o))
    
    def add_wire(self, wire):
        self.wires.append(wire)
        #wire.show()
        
    def update_buttons_inputs(self):
        if self.input:
            for o in self.objets:
                if isinstance(o, Button):
                    o.input = self.input
    
    def reset_buttons_state(self):
        for o in self.objets:
                if isinstance(o, Button):
                    o.pushing_state = False
    
    def reset_inputs_buttons(self):
        for o in self.objets:
            if isinstance(o, Button):
                o.input = None
    
    def add_main_input(self, objet):
        self.input = objet
        for o in self.objets:
            if isinstance(o, Button) and not o.input:
                o.input = self.input
    
    def add_main_output(self, objet):
        self.state_component = objet
    
    def place(self, objet, coord, coord_case = (0, 0)):
        x, y = coord_case[0] * PT_CASE + coord[0], coord_case[1] * PT_CASE + coord[1]
        if x < self.tx and y < self.ty:
            objet.place((self.dx + (x-1) * TPT, self.dy + (y-1) * TPT))
            self.objets.append(objet)
    
    def update(self, app):
        self.update_buttons_inputs()
        for c in self.objets:
            if isinstance(c, Button):
                c.update(app.mouse_coord, "MOUSE_LEFT" in app.is_pushed)
            else:
                c.update()
        if self.state_component:
            self.state = self.state_component.state
        else:
            self.state = False
        for w in self.wires:
            w.update(app)
    
    def draw_background(self):
        clair = mod.transition("#271B0D", "#FFFFFF", 22)
        sombre = mod.transition(clair, "#000000", 20)
        mod.rect_t(self.dx, self.dy, self.tx * TPT, self.ty * TPT, clair)
        for l in range(self.ty//PT_CASE+1):
            mod.line(self.dx, self.dy + l * TPT * PT_CASE - LINEW/2, self.dx + self.tx * TPT, self.dy + l * TPT * PT_CASE - LINEW/2, sombre, LINEW)
        for c in range(self.tx//PT_CASE+1):
            mod.line(self.dx + c * TPT * PT_CASE - LINEW/2, self.dy, self.dx + c * TPT * PT_CASE - LINEW/2, self.dy + self.ty * TPT, sombre, LINEW)
        for ix in range(self.tx):
            x = self.dx + (ix+1/2) * TPT
            for iy in range(self.ty):
                y = self.dy + (iy+1/2) * TPT
                mod.circle(x, y, int(POINTW), sombre)
    
    def draw_wires(self):
        for w in self.wires:
            w.draw()
    
    def draw_components(self):
        for c in self.objets:
            c.draw()
    
    def draw(self):
        clair = mod.transition("#271B0D", "#FFFFFF", 22)
        sombre = mod.transition(clair, "#000000", 20)
        mod.rect_t(self.dx, self.dy, self.tx * TPT, self.ty * TPT, clair)
        for l in range(self.ty//PT_CASE+1):
            mod.line(self.dx, self.dy + l * TPT * PT_CASE - LINEW/2, self.dx + self.tx * TPT, self.dy + l * TPT * PT_CASE - LINEW/2, sombre, LINEW)
        for c in range(self.tx//PT_CASE+1):
            mod.line(self.dx + c * TPT * PT_CASE - LINEW/2, self.dy, self.dx + c * TPT * PT_CASE - LINEW/2, self.dy + self.ty * TPT, sombre, LINEW)
        for ix in range(self.tx):
            x = self.dx + (ix+1/2) * TPT
            for iy in range(self.ty):
                y = self.dy + (iy+1/2) * TPT
                mod.circle(x, y, int(POINTW), sombre)
        for w in self.wires:
            w.draw()
        for c in self.objets:
            c.draw()





###

# Grille 1:

B1_1 = Button()
B1_2 = Button()
B1_3 = Button()
B1_4 = Button()

G1_1 = Gate("AND")
G1_2 = Gate("NOT")
G1_3 = Gate("XOR")
G1_4 = Gate("AND")
G1_5 = Gate("AND", sens = "DOWN")

L1_1 = Light()
L1_2 = Light()
L1_3 = Light()
L1_4 = Light()
L1_5 = Light()
L1_6 = Light()
L1_7 = Light()
L1_8 = Light()
L1_9 = Light(sens = "DOWN")

G1_1.connect([L1_2, L1_1])
G1_2.connect([L1_3])
G1_3.connect([L1_5, L1_4])
G1_4.connect([L1_7, L1_6])
G1_5.connect([L1_8, G1_4])

L1_1.connect(B1_1)
L1_2.connect(B1_2)
L1_3.connect(B1_3)
L1_4.connect(B1_3)
L1_5.connect(B1_4)
L1_6.connect(G1_1)
L1_7.connect(G1_2)
L1_8.connect(G1_3)
L1_9.connect(G1_5)

GR1 = Grid(40, 27, 4, 4)

GR1.place(B1_1, (4, 4))
GR1.place(B1_2, (7, 1), (0, 1))
GR1.place(B1_3, (4, 7), (0, 1))
GR1.place(B1_4, (7, 4), (0, 2))

GR1.place(G1_1, (1, 5), (2, 0))
GR1.place(G1_2, (1, 5), (2, 1))
GR1.place(G1_3, (8, 4), (1, 2))
GR1.place(G1_4, (4, 8), (3, 0))
GR1.place(G1_5, (8, 1), (3, 2))

GR1.place(L1_1, (2, 4), (1, 0))
GR1.place(L1_2, (5, 9), (1, 0))
GR1.place(L1_3, (3, 5), (1, 1))
GR1.place(L1_4, (3, 1), (1, 2))
GR1.place(L1_5, (3, 8), (1, 2))
GR1.place(L1_6, (8, 6), (2, 0))
GR1.place(L1_7, (7, 5), (2, 1))
GR1.place(L1_8, (7, 4), (2, 2))
GR1.place(L1_9, (8, 7), (3, 2))

GR1.add_main_output(L1_9)
GR1.init_wire()

# Grille 2:

B2_1 = Button()
B2_2 = Button()
B2_3 = Button()
B2_4 = Button()
B2_5 = Button()

G2_1 = Gate("AND", sens = "LEFT")
G2_2 = Gate("NOT", sens = "UP")
G2_3 = Gate("NOT")
G2_4 = Gate("AND", sens = "LEFT")
G2_5 = Gate("NOT", sens = "DOWN")
G2_6 = Gate("NOT", sens = "DOWN")
G2_7 = Gate("AND", sens = "DOWN")
G2_8 = Gate("AND")
G2_9 = Gate("AND")
G2_10 = Gate("AND")

G2_1.connect([B2_1, B2_1])
G2_2.connect([G2_8])
G2_3.connect([B2_3])
G2_4.connect([B2_4, G2_3])
G2_5.connect([B2_5])
G2_6.connect([G2_5])
G2_7.connect([G2_1, G2_2])
G2_8.connect([B2_1, B2_2])
G2_9.connect([G2_4, G2_6])
G2_10.connect([G2_7, G2_9])

GR2 = Grid(54, 27, GR1.tx + 9, 4)

GR2.place(B2_1, (2, 6), (1, 0))
GR2.place(B2_2, (1, 5), (2, 0))
GR2.place(B2_3, (9, 6), (2, 0))
GR2.place(B2_4, (7, 4), (3, 0))
GR2.place(B2_5, (5, 6), (4, 0))

GR2.place(G2_1, (2, 5), (1, 1))
GR2.place(G2_2, (5, 7), (2, 1))
GR2.place(G2_3, (4, 8), (3, 1))
GR2.place(G2_4, (8, 4), (3, 1))
GR2.place(G2_5, (9, 4), (4, 1))
GR2.place(G2_6, (9, 4), (0, 2))
GR2.place(G2_7, (6, 3), (1, 2))
GR2.place(G2_8, (4, 5), (2, 2))
GR2.place(G2_9, (7, 6), (3, 2))
GR2.place(G2_10, (2, 5), (5, 2))

GR2.add_main_input(GR1)
GR2.add_main_output(G2_10)
GR2.init_wire()

# Grille 3:

B3_1 = Button()
B3_2 = Button()
B3_3 = Button()
B3_4 = Button()
B3_5 = Button()
B3_6 = Button()
B3_7 = Button()
B3_8 = Button()
B3_9 = Button()
B3_10 = Button()

G3_1 = Gate("AND", sens = "DOWN")
G3_3 = Gate("AND")
G3_5 = Gate("AND", sens = "DOWN")
G3_8 = Gate("AND")
G3_9 = Gate("AND")
G3_14 = Gate("AND")
G3_16 = Gate("AND")
G3_17 = Gate("AND", sens = "UP")
G3_18 = Gate("AND")
G3_19 = Gate("AND", sens = "DOWN")
G3_20 = Gate("AND")

G3_2 = Gate("NOT")
G3_6 = Gate("NOT")
G3_7 = Gate("NOT", sens = "DOWN")
G3_10 = Gate("NOT", sens = "DOWN")
G3_12 = Gate("NOT")
G3_15 = Gate("NOT")

G3_4 = Gate("XOR", sens = "DOWN")
G3_11 = Gate("XOR", sens = "DOWN")
G3_13 = Gate("XOR", sens = "DOWN")

L3_1 = Light()

G3_1.connect([B3_5, B3_6])
G3_2.connect([B3_1])
G3_3.connect([B3_2, G3_2])
G3_4.connect([B3_1, G3_1])
G3_5.connect([G3_3, G3_4])
G3_6.connect([B3_4])
G3_7.connect([B3_3])
G3_8.connect([G3_6, G3_7])
G3_9.connect([G3_8, G3_5])
G3_10.connect([B3_7])
G3_11.connect([B3_6, G3_10])
G3_12.connect([G3_11])
G3_13.connect([B3_7, B3_8])
G3_14.connect([G3_13, B3_8])
G3_15.connect([B3_9])
G3_16.connect([G3_15, B3_10])
G3_17.connect([G3_14, G3_16])
G3_18.connect([G3_12, G3_17])
G3_19.connect([G3_18, GR2])
G3_20.connect([G3_9, G3_19])
L3_1.connect(G3_20)

GR3 = Grid(99, 27, 4, 37)

GR3.place(B3_1, (2, 4), (1, 0))
GR3.place(B3_2, (7, 1), (0, 1))
GR3.place(B3_3, (1, 8), (1, 1))
GR3.place(B3_4, (7, 5), (0, 2))
GR3.place(B3_5, (2, 4), (2, 0))
GR3.place(B3_6, (9, 4), (4, 0))
GR3.place(B3_7, (7, 6), (5, 0))
GR3.place(B3_8, (4, 4), (6, 0))
GR3.place(B3_9, (2, 6), (7, 0))
GR3.place(B3_10, (9, 4), (7, 0))

GR3.place(G3_1, (2, 6), (3, 0))
GR3.place(G3_2, (7, 4), (1, 1))
GR3.place(G3_3, (6, 6), (2, 1))
GR3.place(G3_4, (5, 4), (3, 1))
GR3.place(G3_5, (3, 9), (3, 1))
GR3.place(G3_6, (7, 6), (1, 2))
GR3.place(G3_7, (3, 3), (2, 2))
GR3.place(G3_8, (8, 6), (2, 2))
GR3.place(G3_9, (9, 7), (3, 2))
GR3.place(G3_10, (8, 3), (5, 1))
GR3.place(G3_11, (4, 8), (5, 1))
GR3.place(G3_12, (7, 3), (5, 2))
GR3.place(G3_13, (3, 8), (6, 1))
GR3.place(G3_14, (9, 1), (6, 2))
GR3.place(G3_15, (5, 5), (7, 1))
GR3.place(G3_16, (2, 4), (8, 1))
GR3.place(G3_17, (7, 1), (8, 1))
GR3.place(G3_18, (5, 6), (9, 0))
GR3.place(G3_19, (2, 9), (10, 0))
GR3.place(G3_20, (3, 5), (10, 2))
GR3.place(L3_1, (8, 8), (10, 2))

GR3.add_main_input(GR1)
GR3.add_main_output(L3_1)
GR3.init_wire()

###

class Enigme:
    def __init__(self, grids, win_component):
        self.grids = grids
        self.win_component = win_component
        self.win = False
    def reset(self):
        for g in self.grids:
            g.reset_buttons_state()
        self.win = False
    def update(self, app):
        if not self.win:
            for g in self.grids:
                g.update(app)
        if self.win_component.state:
            self.win = True
        if "BACKSPACE" in app.is_pushed:
            self.reset()
    def draw(self):
        for g in self.grids:
            g.draw_background()
        for g in self.grids:
            g.draw_wires()
        for g in self.grids:
            g.draw_components()

ENIGME = Enigme([GR1, GR2, GR3], GR3)

def init_prog(app):
    global ENIGME
    app.var["enigme"] = ENIGME
    
def update_prog(app):
    app.var["enigme"].update(app)
        
def draw_prog(app):
    app.var["enigme"].draw()

    if True:
        for i in range(len(app.var["enigme"].grids)):
            if app.var["enigme"].grids[i].state:
                mod.circle(30, 30*(i+1), 9, "#00FF00")
            elif not app.var["enigme"].grids[i].input or app.var["enigme"].grids[i].input.state:
                mod.circle(30, 30*(i+1), 9, "#552800")
            else:
                mod.circle(30, 30*(i+1), 9, "#111111")
    
mod.App(init_prog, update_prog, draw_prog, TX, TY, FPS, TITLE, background = '#2A2A2A', touches_players = TOUCHES)      