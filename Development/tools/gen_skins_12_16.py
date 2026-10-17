"""Skins for Act 1 Scenes 12-16 (GENERATED STAND-INS, to be replaced by approved final skins/models).

Original pixel art drawn with tools/skinkit.py from public descriptions of each character's look (film and comic
costume write-ups, collected 2026-10-17). Nothing is traced from or copied out of anyone else's skin or art.
  - Sam Wilson / Captain America: deep blue suit, white star, red-and-white stripes across the torso, red goggles,
    no cowl, metallic wings on his back.
  - Bucky Barnes: all-black tactical gear and vest, black-and-gold metal left arm, short hair.
  - Sue Storm, Reed, Johnny (First Steps look): sky-blue suits, white collar and shoulders, black belt and boots,
    the "4" emblem. Ben Grimm: orange rock, blue trunks, black belt.
  - Cyclops: blue suit, yellow gloves, trunks, boot cuffs and chest harness, ruby visor, X buckle.
  - Joaquin Torres / Falcon: green helmeted flight suit, red lenses, wing pack.
  - Sentry: gold suit, navy collar, gloves, boots and belt with an "S", blue cape.
  - Red Guardian, John Walker, Yelena, Ghost, Clint, Kate, Shang-Chi, Scott Lang, Jean Grey, Beast, Nightcrawler,
    Xavier and Magneto in their best-known colours; plus the pilot, passengers, crews, scientists, staff and students.
Run from the project root: python3 tools/gen_skins_12_16.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from skinkit import SIDES, Skin, faces, shade  # noqa: E402

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
SKINS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/textures/entity")

# Skin tones and hair.
DEEP, BROWN, TAN, OLIVE, FAIR, PALE = (74, 48, 34), (112, 74, 52), (168, 120, 88), (196, 150, 110), (222, 178, 146), (238, 204, 182)
BLACK_HAIR, DARK_BROWN, BROWN_HAIR, BLONDE, RED_HAIR, GREY = (26, 22, 22), (52, 36, 26), (92, 62, 38), (222, 190, 112), (158, 52, 30), (176, 176, 180)
BLACK = (24, 24, 28)
CHAR = (44, 44, 50)

# FF First Steps palette.
FF_BLUE, FF_WHITE = (104, 156, 214), (236, 238, 240)


def ff_suit(s, skin):
    s.suit(FF_BLUE)
    # White collar, shoulders and tops of the arms.
    s.rect("body", "front", 0, 0, 8, 2, FF_WHITE)
    s.rect("body", "back", 0, 0, 8, 2, FF_WHITE)
    s.rect("body", "top", 0, 0, 8, 4, FF_WHITE)
    s.arms(lambda a: s.band(a, 0, 3, FF_WHITE))
    s.arms(lambda a: s.rect(a, "top", 0, 0, 4, 4, FF_WHITE))
    s.collar(FF_WHITE)
    # The 4 in a circle on the chest.
    s.emblem("body", "front", 1, 2, [".####.", "######", "######", "######", ".####."], (24, 40, 90))
    s.emblem("body", "front", 1, 2, ["...#..", "..##..", ".#.#..", ".####.", "...#.."], FF_WHITE)
    s.belt(BLACK, buckle=(150, 154, 160), y=9)
    s.boots(BLACK, rows=4, cuff=BLACK)
    s.gloves(shade(FF_BLUE, 0.85), rows=2)
    # Ribbed knit texture: faint lines across the blue.
    for p in ("body", "rarm", "larm", "rleg", "lleg"):
        for f in SIDES:
            for y in range(3, 12, 2):
                for x in range(0, 8 if p == "body" and f in ("front", "back") else 4):
                    fx, fy, fw, fh = faces(p)[f]
                    if x < fw:
                        r, g, b_, a = s.img.getpixel((fx + x, fy + y))
                        if (r, g, b_) != (0, 0, 0) and b_ > r + 40:
                            s.img.putpixel((fx + x, fy + y), shade((r, g, b_), 0.94) + (a,))


def lab_coat(s, shirt=(140, 170, 200), tie=None, trousers=(60, 64, 74)):
    white = (232, 232, 228)
    s.suit(white, white, trousers)
    s.rect("body", "front", 3, 0, 2, 8, shirt)
    if tie:
        s.rect("body", "front", 3, 1, 1, 6, tie, flat=True)
    s.rect("body", "front", 2, 0, 1, 12, shade(white, 0.85), flat=True)
    s.rect("body", "front", 5, 0, 1, 12, shade(white, 0.85), flat=True)
    s.px("body", "front", 6, 3, (60, 90, 160))  # ID badge
    s.px("body", "front", 6, 4, (220, 220, 220))
    s.rect("body", "front", 1, 7, 2, 2, shade(white, 0.88))  # pocket
    s.legs(lambda l: s.band(l, 0, 3, white))
    s.band("jacket", 0, 1, white)
    s.rect("jacket", "front", 2, 0, 4, 1, (0, 0, 0), a=0)
    s.shoes()


# ------------------------------------------------------------------------------------------------ the four heroes

def sam_wilson():
    s = Skin(1201)
    blue, deep, red, white, silver = (34, 66, 160), (22, 40, 104), (190, 36, 44), (238, 238, 240), (176, 182, 192)
    s.head(BROWN, eyes=(60, 40, 30), hair=BLACK_HAIR, style="buzz", beard=(40, 30, 26))
    s.goggles((210, 40, 40), up=True)
    s.suit(blue, blue, blue)
    # Darker shoulder plates and side panels.
    s.rect("body", "front", 0, 0, 8, 1, deep)
    s.arms(lambda a: s.band(a, 0, 2, deep))
    s.rect("body", "right", 0, 0, 4, 12, deep)
    s.rect("body", "left", 0, 0, 4, 12, deep)
    s.star("body", "front", 2, 2, white)
    s.px("body", "front", 4, 2, white)
    s.px("body", "front", 4, 3, white)
    s.px("body", "front", 5, 3, white)
    for i, c in enumerate((red, white, red, white)):
        s.rect("body", "front", 0, 6 + i, 8, 1, c)
    s.belt(deep, buckle=silver, y=10)
    s.gloves((30, 30, 38), rows=3, cuff=deep)
    s.boots(deep, rows=4, cuff=deep)
    s.legs(lambda l: s.rect(l, "front", 1, 1, 2, 5, shade(blue, 0.9)))
    # Folded vibranium wings: a metal pack across the back with the feathers' edges.
    s.rect("jacket", "back", 0, 0, 8, 10, silver)
    for x in range(0, 8, 2):
        s.rect("jacket", "back", x, 1, 1, 9, shade(silver, 0.78))
    s.rect("jacket", "back", 2, 0, 4, 2, (200, 206, 214))
    s.rect("jacket", "front", 1, 1, 1, 4, deep)  # harness straps
    s.rect("jacket", "front", 6, 1, 1, 4, deep)
    s.finish()
    return s


def bucky_barnes():
    s = Skin(1301)
    jacket, vest, trousers = (34, 34, 38), (22, 22, 24), (40, 40, 46)
    metal, gold = (40, 40, 44), (196, 156, 70)
    s.head(OLIVE, eyes=(80, 110, 150), hair=DARK_BROWN, style="messy", beard=shade(DARK_BROWN, 1.2))
    s.suit(jacket, jacket, trousers)
    s.zip((70, 70, 76), x=4, y0=0, y1=10)
    # Tactical vest with pouches on the outer layer.
    s.rect("jacket", "front", 0, 2, 8, 7, vest)
    for x in (0, 3, 5):
        s.rect("jacket", "front", x, 6, 2, 2, (54, 54, 58))
    s.rect("jacket", "back", 0, 2, 8, 7, vest)
    s.belt((60, 56, 50), buckle=(150, 150, 150), y=9)
    # The left arm: black metal plates with gold seams, a red star long gone.
    s.fill("larm", metal)
    for y in range(1, 12, 3):
        s.band("larm", y, 1, gold)
    s.rect("larm", "right", 0, 0, 4, 12, shade(metal, 1.2))
    s.hands(OLIVE, rows=2)
    s.fill("larm", metal, faces_=("bottom",))
    s.band("larm", 10, 2, metal)
    s.px("larm", "front", 1, 11, gold)
    s.gloves((26, 26, 28), rows=2)
    s.band("larm", 10, 2, (52, 52, 58))
    s.boots(BLACK, rows=4)
    s.legs(lambda l: s.rect(l, "front", 0, 2, 4, 1, (54, 54, 58)))
    s.finish()
    return s


def sue_storm():
    s = Skin(1501)
    s.head(PALE, eyes=(70, 120, 170), hair=BLONDE, style="bob", lips=(186, 96, 100))
    ff_suit(s, PALE)
    s.finish()
    return s


def cyclops():
    s = Skin(1601)
    blue, yellow, deep = (34, 52, 138), (232, 196, 50), (22, 32, 90)
    s.head(FAIR, hair=BROWN_HAIR, style="short")
    s.cowl(blue)
    s.rect("hat", "top", 0, 0, 8, 8, blue)
    s.visor((210, 30, 40), frame=(160, 150, 120))
    s.suit(blue)
    # Yellow harness: shoulder straps down to a ring on the chest, and a crossing X on the back.
    s.rect("body", "front", 1, 0, 1, 5, yellow)
    s.rect("body", "front", 6, 0, 1, 5, yellow)
    s.rect("body", "front", 1, 4, 6, 1, yellow)
    s.rect("body", "front", 3, 3, 2, 3, yellow)
    s.rect("body", "front", 3, 4, 2, 1, (200, 40, 40))
    s.emblem("body", "back", 1, 1, ["#....#", ".#..#.", "..##..", "..##..", ".#..#.", "#....#"], yellow)
    s.belt(yellow, buckle=(200, 40, 40), y=9)
    s.emblem("body", "front", 3, 9, ["##"], deep)
    s.shorts(yellow, rows=2)
    s.gloves(yellow, rows=4)
    s.boots(blue, rows=4, cuff=yellow)
    s.legs(lambda l: s.band(l, 8, 1, yellow))
    s.finish()
    return s


# ------------------------------------------------------------------------------------------------ Scene 12

def joaquin_torres():
    s = Skin(1202)
    green, dark, grey, red = (66, 92, 64), (34, 44, 36), (110, 116, 112), (210, 40, 40)
    s.head(TAN, eyes=(60, 40, 28), hair=BLACK_HAIR, style="short")
    # Helmet on the outer layer: green shell, red lenses, the lower face open.
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 8 if f != "right" and f != "left" else 5, green)
    s.rect("hat", "front", 0, 0, 8, 3, green)
    s.goggles(red, frame=dark)
    s.suit(green, green, dark)
    s.rect("body", "front", 2, 0, 4, 9, grey)
    s.rect("body", "front", 3, 1, 2, 1, red)
    s.arms(lambda a: s.band(a, 2, 1, dark))
    s.belt(dark, buckle=grey, y=9)
    s.gloves(dark, rows=3)
    s.boots(dark, rows=4)
    s.rect("jacket", "back", 0, 0, 8, 10, grey)
    for x in range(0, 8, 2):
        s.rect("jacket", "back", x, 1, 1, 8, shade(grey, 0.75))
    s.finish()
    return s


def shang_chi():
    s = Skin(1203)
    red, gold, black = (156, 30, 34), (196, 150, 70), (28, 24, 26)
    s.head(OLIVE, eyes=(50, 34, 26), hair=BLACK_HAIR, style="messy")
    s.suit(red, red, black)
    # Wrapped tunic: a gold-trimmed diagonal front, scale pattern on the shoulders.
    for y in range(0, 7):
        s.px("body", "front", min(7, 1 + y), y, gold)
    for x in range(0, 8, 2):
        s.px("body", "front", x, 1, shade(red, 0.75))
        s.px("body", "back", x + 1, 1, shade(red, 0.75))
    s.arms(lambda a: s.band(a, 0, 3, shade(red, 0.82)))
    s.arms(lambda a: s.band(a, 9, 1, gold))
    s.belt(black, buckle=gold, y=8)
    s.hands(OLIVE, rows=2)
    s.boots(black, rows=2)
    s.finish()
    return s


def scott_lang():
    s = Skin(1204)
    red, black, silver = (176, 30, 34), (30, 30, 34), (178, 184, 192)
    s.head(FAIR, eyes=(70, 90, 120), hair=BROWN_HAIR, style="short")
    # The helmet: silver shell with black faceplate and red eyes.
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 8, silver)
    s.rect("hat", "front", 0, 0, 8, 8, black)
    s.rect("hat", "front", 0, 0, 8, 2, silver)
    s.rect("hat", "front", 1, 3, 2, 2, (220, 50, 50))
    s.rect("hat", "front", 5, 3, 2, 2, (220, 50, 50))
    s.rect("hat", "front", 2, 6, 4, 2, (90, 94, 100))
    s.suit(red, red, black)
    s.rect("body", "front", 0, 4, 8, 2, black)
    s.rect("body", "front", 2, 0, 4, 4, shade(red, 0.8))
    s.belt(silver, buckle=(220, 50, 50), y=9)
    s.arms(lambda a: s.band(a, 4, 2, black))
    s.legs(lambda l: s.band(l, 2, 2, red))
    s.gloves(black, rows=3)
    s.boots(black, rows=4)
    s.finish()
    return s


def heli_pilot():
    s = Skin(1205)
    olive, patch = (96, 104, 72), (60, 64, 50)
    s.head(TAN, eyes=(60, 50, 40), hair=DARK_BROWN, style="buzz")
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 3 if f != "top" else 8, (226, 228, 230))
    s.rect("hat", "front", 0, 0, 8, 2, (226, 228, 230))
    s.px("hat", "right", 3, 4, BLACK)  # headset
    s.px("hat", "left", 4, 4, BLACK)
    s.suit(olive)
    s.zip(shade(olive, 0.6), x=4, y0=0, y1=11)
    s.rect("body", "front", 1, 2, 2, 2, patch)
    s.rect("body", "front", 5, 2, 2, 1, (180, 40, 40))
    s.legs(lambda l: s.rect(l, "front", 0, 4, 4, 2, patch))
    s.gloves((70, 60, 50), rows=2)
    s.boots(BLACK, rows=3)
    s.finish()
    return s


def passenger_1():
    s = Skin(1206)
    s.head(TAN, eyes=(60, 44, 30), hair=DARK_BROWN, style="short")
    s.suit((150, 150, 156), (30, 50, 90), (60, 80, 120))
    s.rect("body", "front", 0, 0, 2, 12, (30, 50, 90))
    s.rect("body", "front", 6, 0, 2, 12, (30, 50, 90))
    s.band("body", 0, 1, (130, 130, 136))
    s.hands(TAN, rows=2)
    s.shoes((220, 220, 220))
    s.finish()
    return s


def passenger_2():
    s = Skin(1207)
    s.head(FAIR, eyes=(70, 110, 80), hair=(120, 74, 40), style="long", lips=(176, 90, 90))
    coat = (70, 110, 76)
    s.suit(coat, coat, (50, 46, 52))
    s.band("body", 0, 2, (200, 160, 70))  # scarf
    s.rect("body", "front", 1, 2, 2, 3, (200, 160, 70))
    s.zip(shade(coat, 0.7), x=4, y0=2, y1=11)
    s.hands(FAIR, rows=2)
    s.boots((84, 56, 38), rows=3)
    s.finish()
    return s


def ground_crew():
    s = Skin(1208)
    navy, vis, refl = (34, 42, 66), (240, 132, 30), (210, 214, 220)
    s.head(BROWN, eyes=(50, 36, 26), hair=BLACK_HAIR, style="buzz")
    s.rect("hat", "right", 2, 3, 3, 3, (220, 180, 40))  # ear defenders
    s.rect("hat", "left", 3, 3, 3, 3, (220, 180, 40))
    s.rect("hat", "top", 0, 3, 8, 2, (220, 180, 40))
    s.suit(navy)
    s.rect("jacket", "front", 0, 0, 8, 9, vis)
    s.rect("jacket", "back", 0, 0, 8, 9, vis)
    s.rect("jacket", "front", 3, 0, 2, 4, (0, 0, 0), a=0)
    for y in (4, 7):
        s.rect("jacket", "front", 0, y, 8, 1, refl)
        s.rect("jacket", "back", 0, y, 8, 1, refl)
    s.gloves((40, 40, 40), rows=2)
    s.boots(BLACK, rows=3)
    s.finish()
    return s


# ------------------------------------------------------------------------------------------------ Scene 13

def yelena_belova():
    s = Skin(1301 + 1)
    vest, black = (60, 70, 50), (28, 28, 32)
    s.head(PALE, eyes=(80, 120, 110), hair=BLONDE, style="braid", lips=(176, 100, 100))
    s.suit(black, black, (36, 36, 40))
    s.rect("jacket", "front", 0, 1, 8, 8, vest)
    s.rect("jacket", "front", 3, 1, 2, 3, (0, 0, 0), a=0)
    s.rect("jacket", "back", 0, 1, 8, 8, vest)
    s.collar(vest)
    for x in (1, 5):
        s.rect("jacket", "front", x, 5, 2, 2, shade(vest, 0.8))
    s.belt((40, 36, 32), buckle=(190, 40, 40), y=9)
    s.legs(lambda l: s.rect(l, "right" if l == "rleg" else "left", 0, 2, 4, 3, (50, 50, 46)))
    s.gloves(black, rows=2)
    s.boots(BLACK, rows=4)
    s.finish()
    return s


def john_walker():
    s = Skin(1303)
    navy, red, white = (24, 30, 56), (176, 32, 40), (232, 232, 236)
    s.head(FAIR, eyes=(70, 100, 140), hair=BROWN_HAIR, style="slick")
    s.suit(navy)
    s.star("body", "front", 2, 1, white)
    s.px("body", "front", 4, 1, white)
    s.px("body", "front", 4, 2, white)
    s.px("body", "front", 5, 2, white)
    for x in range(0, 8):
        s.rect("body", "front", x, 6, 1, 4, red if x % 2 == 0 else white)
    s.belt(BLACK, buckle=(200, 200, 205), y=10)
    s.arms(lambda a: s.band(OUTER_SLEEVE[a], 0, 2, (16, 18, 30)))
    s.rect("jacket", "top", 0, 0, 8, 4, (16, 18, 30))
    s.gloves(BLACK, rows=3)
    s.boots(BLACK, rows=4)
    s.finish()
    return s


OUTER_SLEEVE = {"rarm": "rsleeve", "larm": "lsleeve"}


def red_guardian():
    s = Skin(1304)
    red, white, gold = (172, 28, 32), (232, 232, 232), (226, 182, 60)
    s.head(FAIR, eyes=(70, 100, 140), hair=BROWN_HAIR, style="short", beard=(104, 72, 46))
    # Helmet with the gold star.
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 4 if f != "top" else 8, red)
    s.rect("hat", "front", 0, 0, 8, 2, red)
    s.star("hat", "front", 3, 0, gold)
    s.suit(red)
    s.star("body", "front", 2, 2, gold)
    s.px("body", "front", 4, 2, gold)
    s.px("body", "front", 4, 3, gold)
    s.px("body", "front", 5, 3, gold)
    s.rect("body", "front", 0, 0, 8, 1, white)
    s.belt(white, buckle=gold, y=9)
    s.arms(lambda a: s.band(a, 8, 1, white))
    s.gloves(shade(red, 0.75), rows=3)
    s.boots(shade(red, 0.75), rows=4, cuff=white)
    s.finish()
    return s


def ghost():
    s = Skin(1305)
    white, grey, line, glow = (220, 224, 230), (150, 156, 166), (90, 96, 108), (150, 230, 255)
    s.head(PALE, hair=BLONDE, style="short")
    s.fill("head", white)
    s.rect("head", "front", 0, 3, 8, 2, (40, 44, 52))
    s.rect("head", "front", 1, 3, 2, 1, glow)
    s.rect("head", "front", 5, 3, 2, 1, glow)
    s.rect("head", "front", 2, 6, 4, 1, line)
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 8 if f in ("top", "back") else 6, grey)  # the hood
    s.rect("hat", "front", 0, 0, 8, 1, grey)
    s.suit(white)
    for y in (3, 6):
        s.rect("body", "front", 0, y, 8, 1, line, flat=True)
    s.rect("body", "front", 3, 0, 2, 12, grey)
    s.rect("jacket", "front", 0, 2, 8, 6, grey)  # tactical vest
    s.rect("jacket", "front", 3, 2, 2, 6, (0, 0, 0), a=0)
    s.arms(lambda a: s.band(a, 4, 1, line))
    s.legs(lambda l: s.band(l, 4, 1, line))
    s.gloves(grey, rows=3)
    s.boots(grey, rows=4)
    s.finish()
    return s


def sentry():
    s = Skin(1306)
    gold, navy, cape = (214, 168, 56), (28, 36, 84), (42, 70, 160)
    s.head(FAIR, eyes=(80, 130, 190), hair=BLONDE, style="slick")
    s.suit(gold)
    s.collar(navy, high=True)
    s.rect("body", "front", 0, 0, 8, 1, navy)
    s.belt(navy, y=9)
    s.emblem("body", "front", 3, 8, ["##", "#.", ".#", "##"][:2], gold)
    s.rect("body", "front", 3, 9, 2, 1, (240, 210, 90))
    s.gloves(navy, rows=4)
    s.boots(navy, rows=5)
    s.cape(cape)
    s.rect("jacket", "back", 0, 0, 8, 1, navy)
    s.finish()
    return s


def clint_barton():
    s = Skin(1307)
    jacket, purple, quiver = (36, 32, 42), (70, 40, 90), (96, 64, 40)
    s.head(FAIR, eyes=(70, 110, 140), hair=(140, 110, 70), style="short", beard=(120, 96, 66))
    s.px("head", "right", 4, 3, (200, 170, 140))  # hearing aid
    s.suit(jacket, jacket, (40, 40, 46))
    s.rect("body", "front", 0, 0, 8, 2, purple)
    s.zip((90, 90, 96), x=4, y0=1, y1=10)
    s.belt(BLACK, buckle=(150, 150, 150), y=9)
    s.rect("jacket", "back", 2, 0, 3, 10, quiver)
    s.rect("jacket", "back", 2, 0, 3, 1, (200, 200, 200))  # fletching
    s.rect("jacket", "front", 0, 0, 1, 6, quiver)  # strap
    s.hands(FAIR, rows=1)
    s.gloves(BLACK, rows=2)
    s.boots(BLACK, rows=3)
    s.finish()
    return s


def kate_bishop():
    s = Skin(1308)
    purple, black = (112, 54, 150), (28, 26, 32)
    s.head(OLIVE, eyes=(50, 36, 26), hair=BLACK_HAIR, style="long", lips=(170, 90, 96))
    s.suit(black, purple, black)
    s.rect("jacket", "front", 0, 0, 2, 11, purple)
    s.rect("jacket", "front", 6, 0, 2, 11, purple)
    s.rect("jacket", "back", 0, 0, 8, 11, purple)
    s.rect("jacket", "back", 3, 0, 2, 9, (96, 64, 40))  # quiver
    s.collar(purple)
    s.belt((60, 40, 30), buckle=(180, 180, 180), y=9)
    s.hands(OLIVE, rows=2)
    s.boots(BLACK, rows=4)
    s.finish()
    return s


def scientist(seed, skin, hair, style, shirt, tie=None, glasses=False, eyes=(70, 90, 120), beard=None):
    s = Skin(seed)
    s.head(skin, eyes=eyes, hair=hair, style=style, beard=beard)
    if glasses:
        s.glasses()
    lab_coat(s, shirt=shirt, tie=tie)
    s.hands(skin, rows=2)
    s.finish()
    return s


def emergency_responder():
    s = Skin(1309)
    green, vis, refl = (46, 84, 62), (226, 220, 60), (210, 214, 220)
    s.head(DEEP, eyes=(46, 32, 22), hair=BLACK_HAIR, style="buzz")
    s.suit(green)
    s.rect("body", "front", 1, 2, 2, 2, (220, 220, 220))
    s.px("body", "front", 2, 2, (40, 120, 70))
    s.band("body", 6, 1, vis)
    s.band("body", 7, 1, refl)
    s.arms(lambda a: s.band(a, 5, 1, vis))
    s.legs(lambda l: s.band(l, 6, 1, refl))
    s.gloves((80, 140, 200), rows=2)
    s.boots(BLACK, rows=3)
    s.finish()
    return s


# ------------------------------------------------------------------------------------------------ Scene 15

def reed_richards():
    s = Skin(1502)
    s.head(FAIR, eyes=(90, 110, 130), hair=BROWN_HAIR, style="slick", beard=None)
    for side in ("right", "left"):
        s.rect("head", side, 1 if side == "right" else 4, 1, 3, 2, GREY)
        s.rect("hat", side, 1 if side == "right" else 4, 1, 3, 1, GREY)
    ff_suit(s, FAIR)
    s.finish()
    return s


def johnny_storm():
    s = Skin(1503)
    s.head(FAIR, eyes=(70, 120, 170), hair=BLONDE, style="spiky")
    ff_suit(s, FAIR)
    s.finish()
    return s


def ben_grimm():
    s = Skin(1504)
    rock, crack = (204, 118, 52), (128, 66, 28)
    s.fill("head", rock)
    s.fill("body", rock)
    for p in ("rarm", "larm", "rleg", "lleg"):
        s.fill(p, rock)
    # Rock plates: an irregular network of dark cracks with lit edges.
    import random as _r
    rr = _r.Random(44)
    for part in ("head", "body", "rarm", "larm", "rleg", "lleg"):
        from skinkit import faces as _f
        for face, (fx, fy, fw, fh) in _f(part).items():
            for j in range(fh):
                for i in range(fw):
                    if (i + j * 2 + rr.randrange(3)) % 4 == 0 or (j % 3 == 0 and rr.random() < 0.5):
                        s.img.putpixel((fx + i, fy + j), crack + (255,))
                    elif rr.random() < 0.12:
                        s.img.putpixel((fx + i, fy + j), shade(rock, 1.18) + (255,))
    # Heavy brow, blue eyes, a set mouth.
    s.rect("head", "front", 0, 3, 8, 1, shade(rock, 0.7), flat=True)
    s.px("head", "front", 1, 4, (240, 236, 228))
    s.px("head", "front", 2, 4, (60, 120, 200))
    s.px("head", "front", 5, 4, (60, 120, 200))
    s.px("head", "front", 6, 4, (240, 236, 228))
    s.rect("head", "front", 2, 6, 4, 1, crack, flat=True)
    # Blue trunks with the black belt.
    s.legs(lambda l: s.band(l, 0, 4, FF_BLUE))
    s.band("body", 10, 2, FF_BLUE)
    s.belt(BLACK, buckle=(150, 154, 160), y=9)
    s.emblem("body", "front", 3, 10, ["##"], FF_WHITE)
    s.finish()
    return s


# ------------------------------------------------------------------------------------------------ Scene 16

def jean_grey():
    s = Skin(1602)
    green, gold = (44, 128, 74), (226, 184, 64)
    s.head(PALE, eyes=(70, 130, 90), hair=RED_HAIR, style="long", lips=(186, 90, 90))
    s.suit(green)
    s.rect("body", "front", 2, 1, 4, 3, gold)
    s.emblem("body", "front", 2, 1, ["#..#", ".##.", "#..#"], green)
    s.belt(gold, buckle=(200, 40, 40), y=8)
    s.gloves(gold, rows=4)
    s.boots(gold, rows=5)
    s.finish()
    return s


def beast():
    s = Skin(1603)
    fur, dark, trunks = (52, 74, 168), (34, 50, 120), (24, 24, 30)
    s.head(fur, eyes=(220, 200, 80), hair=dark, style="messy", brows=dark, lips=(40, 40, 70), beard=dark)
    s.glasses((26, 26, 30))
    for p in ("body", "rarm", "larm", "rleg", "lleg"):
        s.fill(p, fur)
    # Shaggy fur: darker tufts on the outer layer.
    for o in ("jacket", "rsleeve", "lsleeve", "rpants", "lpants"):
        from skinkit import faces as _f
        for face in ("front", "back", "right", "left"):
            fx, fy, fw, fh = _f(o)[face]
            for i in range(fw):
                for j in range(0, fh, 3):
                    if (i + j) % 2 == 0:
                        s.img.putpixel((fx + i, fy + j), dark + (255,))
    s.shorts(trunks, rows=3)
    s.belt((226, 190, 60), buckle=(200, 40, 40), y=10)
    s.legs(lambda l: s.band(l, 10, 2, shade(fur, 0.8)))
    s.finish()
    return s


def nightcrawler():
    s = Skin(1604)
    skin, red, black = (40, 50, 110), (150, 26, 40), (26, 24, 30)
    s.head(skin, eyes=(244, 214, 60), hair=(18, 20, 50), style="messy", eye_white=(244, 214, 60), lips=(30, 30, 60))
    for side in ("right", "left"):
        s.px("head", side, 3, 2, shade(skin, 1.2))  # pointed ears
    s.suit(black)
    s.rect("body", "front", 1, 0, 6, 8, red)
    s.rect("body", "front", 3, 0, 2, 8, black)
    s.arms(lambda a: s.band(a, 0, 4, red))
    s.gloves(skin, rows=2)
    s.boots(black, rows=3)
    s.legs(lambda l: s.rect(l, "front", 0, 0, 4, 2, red))
    s.finish()
    return s


def charles_xavier():
    s = Skin(1605)
    suit = (54, 58, 70)
    s.head(FAIR, eyes=(80, 110, 150), hair=FAIR, bald=True)
    s.rect("head", "top", 0, 0, 8, 8, shade(FAIR, 1.05))
    s.suit(suit)
    s.rect("body", "front", 3, 0, 2, 7, (234, 234, 236))
    s.rect("body", "front", 3, 1, 1, 6, (110, 30, 40), flat=True)
    s.rect("body", "front", 2, 0, 1, 6, shade(suit, 0.8))
    s.rect("body", "front", 5, 0, 1, 6, shade(suit, 0.8))
    s.hands(FAIR, rows=2)
    s.shoes((30, 26, 24))
    s.finish()
    return s


def magneto():
    s = Skin(1606)
    red, purple = (162, 26, 40), (96, 42, 120)
    s.head(FAIR, eyes=(80, 110, 150), hair=GREY, style="short")
    # The helmet: red shell with purple trim, cheek guards, the face open.
    for f in ("top", "right", "left", "back"):
        s.rect("hat", f, 0, 0, 8, 8, red)
    s.rect("hat", "front", 0, 0, 8, 2, red)
    s.rect("hat", "front", 0, 2, 1, 6, red)
    s.rect("hat", "front", 7, 2, 1, 6, red)
    s.rect("hat", "front", 2, 0, 4, 1, purple)
    s.suit(red)
    s.rect("body", "front", 2, 0, 4, 2, purple)
    s.belt(purple, y=9)
    s.shorts(purple, rows=2)
    s.gloves(purple, rows=4)
    s.boots(purple, rows=5)
    s.cape(purple)
    s.finish()
    return s


def station_staff():
    s = Skin(1607)
    navy = (40, 52, 82)
    s.head(TAN, eyes=(60, 44, 30), hair=DARK_BROWN, style="short", beard=(50, 36, 28))
    s.suit(navy)
    s.zip(shade(navy, 0.6), x=4, y0=0, y1=11)
    s.rect("body", "front", 1, 2, 2, 1, (226, 186, 40))
    s.belt(BLACK, y=9)
    s.hands(TAN, rows=2)
    s.boots(BLACK, rows=3)
    s.finish()
    return s


def student(seed, skin, hair, style, top, trousers, eyes=(60, 44, 30)):
    s = Skin(seed)
    s.head(skin, eyes=eyes, hair=hair, style=style)
    s.suit(top, top, trousers)
    s.hands(skin, rows=3)
    s.shoes((230, 230, 230))
    s.finish()
    return s


def main():
    skins = {
        "sam_wilson": sam_wilson(), "bucky_barnes": bucky_barnes(), "sue_storm": sue_storm(), "cyclops": cyclops(),
        "joaquin_torres": joaquin_torres(), "shang_chi": shang_chi(), "scott_lang": scott_lang(),
        "heli_pilot": heli_pilot(), "passenger_1": passenger_1(), "passenger_2": passenger_2(), "ground_crew": ground_crew(),
        "yelena_belova": yelena_belova(), "john_walker": john_walker(), "red_guardian": red_guardian(), "ghost": ghost(),
        "sentry": sentry(), "clint_barton": clint_barton(), "kate_bishop": kate_bishop(),
        "facility_scientist": scientist(1310, BROWN, BLACK_HAIR, "short", (120, 150, 190), tie=(120, 40, 50), glasses=True,
                                        eyes=(50, 36, 26)),
        "facility_staff_1": scientist(1311, FAIR, (150, 96, 50), "ponytail", (190, 140, 160)),
        "facility_staff_2": scientist(1312, DEEP, BLACK_HAIR, "buzz", (90, 140, 110), eyes=(46, 32, 22), beard=(30, 24, 22)),
        "emergency_responder": emergency_responder(),
        "reed_richards": reed_richards(), "johnny_storm": johnny_storm(), "ben_grimm": ben_grimm(),
        "ff_lab_staff": scientist(1505, OLIVE, DARK_BROWN, "bob", (104, 156, 214), glasses=True),
        "jean_grey": jean_grey(), "beast": beast(), "nightcrawler": nightcrawler(),
        "charles_xavier": charles_xavier(), "magneto": magneto(), "station_staff": station_staff(),
        "station_scientist": scientist(1608, PALE, GREY, "short", (40, 52, 82), glasses=True),
        "student_1": student(1609, DEEP, BLACK_HAIR, "short", (214, 170, 50), (50, 60, 92)),
        "student_2": student(1610, PALE, (196, 110, 56), "ponytail", (160, 60, 120), (60, 60, 66), eyes=(70, 120, 90)),
    }
    for name, s in skins.items():
        s.save(os.path.join(SKINS, name + ".png"))
    print(f"{len(skins)} skins")


if __name__ == "__main__":
    main()
