"""Skins for Act 1 Scenes 6-11.

- Daredevil, Jessica Jones, Luke Cage, Danny Rand (Iron Fist), Frank Castle (Punisher) and Uatu are the skins the user
  supplied on 2026-10-17 (planetminecraft downloads), copied unchanged from canon/marvel/art/.
- Everyone else is drawn here with the same simple builder as tools/gen_textures.py: TVA reception/operations staff and
  a medic, the five refugees (each clearly different so they can be recognised again in Scene 10), other Watchers
  (Uatu's skin with re-coloured robes), the unidentified attackers (dark armour, red visor, no insignia) and a bystander.
Run from the project root: python3 tools/gen_skins_6_11.py
"""
import colorsys
import os
import random
import shutil
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_textures import SKINS, base_human, faces, noisy_fill, paint, px, shade  # noqa: E402

ART = "/mnt/project-files/canon/marvel/art"
USER_SKINS = {
    "daredevil": "daredevil_skin_user.png",
    "jessica_jones": "jessica_jones_skin_user.png",
    "luke_cage": "luke_cage_skin_user.png",
    "danny_rand": "iron_fist_skin_user.png",
    "frank_castle": "punisher_skin_user.png",
    "uatu": "uatu_skin_user.png",
}


def tva_staff(rnd):
    """Reception / operations staff: shirt sleeves, brown waistcoat, amber TVA lanyard."""
    img = base_human(rnd, (214, 172, 140), (205, 190, 160), (78, 62, 46), (40, 32, 26), (70, 48, 30))
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 1, y + 1, 6, 9), (110, 78, 50), rnd)
    noisy_fill(img, (x + 3, y, 2, 6), (200, 190, 170), rnd)
    noisy_fill(img, (x + 3, y + 1, 1, 6), (230, 120, 30), rnd)
    return img


def tva_medic(rnd):
    img = base_human(rnd, (150, 105, 75), (236, 236, 230), (64, 70, 86), (30, 30, 34), (24, 20, 18))
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 3, y + 2, 2, 5), (200, 40, 40), rnd)
    noisy_fill(img, (x + 2, y + 3, 4, 2), (200, 40, 40), rnd)
    for arm in ("rarm", "larm"):
        x, y, w, h = faces(arm)["front"]
        noisy_fill(img, (x, y + 2, w, 1), (230, 120, 30), rnd)
    return img


def refugee(rnd, skin, shirt, pants, hair, hair_len=2, extra=None):
    img = base_human(rnd, skin, shirt, pants, (58, 46, 40), hair, hair_len)
    if extra:
        extra(img, rnd)
    return img


def scarf(color):
    def draw(img, rnd):
        x, y, w, h = faces("body")["front"]
        noisy_fill(img, (x, y, w, 2), color, rnd)
        noisy_fill(img, (x + 5, y + 2, 2, 4), color, rnd)
    return draw


