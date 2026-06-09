import os
import shutil
os.environ['SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS'] = '1'

import pygame
import ctypes
import math
import copy
import time
import random
import sys
import win32gui
import win32con

import pathlib

from pynput import keyboard, mouse
from pynput.keyboard import KeyCode
from pynput.mouse import Button as mButton

WINDOW = None
SCREEN = None
WIDTH, HEIGHT = None, None
FONT = None
FONT_SIZE = None
CLOCK = None
TITLE = None

def init(w, h, title, f_s, t = None, icon = None, camera = None):
    global WIDTH, HEIGHT
    pygame.init()
    if camera == True:
        from pygame import camera
        pygame.camera.init('_camera (MSMF)')
    WIDTH, HEIGHT = w, h
    window = set_window(w, h, title = title, icon = icon)
    screen = set_screen()
    font = set_font(f_s, t)
    clock = set_clock()
    pygame.mixer.init()
    return window, screen, clock

def end():
    pygame.quit()

def set_window(w, h, coord = None, title = TITLE, icon = None, mode = []):
    global WINDOW, TITLE
    WINDOW = pygame.display
    if coord != None:
        WINDOW.set_window_position(coord)
    m = []
    if mode != []:
        if "SCALED" in mode:
            m.append(pygame.SCALED)
        if "SHOWN" in mode:
            m.append(pygame.SHOWN)
        if "NOFRAME" in mode:
            m.append(pygame.NOFRAME)
        if "RESIZABLE" in mode:
            m.append(pygame.RESIZABLE)
    if mode == [] or m == []:
        WINDOW.set_mode((w, h))
    elif len(m) == 1:
        WINDOW.set_mode((w, h), m[0])
    else:
        WINDOW.set_mode((w, h), m[0] | m[1])
    set_title(title)
    TITLE = title
    if icon != None:
        WINDOW.set_icon(icon)
    return WINDOW

def set_mode(mode):
    global WINDOW
    WINDOW = pygame.display
    w, h = WINDOW.get_window_size()
    m = []
    if mode != []:
        if "SCALED" in mode:
            m.append(pygame.SCALED)
        if "SHOWN" in mode:
            m.append(pygame.SHOWN)
        if "NOFRAME" in mode:
            m.append(pygame.NOFRAME)
        if "RESIZABLE" in mode:
            m.append(pygame.RESIZABLE)
    if mode == [] or m == []:
        WINDOW.set_mode((w, h))
    elif len(m) == 1:
        WINDOW.set_mode((w, h), m[0])
    else:
        WINDOW.set_mode((w, h), m[0] | m[1])

def screen_pos(num, dic):
    if num in dic:
        if dic[num] == 'CENTRE':
            return (0, 0)
        else:
            index = None
            for n in dic:
                if dic[n] == 'CENTRE':
                    index = n
            if index == None:
                return (0, 0)
            else:
                x = 0
                y = 0
                if dic[num] == 'DROITE':
                    x = get_desktop_sizes()[index][0]
                elif dic[num] == 'GAUCHE':
                    x = - get_desktop_sizes()[num][0]
                    return (x, y)
                elif dic[num] == 'HAUT':
                    y = - get_desktop_sizes()[num][1]
                elif dic[num] == 'BAS':
                    y = get_desktop_sizes()[index][1]
                return (x, y)
    else:
        return (0, 0)

def locate_window(coord, dic):
    x, y = coord
    if x < 0:
        return 'GAUCHE'
    elif y < 0:
        return 'HAUT'
    else:
        index = None
        for n in dic:
            if dic[n] == 'CENTRE':
                index = n
        if index != None:
            xc, yc = get_desktop_sizes()[index]
            if x >= xc:
                return 'DROITE'
            elif y >= yc:
                return 'BAS'
            else:
                return 'CENTRE'
        else:
            return 'CENTRE'

def locate_screen(coord, dic):
    c = locate_window(coord, dic)
    for t in dic:
        if dic[t] == c:
            return t
    return 0
    
def set_screen():
    global SCREEN, WINDOW
    SCREEN = WINDOW.get_surface()
    return SCREEN

def fullscreen():
    global WINDOW
    WINDOW.toggle_fullscreen()
    set_screen()

def get_window_size():
    global WINDOW
    return WINDOW.get_window_size()

def get_desktop_sizes():
    global WINDOW
    return WINDOW.get_desktop_sizes()

def get_window_system_info():
    global WINDOW
    return WINDOW.get_wm_info()

def change_prop(w = None, h = None, title = TITLE, coord = None):
    if w == None:
        w = get_window_size()[0]
    if h == None:
        h = get_window_size()[1]
    set_window(w, h, title = title)
    if coord != None:
        set_window_position(coord)
    set_screen()

def set_title(title = TITLE):
    if title != None and isinstance(title, str):
        WINDOW.set_caption(title)
        set_screen()

def set_font(s, t = None):
    global FONT, FONT_SIZE
    FONT = pygame.font.SysFont(t, s)
    FONT_SIZE = s
    return FONT

def set_clock():
    global CLOCK
    CLOCK = pygame.time.Clock()
    return CLOCK

def text(t, c, anticrenelage = True):
    global FONT
    return FONT.render(t, anticrenelage, c)

def write(x, y, t, c, anticrenelage = True):
    global SCREEN
    SCREEN.blit(text(t, c, anticrenelage), (x, y))
    
def write_scale(x, y, t, c, taille, police = None, anticrenelage = True):
    global SCREEN
    commande = pygame.font.SysFont(police, taille).render(t, anticrenelage, c)
    SCREEN.blit(commande, (x, y))

def draw(image, coord):
    global SCREEN
    SCREEN.blit(image, coord)

def circle(x, y, r, col, width = 0):
    global SCREEN
    pygame.draw.circle(SCREEN, col, (x, y), r, width = width)

def rect_t(x, y, tx, ty, col, contour = 0, *args):
    global SCREEN
    if args == None or len(args) == 1 or len(args) == 0:
        if args != None and args != () and args[0] != None:
            elmt = args[0]
        else:
            elmt = -1
        tl, tr, bl, br = elmt, elmt, elmt, elmt
    elif len(args) == 4:
        tl, tr, bl, br = args
    else:
        raise Exception("ERROR : list is too small/big")
    pygame.draw.rect(SCREEN, col, (x, y, tx, ty), contour, border_top_left_radius=tl, border_top_right_radius=tr, border_bottom_left_radius=bl, border_bottom_right_radius=br)

def rect(x1, y1, x2, y2, col, contour = 0, *args):
    global SCREEN
    tx, ty = x2 - x1, y2 - y1
    rect_t(x1, y1, tx, ty, col, contour, *args)

def triangle(p1, p2, p3, col, width = 0):
    global SCREEN
    pygame.draw.polygon(SCREEN, col, (p1, p2, p3), width)

def rotation(p, centre, angle):
    d = dist2(p, centre)
    a_rad = math.radians(angle)
    dx = p[0] - centre[0]
    dy = p[1] - centre[1]
    a_i = math.atan2(dy, dx)
    a = (a_i + a_rad)
    x = centre[0] + math.cos(a) * d
    y = centre[1] - math.sin(a) * d
    return [x, y]

def eloigner(p, centre, final_dist):
    if dist2(p, centre) == 0:
        return centre
    k = final_dist/dist2(p, centre)
    dx = p[0] - centre[0]
    dy = p[1] - centre[1]
    x = centre[0] + k * dx
    y = centre[1] + k * dy
    return [x, y]

def eloigner2(p, centre, coeff):
    if dist2(p, centre) == 0:
        return centre
    dx = p[0] - centre[0]
    dy = p[1] - centre[1]
    x = centre[0] + coeff * dx
    y = centre[1] + coeff * dy
    return [x, y]

def line(x1, y1, x2, y2, col, epaisseur):
    global SCREEN
    pygame.draw.line(SCREEN, col, (x1, y1), (x2, y2), width = round(epaisseur))

def line_p(x1, y1, angle, long, col, epaisseur):
    x2 = x1 + math.cos(math.radians(angle)) * long
    y2 = y1 - math.sin(math.radians(angle)) * long
    line(x1, y1, x2, y2, col, epaisseur)

def forme(l_p : list, col, width = 0):
    global SCREEN
    pygame.draw.polygon(SCREEN, col, tuple(l_p), width)

def portion_cercle(centre : tuple, rayon, angle_depart, angle_fin, col, width = 0, qualite = 1, precision = 0, pas = 1):
    points = [centre]
    if angle_depart > angle_fin:
        angle_fin += 360
    for angle in range(round(angle_depart * 10**precision) * qualite, round(angle_fin * 10**precision) * qualite + 1, pas):
        rad = math.radians(angle)/(qualite * 10 ** precision)
        x = centre[0] + rayon * math.cos(rad)
        y = centre[1] - rayon * math.sin(rad) # - car Y descend vers le bas
        points.append((x, y))
    if len(points) > 2:
        forme(points, col, width = width)

def milieu_portion_cercle(centre : tuple, rayon1, rayon2, angle_depart, angle_fin, col, width = 0, qualite = 1, precision = 0, pas = 1):
    points = []
    if angle_depart > angle_fin:
        angle_fin += 360
    if rayon2 > rayon1:
        rayon1, rayon2 = rayon2, rayon1
    for angle in range(round(angle_depart * 10 ** precision) * qualite, round(angle_fin * 10 ** precision) * qualite + 1, pas):
        rad = math.radians(angle)/(qualite * 10 ** precision)
        x = centre[0] + rayon1 * math.cos(rad)
        y = centre[1] - rayon1 * math.sin(rad) # - car Y descend vers le bas
        points.append((x, y))
    for angle in range(round(angle_fin * 10 ** precision) * qualite, round(angle_depart * 10 ** precision) * qualite - 1, -pas):
        rad = math.radians(angle)/(qualite * 10 ** precision)
        x = centre[0] + rayon2 * math.cos(rad)
        y = centre[1] - rayon2 * math.sin(rad) # - car Y descend vers le bas
        points.append((x, y))
    if len(points) > 2:
        forme(points, col, width = width)

def coord_point_cercle(x_c, y_c, r, v, e):
    '''
    x_c, y_c : coord centre cercle
    r : rayon
    v : (0-100)
    e : epaisseur
    '''
    c, s = math.cos((2 * math.pi)/100 * v), math.sin((2 * math.pi)/100 * v)
    x = round(x_c + (r * c) + (abs(c)/c) * -4/7*e)
    y = round(y_c + (r * s) + (abs(c)/c) * -4/7*e)
    return x, y

def rgb(t):
    l = list(t)
    l.pop(0)
    r = hex_to_dec(l[0] + l[1])
    g = hex_to_dec(l[2] + l[3])
    b = hex_to_dec(l[4] + l[5])
    return r, g, b

def complete_dec_to_hex(t : int):
    return hex(t)[2:].upper()

def complete_hex_to_dec(t : str):
    a = ['A', 'B', 'C', 'D', 'E', 'F']
    t = list(t.upper())
    tot = 0
    for i in range(len(t)):
        p = len(t)-1-i
        u = t[i]
        try:
            u = int(u)
        except:
            u = 10+a.index(u)
        tot += u*16**p
    return tot

def get_full_ip(ip : str, port : int):
    return ip+":"+str(port)
def get_ip_from_full(ip_full):
    ip, port = ip_full.split(":")
    port = int(port)
    return ip, port

def local_ip_to_str(ip = None, port = None, category = "MAX"):
    if ":" in ip:
        ip, port = get_ip_from_full(ip)
    ip = ip.split(".")
    assert ip[0] == "192" and ip[1] == "168", "ERREUR : Vous essayez d'obtenir le code d'une adresse IP Publique (ou non conforme : réseaux local d'une entreprise...)."
    n = int(str(len(ip[2]))+ip[2]+str(len(ip[3]))+ip[3]+str(len(str(port)))+str(port))
    return dec_to_any(n, category)

def local_ip_from_str(code : str, type_retour = None, category = "MAX"):
    d = str(any_to_dec(code, category))
    tab = []
    count = 0
    temp = ""
    for i in range(len(d)):
        if count == 0:
            try:
                count = int(d[i])
                if temp != "":
                    tab.append(temp)
                    temp = ""
            except:
                raise Exception("Le code est incorrect.")
        else:
            temp += d[i]
            count -= 1
    if count == 0:
        tab.append(temp)
        temp = ""
    else:
        raise Exception("Le code est incorrect.")
    if len(tab) != 3:
        raise Exception("Le code est incorrect.")
    ip = "192.168."+tab[0]+"."+tab[1]
    port = int(tab[2])
    if type_retour == "FULL":
        return get_full_ip(ip, port)
    else:
        return ip, port

def dec_to_any(n : int, category):
    m = n
    rep = ""
    if isinstance(category, int) and category <= 26:
        while m != -1:
            c = m%category
            m = m//category
            rep = chr(65+c) + rep
            if m == 0:
                m = -1
        return rep
    elif category == "MAX":
        nb = 36
        while m != -1:
            c = m%nb
            m = m//nb
            if c < 10:
                rep = str(c) + rep
            else:
                rep = chr(65+c-10) + rep
            if m == 0:
                m = -1
        return rep

