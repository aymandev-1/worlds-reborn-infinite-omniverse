"""Generates the TEMPORARY placeholder skins and block textures for Worlds Reborn: Marvel Scenes 1-5.

These are simple programmatic stand-ins so every character and block is visible and recognisable in game.
They are meant to be replaced by final art. Run: python3 tools/gen_textures.py (from the project root).
"""
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SKINS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/textures/entity")
BLOCKS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/textures/block")
CHAR_MISSING = os.path.join(ROOT, "characters/src/main/resources/assets/wrcharacters/textures/entity")

# Player skin UV layout (64x64). Each part: origin (u, v) and size (w, h, d).
PARTS = {
    "head": ((0, 0), (8, 8, 8)), "hat": ((32, 0), (8, 8, 8)),
    "body": ((16, 16), (8, 12, 4)), "jacket": ((16, 32), (8, 12, 4)),
    "rarm": ((40, 16), (4, 12, 4)), "rsleeve": ((40, 32), (4, 12, 4)),
    "larm": ((32, 48), (4, 12, 4)), "lsleeve": ((48, 48), (4, 12, 4)),
    "rleg": ((0, 16), (4, 12, 4)), "rpants": ((0, 32), (4, 12, 4)),
    "lleg": ((16, 48), (4, 12, 4)), "lpants": ((0, 48), (4, 12, 4)),
}


def faces(part):
    (u, v), (w, h, d) = PARTS[part]
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }


def shade(c, f):
    return tuple(max(0, min(255, int(x * f))) for x in c[:3]) + ((c[3],) if len(c) == 4 else (255,))


def noisy_fill(img, rect, color, rnd, amount=0.06):
    x, y, w, h = rect
    for i in range(x, x + w):
        for j in range(y, y + h):
            img.putpixel((i, j), shade(color, 1 + rnd.uniform(-amount, amount)))


def paint(img, part, color, rnd, sides=None):
    for name, rect in faces(part).items():
        c = (sides or {}).get(name, color)
        f = {"top": 1.08, "bottom": 0.8, "left": 0.92, "right": 0.92, "back": 0.9}.get(name, 1.0)
        noisy_fill(img, rect, shade(c, f), rnd)


def front(part):
    return faces(part)["front"]


def px(img, part, face, dx, dy, color, w=1, h=1):
    x, y, _, _ = faces(part)[face]
    for i in range(w):
        for j in range(h):
            img.putpixel((x + dx + i, y + dy + j), color if len(color) == 4 else color + (255,))


