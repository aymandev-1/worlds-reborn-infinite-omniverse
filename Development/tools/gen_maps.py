"""Draws the cartographer's two surveys of the valley (Scene 1) and their item icons.

Both maps show the same valley from the same survey points; the new one also shows the ruins and the route to them.
Run: python3 tools/gen_maps.py
"""
import math
import os

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
N = 256
INK = (72, 46, 24, 255)
INK_SOFT = (110, 78, 46, 255)
WATER = (78, 120, 150, 255)
FOREST = (70, 98, 52, 255)
RED = (168, 38, 30, 255)


def parchment(seed):
    rng = np.random.default_rng(seed)
    base = np.array([226, 205, 160], float)
    noise = rng.normal(0, 6, (N // 4, N // 4))
    noise = np.kron(noise, np.ones((4, 4))) + rng.normal(0, 3, (N, N))
    y, x = np.mgrid[0:N, 0:N]
    edge = np.clip(np.minimum.reduce([x, y, N - 1 - x, N - 1 - y]) / 26.0, 0, 1)
    shade = 0.78 + 0.22 * edge
    img = np.zeros((N, N, 4), np.uint8)
    for i in range(3):
        img[..., i] = np.clip((base[i] + noise) * shade, 0, 255)
    img[..., 3] = 255
    # torn corners
    for cx, cy in ((0, 0), (N - 1, 0), (0, N - 1), (N - 1, N - 1)):
        r = rng.integers(6, 12)
        img[(x - cx) ** 2 + (y - cy) ** 2 < r * r, 3] = 0
    return Image.fromarray(img, "RGBA")


def river(d):
    pts = [(int(30 + t * 196), int(150 + 38 * math.sin(t * 5.2) + 20 * t)) for t in np.linspace(0, 1, 60)]
    d.line(pts, fill=WATER, width=6)
    d.line(pts, fill=(120, 160, 186, 255), width=2)


def mountains(d):
    for mx, my, s in ((40, 52, 16), (66, 44, 20), (96, 56, 14), (196, 40, 18), (222, 54, 14)):
        d.polygon([(mx - s, my + s // 2), (mx, my - s), (mx + s, my + s // 2)], outline=INK, fill=(196, 176, 136, 255))
        d.line([(mx, my - s), (mx + s // 3, my - s // 3)], fill=(250, 245, 235, 255), width=2)


def forest(d, rng):
    for _ in range(46):
        x = int(rng.uniform(24, 232))
        y = int(rng.uniform(84, 230))
        if 108 <= x <= 156 and 96 <= y <= 136:
            continue  # the village clearing
        if 168 <= x <= 222 and 92 <= y <= 132:
            continue  # the empty field east of the village
        d.ellipse([x - 4, y - 4, x + 4, y + 4], fill=FOREST, outline=INK)
        d.line([(x, y + 4), (x, y + 7)], fill=INK, width=1)


def village(d):
    for hx, hy in ((116, 104), (130, 100), (144, 108), (122, 118), (138, 122)):
        d.rectangle([hx - 4, hy - 2, hx + 4, hy + 5], fill=(214, 190, 150, 255), outline=INK)
        d.polygon([(hx - 5, hy - 2), (hx, hy - 7), (hx + 5, hy - 2)], fill=(150, 70, 44, 255), outline=INK)
    # the crooked oak beside the village
    d.ellipse([150, 128, 162, 140], fill=FOREST, outline=INK)
    d.line([(156, 140), (154, 146)], fill=INK, width=2)


def compass(d):
    cx, cy = 222, 214
    d.ellipse([cx - 14, cy - 14, cx + 14, cy + 14], outline=INK_SOFT)
    d.polygon([(cx, cy - 13), (cx - 4, cy), (cx + 4, cy)], fill=RED)
    d.polygon([(cx, cy + 13), (cx - 4, cy), (cx + 4, cy)], fill=INK_SOFT)


def border(d):
    d.rectangle([12, 12, N - 13, N - 13], outline=INK_SOFT, width=2)
    d.rectangle([16, 16, N - 17, N - 17], outline=INK_SOFT, width=1)


def ruins(d):
    # broken walls and two towers, drawn in fresh ink
    x0, y0 = 182, 98
    d.rectangle([x0, y0 + 10, x0 + 34, y0 + 28], outline=INK, width=2)
    for tx in (x0 - 2, x0 + 30):
        d.rectangle([tx, y0, tx + 7, y0 + 28], fill=(170, 160, 150, 255), outline=INK)
        for k in range(3):
            d.rectangle([tx + k * 3, y0 - 3, tx + k * 3 + 1, y0], fill=INK)
    d.line([(x0 + 12, y0 + 10), (x0 + 16, y0 + 18), (x0 + 20, y0 + 10)], fill=INK, width=1)
    d.rectangle([x0 + 14, y0 + 20, x0 + 20, y0 + 28], fill=(40, 30, 24, 255))


def route(d):
    pts = [(156, 112), (166, 108), (176, 112), (184, 116)]
    for a, b in zip(pts, pts[1:]):
        steps = 4
        for s in range(steps):
            if s % 2 == 0:
                p = (a[0] + (b[0] - a[0]) * s / steps, a[1] + (b[1] - a[1]) * s / steps)
                q = (a[0] + (b[0] - a[0]) * (s + 1) / steps, a[1] + (b[1] - a[1]) * (s + 1) / steps)
                d.line([p, q], fill=RED, width=2)
    x, y = 199, 134
    d.line([(x - 5, y - 5), (x + 5, y + 5)], fill=RED, width=3)
    d.line([(x - 5, y + 5), (x + 5, y - 5)], fill=RED, width=3)


def survey(new):
    img = parchment(616 if not new else 617)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(7)  # same valley on both maps
    border(d)
    mountains(d)
    river(d)
    forest(d, rng)
    village(d)
    compass(d)
    if new:
        ruins(d)
        route(d)
    return img


def icon(new):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    paper = (224, 204, 158, 255)
    d.rectangle([2, 2, 13, 13], fill=paper, outline=(96, 66, 36, 255))
    d.line([(3, 9), (6, 8), (9, 10), (12, 9)], fill=WATER)
    d.point([(4, 5), (5, 6), (10, 4)], fill=FOREST)
    if new:
        d.rectangle([9, 5, 11, 7], fill=(120, 110, 100, 255))
        d.point([(7, 6), (8, 6)], fill=RED)
        d.line([(1, 1), (1, 3)], fill=(212, 175, 55, 255))
    else:
        d.point([(11, 12)], fill=(96, 66, 36, 255))
    return img


def main():
    import json
    for name, new in (("old_survey", False), ("new_survey", True)):
        survey(new).save(os.path.join(ASSETS, "textures/gui", name + ".png"))
        icon(new).save(os.path.join(ASSETS, "textures/item", name + ".png"))
        with open(os.path.join(ASSETS, "models/item", name + ".json"), "w") as f:
            json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "wrmarvel:item/" + name}}, f, indent=2)
    print("maps drawn")


if __name__ == "__main__":
    main()