def any_to_dec(t : str, category):
    rep = 0
    if isinstance(category, int) and category <= 26:
        l = len(t)
        for i in range(l):
            p = l-1-i
            v = ord(t[i])-65
            rep += v*category**p
        return rep
    elif category == "MAX":
        nb = 36
        l = len(t)
        for i in range(l):
            p = l-1-i
            try:
                v = int(t[i])
            except:
                v = ord(t[i])-65+10
            rep += v*nb**p
        return rep

def hex_to_dec(t):
    # len t = 2
    t = list(t)
    for i in range(2):
        try:
            t[i] = int(t[i])
        except:
            a = ['A', 'B', 'C', 'D', 'E', 'F']
            b = [10, 11, 12, 13, 14, 15]
            t[i] = b[a.index(t[i])]
    return t[0] * 16 + t[1]

def dec_to_hex(t):
    lettres = ['A', 'B', 'C', 'D', 'E', 'F']
    nombres = [10, 11, 12, 13, 14, 15]
    a = t // 16
    b = t % 16
    l = [a, b]
    for i in range(2):
        if l[i] in nombres:
            l[i] = lettres[nombres.index(l[i])]
        else:
            l[i] = str(l[i])
    return l[0] + l[1]

def col_hex(r = 0, g = 0, b = 0, t = None):
    if t != None:
        r, g, b = t
    return "#"+dec_to_hex(r)+dec_to_hex(g)+dec_to_hex(b)

def somme_couleurs(c1, c2):
    r1, g1, b1 = rgb(c1)
    r2, g2, b2 = rgb(c2)
    r = limit(round(r1 + r2), 0, 255)
    g = limit(round(g1 + g2), 0, 255)
    b = limit(round(b1 + b2), 0, 255)
    return col_hex(r = r, g = g, b = b)

def somme_couleurs_liste(l):
    rs = []
    gs = []
    bs = []
    for c in l:
        rc, gc, bc = rgb(c)
        rs.append(rc)
        gs.append(gc)
        bs.append(bc)
    r = limit(round(sum(rs)), 0, 255)
    g = limit(round(sum(gs)), 0, 255)
    b = limit(round(sum(bs)), 0, 255)
    return col_hex(r = r, g = g, b = b)

def moyenne_couleurs(l):
    rs = []
    gs = []
    bs = []
    for c in l:
        rc, gc, bc = rgb(c)
        rs.append(rc)
        gs.append(gc)
        bs.append(bc)
    r = limit(round(sum(rs)/len(rs)), 0, 255)
    g = limit(round(sum(gs)/len(gs)), 0, 255)
    b = limit(round(sum(bs)/len(bs)), 0, 255)
    return col_hex(r = r, g = g, b = b)

def transition(c1, c2, etat, limit_inf = None, limit_sup = None, floating = False):
    # etat (%) : 0 - 100
    if etat > 100:
        return c2
    if isinstance(c1, str):
        r1, g1, b1 = rgb(c1)
        r2, g2, b2 = rgb(c2)
        r = limit(r1 + round((r2 - r1)/100 * etat), 0, 255)
        g = limit(g1 + round((g2 - g1)/100 * etat), 0, 255)
        b = limit(b1 + round((b2 - b1)/100 * etat), 0, 255)
        return '#' + dec_to_hex(r) + dec_to_hex(g) + dec_to_hex(b)
    else:
        if not floating:
            rep = c1 + round((c2 - c1)/100 * etat)
        else:
            rep = c1 + (c2 - c1)/100 * etat
        return limit(rep, limit_inf, limit_sup)

def limit(x, inf, sup):
    if inf != None and x < inf:
        return inf
    elif sup != None and x > sup:
        return sup
    else:
        return x

def x_mul(x, p):
    if not isinstance(p, float):
        p = p[0]
    return x * p

def x_add(x, p):
    if not isinstance(p, float):
        p = p[0]
    return x + p

def x_puissance(x, p):
    if not isinstance(p, float):
        p = p[0]
    return x ** p

def x_racine(x, p):
    if not isinstance(p, float):
        p = p[0]
    return x ** (1/p)

def x_second_degree(x, a, b = None, c = None):
    if not isinstance(a, float) or not isinstance(a, int):
        a, b, c = a
    return a * (x ** 2) + b * x + c

def transition_speed(value, fonction, ceiling = None, borne = 'SUP', *args):
    sens = [1, -1][['SUP', 'INF'].index(borne)]
    if ceiling != None:
        if sens:
            v = int(fonction(value, args))
            if v > ceiling:
                return ceiling
        else:
            v = int(fonction(value, args))
            if v < ceiling:
                return ceiling
        return v
    else:
        return int(function(value, args))
    
    
    
def get_image(nom, fichier = None):
    if fichier != None:
        nom = fichier + nom
    return pygame.image.load(nom)

def get_image_size(image):
    return image.size

def get_zone(image, coord):
    """
    return ((xmin, xmax), (ymin, ymax))
    """
    x, y = coord
    dx, dy = get_image_size(image)
    xmin = x
    ymin = y
    xmax = x + dx
    ymax = y + dy
    return ((xmin, xmax), (ymin, ymax))

def is_in_zone(coord, image, coord_im):
    xs, ys = get_zone(image, coord_im)
    xmin, xmax = xs
    ymin, ymax = ys
    x, y = coord
    return xmin <= x <= xmax and ymin <= y <= ymax

def scale(image, x, y):
    return pygame.transform.scale(image, (x, y))

def zoom(image, zoom):
    return pygame.transform.rotozoom(image, 0, zoom)

def center_zoom(image, zoom_m, coord):
    new_im = copy.deepcopy(image)
    t = get_image_size(new_im)
    new_im = zoom(new_im, zoom_m)
    nv_t = get_image_size(new_im)
    dx = (nv_t[0] - t[0])/2
    dy = (nv_t[1] - t[1])/2
    new_coord = [coord[0] - dx, coord[1] - dy]
    return new_im, new_coord

def get_center(image, coord):
    return coord[0] + image.width, coord[1]/2 + image.height/2

def rotate(image, rot):
    return pygame.transform.rotate(image, -1 * rot)

def center_rotate(image, angle, coord):
    """rotate an image while keeping its center and size
    A FINIR AVEC LE ALPHA """
    """
    orig_rect = image.get_rect()
    rot_image = rotate(image, angle)
    rot_rect = orig_rect.copy()
    rot_rect.center = rot_image.get_rect().center
    #rot_image = rot_image.subsurface(rot_rect).copy()
    new_coord = coord
    """
    """
    En supposant la carte droite au début
    """
    pdb = [coord[0] + image.get_width(), coord[1]+image.get_height()]
    br = pdb
    rot_image = rotate(image, angle)
    pdb = [coord[0] + rot_image.get_width(), coord[1]+rot_image.get_height()]
    nv_br = pdb
    dx = nv_br[0] - br[0]
    dy = nv_br[1] - br[1]
    new_coord = [coord[0] - dx/2, coord[1] - dy/2]
    return rot_image, new_coord

def flip_x(image):
    return pygame.transform.flip(image, True, False)

def flip_y(image):
    return pygame.transform.flip(image, False, True)

def black_and_white(image):
    return pygame.transform.grayscale(image)

def chop(image, x1, y1, x2, y2):
    return image.subsurface(pygame.rect.Rect(x1, y1, x2 - x1, y2 - y1))

def contour(image):
    return pygame.transform.laplacian(image)

def flou_box(image, rayon):
    return pygame.transform.box_blur(image, rayon)

def flou_gaussien(image, rayon):
    return pygame.transform.gaussian_blur(image, rayon)

def opacite(image, opacite):
    i = copy.deepcopy(image)
    i.set_alpha(min(255, max(0, int(opacite/100*255))))
    return i
    
def show():
    pygame.display.flip()

DICT_KEYS = {9 : 'TAB', 32 : 'SPACE', 48 : '0', 49 : '1',
             50 : '2', 51 : '3', 52 : '4', 53 : '5', 54 : '6',
             55 : '7', 56 : '8', 57 : '9', 8 : 'BACKSPACE',
             27 : 'ESCAPE', 1073741905 : 'DOWN',
             1073741904 : 'LEFT', 1073741903 : 'RIGHT',
             1073741906 : 'UP', 13 : 'ENTER', 37 : '%',
             1073742049 : 'SHIFT', 1073742048 : 'CTRL',
             1073742050 : 'ALT', 42 : '*', 59 : ';',
             1073741881 : 'CAPSLOCK', 94 : '^', 58 : ':',
             44 : ',', 127 : 'DELETE', 36 : '$', 61 : '=',
             33 : '!', 60 : '<', 1073741922 : 'KP_0',
             1073741913 : 'KP_1', 1073741914 : 'KP_2',
             1073741915 : 'KP_3', 1073741916 : 'KP_4',
             1073741917 : 'KP_5', 1073741918 : 'KP_6',
             1073741919 : 'KP_7', 1073741920 : 'KP_8',
             1073741921 : 'KP_9', 1073741908 : 'KP_/',
             1073741912 : 'KP_ENTER', 1073741910 : 'KP_-',
             1073741909 : 'KP_*', 1073741923 : 'KP_.',
             1073741911 : 'KP_+', 1073742051 : 'WINDOWS',
             41 : ')', 1073741907 : 'NUMLOCK',
             1073742054 : 'RALT', 1073742052 : 'RCTRL',
             1073742053 : 'RSHIFT', 1073741882 : 'F1',
             1073741883 : 'F2', 1073741884 : 'F3',
             1073741885 : 'F4', 1073741886 : 'F5',
             1073741887 : 'F6', 1073741888 : 'F7',
             1073741889 : 'F8', 1073741890 : 'F9',
             1073741891 : 'F10', 1073741892 : 'F11',
             1073741893 : 'F12', 249 : 'ù', 178 : '²'}

# Dictionnaire de mapping pynput -> nom de touche
DICT_KEYBOARD_BG = {
    # Touches spéciales pynput
    keyboard.Key.tab: 'TAB',
    keyboard.Key.space: 'SPACE',
    keyboard.Key.backspace: 'BACKSPACE',
    keyboard.Key.esc: 'ESCAPE',
    keyboard.Key.down: 'DOWN',
    keyboard.Key.left: 'LEFT',
    keyboard.Key.right: 'RIGHT',
    keyboard.Key.up: 'UP',
    keyboard.Key.enter: 'ENTER',
    keyboard.Key.shift_l: 'SHIFT',
    keyboard.Key.shift_r: 'RSHIFT',
    keyboard.Key.ctrl_l: 'CTRL',
    keyboard.Key.ctrl_r: 'RCTRL',
    keyboard.Key.alt_l: 'ALT',
    keyboard.Key.alt_gr: 'RALT',
    keyboard.Key.caps_lock: 'CAPSLOCK',
    keyboard.Key.delete: 'DELETE',
    keyboard.Key.cmd: 'WINDOWS',
    keyboard.Key.num_lock: 'NUMLOCK',
    keyboard.Key.f1: 'F1',
    keyboard.Key.f2: 'F2',
    keyboard.Key.f3: 'F3',
    keyboard.Key.f4: 'F4',
    keyboard.Key.f5: 'F5',
    keyboard.Key.f6: 'F6',
    keyboard.Key.f7: 'F7',
    keyboard.Key.f8: 'F8',
    keyboard.Key.f9: 'F9',
    keyboard.Key.f10: 'F10',
    keyboard.Key.f11: 'F11',
    keyboard.Key.f12: 'F12',
    
    # Caractères AZERTY français (via leur char)
    'à': '0',
    '&': '1',
    'é': '2',
    '"': '3',
    "'": '4',
    '(': '5',
    '-': '6',
    'è': '7',
    '_': '8',
    'ç': '9',
    ')': ')',
    '*': '*',
    ';': ';',
    '^': '^',
    ':': ':',
    ',': ',',
    '$': '$',
    '=': '=',
    '!': '!',
    '<': '<',
    'ù': 'ù',
    '²': '²',
    
    # Pavé numérique (via VK codes)
    96: 'KP_0',
    97: 'KP_1',
    98: 'KP_2',
    99: 'KP_3',
    100: 'KP_4',
    101: 'KP_5',
    102: 'KP_6',
    103: 'KP_7',
    104: 'KP_8',
    105: 'KP_9',
    111: 'KP_/',  # VK code 111 pour /
    106: 'KP_*',  # VK code 106 pour *
    109: 'KP_-',  # VK code 109 pour -
    107: 'KP_+',  # VK code 107 pour +
    110: 'KP_.',  # VK code 110 pour .
    # Note: KP_ENTER a le même VK que Enter (13), difficile à distinguer
}