def base_human(rnd, skin, shirt, pants, shoes, hair, hair_len=2):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    paint(img, "head", skin, rnd)
    for f in ("top",):
        noisy_fill(img, faces("head")[f], hair, rnd)
    for f in ("back",):
        noisy_fill(img, faces("head")[f], hair, rnd)
    for f in ("left", "right", "front"):
        x, y, w, h = faces("head")[f]
        noisy_fill(img, (x, y, w, hair_len), hair, rnd)
    for f in ("left", "right"):
        x, y, w, h = faces("head")[f]
        noisy_fill(img, (x + (w - 3 if f == "left" else 0), y, 3, 5), hair, rnd)
    # eyes and mouth
    px(img, "head", "front", 1, 4, (255, 255, 255)); px(img, "head", "front", 2, 4, (60, 90, 150))
    px(img, "head", "front", 5, 4, (60, 90, 150)); px(img, "head", "front", 6, 4, (255, 255, 255))
    px(img, "head", "front", 3, 6, shade(skin, 0.7), w=2)
    paint(img, "body", shirt, rnd)
    paint(img, "rarm", shirt, rnd); paint(img, "larm", shirt, rnd)
    for arm in ("rarm", "larm"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(arm)[f]
            noisy_fill(img, (x, y + 9, w, 3), skin, rnd)
        noisy_fill(img, faces(arm)["bottom"], skin, rnd)
    paint(img, "rleg", pants, rnd); paint(img, "lleg", pants, rnd)
    for leg in ("rleg", "lleg"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(leg)[f]
            noisy_fill(img, (x, y + 10, w, 2), shoes, rnd)
        noisy_fill(img, faces(leg)["bottom"], shoes, rnd)
    return img


def spiderman(rnd):
    red, blue, black = (196, 30, 40), (30, 60, 160), (25, 20, 25)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for p in ("head", "body", "rarm", "larm", "rleg", "lleg"):
        paint(img, p, red, rnd)
    # blue sides of torso, blue legs with red boots, blue upper arms
    for f in ("left", "right"):
        noisy_fill(img, faces("body")[f], blue, rnd)
    for leg in ("rleg", "lleg"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(leg)[f]
            noisy_fill(img, (x, y, w, 8), blue, rnd)
    for arm in ("rarm", "larm"):
        for f in ("left", "right", "back"):
            x, y, w, h = faces(arm)[f]
            noisy_fill(img, (x, y + 1, w, 7), blue, rnd)
    # web lines (dark) on red areas
    for p in ("head", "body"):
        for f, (x, y, w, h) in faces(p).items():
            if f in ("top", "bottom"):
                continue
            for i in range(0, w, 3):
                for j in range(h):
                    if img.getpixel((x + i, y + j))[:3] != blue:
                        img.putpixel((x + i, y + j), shade(red, 0.55))
            for j in range(0, h, 3):
                for i in range(w):
                    if img.getpixel((x + i, y + j))[:3] != blue:
                        img.putpixel((x + i, y + j), shade(red, 0.55))
    # big white eyes with black rim
    for dx in (1, 5):
        px(img, "head", "front", dx, 2, black, w=2, h=3)
        px(img, "head", "front", dx, 3, (240, 240, 245), w=2, h=1)
        px(img, "head", "front", dx + (1 if dx == 1 else 0), 2, (240, 240, 245))
    # spider emblem on chest (black)
    px(img, "body", "front", 3, 3, black, w=2, h=3)
    px(img, "body", "front", 2, 2, black); px(img, "body", "front", 5, 2, black)
    px(img, "body", "front", 2, 6, black); px(img, "body", "front", 5, 6, black)
    return img


def hooded(rnd):
    cloak, dark, lining = (52, 50, 58), (22, 20, 26), (70, 66, 78)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for p in ("head", "body", "rarm", "larm", "rleg", "lleg"):
        paint(img, p, cloak, rnd)
    # shadowed face, faint pale eyes
    noisy_fill(img, (faces("head")["front"][0] + 1, faces("head")["front"][1] + 2, 6, 6), dark, rnd, 0.02)
    px(img, "head", "front", 2, 5, (180, 200, 215)); px(img, "head", "front", 5, 5, (180, 200, 215))
    # hood as hat layer: everything except the face opening
    for f, (x, y, w, h) in faces("hat").items():
        noisy_fill(img, (x, y, w, h), shade(cloak, 0.95), rnd)
    x, y, w, h = faces("hat")["front"]
    for i in range(1, 7):
        for j in range(2, 8):
            img.putpixel((x + i, y + j), (0, 0, 0, 0))
    # cloak hem and belt
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x, y + 6, w, 1), lining, rnd)
    noisy_fill(img, (x + 3, y, 2, 12), shade(cloak, 0.8), rnd)
    return img


def mobius(rnd):
    skin, suit, shirt, hair = (232, 190, 160), (120, 86, 60), (215, 205, 180), (200, 200, 200)
    img = base_human(rnd, skin, suit, suit, (50, 36, 28), hair, hair_len=1)
    # moustache and shirt/tie
    px(img, "head", "front", 2, 6, hair, w=4)
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 2, y, 4, 5), shirt, rnd)
    noisy_fill(img, (x + 3, y + 1, 2, 5), (60, 60, 70), rnd)
    return img


def robber(rnd):
    img = base_human(rnd, (210, 170, 140), (40, 42, 48), (52, 56, 70), (25, 25, 28), (20, 20, 22))
    # balaclava: whole head dark except eye strip
    paint(img, "head", (24, 24, 28), rnd)
    px(img, "head", "front", 1, 3, (210, 170, 140), w=6, h=2)
    px(img, "head", "front", 2, 3, (30, 30, 30)); px(img, "head", "front", 5, 3, (30, 30, 30))
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 2, y + 7, 4, 3), (32, 34, 40), rnd)  # hoodie pocket
    return img


