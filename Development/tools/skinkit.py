"""A small pixel-art kit for 64x64 Minecraft player skins (wide arms), used by gen_skins_12_16.py.

It paints with light: every face is shaded (top lit, sides and back darker, a little darker towards the bottom, a hint of
grain), edges are picked out so parts read as solid, and the outer layer (hat, jacket, sleeves, trousers) is used for
anything that should stand off the body: hair volume, goggles, helmets, vests, collars, capes and cuffs.
"""
import random

from PIL import Image

# (u, v) of each part's texture box and its (w, h, d).
PARTS = {
    "head": ((0, 0), (8, 8, 8)), "hat": ((32, 0), (8, 8, 8)),
    "body": ((16, 16), (8, 12, 4)), "jacket": ((16, 32), (8, 12, 4)),
    "rarm": ((40, 16), (4, 12, 4)), "rsleeve": ((40, 32), (4, 12, 4)),
    "larm": ((32, 48), (4, 12, 4)), "lsleeve": ((48, 48), (4, 12, 4)),
    "rleg": ((0, 16), (4, 12, 4)), "rpants": ((0, 32), (4, 12, 4)),
    "lleg": ((16, 48), (4, 12, 4)), "lpants": ((0, 48), (4, 12, 4)),
}
OUTER = {"head": "hat", "body": "jacket", "rarm": "rsleeve", "larm": "lsleeve", "rleg": "rpants", "lleg": "lpants"}
SIDES = ("front", "right", "back", "left")
LIGHT = {"top": 1.10, "front": 1.0, "right": 0.88, "left": 0.88, "back": 0.80, "bottom": 0.70}


def faces(part):
    (u, v), (w, h, d) = PARTS[part]
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }


def rgb(c):
    return tuple(int(x) for x in c[:3])


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def shade(c, f):
    return tuple(max(0, min(255, int(x * f))) for x in c[:3])