DICT_BUTTONS = {'PS4' :
                {0 : 'CROIX', 1 : 'ROND', 2 : 'CARRE', 3 : 'TRIANGLE',
                 4 : 'SHARE', 5 : 'PS', 6 : 'OPTIONS', 7 : 'L3',
                 8 : 'R3', 9 : 'L1', 10 : 'R1', 11 : 'BTN_HAUT',
                 12 : 'BTN_BAS', 13 : 'BTN_GAUCHE', 14 : 'BTN_DROITE',
                 15 : 'PAD'},
                'XBOX' :
                {0 : 'BTN_A', 1 : 'BTN_B', 2 : 'BTN_X', 3 : 'BTN_Y',
                 4 : 'LB', 5 : 'RB', 6 : 'BACK', 7 : 'START',
                 8 : 'LS', 9 : 'RS', 10 : 'XBOX'},
                'Thrustmaster Joystick' :
                {0 : 'GACHETTE', 1 : '2', 2 : '3', 3 : '4', 4 : '5', 5 : '6', 6 : '7', 7 : '8', 8 : '9', 9 : '10', 10 : '11', 11 : '12'},
                'Unknown' :
                {0 : '0', 1 : '1', 2 : '2', 3 : '3', 4 : '4', 5 : '5', 6 : '6', 7 : '7', 8 : '8', 9 : '9', 10 : '10',
                 11 : '11', 12 : '12', 13 : '13', 14 : '14', 15 : '15', 16 :'16', 18 :'18', 19 : '19', 20 : '20'}}
DICT_AXES = {'PS4' :
             {0 : 'JOYSTICK_LH',
             1 : 'JOYSTICK_LV',
             2 : 'JOYSTICK_RH',
             3 : 'JOYSTICK_RV',
             4 : 'L', 5 : 'R'},
             'XBOX' :
             {0 : 'JOYSTICK_LH',
             1 : 'JOYSTICK_LV',
             2 : 'JOYSTICK_RH',
             3 : 'JOYSTICK_RV',
             4 : 'L', 5 : 'R'},
             'Thrustmaster Joystick' :
             {0 : 'JOYSTICK_LEFT_RIGHT',
              1 : 'JOYSTICK_FORWARD_BACKWARD',
              2 : 'JOYSTICK_TORSION',
              3 : 'SELECTOR'},
             'Unknown' :
             {0 : '0',
              1 : '1',
              2 : '2',
              3 : '3',
              4 : '4',
              5 : '5',
              6 : '6',
              7 : '7',
              8 : '8',
              9 : '9',
              10 : '10'}}

DICT_HATS = {'XBOX':
            {0 : {0 : {1 : 'BTN_DROITE',
                 -1 : 'BTN_GAUCHE',
                  0 : ['BTN_DROITE', 'BTN_GAUCHE']},
            1 : {1 : 'BTN_HAUT',
                -1 : 'BTN_BAS',
                 0 : ['BTN_HAUT', 'BTN_BAS']}}},
            'Thrustmaster Joystick':
             {0 :
            {0 : {1 : 'BTN_DROITE',
                 -1 : 'BTN_GAUCHE',
                  0 : ['BTN_DROITE', 'BTN_GAUCHE']},
            1 : {1 : 'BTN_HAUT',
                -1 : 'BTN_BAS',
                 0 : ['BTN_HAUT', 'BTN_BAS']}}},
            'Unknown':
            {0 : {0 : '0', 1 : '1'},
             1 : {0 : '0', 1 : '1'}}}

def get_controller_keys():
    global DICT_BUTTONS, DICT_HATS
    l = []
    for type_c in ['XBOX', 'PS4', 'Thrustmaster Joystick']:
        for c in DICT_BUTTONS[type_c]:
            l.append(DICT_BUTTONS[type_c][c])
        if type_c in ['XBOX', 'Thrustmaster Joystick']:
            for c in DICT_HATS[type_c]:
                for t in DICT_BUTTONS[type_c][c][0]:
                    l.append(t)
    return l

def get_keyboard_keys():
    global DICT_KEYS
    l = []
    for k in DICT_KEYS:
        l.append(DICT_KEYS[k])
    for k in range(97, 123):
        l.append(chr(k).upper())
    return l

def get_mouse_keys():
    return ['MOUSE_LEFT', 'MOUSE_CENTER', 'MOUSE_RIGHT']

# Dictionnaire pour les boutons de souris
DICT_MOUSE_BG = {
    mButton.left: 'MOUSE_LEFT',
    mButton.middle: 'MOUSE_CENTER',
    mButton.right: 'MOUSE_RIGHT',
}
        

def get_mouse_pos(noframe = False):
    global HWND
    if not noframe:
        return pygame.mouse.get_pos()
    else:
        cursor_pos = win32gui.GetCursorPos()
        # Position de la fenêtre (bord extérieur)
        window_rect = win32gui.GetWindowRect(HWND)
        # Position de la zone client (zone de dessin réelle)
        client_pt = win32gui.ScreenToClient(HWND, cursor_pos)
        
        return (client_pt[0], client_pt[1])

def get_mouse_moves():
    return pygame.mouse.get_rel()

def toggle_mouse():
    pygame.mouse.set_visible(not pygame.mouse.get_visible())

def move_mouse(x, y):
    pygame.mouse.set_pos(x, y)

def change_mouse(text = None):
    a = 0
    if text == 'WRITE':
        a = 1
    elif text == 'LOAD':
        a = 2
    elif text == 'SELECT':
        a = 3
    elif text == 'MOVE_DIAG_HG':
        a = 5
    elif text == 'MOVE_DIAG_HD':
        a = 6
    elif text == 'MOVE_HORI':
        a = 7
    elif text == 'MOVE_VERT':
        a = 8
    elif text == 'MOVE':
        a = 9
    elif text == 'BLOCKED':
        a = 10
    elif text == 'POINT':
        a = 11
    '''
    elif text == 'LOAD':
        a = 4
    '''
    pygame.mouse.set_cursor(a)

def get_mouse_buttons(mouse_wheel):
    t = {0 : 'MOUSE_LEFT', 1 : 'MOUSE_CENTER', 2 : 'MOUSE_RIGHT'}
    is_pressed = []
    for i in range(3):
        if pygame.mouse.get_pressed()[i]:
            is_pressed.append(t[i])
    if mouse_wheel > 0:
        is_pressed.append("MOUSE_WHEEL_UP")
    elif mouse_wheel < 0:
        is_pressed.append("MOUSE_WHEEL_DOWN")
    return is_pressed

def get_joy_buttons(joy, type_j):
    global DICT_BUTTONS, DICT_HATS
    is_pressed = []
    for n in DICT_BUTTONS[type_j]:
        try:
            if joy.get_button(n):
                is_pressed.append(DICT_BUTTONS[type_j][n])
        except:
            pass
    if type_j in ['XBOX', 'Thrustmaster Joystick']:
        h = get_hats(joy, type_j)[0]
        x, y = h
        if x != 0:
            is_pressed.append(DICT_HATS[type_j][0][0][x])
        if y != 0:
            is_pressed.append(DICT_HATS[type_j][0][1][y])
    return is_pressed

def get_axes(joy, type_j, zm = 6, debug = False):
    global DICT_AXES
    rep = {}
    for n in DICT_AXES[type_j]:
        try:
            axe = DICT_AXES[type_j][n]
            value = round(joy.get_axis(n) * 100, 1)
            if debug and axe == 'SELECTOR':
                value = 100
            if abs(value) <= zm and axe not in ['JOYSTICK_FORWARD_BACKWARD', 'JOYSTICK_LEFT_RIGHT', 'SELECTOR']:
                value = 0
            if axe in ['L', 'R']:
                value += 100
                value = round(value / 2, 1)
            if axe in ['SELECTOR']:
                value += 100
                value = round(100-value / 2, 1)
            if DICT_AXES[type_j][n] in ['JOYSTICK_LV', 'JOYSTICK_RV', 'JOYSTICK_FORWARD_BACKWARD']:
                rep[axe] = -value
            else:
                rep[axe] = value
        except:
            pass
    return rep

def get_hats(joy, type_j):
    rep = {}
    for n in DICT_HATS[type_j]:
        rep[n] = joy.get_hat(n)
    return rep

def get_new_controller_id(d):
    for i in range(len(d)):
        if i not in d:
            return i
    else:
        i = len(d)
        while i in d:
            i += 1
        return i

def get_keys():
    global DICT_KEYS
    keys = pygame.key.get_pressed()
    is_pressed = []
    for k in range(97, 123):
        if keys[k] == True:
            is_pressed.append(chr(k).upper())
        
    for k in DICT_KEYS:
        if keys[k] == True:
            is_pressed.append(DICT_KEYS[k])

    if 'RALT' in is_pressed and 'CTRL' in is_pressed:
        is_pressed.remove('CTRL')
    return is_pressed

def get_keys_pushed(old, now):
    ans = []
    for e in now:
        if e not in old:
            ans.append(e)
    return ans

def get_keys_released(old, now):
    ans = []
    for e in old:
        if e not in now:
            ans.append(e)
    return ans

def get_type_controller(j):
    if 'PS4' in j.get_name():
        return 'PS4'
    elif 'Xbox 360 Controller' in j.get_name():
        return 'XBOX'
    elif 'T.Flight Stick' in j.get_name():
        return 'Thrustmaster Joystick'
    else:
        return 'Unknown'
    

def get_sound(nom, fichier = None):
    if fichier != None:
        nom = fichier + nom
    return pygame.mixer.Sound(nom)

def get_volume(sound_or_channel):
    return sound_or_channel.get_volume()

def set_volume(sound_or_channel, v):
    sound_or_channel.set_volume(v)

def get_length(sound):
    return sound.get_length

def play(sound, loops = 1, fade_in = 0, max_time = 0, channel = None):
    if channel == None:
        sound.play(loops - 1, maxtime = max_time, fade_ms = int(1000 * fade_in))
    else:
        channel.play(sound, loops - 1, maxtime = max_time, fade_ms = int(1000 * fade_in))

def stop(sound_or_channel, fade_out = None):
    if fade_out != None:
        sound_or_channel.fadeout(int(fade_out * 1000))
    else:
        sound_or_channel.stop()

def pause(channel):
    channel.pause()
def unpause(channel):
    channel.unpause()

def fade_out_all(v):
    pygame.mixer.fadeout(int(v * 1000))

def pause_all():
    pygame.mixer.pause()

def unpause_all():
    pygame.mixer.unpause()

def stop_all():
    pygame.mixer.stop()



def get_channel():
    return pygame.mixer.Channel

def get_channel_queue(channel):
    return channel.get_queue()
def get_channel_sound(channel):
    return channel.get_sound()
def is_channel_active(channel):
    return channel.get_busy()
def get_channel_stop(channel):
    return channel.get_endevent()
def set_in_channel_queue(channel, sound):
    channel.queue(sound)

def get_sound_list(*args):
    tab = []
    for sound in list(args):
        tab.append(get_sound(sound))
    return tab

def one_is_in_list(l1, l2):
    for e in l1:
        if e in l2:
            return True
    return False

def all_are_in_list(l1, l2):
    for e in l1:
        if e not in l2:
            return False
    return True
    
class Button:
    def __init__(self, x, y, tx, ty, texte, col, col_fond, col_contour, col_over, col_click, taille_contour = 2, angle = 0, son = None, volume_son = 1):
        global FONT_SIZE
        self.x = x
        self.y = y
        self.tx = tx
        self.ty = ty
        self.angle = angle
        self.texte = texte
        self.col = col
        self.col_fond = col_fond
        self.taille_contour = taille_contour
        self.col_contour = col_contour
        self.col_over = col_over
        self.col_click = col_click
        self.x_t = self.x + (self.tx - len(self.texte) * FONT_SIZE * 0.45) // 2
        self.y_t = self.y + (self.ty - FONT_SIZE * 0.5) / 2
        self.temp_tab = []
        self.old_temp_tab = []
        self.son = son
        self.volume = volume_son
        if self.son != None:
            try:
                self.son = get_sound(self.son)
                self.son.set_volume(self.volume)
            except:
                self.son = None

    @property
    def selected(self):
        x_s, y_s = get_mouse_pos()
        if x_s >= self.x and x_s <= self.x + self.tx:
            if y_s >= self.y and y_s <= self.y + self.ty:
                return True
        return False
    
    @property
    def is_click(self):
        return self.selected and 'MOUSE_LEFT' in self.temp_tab
    
    @property
    def is_pushed(self):
        return self.is_click and not 'MOUSE_LEFT' in self.old_temp_tab
    
    @property
    def is_released(self):
        return not self.is_click and 'MOUSE_LEFT' in self.old_temp_tab
    
    def update(self, tab):
        self.old_temp_tab = self.temp_tab
        self.temp_tab = tab
        if self.son != None and self.is_pushed:
            play(self.son)
    
    def draw(self):
        if not self.selected:
            rect_t(self.x, self.y, self.tx, self.ty, self.col_fond, 0, self.angle)
        elif not self.is_click:
            rect_t(self.x, self.y, self.tx, self.ty, self.col_over, 0, self.angle)
        else:
            rect_t(self.x, self.y, self.tx, self.ty, self.col_click, 0, self.angle)
        rect_t(self.x, self.y, self.tx, self.ty, self.col_contour, self.taille_contour, self.angle)
        write(self.x_t, self.y_t, self.texte, self.col)