def bandage(img, rnd):
    """Teodor: older, grey hair and beard, a sling on his left arm."""
    px(img, "head", "front", 1, 6, (190, 190, 190), w=6, h=2)
    x, y, w, h = faces("larm")["front"]
    noisy_fill(img, (x, y + 3, w, 4), (240, 240, 235), rnd)
    x, y, w, h = faces("body")["front"]
    for i in range(6):
        img.putpixel((x + 1 + i, y + 1 + i // 2), (240, 240, 235, 255))


def backpack(img, rnd):
    x, y, w, h = faces("body")["back"]
    noisy_fill(img, (x + 1, y + 1, 6, 8), (200, 110, 40), rnd)
    x, y, w, h = faces("body")["front"]
    noisy_fill(img, (x + 1, y, 1, 6), (120, 70, 30), rnd)
    noisy_fill(img, (x + 6, y, 1, 6), (120, 70, 30), rnd)


def attacker(rnd):
    """Unidentified attacker: gunmetal plates, black under-suit, red glowing visor slit. No insignia of any kind."""
    plate, suit, dark = (58, 60, 66), (26, 26, 30), (16, 16, 18)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for p in ("head", "body", "rarm", "larm", "rleg", "lleg"):
        paint(img, p, suit, rnd)
    paint(img, "head", plate, rnd)
    px(img, "head", "front", 1, 3, (20, 6, 6), w=6, h=2)
    px(img, "head", "front", 1, 3, (255, 60, 40), w=2)
    px(img, "head", "front", 5, 3, (255, 60, 40), w=2)
    px(img, "head", "front", 3, 6, dark, w=2, h=2)
    for f in ("front", "back"):
        x, y, w, h = faces("body")[f]
        noisy_fill(img, (x + 1, y + 1, 6, 6), plate, rnd)
        noisy_fill(img, (x, y + 8, w, 1), dark, rnd)
        img.putpixel((x + 3, y + 3), (200, 40, 30, 255))
        img.putpixel((x + 4, y + 3), (200, 40, 30, 255))
    for arm in ("rarm", "larm"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(arm)[f]
            noisy_fill(img, (x, y, w, 4), plate, rnd)
            noisy_fill(img, (x, y + 8, w, 4), plate, rnd)
    for leg in ("rleg", "lleg"):
        for f in ("front", "back", "left", "right"):
            x, y, w, h = faces(leg)[f]
            noisy_fill(img, (x, y + 3, w, 3), plate, rnd)
            noisy_fill(img, (x, y + 9, w, 3), dark, rnd)
    return img


def recolour_robes(src, hue_shift, sat=1.0, val=1.0):
    """Another Watcher: Uatu's skin with the robes re-coloured; skin tones (low saturation, warm) are kept."""
    img = src.copy()
    pxs = img.load()
    for x in range(img.width):
        for y in range(img.height):
            r, g, b, a = pxs[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            warm = 0.0 <= h <= 0.14 and s < 0.6 and v > 0.45
            if warm or s < 0.12:
                continue
            nr, ng, nb = colorsys.hsv_to_rgb((h + hue_shift) % 1.0, min(1, s * sat), min(1, v * val))
            pxs[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return img


def main():
    rnd = random.Random(611)
    for name, art in USER_SKINS.items():
        shutil.copy(os.path.join(ART, art), os.path.join(SKINS, name + ".png"))
    uatu = Image.open(os.path.join(SKINS, "uatu.png")).convert("RGBA")
    skins = {
        "tva_staff": tva_staff(rnd),
        "tva_medic": tva_medic(rnd),
        "unknown_attacker": attacker(rnd),
        "watcher": recolour_robes(uatu, 0.12, 1.1, 0.8),
        # Ana (green coat and red scarf), Jonah (blue jacket, beard), Teodor (older, sling), Mara (purple, long
        # dark hair), Kip (orange backpack).
        "refugee_ana": refugee(rnd, (226, 186, 152), (62, 120, 70), (70, 60, 52), (120, 60, 30), 3, scarf((190, 40, 40))),
        "refugee_jonah": refugee(rnd, (196, 150, 112), (52, 86, 150), (54, 54, 60), (50, 34, 24), 1,
                                 lambda i, r: px(i, "head", "front", 1, 6, (50, 34, 24), w=6, h=2)),
        "refugee_teodor": refugee(rnd, (232, 196, 170), (120, 110, 96), (80, 76, 70), (196, 196, 196), 1, bandage),
        "refugee_mara": refugee(rnd, (120, 84, 60), (120, 70, 150), (40, 40, 52), (20, 16, 14), 4),
        "refugee_kip": refugee(rnd, (240, 210, 180), (230, 230, 220), (60, 90, 140), (220, 170, 70), 2, backpack),
        "bystander": base_human(rnd, (170, 120, 90), (90, 140, 120), (50, 50, 60), (40, 40, 44), (40, 26, 18)),
    }
    for name, img in skins.items():
        img.save(os.path.join(SKINS, name + ".png"))
    print("wrote", len(USER_SKINS) + len(skins), "skins")


if __name__ == "__main__":
    main()