def tva_guard(rnd):
    """TVA guard (generic Minuteman look): dark tactical uniform, armoured vest, helmet with a visor band."""
    skin, suit, vest, boots = (205, 160, 125), (58, 52, 44), (34, 34, 36), (22, 20, 18)
    img = base_human(rnd, skin, suit, suit, boots, (40, 30, 24), hair_len=1)
    # helmet: top, back and sides of the head, rim over the brow
    helmet = (46, 44, 40)
    for f in ("top", "back"):
        noisy_fill(img, faces("head")[f], helmet, rnd)
    for f in ("left", "right", "front"):
        x, y, w, h = faces("head")[f]
        noisy_fill(img, (x, y, w, 3), helmet, rnd)
    px(img, "head", "front", 0, 3, (230, 120, 30), w=8)  # amber band
    # armoured vest with an amber TVA stripe and belt
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 1, y + 1, 6, 7), vest, rnd)
    noisy_fill(img, (x + 1, y + 3, 6, 1), (230, 120, 30), rnd)
    noisy_fill(img, (x, y + 9, w, 1), (20, 20, 20), rnd)
    x, y, w, h = faces("body")["back"]
    noisy_fill(img, (x + 1, y + 1, 6, 7), vest, rnd)
    # gloves
    for arm in ("rarm", "larm"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(arm)[f]
            noisy_fill(img, (x, y + 9, w, 3), (24, 22, 20), rnd)
    return img


def civilian(rnd, shirt, pants, hair, skin):
    return base_human(rnd, skin, shirt, pants, (60, 50, 45), hair)


def missing():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 255))
    for i in range(64):
        for j in range(64):
            if (i // 4 + j // 4) % 2 == 0:
                img.putpixel((i, j), (248, 0, 248, 255))
    return img


def block_tex(name, painter):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    painter(img, random.Random(hash(name) & 0xFFFF))
    img.save(os.path.join(BLOCKS, name + ".png"))


def portal(img, rnd):
    for i in range(16):
        for j in range(16):
            d = ((i - 7.5) ** 2 + (j - 7.5) ** 2) ** 0.5
            t = (d + rnd.uniform(-1.2, 1.2)) % 4 / 4
            c = (int(150 + 90 * t), int(190 + 50 * t), 245)
            img.putpixel((i, j), c + (170 + int(60 * (1 - t)),))


def portal_core(img, rnd):
    for i in range(16):
        for j in range(16):
            v = rnd.randint(38, 52)
            img.putpixel((i, j), (v, v, v + 6, 255))
    for k in range(3, 13):
        img.putpixel((k, 7), (150, 200, 245, 255)); img.putpixel((7, k), (150, 200, 245, 255))
    for k in (4, 11):
        img.putpixel((k, 4), (190, 220, 250, 255)); img.putpixel((k, 11), (190, 220, 250, 255))


def trace(img, rnd):
    for _ in range(14):
        i, j = rnd.randint(1, 14), rnd.randint(1, 14)
        img.putpixel((i, j), (200, 225, 255, rnd.randint(120, 220)))


def terminal(img, rnd):
    for i in range(16):
        for j in range(16):
            v = rnd.randint(70, 84)
            img.putpixel((i, j), (v, v - 12, v - 26, 255))
    for i in range(2, 14):
        for j in range(2, 11):
            img.putpixel((i, j), (40, 22, 8, 255))
    for j in (4, 6, 8):
        for i in range(3, 3 + rnd.randint(5, 10)):
            img.putpixel((i, j), (232, 163, 61, 255))
    for i in range(3, 13, 3):
        img.putpixel((i, 13), (232, 163, 61, 255))


def main():
    os.makedirs(SKINS, exist_ok=True)
    os.makedirs(BLOCKS, exist_ok=True)
    os.makedirs(CHAR_MISSING, exist_ok=True)
    rnd = random.Random(616)
    spiderman(rnd)  # still drawn so the other skins keep their random details; the real skin is gen_spiderman_hd.py
    skins = {
        "hooded_traveller": hooded(rnd),
        "mobius": mobius(rnd),
        "robber": robber(rnd),
        "tva_guard": tva_guard(rnd),
        "civilian_1": civilian(rnd, (70, 120, 170), (60, 60, 80), (90, 60, 30), (230, 190, 160)),
        "civilian_2": civilian(rnd, (170, 70, 80), (40, 50, 90), (30, 25, 20), (150, 105, 75)),
        "civilian_3": civilian(rnd, (220, 190, 80), (90, 80, 70), (200, 160, 80), (240, 205, 175)),
    }
    for name, img in skins.items():
        img.save(os.path.join(SKINS, name + ".png"))
    missing().save(os.path.join(CHAR_MISSING, "missing_character.png"))
    # silver_portal is animated now: tools/gen_fx.py draws it.
    block_tex("portal_core", portal_core)
    block_tex("fading_trace", trace)
    block_tex("tva_terminal", terminal)
    print("generated", len(skins), "skins and 4 block textures")


if __name__ == "__main__":
    main()