class Button_Image:
    def __init__(self, x, y, image, image_select = None, image_click = None, size = (1, 1), son = None, volume_son = 1):
        global FONT_SIZE
        self.x = x
        self.y = y
        self.size = size
        self.image = get_image(image)
        if image_select != None:
            self.image_select = get_image(image_select)
        else:
            self.image_select = self.image
        if image_click != None:
            self.image_click = get_image(image_click)
        else:
            self.image_click = self.image
        self.image = scale(self.image, self.image.get_width() * self.size[0], self.image.get_height() * self.size[1])
        self.image_select = scale(self.image_select, self.image_select.get_width() * self.size[0], self.image_select.get_height() * self.size[1])
        self.image_click = scale(self.image_click, self.image_click.get_width() * self.size[0], self.image_click.get_height() * self.size[1])
        self.tx, self.ty = self.image.get_width(), self.image.get_height()
        self.temp_tab = []
        self.old_temp_tab = []
        self.son = son
        self.volume = volume_son
        if self.son != None:
            try:
                self.son = get_sound(self.son)
                self.son.set_volume(self.volume)
            except:
                self.son = None

    @property
    def selected(self):
        x_s, y_s = get_mouse_pos()
        if x_s >= self.x and x_s <= self.x + self.tx:
            if y_s >= self.y and y_s <= self.y + self.ty:
                return True
        return False
    
    @property
    def is_click(self):
        return self.selected and 'MOUSE_LEFT' in self.temp_tab
    
    @property
    def is_pushed(self):
        return self.is_click and not 'MOUSE_LEFT' in self.old_temp_tab
    
    @property
    def is_released(self):
        return not self.is_click and 'MOUSE_LEFT' in self.old_temp_tab
    
    def update(self, tab):
        self.old_temp_tab = self.temp_tab
        self.temp_tab = tab
        if self.son != None and self.is_pushed:
            play(self.son)
    
    def draw(self):
        if not self.selected:
            draw(self.image, (self.x, self.y))
        elif not self.is_click:
            draw(self.image_select, (self.x, self.y))
        else:
            draw(self.image_click, (self.x, self.y))

class Checkbox:
    def __init__(self, x, y, t, size, couleur, value = None, text = '', text_position = 'GAUCHE', text_color = 'Grey', couleur_interne = None, couleur_off = '#777777', etat_initial = False, id = None, angle = 0, symbol = '', symbol_color = 'White'):
        self.type = t
        self.id = id
        self.x = x
        self.y = y
        self.size = size
        self.tx = self.size
        self.ty = self.size
        self.on = etat_initial
        self.col = couleur
        self.col_off = couleur_off
        self.text = text
        if value == None:
            self.value = self.text
        else:
            self.value = value
        self.text_position = text_position
        self.text_color = text_color
        if couleur_interne != None:
            self.col_interieur = couleur_interne
        else:
            self.col_interieur = self.col
        self.angle = angle
        if self.angle == 'AUTO':
            self.angle = round(7/25 * self.size)
        if self.type == 'CARRE':
            self.contour = self.size // 8
        elif self.type == 'ROND':
            self.contour = self.size // 8 // 2
        self.symbol = symbol
        self.symbol_color = symbol_color
        self.temp_tab = []
        self.old_temp_tab = []
    
    @property
    def selected(self):
        x_s, y_s = get_mouse_pos()
        if x_s >= self.x and x_s <= self.x + self.tx:
            if y_s >= self.y and y_s <= self.y + self.ty:
                return True
        return False
    
    @property
    def is_click(self):
        return self.selected and 'MOUSE_LEFT' in self.temp_tab
    
    @property
    def is_pushed(self):
        return self.is_click and not 'MOUSE_LEFT' in self.old_temp_tab
    
    def update(self, tab):
        self.old_temp_tab = self.temp_tab
        self.temp_tab = tab
        if self.is_pushed:
            self.on = not self.on
    
    def draw(self):
        if self.type == 'CARRE':
            if self.on:
                rect_t(self.x, self.y, self.size, self.size, self.col_interieur, 0, self.angle)
            rect_t(self.x, self.y, self.size, self.size, self.col, self.contour, self.angle)
            
            if self.symbol != '' and self.on:
                x, y = self.x + 1/3 * self.size, self.y + 1/5 * self.size
                write_scale(x, y, self.symbol, self.symbol_color, self.size, police = None, anticrenelage = True)
            
        elif self.type == 'ROND':
            x_c, y_c = self.x + round(self.size / 2), self.y + round(self.size / 2)
            if self.on:
                r = 0.75 * round(self.size / 3)
                circle(x_c, y_c, r, self.col_interieur)
            circle(x_c, y_c, round(self.size / 3), self.col, self.contour)

        elif self.type == 'SWITCH':
            d = 1 / 2 * self.size
            r = 1/2 * d
            y = self.y + d
            x1 = self.x + r
            x2 = self.x + 3 * r
            circle(x1, y, r, self.col)
            circle(x2, y, r, self.col)
            rect(x1, y - r, x2, y + r, self.col)
            if self.on:
                circle(x2, y, 0.77 * r, self.col_interieur)
            else:
                circle(x1, y, 0.77 * r, self.col_off)
        
        if self.text != '':
            y = self.y + 1/5 * self.size
            if self.text_position == 'DROITE':
                x = self.x + self.size + 2/3 * self.size
            elif self.text_position == 'GAUCHE':
                x = self.x - 2/3 * self.size - len(self.text) * self.size // 2.5
            write_scale(x, y, self.text, self.text_color, self.size, police = None, anticrenelage = True)

class Radio:
    def __init__(self, nb_max, liste_checkbox):
        self.max = nb_max
        self.liste = liste_checkbox
        self.init_checkbox()
        self.old_liste_active = []
        self.on_id = []
    
    def init_checkbox(self):
        for c in range(len(self.liste)):
            self.liste[c].id = c
    
    @property
    def liste_active(self):
        t = []
        for c in range(len(self.liste)):
            if self.liste[c].on:
                t.append(c)
        return t
    
    @property
    def pushed(self):
        t = []
        for c in self.liste_active:
            if c not in self.old_liste_active:
                t.append(c)
        return t
    
    @property
    def released(self):
        t = []
        for c in self.old_liste_active:
            if c not in self.liste_active:
                t.append(c)
        return t
    
    @property
    def nb_active(self):
        return len(self.liste_active)
    
    def turn_off(self):
        self.liste[self.on_id.pop(0)].on = False
    
    def upd_check(self, is_pressed):
        for c in self.liste:
            c.update(is_pressed)

    def drw_check(self):
        for c in self.liste:
            c.draw()
    
    @property
    def on(self):
        t = []
        for i in self.on_id:
            t.append(self.liste[i].value)
        return t
    
    def update(self, is_pressed):
        self.upd_check(is_pressed)
        for c in self.pushed:
            self.on_id.append(c)
        for c in self.released:
            self.on_id.remove(c)
        while self.nb_active > self.max:
            self.turn_off()
        
        self.old_liste_active = self.liste_active
    
    def draw(self):
        self.drw_check()
            
    

# RANGE (Verticaux / Horizontaux)

class Range:
    def __init__(self, x, y, tx, ty, texte, mini, maxi, pas, v_init, type_r, col, col_barre, col_rond, col_click, col_contour, col_texte = "#FFFFFF", taille_contour = 2, angle = 0, son = None, volume_son = 1):
        global FONT_SIZE
        self.x = x
        self.y = y
        self.tx = tx
        self.ty = ty
        self.min = mini
        self.max = maxi
        self.pas = pas
        self.v = v_init
        self.decal = False
        self.angle = angle
        self.texte = texte
        self.type = type_r
        self.col = col
        self.col_barre = col_barre
        self.taille_contour = taille_contour
        self.col_rond = col_rond
        self.col_contour = col_contour
        self.col_click = col_click
        self.col_texte = col_texte
        self.x_t = self.x + (self.tx - len(self.texte) * FONT_SIZE * 0.45) // 2
        self.y_t = self.y + (self.ty + FONT_SIZE * 0.5)
        self.y_texte = self.y + (self.ty - FONT_SIZE * 0.5) / 2
        self.temp_tab = []
        self.old_temp_tab = []
        self.son = son
        self.volume = volume_son
        if self.son != None:
            try:
                self.son = get_sound(self.son)
                self.son.set_volume(self.volume)
            except:
                self.son = None

    @property
    def selected(self):
        x_s, y_s = get_mouse_pos()
        if x_s >= self.x and x_s <= self.x + self.tx:
            if y_s >= self.y and y_s <= self.y + self.ty:
                return True
        return False
    
    @property
    def is_click(self):
        return self.selected and 'MOUSE_LEFT' in self.temp_tab
    
    @property
    def is_pushed(self):
        return self.is_click and not 'MOUSE_LEFT' in self.old_temp_tab
    
    @property
    def is_released(self):
        return not self.is_click and 'MOUSE_LEFT' in self.old_temp_tab
    
    def get_v(self):
        x_s, y_s = get_mouse_pos()
        x, y = self.x, self.y
        xmax, ymax = self.x + self.tx, self.y + self.ty
        if self.type == "Horizontal":
            vs = x_s
            vmin = x
            vmax = xmax
        else:
            vs = y_s
        if vs < vmin:
            vs = vmin
        elif vs > vmax:
            vs = vmax
            vmin = y
            vmax = ymax
        v = (vs-vmin)/(vmax-vmin) * (self.max - self.min)
        if isinstance(self.pas, int):
            v = max(min(round(v), self.max), self.min)
        return v
        
    
    def update(self, app):
        self.old_temp_tab = self.temp_tab
        self.temp_tab = app.is_pressed
        if self.is_click:
            self.v = self.get_v()
            
            
        if self.son != None and self.decal:
            play(self.son)
    
    def draw(self):
        rect_t(self.x, self.y, self.tx, self.ty, self.col)
        if self.type == "Horizontal":
            rect_t(self.x, self.y, self.v / (self.max-self.min) * self.tx, self.ty, self.col_barre)
        else:
            rect_t(self.x, self.y, self.tx, self.v / (self.max-self.min) * self.ty, self.col_barre)
        rect_t(self.x, self.y, self.tx, self.ty, self.col_contour, self.taille_contour, self.angle)
        write(self.x_t, self.y_t, self.texte, self.col_texte)
        write(self.x_t, self.y_texte, str(round(self.v, 1)), self.col_texte)

# INPUT

CARRE_H, CERCLE_H = 350, 275
CARRE_B, CERCLE_B = 350, 335

def norme_classic(axe):
    global CARRE_H, CARRE_B, CERCLE_H, CERCLE_B
    if axe[1] >= 0:
        xb = yb = (CARRE_H-CERCLE_H)/CARRE_H * 100
        if abs(axe[0]) <= xb or abs(axe[1]) <= yb:
            return min((axe[0] ** 2 + axe[1] ** 2) ** 0.5, 100)
        else:
            r = ((abs(axe[0]) - xb) ** 2 + (abs(axe[1]) - yb) ** 2) ** 0.5
            rmax = CERCLE_H/CARRE_H * 100
            alpha = math.atan((axe[1] - yb)/(axe[0] - xb))
            t1 = xb + r * math.cos(alpha)
            t2 = yb + r * math.sin(alpha)
            t1max = xb + rmax * math.cos(alpha)
            t2max = yb + rmax * math.sin(alpha)
            n = (t1 ** 2 + t2 ** 2) ** 0.5
            nmax = (t1max ** 2 + t2max ** 2) ** 0.5
            return min(round(100 * n/nmax, 2), 100)
    else:
        xb = yb = (CARRE_B-CERCLE_B)/CARRE_B * 100
        if abs(axe[0]) <= xb or abs(axe[1]) <= yb:
            return min((axe[0] ** 2 + axe[1] ** 2) ** 0.5, 100)
        else:
            r = ((abs(axe[0]) - xb) ** 2 + (abs(axe[1]) - yb) ** 2) ** 0.5
            rmax = CERCLE_B/CARRE_B * 100
            alpha = math.atan((axe[1] - yb)/(axe[0] - xb))
            t1 = xb + r * math.cos(alpha)
            t2 = yb + r * math.sin(alpha)
            t1max = xb + rmax * math.cos(alpha)
            t2max = yb + rmax * math.sin(alpha)
            n = (t1 ** 2 + t2 ** 2) ** 0.5
            nmax = (t1max ** 2 + t2max ** 2) ** 0.5
            return min(round(100 * n/nmax, 2), 100)