class Skin:
    def __init__(self, seed):
        self.img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        self.rnd = random.Random(seed)

    # -------------------------------------------------------------- low level

    def put(self, x, y, c, a=255):
        self.img.putpixel((x, y), rgb(c) + (a,))

    def lit(self, c, face, row, rows, grain=0.035):
        """Colour of a pixel on a face: face light, a soft top-to-bottom falloff and grain."""
        fall = 1.04 - 0.10 * (row / max(1, rows - 1))
        return shade(c, LIGHT[face] * fall * (1 + self.rnd.uniform(-grain, grain)))

    def rect(self, part, face, x, y, w, h, c, grain=0.035, a=255, flat=False):
        fx, fy, fw, fh = faces(part)[face]
        for i in range(x, x + w):
            for j in range(y, y + h):
                if 0 <= i < fw and 0 <= j < fh:
                    col = rgb(c) if flat else self.lit(c, face, j, fh, grain)
                    self.put(fx + i, fy + j, col, a)

    def px(self, part, face, x, y, c, a=255):
        self.rect(part, face, x, y, 1, 1, c, a=a, flat=True)

    def fill(self, part, c, faces_=None, grain=0.035):
        for f in faces_ or ("top", "bottom") + SIDES:
            fx, fy, fw, fh = faces(part)[f]
            self.rect(part, f, 0, 0, fw, fh, c, grain)

    def band(self, part, y, h, c, grain=0.035, sides=SIDES):
        """A band all the way round a part (belt, cuff, stripe)."""
        for f in sides:
            fx, fy, fw, fh = faces(part)[f]
            self.rect(part, f, 0, y, fw, h, c, grain)

    def edges(self, part, dark=0.78, sides=SIDES):
        """Darkens the vertical edges of each side face so parts read as solid."""
        for f in sides:
            fx, fy, fw, fh = faces(part)[f]
            for j in range(fh):
                for i in (0, fw - 1):
                    r, g, b, a = self.img.getpixel((fx + i, fy + j))
                    if a:
                        self.put(fx + i, fy + j, shade((r, g, b), dark), a)

    def clear(self, part):
        for f, (fx, fy, fw, fh) in faces(part).items():
            for i in range(fx, fx + fw):
                for j in range(fy, fy + fh):
                    self.img.putpixel((i, j), (0, 0, 0, 0))

    # -------------------------------------------------------------- the body

    def suit(self, body, arms=None, legs=None):
        self.fill("body", body)
        for a in ("rarm", "larm"):
            self.fill(a, arms or body)
        for l in ("rleg", "lleg"):
            self.fill(l, legs or body)

    def arms(self, fn):
        for a in ("rarm", "larm"):
            fn(a)

    def legs(self, fn):
        for l in ("rleg", "lleg"):
            fn(l)

    def hands(self, skin, rows=2):
        self.arms(lambda a: (self.band(a, 12 - rows, rows, skin), self.rect(a, "bottom", 0, 0, 4, 4, skin)))

    def gloves(self, c, rows=3, cuff=None):
        self.arms(lambda a: (self.band(a, 12 - rows, rows, c), self.rect(a, "bottom", 0, 0, 4, 4, c)))
        if cuff:
            self.arms(lambda a: self.band(OUTER[a], 12 - rows, 1, cuff))

    def boots(self, c, rows=4, sole=(26, 24, 26), cuff=None):
        def one(l):
            self.band(l, 12 - rows, rows, c)
            self.band(l, 11, 1, sole)
            self.rect(l, "bottom", 0, 0, 4, 4, sole)
            if cuff:
                self.band(OUTER[l], 12 - rows, 1, cuff)
        self.legs(one)

    def shoes(self, c=(40, 34, 30)):
        self.boots(c, rows=1, sole=shade(c, 0.6))

    def belt(self, c, buckle=None, y=10):
        self.band("body", y, 1, c)
        if buckle:
            self.rect("body", "front", 3, y, 2, 1, buckle, flat=True)

    def shorts(self, c, rows=3):
        self.legs(lambda l: self.band(l, 0, rows, c))
        self.band("body", 11, 1, c)

    def collar(self, c, high=False):
        self.band("jacket", 0, 1, c)
        if high:
            self.rect("jacket", "top", 0, 0, 8, 1, c)
            self.rect("jacket", "top", 0, 3, 8, 1, c)
            self.rect("jacket", "top", 0, 0, 1, 4, c)
            self.rect("jacket", "top", 7, 0, 1, 4, c)

    def cape(self, c):
        self.rect("jacket", "back", 0, 0, 8, 12, c)
        self.rect("jacket", "right", 3, 0, 1, 12, c)
        self.rect("jacket", "left", 0, 0, 1, 12, c)

    def emblem(self, part, face, x, y, rows, c):
        """Draws a small pixel picture: rows of '#' (colour c) and '.' (skip)."""
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch == "#":
                    self.px(part, face, x + i, y + j, c)

    def star(self, part, face, x, y, c):
        self.emblem(part, face, x, y, [".#.", "###", "#.#"], c)

    def zip(self, c, x=3, y0=1, y1=9):
        self.rect("body", "front", x, y0, 1, y1 - y0, c, flat=True)

    def finish(self):
        for p in ("body", "rarm", "larm", "rleg", "lleg"):
            self.edges(p)

    # -------------------------------------------------------------- the head

    def head(self, skin, eyes=(70, 100, 150), hair=(50, 34, 24), style="short", brows=None, beard=None, lips=None,
             ears=True, eye_white=(240, 238, 232), bald=False):
        """A face with brows, eyes (white + iris), a shaded nose, a mouth, and hair in one of a few styles."""
        self.fill("head", skin)
        brows = brows or shade(hair, 0.8)
        lips = lips or mix(skin, (150, 70, 70), 0.35)
        f = "front"
        # Cheek and jaw shading, nose, mouth.
        for y in (6, 7):
            self.px("head", f, 0, y, shade(skin, 0.88))
            self.px("head", f, 7, y, shade(skin, 0.88))
        self.px("head", f, 3, 5, shade(skin, 0.86))
        self.px("head", f, 4, 5, shade(skin, 0.92))
        self.px("head", f, 3, 6, lips)
        self.px("head", f, 4, 6, lips)
        self.px("head", f, 2, 6, shade(skin, 0.85))
        self.px("head", f, 5, 6, shade(skin, 0.85))
        # Eyes: white outside, iris inside, a lid shadow above.
        for x, white_first in ((1, True), (5, False)):
            self.px("head", f, x, 4, eye_white if white_first else eyes)
            self.px("head", f, x + 1, 4, eyes if white_first else eye_white)
            self.px("head", f, x, 3, shade(skin, 0.9))
            self.px("head", f, x + 1, 3, shade(skin, 0.9))
        self.px("head", f, 1, 3, brows)
        self.px("head", f, 2, 3, brows)
        self.px("head", f, 5, 3, brows)
        self.px("head", f, 6, 3, brows)
        if ears:
            for side in ("right", "left"):
                self.px("head", side, 3, 4, shade(skin, 0.82))
                self.px("head", side, 4, 4, shade(skin, 0.86))
                self.px("head", side, 3, 5, shade(skin, 0.8))
        if beard:
            for y in (5, 6, 7):
                self.px("head", f, 0, y, beard)
                self.px("head", f, 7, y, beard)
            for x in (1, 2, 5, 6):
                self.px("head", f, x, 7, beard)
            self.px("head", f, 3, 7, beard)
            self.px("head", f, 4, 7, beard)
            self.px("head", f, 2, 6, beard)
            self.px("head", f, 5, 6, beard)
            for side in ("right", "left"):
                self.rect("head", side, 4 if side == "right" else 0, 5, 4, 3, beard)
        if not bald:
            self.hair(hair, style)

    def hair(self, c, style):
        hi = shade(c, 1.25)
        top = lambda: self.rect("head", "top", 0, 0, 8, 8, c)  # noqa: E731
        if style == "buzz":
            top()
            self.rect("head", "front", 0, 0, 8, 1, c)
            for s in ("right", "left", "back"):
                self.rect("head", s, 0, 0, 8, 2, c)
            self.rect("head", "back", 0, 2, 8, 3, c)
            return
        top()
        for s in ("right", "left"):
            self.rect("head", s, 0, 0, 8, 3, c)
            self.rect("head", s, 0 if s == "right" else 5, 3, 3, 3, c)
        self.rect("head", "back", 0, 0, 8, 7, c)
        self.rect("head", "back", 0, 7, 8, 1, shade(c, 0.85))
        self.rect("head", "front", 0, 0, 8, 2, c)
        self.px("head", "front", 0, 2, c)
        self.px("head", "front", 7, 2, c)
        if style in ("short", "slick", "spiky", "messy"):
            # Volume on the outer layer: a crown and a fringe edge.
            self.rect("hat", "top", 1, 1, 6, 6, c)
            self.rect("hat", "front", 1, 0, 6, 1, hi if style == "spiky" else c)
            if style == "spiky":
                for x in (0, 2, 4, 6):
                    self.px("hat", "front", x, 0, hi)
                    self.px("hat", "right", x, 0, c)
                    self.px("hat", "left", x, 0, c)
            if style == "messy":
                self.px("hat", "front", 2, 1, c)
                self.px("hat", "front", 5, 1, c)
                self.rect("hat", "back", 0, 0, 8, 3, c)
            if style == "slick":
                for x in range(1, 7, 2):
                    self.px("head", "top", x, 2, hi)
        if style in ("long", "bob", "braid", "ponytail"):
            length = {"long": 8, "bob": 6, "braid": 5, "ponytail": 5}[style]
            for s in ("right", "left"):
                self.rect("head", s, 0, 0, 8, length, c)
                self.rect("hat", s, 0, 1, 8, length - 1, c)
            self.rect("head", "back", 0, 0, 8, 8, c)
            self.rect("hat", "back", 0, 0, 8, length, c)
            self.rect("hat", "top", 0, 0, 8, 8, c)
            self.rect("hat", "front", 0, 0, 8, 1, c)
            self.px("head", "front", 1, 2, c)
            self.px("head", "front", 6, 2, c)
            # Strands catching the light.
            for x in (1, 3, 5):
                self.px("hat", "back", x, 1, hi)
                self.px("head", "top", x + 1, 3, hi)
            if style in ("braid", "ponytail"):
                # Hanging down the back over the outer layer, plaited for a braid.
                for y in range(0, 8 if style == "braid" else 5):
                    for x in (3, 4):
                        self.px("jacket", "back", x, y, shade(c, 0.8) if style == "braid" and (x + y) % 2 else c)
        if style in ("short", "slick", "spiky", "messy", "buzz"):
            for x in (2, 5):
                self.px("head", "top", x, 1, hi)

    def goggles(self, lens, frame=(30, 30, 34), y=3, up=False):
        y = 2 if up else y
        self.rect("hat", "front", 0, y, 8, 2, frame, flat=True)
        self.rect("hat", "front", 1, y, 2, 2 if not up else 1, lens)
        self.rect("hat", "front", 5, y, 2, 2 if not up else 1, lens)
        for s in ("right", "left", "back"):
            self.rect("hat", s, 0, y, 8, 1, frame, flat=True)

    def glasses(self, frame=(30, 30, 34)):
        """Frames on the outer layer: a rim over each eye and a bridge, the eyes still showing."""
        for x in (0, 3, 4, 7):
            self.px("hat", "front", x, 4, frame)
        for x in (1, 2, 5, 6):
            self.px("hat", "front", x, 3, frame)
        for side in ("right", "left"):
            self.rect("hat", side, 0 if side == "left" else 4, 4, 4, 1, frame, flat=True)

    def visor(self, lens, frame=(70, 74, 82)):
        self.rect("hat", "front", 0, 3, 8, 2, lens)
        self.rect("hat", "front", 0, 3, 8, 1, shade(lens, 1.2))
        for s in ("right", "left"):
            self.rect("hat", s, 3 if s == "left" else 0, 3, 5, 2, frame)
        self.px("hat", "front", 0, 4, frame)
        self.px("hat", "front", 7, 4, frame)

    def cowl(self, c, face_open=True):
        """A cowl over the head on the inner layer, leaving the lower face open."""
        for f in ("top", "right", "left", "back"):
            fx, fy, fw, fh = faces("head")[f]
            self.rect("head", f, 0, 0, fw, fh, c)
        self.rect("head", "front", 0, 0, 8, 3 if face_open else 8, c)
        if face_open:
            for y in (3, 4, 5):
                self.px("head", "front", 0, y, c)
                self.px("head", "front", 7, y, c)

    def save(self, path):
        self.img.save(path)