def norme_flight(axe):
    x = axe[0]
    y = axe[1]
    max(min(round(n, 1), 100), 0)

def angle(axe):#angle en degrés, non-arrondi
    if axe[0] > 0:
        return math.degrees(math.atan(axe[1]/axe[0]))
    elif axe[0] < 0 and axe[1] > 0:
        return 180 - math.degrees(math.atan(-axe[1]/axe[0]))
    elif axe[0] < 0 and axe[1] < 0:
        return -180 + math.degrees(math.atan(axe[1]/axe[0]))
    elif axe[0] < 0 and axe[1] == 0:
        return 180
    elif axe[0] == 0 and axe[1] != 0:
        return 90 * abs(axe[1])/axe[1]
    else:
        return 0

def angle_torsion_flight(axe):
    pass

def vect_joy_classic(axes):
    g = (axes['JOYSTICK_LH'], axes['JOYSTICK_LV']) # 2 float entre -100 et 100
    d = (axes['JOYSTICK_RH'], axes['JOYSTICK_RV'])
    vg = [norme_classic(g), angle(g)]
    vd = [norme_classic(d), angle(d)]
    return vg, vd

def vect_gachette(axes):
    g = axes['L']
    d = axes['R']
    return g, d

def vect_pos_flight_from_controller_state(state):
    axes = state['Axes']
    
    move = (axes['JOYSTICK_LEFT_RIGHT'], axes['JOYSTICK_FORWARD_BACKWARD'])
    x, y = move
    a_m = angle((x, y))
    
    if x == 0:
        if y != 0:
            n_m = abs(y)
        else:
            n_m = 0
    else:
        if y != 0:
            r1 = abs(100/x)
            r2 = abs(100/y)
            r = min(r1, r2)
            n_m = round(100 / r, 1)
        else:
            n_m = abs(x)
    
    a_pourcent = axes['JOYSTICK_TORSION']
    n_t = abs(a_pourcent)
    sens = 0 if n_t == 0 else a_pourcent/n_t
    a_t = round(a_pourcent * 15/100, 1)
    
    return {'JOY_MOVE' : {'Norme': n_m, 'Angle' : a_m, 'X' : x, 'Y' : y},
                'JOY_TORSION' : {'Angle_Pourcent' : a_pourcent, 'Norme' : n_t, 'Sens' : sens, 'Angle' : a_t},
                'SELECTOR' : axes['SELECTOR']}
    

def correction_vect_pos(vect_pos, old_vect_pos):
    zm = 6
    if old_vect_pos != {}:
        for t in vect_pos:
            v = vect_pos[t]
            if t not in ['L', 'R']:
                v1, v2, v3, v4 = v['Norme'], v['Angle'], v['X'], v['Y']
                if abs(v1 - old_vect_pos[t]['Norme']) < zm and v1 != 0:
                    vect_pos[t]['Norme'] = old_vect_pos[t]['Norme']
                if abs(v2 - old_vect_pos[t]['Angle']) < zm and v2 != 0:
                    vect_pos[t]['Angle'] = old_vect_pos[t]['Angle']
                if abs(v1 - old_vect_pos[t]['X']) < zm and v3 != 0:
                    vect_pos[t]['X'] = old_vect_pos[t]['X']
                if abs(v2 - old_vect_pos[t]['Y']) < zm and v4 != 0:
                    vect_pos[t]['Y'] = old_vect_pos[t]['Y']
            else:
                pass
    return vect_pos

#RUMBLE
def rumble(joy, low, high, t):
    joy.rumble(low / 100, high / 100, t * 1000)# t en sec, low et high en pourcentage
def stop_rumble(joy):
    joy.stop_rumble()
def powerstate(joy):
    return joy.get_power_level()

def coord_pad(x, y):
    nx, ny = x - 0.5, y - 0.5
    return (round(nx * 200, 1), round(ny * 200, 1))

def get_ind_manette(jid, dic):
    for e in dic:
        if dic[e].get_instance_id() == jid:
            return e

def pad_depl(d, init, pos):# doigt d
    dx = pos[d][0] - init[d][0]
    dy = pos[d][1] - init[d][1]
    return (dx, dy)

def pad_zoom(init, pos):
    if pos[0] != None and pos[1] != None:
        dx1 = init[0][0] - init[1][0]
        dy1 = init[0][1] - init[1][1]
        d1 = (dx1 ** 2 + dy1 ** 2) ** 0.5
        dx2 = pos[0][0] - pos[1][0]
        dy2 = pos[0][1] - pos[1][1]
        d2 = (dx2 ** 2 + dy2 ** 2) ** 0.5
        return round(d2/d1, 2)
    else:
        return None

def get_type_player(dic):
    k = 'Keys' in dic and dic['Keys'] != [] and dic['Keys'] != None
    c = 'Controller' in dic and dic['Controller'] != None
    m = 'Mouse' in dic and dic['Mouse'] != None and dic['Mouse'] != {}
    if c and not k and not m:
        return 'Controller'
    elif k and not c and not m:
        return 'Keyboard'
    elif k and m and not c:
        return 'Keyboard + Mouse'
    elif m and not k and not c:
        return 'Mouse'
    elif c and k and not m:
        return 'Keyboard + Controller'
    elif c and m and not k:
        return 'Controller + Mouse'
    elif c and m and k:
        return 'Mix'
    else:
        return 'Spectator'

def verif_touches_player(t, a):
    rep = []
    for c in a:
        if c in t:
            rep.append(c)
    return rep

# self.type_players = {1 : 'Controller', 2 : 'Keyboard', 3 : 'Keyboard + Mouse', 4 : 'Mix', 5 : 'Mouse'}
# {'Keys' : [], 'Controller' : n_joy, 'Mouse' : {'Coord' : self.coord_mouse, 'Wheel' : self.mouse_wheel}}
# self.touches_players = {1 : n_joy, 2 : [touches_clavier], 3 : {'key' : [touches_clavier]}} n_joy : id_manette
# self.players = {1 : {'is_pressed' : self.btn_is_pressed[n_joy], 'is_pushed', 'is_released',
#                      'Axes' : self.vect_pos[n_joy], 'Pad' : {'Init' : self.pad_init[], 'Pos' : self.pad_pos[]}}
#				  2 : {'is_pressed' : verif_touches_joueurs(self.is_pressed, self.touches_players['Keys'])}}
# self.players_name = {1 : 'ArtandProFr'}

def change_pseudo(app, i, t):
    if i in app.players_name:
        app.players_name[i] = t
        c = app.touches_players[i]['Controller']
        if c != None:
            for m in app.controllers:
                if c == app.controllers[m].get_instance_id():
                    if t != 'Player n°{}'.format(i):
                        app.nom_controllers[m] = 'Player n°{} : {}'.format(i, t)
                    else:
                        app.nom_controllers[m] = 'Player n°{}'.format(i)

def assigner_manette(app, i, c):
    for p in app.touches_players:
        if app.touches_players[p]['Controller'] == app.controllers[c].get_instance_id() and p != i:
            app.touches_players[p]['Controller'] = None
    if i in app.players:
        app.touches_players[i]['Controller'] = app.controllers[c].get_instance_id()

def get_player_associated(jid, t, pseudo):
    text = ''
    for p in t:
        if t[p]['Controller'] == jid:
            text = 'Player n°{}'.format(p)
            if text != pseudo[p]:
                text += ' : {}'.format(pseudo[p])
    if text == '':
        text = 'Controller n°{}'.format(jid)
    return text

def info(message, title = 'Info',boutons = ('Ok',), preselect = 0):
    return pygame.display.message_box(title, message, 'info', buttons = boutons, return_button = preselect)
def warn(message, title = 'Warning', boutons = ('Continue', 'Cancel'), preselect = 0):
    return pygame.display.message_box(title, message, 'warn', buttons = boutons, return_button = preselect)
def error(message, title = 'Error', boutons = ('Continue', 'See more'), preselect = 0):
    return pygame.display.message_box(title, message, 'error', buttons = boutons, return_button = preselect)

def ajouter_ecran(app, num = 1, pos = 'DROITE'):
    app.screens_info[num] = pos

def get_info_joystick(j):
    print('Joystick n°{} / Name : {} / Axes : {} / Balls : {} / Buttons : {} / Hats : {}'.format(j.get_instance_id(), j.get_name(), j.get_numaxes(), j.get_numballs(), j.get_numbuttons(), j.get_numhats()))

def get_vect_pos(type_c):
    if type_c in ['XBOX', 'PS4']:
        return {'JOY_L': {'Norme': 0.0, 'Angle' : 0, 'X' : 0, 'Y' : 0}, 'JOY_R': {'Norme': 0.0, 'Angle' : 0, 'X' : 0, 'Y' : 0}, 'L': 0.0, 'R': 0.0}
    elif type_c in ['Thrustmaster Joystick']:
        return {'JOY_MOVE' : {'Norme': 0.0, 'Angle' : 0, 'X' : 0, 'Y' : 0}, 'JOY_TORSION' : {'Angle_Pourcent' : 0.0, 'Norme' : 0.0, 'Sens' : 0, 'Angle' : 0.0}, 'SELECTOR' : 0.0}
    else:
        return {'0' : {'Norme': 0.0, 'Angle' : 0, 'X' : 0, 'Y' : 0}, '1' : {'Norme': 0.0, 'Angle' : 0, 'X' : 0, 'Y' : 0}}

def coeff_dir(image, comparateur, ecart):
    return (image - comparateur)/ecart

def derivee(f, a, dt = 10**(-10), n = 1):
    if n == 0:
        return f(a)
    else:
        return coeff_dir(derivee(f, a+dt, dt, n - 1), derivee(f, a, dt, n - 1),dt)

def tangente(f, a, x):
    return f(a) + derivee(f, a) * (x-a)

def contenu_dossier(repertoire):
    dossiers = [f for f in os.listdir(repertoire) if os.path.isdir(os.path.join(repertoire, f))]
    fichiers = [f for f in os.listdir(repertoire) if os.path.isfile(os.path.join(repertoire, f))]
    D = {'Files' : fichiers, 'Folders' : dossiers}
    return D

def rise(initial, new):
    return (not initial) and new
def fall(initial, new):
    return initial and (not new)

class Textfile:
    def __init__(self, name = "New_file_from_python", path = "", txt = None):
        self.name = name
        path += self.name + '.txt'
        self.path = path
        if self.actual_text == ("No file existing"):
            with open(self.path, 'w'):
                pass
        if txt != None:
            self.rewrite(txt)
        self.text = self.actual_text
    def rewrite(self, txt = ""):
        with open(self.path, 'w') as file:
            file.write(txt)
        self.mise_a_jour()
    def write(self, txt):
        with open(self.path, 'r') as file:
            old = file.read()
        with open(self.path, 'w') as file:
            file.write(old + txt)
        self.mise_a_jour()
    def clear(self):
        self.rewrite()
    @property
    def actual_text(self):
        try:
            with open(self.path, 'r') as file:
                t = file.read()
            return t
        except:
            return ("No file existing")
    @property
    def a_jour(self):
        return self.actual_text == self.text
    def mise_a_jour(self):
        self.text = self.actual_text
    def update(self):
        if not self.a_jour:
            self.mise_a_jour()

class File:
    def __init__(self, name = "New_file_from_python", format = "", folder = "", txt = None):
        self.name = name
        self.format = format.lstrip(".")
        if isinstance(folder, str):
            s = folder.split("/")
            if s[-1] != "":
                name = s[-1]
                f = 1
            else:
                name = s[-2]
                f = 2
            nv_path = ""
            for i in range(len(folder) - f):
                e = s[i]
                nv_path += e + "/"
            self.folder = Folder(name = path.split, path=Path(nv_path))
        else:
            self.folder = folder
        
        if self.actual_text == ("No file existing"):
            with open(self.file_path, 'w'):
                pass
        if txt != None:
            self.rewrite(txt)
        self.text = self.actual_text
    @property
    def file_path(self):
        if not isinstance(self.name, str):
            self.name = self.name[0]
        return self.folder_path + self.name + "." + self.format
    @property
    def folder_path(self):
        return self.folder.chemin_full
    def rewrite(self, txt = ""):
        with open(self.file_path, 'w') as file:
            file.write(txt)
        self.mise_a_jour()
    def write(self, txt):
        with open(self.file_path, 'r') as file:
            old = file.read()
        with open(self.file_path, 'w') as file:
            file.write(old + txt)
        self.mise_a_jour()
    @property
    def actual_text(self):
        try:
            with open(self.file_path, 'r') as file:
                t = file.read()
            return t
        except:
            return ("No file existing")
    def clear(self):
        self.rewrite()
    @property
    def a_jour(self):
        return self.actual_text == self.text
    def mise_a_jour(self):
        self.text = self.actual_text
    def update(self):
        if not self.a_jour:
            self.mise_a_jour()
    def rename(self, nv_nom):
        nv = self.folder_path+nv_nom
        os.rename(self.file_path, nv)
        self.name = nv_nom
    def copier(self, file):
        self.format = file.format
        self.rename(file.name)
        self.rewrite(file.text)
        self.folder.suppr_vide(noms = [self.name])
    def supprimer(self):
        self.rewrite()
        self.folder.suppr_vide(noms = [self.name])
    def dupliquer(self):
        new = copy.deepcopy(self)
        new.name += "_Copie"
        new.rewrite(new.text)
    def cloner(self, folder):
        new = copy.deepcopy(self)
        new.folder = folder
        new.rewrite(new.text)

class Folder:
    def __init__(self, name = "New_Folder_From_Python", path = "", admin_code = None):
        self.name = name
        self.path = Path(path)
        self.already_existing = False
        self.admin_code = admin_code
        self.last_error = ""
        try:
            os.mkdir(self.chemin+name)
            if name == "":
                self.path.change_path(path = self.chemin)
            else:
                self.path.change_path(path = self.chemin+name+"/")
        except:
            self.last_error = "Folder already exists"
            self.already_existing = True
        if name == "":
            self.name = self.path.last
            self.path.back()
        
    @property
    def chemin(self):
        return self.path.path
    @property
    def chemin_full(self):
        return self.chemin+self.name+"/"
    def delete(self, code = None):
        if self.admin_code == None or code == self.admin_code:
            try:
                os.rmdir(self.chemin_full)
                #shutil.rmtree(self.path+self.name)
            except:
                self.last_error = "No such file or WinError5 no admin rights"
    def clear(self):
        for f in self.contenu["Files"]:
            os.remove(self.chemin_full+f)
        for folder in self.contenu["Folders"]:
            nv_f = Folder(folder, path = self.chemin_full)
            nv_f.clear()
            nv_f.delete()
    
    def suppr_vide(self, noms = None):
        for f in self.contenu["Files"]:
            #if get_format(f) == "" and get_file_size(self.chemin_full+f) == 0 and (noms == None or f in noms):
            if get_file_size(self.chemin_full+f) == 0:
                os.remove(self.chemin_full+f)
    
    def rename(self, nv_nom):
        nv = self.path.path+nv_nom
        os.rename(self.chemin_full, nv)
        self.name = nv_nom
    @property
    def contenu(self):
        return contenu_dossier(self.chemin_full)
    @property
    def architecture(self):
        d = {}
        d["Files"] = self.contenu["Files"]
        d["Folders"] = {}
        for f in self.contenu["Folders"]:
            d["Folders"][f] = Folder(f, path = self.chemin_full).architecture
        return d

def get_file_size(path):
    if isinstance(path, Path):
        path = path.path
    return os.path.getsize(path)

class Path:
    def __init__(self, path = "", search = "", local = "", force = False):
        if force:
            self.path = path
        else:
            self.init_path(path, search, local)
    
    def init_path(self, path = "", search = "", local = ""):
        original_path = path.replace("\\", "/")
        s = False
        if not ":" in path and path != "":
            search = __file__
            s = True
        if search != "":
            # local = "5_Alive", search = "Jeu.py"
            path = str(pathlib.Path(local+"/"+search).resolve()).replace("\\", "/")
            if s:
                c = path.split("/")
                nb = 0
                while original_path not in c[-1] and nb < 10:
                    nb += 1
        self.path = path
        c = self.path.split("/")
        self.path = self.ecrire_chemin(c)
        
        if "." in c[-1]: # On suppose tout "dossiers" contenant un point étant un fichier (permet de ne pas cibler un fichier)
            c.pop(-1)
            self.path = self.ecrire_chemin(c)
        
    
    @property
    def source(self):
        t = self.path.split("/")
        t.pop(-1)
        return self.ecrire_chemin(t)
    
    def back(self):
        t = self.path.split("/")
        if t[-1] == "":
            t.pop(-1)
        t.pop(-1)
        self.path = self.ecrire_chemin(t)
    
    def change_path(self, new_path = "", search = "", local = ""):
        self.init_path(path, search, local)
    
    def ecrire_chemin(self, tab):
        if "" in tab:
            n = tab.count("")
            for i in range(n-1):
                tab.remove("")
        c = ""
        if tab[0][-1] != ":":
            new_tab = [tab[0][:tab[0].index(":")+1]] + [""] + [tab[0][tab[0].index(":")+1:]]
            for i in range(1, len(tab)):
                e = tab[i]
                new_tab.append(e)
        else:
            new_tab = tab
            if tab[1] != "":
                tab.insert(1, "")
        tab = new_tab
        while tab[-1] == "" or tab[-1] == tab[-2]:
            tab.pop(-1)     
        for i in range(len(tab)):
            e = tab[i]
            c += e
            if ("." not in e):
                c += "/"
        return c
    @property
    def last(self):
        c = self.path.split("/")
        if c[-1] != "":
            return c[-1]
        else:
            return c[-2]

def get_format(titre_doc:str):
    if "." in titre_doc:
        return titre_doc[titre_doc.index("."):]
    else:
        return ""

def lerp(a, b, t):
    return a + (b-a) * t

def lerp_value(p1, p2, t):
    ax, ay = p1
    bx, by = p2
    return ax + (bx-ax) * t, ay + (by-ay) * t

def dist2(a, b):
    return ((a[0]-b[0])**2 + (a[1]-b[1])**2)**0.5

class Courbe_Bezier:
    def __init__(self, points : list = []):
        self.points = points
        self.modifier = True
        self.pos = {}
        self.longueur = None
        self.partition = {}
    def ajouter_point(self, point, i = None):
        if i == None:
            self.points.append(point)
        elif isinstance(i, int) and i>=0 and i <= len(self.points):
            if i == len(self.points):
                self.points.append(point)
            else:
                self.points.insert(i, point)
    def supprimer_point(self, i = None):
        if i == None and len(self.points) > 0:
            self.points.pop(-1)
        elif isinstance(i, int) and i>=0 and i <= len(self.points):
            self.points.pop(i)
    def bouger_point(self, i, nv_point):
        if i < len(self.points):
            self.points[i] = nv_point
    def get_coord(self, t, relative = False):
        if len(self.points)>1:
            if not relative:
                if not self.modifier:
                    if not t in self.pos:
                        self.pos[t] = self.rec_pos(copy.deepcopy(self.points), t)
                    return self.pos[t]
                else:
                    return self.rec_pos(copy.deepcopy(self.points), t)
            else:
                if not self.modifier:
                    if not t in self.pos:
                        dmin = None
                        pimin = None
                        for pi in self.partition:
                            p = pi/self.longueur
                            if pimin == None:
                                pimin = pi
                                dmin = (abs(t - p))
                            else:
                                if abs(t - p) < dmin:
                                    pimin = pi
                                    dmin = abs(t - p)
                        self.pos[t] = self.partition[pimin]
                    return self.pos[t]
        else:
            return self.points[0]

    def tangente(self, t, relative = False):
        pas = 0.00001
        p1 = self.get_coord(t, relative = relative)
        p2 = self.get_coord(t+pas, relative = relative)
        dist = dist2(p1, p2)
        d = [(p2[0]-p1[0])/dist, (p2[1]-p1[1])/dist]
        return d
    
    def normale(self, t, relative = False):
        x,y = tangente(t, relative = relative)
        return [-y, x]

    def initialiser_courbe(self, precision, pas):
        self.pos = {}
        self.longueur = 0
        self.partition = {}
        self.partition[0] = self.get_coord(0.0)
        for t in range(0, precision - pas, pas):
            p1 = self.get_coord(t/precision)
            p2 = self.get_coord((t+pas)/precision)
            d = dist2(p1, p2)
            self.longueur += d
            self.partition[self.longueur] = p2
            self.get_coord(t/precision)
    def decharger_courbe(self):
        self.pos = {}
        self.longueur = 0
        self.partition = {}
        

    def rec_pos(self, points, t):
        if len(points) == 1:
            return points[0]
        else:
            nv_points = []
            for i in range(len(points)-1):
                nv_points.append(lerp_value(points[i], points[i+1], t))
            return self.rec_pos(nv_points, t)
    def get_selection(self, coord, rayon):
        xm, ym = coord
        for i in range(len(self.points)):
            p = self.points[i]
            xp, yp = p
            if ((xm - xp) ** 2 + (ym - yp) ** 2)**0.5 < rayon:
                return i

class Circuit:
    def __init__(self, courbes : list = []):
        self.courbes = courbes
        self.modifier = True
        self.longueur = 0
        self.pos = {}
    def set_modifier(self, valeur):
        self.modifier = valeur
        for c in self.courbes:
            c.modifier = valeur
    def ajouter_section(self, courbe, i = None):
        if i == None:
            self.courbes.append(courbe)
            if len(self.courbes) > 1:
                self.courbes[-1].points[0] = self.courbes[-2].points[-1]
        elif isinstance(i, int) and i>=0 and i <= len(self.courbes):
            self.courbes.insert(i, )
    def supprimer_section(self, i):
        self.courbes.pop(i)
    def get_coord(self, t, section = None, relative = False):
        if len(self.courbes) > 0:
            if not relative:
                return self.courbes[section].get_coord(t)
            else:
                if not self.modifier:
                    if not t in self.pos:
                        nv_t = (t%1)*self.longueur
                        seg = 0
                        while seg < len(self.partition)-1 and (nv_t >= sorted(self.partition)[seg]):
                            seg += 1
                        maxi = sorted(self.partition)[seg]
                        c = self.partition[maxi]
                        if seg > 0:
                            av = sorted(self.partition)[seg-1]
                        else:
                            av = 0
                        if c.longueur != 0:
                            self.pos[t] = c.get_coord((nv_t-av)/c.longueur, relative = True)
                        else:
                            self.pos[t] = c.get_coord(0)
                    return self.pos[t]
    @property
    def est_ferme(self):
        return len(self.courbes) > 1 and self.courbes[0].points[0] == self.courbes[-1].points[-1]
    def fermer(self):
        if len(self.courbes) > 1:
            self.courbes[-1].points[-1] = self.courbes[0].points[0]
    def trier_courbes(self):
        d = 0
        for i in range(len(self.courbes)):
            j = len(self.courbes) - i - 1
            if self.courbes[j].points[0] != self.courbes[j-1].points[-1]:
                d = j
        nv_tab = []
        for i in range(len(self.courbes)):
            nv_tab.append(self.courbes[(d+i)%len(self.courbes)])
        self.courbes = nv_tab
    def initialiser_circuit(self, precision, pas):
        self.partition = {}
        self.longueur = 0
        for c in self.courbes:
            c.initialiser_courbe(precision, pas)
            self.longueur += c.longueur
            self.partition[self.longueur] = c
    def decharger_circuit(self):
        for c in self.courbes:
            c.decharger_courbe()
        self.partition = {}
        self.longueur = 0
        self.pos = {}

def repartir(r, g, b, reste):
    if reste == 0:
        return r, g, b
    else:
        mr = 255-r
        mg = 255-g
        mb = 255-b
        tr = min(random.randint(0, mr), reste)
        r += tr
        reste -= tr
        tg = min(random.randint(0, mg), reste)
        g += tg
        reste -= tg
        tb = min(random.randint(0, mb), reste)
        b += tb
        reste -= tb
        return repartir(r, g, b, reste)

def get_random_color(brightness = None):
    r = random.randint(0, 255)
    g = random.randint(0, 255)
    b = random.randint(0, 255)
    if isinstance(brightness, int) and brightness >= 0 and brightness < 766:
        r = random.randint(0, brightness//3)
        g = random.randint(0, brightness//3)
        b = random.randint(0, brightness//3)
        s = brightness - r - g - b
        r, g, b = repartir(r, g, b, s)
    c = col_hex(r, g, b)
    return c

DEFAULT_NOFRAME_TRANSPARENCY = None
HWND = None
def transparence(app, color):
    global DEFAULT_NOFRAME_TRANSPARENCY, HWND
    HWND = pygame.display.get_wm_info()["window"]
    if color != None:
        DEFAULT_NOFRAME_TRANSPARENCY = app.noframe
        app.noframe = True
        #set_mode("NOFRAME")
        # Définir une couleur comme transparente (par exemple le magenta)
        c = rgb(color)
        # Windows seulement
        if sys.platform == "win32":
            current_ex_style = win32gui.GetWindowLong(HWND, win32con.GWL_EXSTYLE)
            new_style = current_ex_style | win32con.WS_EX_LAYERED
            # IMPORTANT: Ne PAS ajouter WS_EX_TRANSPARENT (sinon les clics passent à travers)
            new_style = new_style & ~win32con.WS_EX_TRANSPARENT
            win32gui.SetWindowLong(HWND, win32con.GWL_EXSTYLE, new_style)
        
            # Appliquer la transparence de couleur
            color_key = c[0] | (c[1] << 8) | (c[2] << 16)
            win32gui.SetLayeredWindowAttributes(HWND, color_key, 0, win32con.LWA_COLORKEY)
        
            # Mettre la fenêtre en topmost pour qu'elle reste au-dessus
            win32gui.SetWindowPos(HWND, win32con.HWND_TOPMOST, 0, 0, 0, 0, win32con.SWP_NOMOVE | win32con.SWP_NOSIZE | win32con.SWP_SHOWWINDOW)
            
    else:
        app.noframe = DEFAULT_NOFRAME_TRANSPARENCY
        DEFAULT_NOFRAME_TRANSPARENCY = None
        # Retirer WS_EX_LAYERED
        current_ex_style = win32gui.GetWindowLong(HWND, win32con.GWL_EXSTYLE)
        new_style = current_ex_style & ~win32con.WS_EX_LAYERED
        win32gui.SetWindowLong(HWND, win32con.GWL_EXSTYLE, new_style)
        
        # IMPORTANT: Retirer le topmost pour que les autres fenêtres puissent être cliquées
        win32gui.SetWindowPos(HWND, win32con.HWND_NOTOPMOST, 0, 0, 0, 0, win32con.SWP_NOMOVE | win32con.SWP_NOSIZE | win32con.SWP_SHOWWINDOW)

def force_focus():
    """Force le focus de manière plus agressive en contournant les restrictions Windows"""
    global HWND
    if sys.platform == "win32" and win32gui and HWND:
        try:
            # Obtenir le thread de la fenêtre en premier plan
            foreground_hwnd = win32gui.GetForegroundWindow()
            if foreground_hwnd == HWND:
                return  # On a déjà le focus
            
            # Obtenir les IDs de thread
            foreground_thread_id = win32process.GetWindowThreadProcessId(foreground_hwnd)[0]
            current_thread_id = win32process.GetWindowThreadProcessId(HWND)[0]
            
            # Attacher notre thread au thread de la fenêtre en premier plan
            # Cela permet de contourner les restrictions de SetForegroundWindow
            if foreground_thread_id != current_thread_id:
                ctypes.windll.user32.AttachThreadInput(current_thread_id, foreground_thread_id, True)
                win32gui.SetForegroundWindow(HWND)
                win32gui.SetFocus(HWND)
                ctypes.windll.user32.AttachThreadInput(current_thread_id, foreground_thread_id, False)
            else:
                win32gui.SetForegroundWindow(HWND)
                win32gui.SetFocus(HWND)
                
        except Exception as e:
            # Fallback simple si ça échoue
            try:
                win32gui.SetForegroundWindow(HWND)
            except:
                pass

def get_key_bg(key):
    """Convertit une touche pynput en nom"""
    global DICT_KEYBOARD_BG
    # 1. Vérifier si c'est une touche spéciale (Key.xxx)
    if key in DICT_KEYBOARD_BG:
        return DICT_KEYBOARD_BG[key]
    
    # 2. Vérifier si c'est un KeyCode avec VK
    if isinstance(key, KeyCode) and hasattr(key, 'vk') and key.vk:
        if key.vk in DICT_KEYBOARD_BG:
            return DICT_KEYBOARD_BG[key.vk]
    
    # 3. Vérifier si c'est un caractère
    if hasattr(key, 'char') and key.char:
        if key.char in DICT_KEYBOARD_BG:
            return DICT_KEYBOARD_BG[key.char]
        # Sinon retourner le caractère en majuscules
        return key.char.upper()
    
    # 4. Par défaut, retourner la représentation str
    return str(key)

PRESSED = []
def on_press(key):
    """Cette fonction est appelée à CHAQUE touche pressée, même sans focus"""
    global PRESSED
    k = get_key_bg(key)
    if k not in PRESSED:
        PRESSED.append(k)

def on_release(key):
    global PRESSED
    k = get_key_bg(key)
    while k in PRESSED:
        PRESSED.remove(k)

MOUSE_WHEEL = 0

# Callbacks pour la souris
def on_mouse_click(x, y, button, pressed):
    """Appelé quand un bouton de souris est pressé ou relâché"""
    global PRESSED
    button_name = DICT_MOUSE_BG.get(button, str(button))
    if pressed:
        PRESSED.append(button_name)
    else:
        if button_name in PRESSED:
            PRESSED.remove(button_name)

def on_mouse_scroll(x, y, dx, dy):
    """Appelé quand la molette est utilisée"""
    global MOUSE_WHEEL, PRESSED
    while 'MOUSE_WHEEL_UP' in PRESSED:
        PRESSED.remove('MOUSE_WHEEL_UP')
    while 'MOUSE_WHEEL_DOWN' in PRESSED:
        PRESSED.remove('MOUSE_WHEEL_DOWN')
    
    MOUSE_WHEEL = dy
    
    if dy > 0:
        PRESSED.append('MOUSE_WHEEL_UP')
    elif dy < 0:
        PRESSED.append('MOUSE_WHEEL_DOWN')

def on_mouse_move(x, y):
    """Appelé quand la souris bouge (optionnel, peut être très verbeux)"""
    # Tu peux utiliser cette fonction si tu veux tracker le mouvement
    pass

class App:
    def __init__(self, init_f, update_f, draw_f, w, h, fps, title, font = None, font_size = 30, background = 'Black', icon = None, fullscreen = False, onQuit_f = None, display = 0, touches_players = {}, correction_joy = False, auto_drawing = True, camera = False):
        self.screen, self.clock = self.background_init_start(w, h)
        self.font = font
        self.font_size = font_size
        self.w = w
        self.h = h
        self.base_tx = w
        self.base_ty = h
        self.camera_on = camera
        self.icon = icon
        self.title = title
        self.old_title = title
        init(self.w, self.h, self.title, self.font_size, self.font, camera = self.camera_on, icon = self.icon)
        self.running = True
        
        self.fps = fps
        self.real_fps = 0
        self.real_t1 = 0
        self.real_t2 = 0
        self.time = 0
        self.background = background
        self.fullscreen = fullscreen
        
        self.controllers = {}
        self.nom_controllers = {}
        self.type_controllers = {}
        
        self.input = ''
        self.is_pressed = []
        self.is_pushed = []
        self.was_pressed = []
        self.is_released = []
        self.axes = {}
        self.hats = {}
        self.btn_is_pressed = {}
        self.btn_was_pressed = {}
        self.btn_is_pushed = {}
        self.btn_is_released = {}
        self.vect_pos = {}
        self.old_vect_pos = {}
        self.controller_state = {}
        self.correction_joy = correction_joy
        self.pad_initial = {}
        self.pad_pos = {}#{jid : {d1 : {x, y, norme, angle}}}, d1 : None si pas appuyé
        self.btn_played = {}
        
        self.mouse_wheel = 0
        self.debug_selector = {}
        self.v_sel_init = {}
        
        self.window_visible = True
        self.window_focus = True
        self.cursor_on_window = None
        self.player_is_active = None
        self.position_fenetre = [0, 0]
        self.display = 0
        self.window_size = [self.w, self.h]
        self.resizable = False
        self.noframe = False
        self.fake_fullscreen = True
        self.bigscreen = False
        
        self.touches_players = touches_players
        self.type_players = {}
        self.players = {}
        self.players_name = {}
        self.screens_info = {0 : 'CENTRE'}
        self.static_display = display
        def_pos = ['DROITE', 'GAUCHE', 'HAUT', 'BAS']
        for s in range(WINDOW.get_num_displays()):
            if s != 0:
                ajouter_ecran(self, s, def_pos[s-1])
        self.auto_drawing = auto_drawing
        self.refresh = True
        
        self.t = 0
        self.t2 = time.time() - 1/self.fps
        self.dt = 1/self.fps
        self.real_time_init = time.time()
        
        self.real_t1 = self.real_t2
        self.real_t2 = time.time()
        self.real_dt = max(self.real_t2-self.real_t1, 0.0000000001)
        self.real_fps = 1/self.real_dt
        
        self.var = {}
        self.transparent = False
        self.old_key_bg = False
        self.key_bg = False
        self.start_key_bg = False
        self.stop_key_bg = False
        
        if self.camera_on:
            integrated = pygame.camera.list_cameras()[0]
            self.camera = pygame.camera.Camera(integrated, (640, 480), 'RGB')
            #self.camera = pygame.camera.Camera(0)
            #self.camera.start()
            #self.image_camera = pygame.surface.Surface((640, 480))
        else:
            self.camera = None
            #self.image_camera = None
        
        self.init_function = init_f
        self.init_function(self)
        
        for p in self.touches_players:
            self.type_players[p] = get_type_player(self.touches_players[p])
            self.players_name[p] = 'Player n°'+str(p)
            self.players[p] = {'is_pressed' : [], 'is_pushed' : [],
                               'is_released' : [], 'Axes' : {},
                               'Pad' : {}, 'Mouse' : {'Coord' : (0, 0), 'Wheel' : 0}}
        
        self.update_function = update_f
        self.draw_function = draw_f
        self.onQuit_function = onQuit_f
        
        while self.running:
            self.update()
            self.draw()
        pygame.quit()
    
    def background_init_start(self, w, h):
        pygame.init()
        return pygame.display.set_mode((w, h)), pygame.time.Clock()
    
    def background_update(self):
        global DICT_KEYS, PRESSED, MOUSE_WHEEL
        self.input = ''
        self.t = self.t2
        self.t2 = time.time()
        self.dt = self.t2 - self.t
        self.mouse_wheel = 0
        self.mouse_x, self.mouse_y = self.mouse_coord = get_mouse_pos(self.noframe)
        try:
            self.player_is_active = False
            l = pygame.event.get()
            if l != []:
                for event in l:
                    if event.type == pygame.QUIT:
                        self.running = False
                        if self.onQuit_function != None:
                            self.onQuit_function(self)
                    
                    elif event.type == pygame.JOYDEVICEADDED:
                        # This event will be generated when the program starts for every
                        # joystick, filling up the list without needing to create them manually.
                        joy = pygame.joystick.Joystick(event.device_index)
                        get_info_joystick(joy)
                        #jid = joy.get_instance_id()
                        jid = get_new_controller_id(self.controllers)
                        self.controllers[jid] = joy
                        self.nom_controllers[jid] = get_player_associated(jid, self.touches_players, self.players_name)
                        self.type_controllers[jid] = get_type_controller(joy)
                        if self.type_controllers[jid] == "Unknown":
                            del self.controllers[jid]
                            self.type_controllers[jid]
                        else:
                            self.vect_pos[jid] = get_vect_pos(self.type_controllers[jid])
                            self.controller_state[jid] = {'Buttons' : None, 'Axes' : None, 'Hats' : None}
                            self.btn_played[jid] = []
                            self.btn_is_pressed[jid] = []
                            self.btn_was_pressed[jid] = []
                            self.btn_is_pushed[jid] = []
                            self.btn_is_released[jid] = []
                            self.pad_pos[jid] = {0 : None, 1 : None}
                            self.pad_initial[jid] = {0 : None, 1 : None}
                            if self.type_controllers[jid] in ['Thrustmaster Joystick']:
                                self.debug_selector[jid] = True
                            else:
                                self.debug_selector[jid] = False
                            self.v_sel_init[jid] = joy.get_axis(3)
                            print('{} / {} connected'.format(self.nom_controllers[jid], self.type_controllers[jid]))
                    elif event.type == pygame.JOYDEVICEREMOVED:
                        jid = event.instance_id
                        del self.controllers[jid]
                        print('{} / {} disconnected'.format(self.nom_controllers[jid], self.type_controllers[jid]))
                        del self.nom_controllers[jid]
                        del self.type_controllers[jid]
                        del self.vect_pos[jid]
                        del self.controller_state[jid]
                        del self.vect_pos[jid]
                        del self.old_vect_pos[jid]
                        del self.btn_played[jid]
                        del self.btn_is_pressed[jid]
                        del self.btn_was_pressed[jid]
                        del self.btn_is_pushed[jid]
                        del self.btn_is_released[jid]
                        del self.pad_initial[jid]
                        del self.pad_pos[jid]
                        del self.debug_selector[jid]
                        del self.v_sel_init[jid]
                    
                    elif event.type == pygame.MOUSEWHEEL:
                       self.mouse_wheel = event.y
                    
                    elif event.type == 32785:#Fenetre visible
                        self.window_focus = True
                    elif event.type == 32786:#Fenetre invisible / perte de focus
                        self.window_focus = False
                        
                    elif event.type == 32783:#Curseur qui revient sur la fenetre
                        self.cursor_on_window = True
                    elif event.type == 32784:#Curseur qui sort de la fenetre
                        self.cursor_on_window = False
                    
                    elif event.type == 1024 and self.cursor_on_window == None:
                        # 1024 : mouvement de souris (curseur sur la fenetre)
                        self.cursor_on_window = True
                    
                    elif event.type == 32777:# Fenetre déplacée sur l'écran
                        self.position_fenetre = [event.x, event.y]
                        
                    #fonction de finger_id 0 ou 1, premier à etre mis ou deuxieme
                    elif event.type == 1622:
                        m = get_ind_manette(event.instance_id, self.controllers)
                        self.pad_initial[m][event.finger_id] = coord_pad(event.x, event.y)
                        self.pad_pos[m][event.finger_id] = copy.deepcopy(self.pad_initial[m][event.finger_id])
                    elif event.type == 1623:#Déplacements sur PAD
                        m = get_ind_manette(event.instance_id, self.controllers)
                        self.pad_pos[m][event.finger_id] = coord_pad(event.x, event.y)
                    elif event.type == 1624:
                        m = get_ind_manette(event.instance_id, self.controllers)
                        self.pad_initial[m][event.finger_id] = None
                        self.pad_pos[m][event.finger_id] = None
                    
                    
                    
                    elif event.type == 32780:
                        self.window_visible = False
                    elif event.type == 32782:
                        self.window_visible = True
                    
                    elif event.type == 32779:
                        self.window_size = [event.x, event.y]
                        
                    elif event.type == 32791:
                        self.display = event.display_index
                    elif event.type == 771:
                        self.input += event.text
                        
                        
                    elif event.type in [1024, 32768, 770, 32770, 32776, 32774, 4352, 4353, 771, 32787, 1536, 1543, 32769, 32778, 32769, 32775, 336, 32781, 1538, 2304] or 'key' in event.dict or 'button' in event.dict:
                        #
                        # 1024 : mouvement souris
                        # 32774 : fenetre affiché (éq : début du programme)
                        # 32787 : fenetre fermée (éq : fin du programme)
                        # 4352 : Périphérique audio ajouté
                        # 4353 : Périphérique audio supprimé
                        # 771 : Entrée de texte
                        # key / button : correspond aux entrées clavier etc...
                        #
                        # -----------------------------------------------------
                        #
                        # 32768 : etats de fenetre (pas utile)
                        # 32781 : fenetre agrandie au max
                        # 770 : edition de texte (pas utile)
                        # 32770 : video exposé (pas utile)
                        # 32776 : ecran exposé (pas utile)
                        # 1536 : Joystick en mouvement
                        # 1543, 336 : Inconnu
                        # 2304 : clipboard update
                        pass
                    else:
                        print('EVENT', event.type, event)
                    '''
                    elif event.type == 1538:
                        m = get_ind_manette(event.instance_id, self.controllers)
                        for i in range(len(event.value)):
                            v = DICT_HATS[self.type_controllers[m]][i][event.value[i]]
                            if not isinstance(v, list) and v not in self.btn_played[m]:
                                self.btn_played[m].append(v)
                            else:
                                for e in v:
                                    if e in self.btn_played[m]:
                                        self.btn_played[m].remove(e)
                    '''
        except:
            print('Error : Events ,', pygame.event.get())
            
        if [self.w, self.h] != self.window_size:
            self.w, self.h = self.window_size
            x, y = screen_pos(self.static_display, self.screens_info)
            set_window(self.w, self.h, coord = (x, y), title = WINDOW.get_caption(), icon = self.icon, mode = ["RESIZABLE"])
        self.clock.tick(self.fps) #limite les FPS à 60
        self.time += 1
        self.time_sec = self.time // self.fps
        self.real_time = self.t - self.real_time_init
        
        self.start_key_bg = self.key_bg and not self.old_key_bg
        self.stop_key_bg = not self.key_bg and self.old_key_bg
        self.old_key_bg = self.key_bg
        
        if self.start_key_bg:
            self.key_listener = keyboard.Listener(on_press=on_press, on_release=on_release)
            self.key_listener.start()
            self.mouse_listener = mouse.Listener(on_click=on_mouse_click, on_scroll=on_mouse_scroll, on_move=on_mouse_move)  # Enlève cette ligne si tu ne veux pas tracker les mouvements
            self.mouse_listener.start()
        self.was_pressed = self.is_pressed
        if self.key_bg:
            self.mouse_wheel = MOUSE_WHEEL
        self.is_pressed = get_keys() + get_mouse_buttons(self.mouse_wheel)
        if self.key_bg:
            self.is_pressed += PRESSED
        t=0
        while t < len(self.is_pressed):
            while self.is_pressed.count(self.is_pressed[t])>1:
                self.is_pressed.remove(self.is_pressed[t])
            t+=1
        self.is_pushed = get_keys_pushed(self.was_pressed, self.is_pressed)
        self.is_released = get_keys_released(self.was_pressed, self.is_pressed)
        if self.stop_key_bg:
            self.key_listener.stop()
            self.mouse_listener.stop()
        if len(self.controllers) > 0:
            for n in self.controllers:
                self.btn_was_pressed[n] = copy.deepcopy(self.btn_is_pressed[n])
                self.btn_is_pressed[n] = get_joy_buttons(self.controllers[n], self.type_controllers[n])
                self.btn_is_pushed[n] = get_keys_pushed(self.btn_was_pressed[n], self.btn_is_pressed[n])
                self.btn_is_released[n] = get_keys_released(self.btn_was_pressed[n], self.btn_is_pressed[n])
                for b in self.btn_played[n]:
                    self.btn_is_pressed[n].append(b)
                if self.type_controllers[n] in ['Thrustmaster Joystick'] and self.debug_selector[n] != False and self.controllers[n].get_axis(3) != self.v_sel_init[n]:
                    self.debug_selector[n] = False
                if self.debug_selector[n] == True:
                    self.axes[n] = get_axes(self.controllers[n], self.type_controllers[n], debug = self.debug_selector[n])
                else:
                    self.axes[n] = get_axes(self.controllers[n], self.type_controllers[n])
                self.hats[n] = get_hats(self.controllers[n], self.type_controllers[n])
                self.controller_state[n] = {'Buttons' : self.btn_is_pressed[n], 'Axes' : self.axes[n], 'Hats' : self.hats[n]}
                if self.vect_pos != {}:
                    self.old_vect_pos = copy.deepcopy(self.vect_pos)
                if self.type_controllers[n] in ['PS4', 'XBOX']:
                    l, r = vect_joy_classic(self.axes[n])
                    l2, r2 = vect_gachette(self.axes[n])
                    self.vect_pos[n]['JOY_L']['Norme'] = l[0]
                    self.vect_pos[n]['JOY_L']['Angle'] = l[1]
                    self.vect_pos[n]['JOY_L']['X'] = l[0] * math.cos(math.radians(l[1]))
                    self.vect_pos[n]['JOY_L']['Y'] = l[0] * math.sin(math.radians(l[1]))
                    
                    self.vect_pos[n]['JOY_R']['Norme'] = r[0]
                    self.vect_pos[n]['JOY_R']['Angle'] = r[1]
                    self.vect_pos[n]['JOY_R']['X'] = r[0] * math.cos(math.radians(r[1]))
                    self.vect_pos[n]['JOY_R']['Y'] = r[0] * math.sin(math.radians(r[1]))
                    
                    self.vect_pos[n]['L'] = l2
                    self.vect_pos[n]['R'] = r2
                    if self.correction_joy:
                        self.vect_pos[n] = correction_vect_pos(self.vect_pos[n], self.old_vect_pos[n])
                elif self.type_controllers[n] in ['Thrustmaster Joystick']:
                    self.vect_pos[n] = vect_pos_flight_from_controller_state(self.controller_state[n])
                else:
                    pass
                    
        for p in self.players:
            self.type_players[p] = get_type_player(self.touches_players[p])
            self.players[p]['is_pressed'] = verif_touches_player(self.is_pressed, self.touches_players[p]['Keys'])
            self.players[p]['is_pushed'] = verif_touches_player(self.is_pushed, self.touches_players[p]['Keys'])
            self.players[p]['is_released'] = verif_touches_player(self.is_released, self.touches_players[p]['Keys'])
            if self.type_players[p] in ['Mix', 'Controller', 'Controller + Mouse', 'Keyboard + Controller']:
                m = self.touches_players[p]['Controller']
                if m in self.controllers:
                    for b in verif_touches_player(self.btn_is_pressed[m], self.touches_players[p]['Keys']):
                        self.players[p]['is_pressed'].append(b)
                    for b in verif_touches_player(self.btn_is_pushed[m], self.touches_players[p]['Keys']):
                        self.players[p]['is_pushed'].append(b)
                    for b in verif_touches_player(self.btn_is_released[m], self.touches_players[p]['Keys']):
                        self.players[p]['is_released'].append(b)
                    self.players[p]['Axes'] = self.vect_pos[m]
                    if self.type_controllers[m] == 'PS4':
                        self.players[p]['Pad']['Init'] = self.pad_initial[m]
                        self.players[p]['Pad']['Pos'] = self.pad_pos[m]
                    else:
                        self.players[p]['Pad'] = {}
            if self.type_players[p] in ['Mouse', 'Keyboard + Mouse', 'Controller + Mouse', 'Mix']:
                self.players[p]['Mouse']['Coord'] = self.mouse_coord
                self.players[p]['Mouse']['Wheel'] = self.mouse_wheel
        
        #if self.camera_on and self.camera != None:
            #self.camera.get_image()
        
        self.static_display = locate_screen(self.position_fenetre, self.screens_info)
        if (not self.fake_fullscreen and self.fullscreen and not WINDOW.is_fullscreen()) or (not self.fake_fullscreen and not self.fullscreen and WINDOW.is_fullscreen()) or (self.fake_fullscreen and self.fullscreen and not self.bigscreen) or (self.fake_fullscreen and not self.fullscreen and self.bigscreen):
            if self.fake_fullscreen and not self.bigscreen:
                w, h = get_desktop_sizes()[self.static_display]
                x, y = screen_pos(self.static_display, self.screens_info)
                set_window(w, h, coord = (x, y), title = self.title, icon = self.icon, mode = ["NOFRAME"])
                self.bigscreen = True
            elif self.fake_fullscreen:
                w, h = get_desktop_sizes()[self.static_display]
                x, y = screen_pos(self.static_display, self.screens_info)
                set_window(self.base_tx, self.base_ty, coord = (x + (w-self.base_tx)/2, y + (h-self.base_ty)/2), title = WINDOW.get_caption(), icon = self.icon, mode = ["RESIZABLE"])
                self.bigscreen = False
            else:
                fullscreen()
        """
        if not self.fullscreen:
            pass
        """
        if self.title != self.old_title:
            self.old_title = self.title
            change_prop(title = self.title)
        self.real_t1 = self.real_t2
        self.real_t2 = time.time()
        self.real_dt = max(self.real_t2-self.real_t1, 0.0000000001)
        self.real_fps = 1/max(self.real_t2-self.real_t1, 0.0000000001)
        
        """
        has_focus = False
        if sys.platform == "win32" and win32gui and HWND:
            foreground = win32gui.GetForegroundWindow()
            has_focus = (foreground == HWND)
        print(has_focus)
        """
    
    def clear(self):
        self.screen.fill(self.background)
    
    def background_start_draw(self):
        self.clear()

    def background_end_draw(self):
        show()

    def update(self):
        self.background_update()
        self.main_update()
    
    def draw(self):
        if self.auto_drawing or self.refresh:
            self.background_start_draw()
            self.main_draw()
            self.background_end_draw()
            if self.refresh:
                self.refresh = False
            
    
    def main_update(self):
        self.update_function(self)
        
    def main_draw(self):
        self.draw_function(self)

if __name__ == '__main__':


    TX, TY = 1920, 1080
    FPS = 60
    FONT = None
    FONT_SIZE = 30
    TITLE = 'TEST PROGRAMME'
    TOUCHES = {1 : {'Keys' : get_keyboard_keys() + get_controller_keys() + get_mouse_keys(), 'Controller' : None, 'Mouse' : {'Coord' : (0, 0), 'Wheel' : 0}}}
    import math
    def f(x, a=10):
        return (1-x)*math.exp(-a*x)
    
    def init_prog(app):
        app.var["t"] = -1
        app.var["precision"] = 1000
        app.var["saturer_max"] = 1
        app.var["saturer_min"] = 0
        app.var["x_min"] = TX//20
        app.var["x_max"] = TX-TX//20
        app.var["y"] = TY//2
        app.var["x"] = None
    
    def update_prog(app):
        if app.var["t"] >= 0:
            app.var["t"]+=1
        
        if "SPACE" in app.is_pushed:
            if app.var["t"] < 0:
                app.var["t"] += 1
            else:
                app.var["t"] = -1
        x = max(min(f(app.var["t"]/app.var["precision"], a = 50), app.var["saturer_max"]), app.var["saturer_min"])
        app.var["x"] = app.var["x_max"] - (app.var["x_max"]-app.var["x_min"])*x
        
    def draw_prog(app):
        global TX, TY
        rect(app.var["x_min"], app.var["y"]-1, app.var["x_max"], app.var["y"]+1, "#FFFFFF")
        circle(app.var["x"], app.var["y"], TX//100, "#FF0000")
    #App(init_prog, update_prog, draw_prog, TX, TY, FPS, TITLE, background = '#2A2A2A', touches_players = TOUCHES)