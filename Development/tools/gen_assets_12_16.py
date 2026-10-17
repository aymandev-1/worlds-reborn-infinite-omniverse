"""Block, item and picture art for Act 1 Scenes 12-16, plus their block/item model JSON (GENERATED STAND-INS).

- The scene controls (one block, eight kinds: flight route table, door brace, recorder, power, dish alignment, channel
  test, transmitter, clamps) and Sue Storm's force field.
- The monitoring recorder item.
- Pictures shown during conversations: Shuri's processed imagery of the other Earth (different coastlines, surface
  features, artificial lights; 12.6), the joint display comparing flight readings, facility records and public sky
  imagery (14.1), Reed's restrained view of several detected events, not a full map (15.6), and the station recording
  of unfamiliar terrain at the station's coordinates (16.6).
Run from the project root: python3 tools/gen_assets_12_16.py
"""
import json
import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
A = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
BLOCK_TEX = os.path.join(A, "textures/block")
ITEM_TEX = os.path.join(A, "textures/item")
GUI = os.path.join(A, "textures/gui/cinematic")

KINDS = ["route_table", "brace", "recorder", "power", "dish", "test", "transmit", "clamp"]


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=1)


def panel(rnd, base=(70, 76, 86)):
    img = Image.new("RGBA", (16, 16))
    p = img.load()
    for x in range(16):
        for y in range(16):
            k = rnd.uniform(-6, 6)
            edge = x in (0, 15) or y in (0, 15)
            c = tuple(max(0, min(255, int(v * (0.7 if edge else 1) + k))) for v in base)
            p[x, y] = c + (255,)
    return img


def control(kind, rnd):
    img = panel(rnd)
    d = ImageDraw.Draw(img)
    if kind == "route_table":
        d.rectangle((2, 2, 13, 13), fill=(16, 30, 52))
        pts = [(3, 11), (6, 5), (9, 9), (12, 4)]
        d.line(pts, fill=(90, 200, 255))
        for x, y in pts[1:]:
            img.putpixel((x, y), (255, 230, 120, 255))
    elif kind == "brace":
        d.rectangle((2, 2, 13, 13), fill=(150, 112, 70))
        d.line((2, 13, 13, 2), fill=(90, 64, 40), width=2)
        d.rectangle((6, 6, 9, 9), fill=(120, 124, 130))
    elif kind == "recorder":
        d.rectangle((2, 3, 13, 12), fill=(30, 32, 36))
        d.rectangle((3, 4, 12, 7), fill=(40, 120, 70))
        d.line((3, 6, 5, 5, 7, 7, 9, 4, 12, 6), fill=(150, 255, 170))
        img.putpixel((11, 10), (230, 60, 50, 255))
    elif kind == "power":
        d.rectangle((3, 3, 12, 12), fill=(40, 40, 44))
        d.rectangle((6, 4, 9, 11), fill=(200, 60, 40))
        d.rectangle((6, 4, 9, 6), fill=(240, 220, 80))
    elif kind == "dish":
        d.ellipse((2, 2, 13, 13), fill=(220, 224, 228))
        d.ellipse((5, 5, 10, 10), fill=(150, 156, 164))
        d.line((7, 7, 13, 2), fill=(60, 60, 64))
    elif kind == "test":
        d.rectangle((2, 2, 13, 13), fill=(20, 24, 30))
        for x in range(3, 13):
            y = 8 + int(3 * math.sin(x * 1.1))
            img.putpixel((x, y), (90, 220, 255, 255))
        d.rectangle((10, 11, 12, 12), fill=(80, 220, 120))
    elif kind == "transmit":
        d.rectangle((2, 2, 13, 13), fill=(14, 20, 36))
        for r in (3, 5, 7):
            d.arc((7 - r, 9 - r, 7 + r, 9 + r), 220, 320, fill=(120, 200, 255))
        d.rectangle((6, 9, 8, 13), fill=(200, 200, 210))
    elif kind == "clamp":
        d.rectangle((2, 5, 13, 10), fill=(110, 116, 124))
        d.rectangle((2, 5, 4, 10), fill=(226, 186, 40))
        d.rectangle((11, 5, 13, 10), fill=(226, 186, 40))
    return img


def force_field():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    p = img.load()
    for x in range(16):
        for y in range(16):
            # A faint hex lattice, brighter at the edges, mostly see-through.
            hx = (x + (y // 4 % 2) * 2) % 4 == 0 or y % 4 == 0
            a = 70 if hx else 34
            if x in (0, 15) or y in (0, 15):
                a = 26
            p[x, y] = (150, 215, 255, a)
    return img


def blocks():
    rnd = random.Random(1216)
    variants = {}
    for k in KINDS:
        name = "story_control_" + k
        control(k, rnd).save(os.path.join(BLOCK_TEX, name + ".png"))
        write_json(os.path.join(A, "models/block", name + ".json"),
                   {"parent": "minecraft:block/cube_all", "textures": {"all": "wrmarvel:block/" + name}})
        variants["kind=" + k] = {"model": "wrmarvel:block/" + name}
    write_json(os.path.join(A, "blockstates/story_control.json"), {"variants": variants})
    force_field().save(os.path.join(BLOCK_TEX, "force_field.png"))
    write_json(os.path.join(A, "models/block/force_field.json"),
               {"parent": "minecraft:block/cube_all", "render_type": "minecraft:translucent",
                "textures": {"all": "wrmarvel:block/force_field", "particle": "wrmarvel:block/force_field"}})
    write_json(os.path.join(A, "blockstates/force_field.json"), {"variants": {"": {"model": "wrmarvel:block/force_field"}}})


def items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle((2, 4, 13, 12), fill=(36, 38, 44), outline=(14, 14, 16))
    d.rectangle((4, 6, 11, 9), fill=(40, 120, 70))
    d.line((4, 8, 6, 7, 8, 9, 10, 6, 11, 8), fill=(150, 255, 170))
    d.rectangle((6, 2, 9, 3), fill=(120, 124, 130))
    img.putpixel((12, 11), (230, 60, 50, 255))
    img.save(os.path.join(ITEM_TEX, "monitoring_recorder.png"))
    write_json(os.path.join(A, "models/item/monitoring_recorder.json"),
               {"parent": "minecraft:item/generated", "textures": {"layer0": "wrmarvel:item/monitoring_recorder"}})


# ------------------------------------------------------------------------------------------------ pictures

def fbm(size, octaves, seed):
    g = np.random.default_rng(seed)
    out = np.zeros((size, size), np.float32)
    amp = 1.0
    for o in range(octaves):
        n = 4 * 2 ** o
        grid = g.random((n + 1, n + 1)).astype(np.float32)
        img = Image.fromarray((grid * 255).astype(np.uint8)).resize((size, size), Image.BICUBIC)
        out += np.asarray(img, np.float32) / 255 * amp
        amp *= 0.5
    return out / out.max()


def shuri_imagery(w=256, h=144):
    """Processed surface imagery: a crescent of the other Earth, its coastlines traced, the night side's lights marked."""
    size = 144
    land = fbm(size, 6, 1212)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    c = (size - 1) / 2
    dx, dy = (xx - c) / (size * 0.46), (yy - c) / (size * 0.46)
    d = np.hypot(dx, dy)
    disc = d < 1
    lit = np.clip(0.2 + 1.2 * (dx * -0.7 + dy * -0.2 + np.sqrt(np.clip(1 - d * d, 0, 1)) * 0.5), 0, 1)
    is_land = land > 0.6
    col = np.where(is_land[..., None], np.stack([60 + 40 * land, 100 + 40 * land, 60 + 20 * land], -1),
                   np.stack([20 + 0 * land, 50 + 20 * land, 90 + 40 * land], -1))
    col = col * (0.15 + 0.85 * lit[..., None])
    rgba = np.zeros((size, size, 4), np.float32)
    rgba[..., :3] = col
    rgba[..., 3] = disc * 255
    planet = Image.fromarray(rgba.clip(0, 255).astype(np.uint8), "RGBA").copy()
    pd = planet.load()
    rnd = random.Random(1212)
    # Coastlines traced in cyan.
    for y in range(1, size - 1):
        for x in range(1, size - 1):
            if disc[y, x] and is_land[y, x] != is_land[y, x + 1] or disc[y, x] and is_land[y, x] != is_land[y + 1, x]:
                pd[x, y] = (110, 230, 255, 255)
    # Artificial lights on the night side's land: small warm clusters.
    for _ in range(140):
        x, y = rnd.randrange(size), rnd.randrange(size)
        if disc[y, x] and is_land[y, x] and lit[y, x] < 0.35:
            pd[x, y] = (255, 214, 120, 255)
            if rnd.random() < 0.4 and x + 1 < size:
                pd[x + 1, y] = (255, 190, 90, 255)
    img = Image.new("RGBA", (w, h), (6, 10, 20, 255))
    img.alpha_composite(planet, ((w - size) // 2, 0))
    dr = ImageDraw.Draw(img)
    # Analysis frame: corner brackets and a scale bar, no words (the conversation says what it shows).
    for (x0, y0, sx, sy) in ((8, 8, 1, 1), (w - 9, 8, -1, 1), (8, h - 9, 1, -1), (w - 9, h - 9, -1, -1)):
        dr.line((x0, y0, x0 + 12 * sx, y0), fill=(120, 200, 255))
        dr.line((x0, y0, x0, y0 + 12 * sy), fill=(120, 200, 255))
    dr.line((14, h - 18, 50, h - 18), fill=(200, 220, 240))
    return img


def joint_display(w=256, h=144):
    """Three panels side by side: flight readings, facility records, sky imagery; the same rising trend in each."""
    img = Image.new("RGBA", (w, h), (10, 14, 22, 255))
    d = ImageDraw.Draw(img)
    rnd = random.Random(1414)
    pw = (w - 16) // 3
    colours = [(90, 200, 255), (150, 255, 170), (255, 200, 110)]
    for i in range(3):
        x0 = 4 + i * (pw + 4)
        d.rectangle((x0, 6, x0 + pw, h - 6), outline=(60, 80, 100), fill=(14, 20, 32))
        # Appearances lasting longer, intervals shortening: bars that grow and crowd together.
        x = x0 + 6
        gap = 26
        length = 4
        while x < x0 + pw - 6:
            top = h - 16 - int(length * 1.6 + rnd.uniform(-2, 2))
            d.rectangle((x, top, x + 3, h - 16), fill=colours[i])
            x += gap
            gap = max(7, int(gap * 0.78))
            length += 6
        d.line((x0 + 4, h - 15, x0 + pw - 4, h - 15), fill=(80, 96, 120))
        if i == 2:
            # Public sky imagery: a small disc above the bars.
            d.ellipse((x0 + pw // 2 - 10, 14, x0 + pw // 2 + 10, 34), fill=(60, 110, 170), outline=(160, 210, 255))
    return img


def detected_events(w=256, h=144):
    """A restrained plot: two known universes and several separate detected events, partly outside the measured
    region. Not a map of the multiverse."""
    img = Image.new("RGBA", (w, h), (8, 10, 18, 255))
    d = ImageDraw.Draw(img)
    # The measured region: a rounded box; what is outside it is simply not drawn.
    d.rounded_rectangle((16, 12, w - 16, h - 12), radius=10, outline=(70, 90, 120))
    for gx in range(36, w - 16, 20):
        d.line((gx, 14, gx, h - 14), fill=(18, 24, 36))
    for gy in range(32, h - 12, 20):
        d.line((18, gy, w - 18, gy), fill=(18, 24, 36))
    a, b = (92, 76), (150, 72)
    for (x, y) in (a, b):
        d.ellipse((x - 7, y - 7, x + 7, y + 7), outline=(140, 210, 255), width=2)
    d.line((a[0] + 8, a[1], b[0] - 8, b[1]), fill=(255, 200, 110), width=2)
    rnd = random.Random(1515)
    for (x, y) in ((44, 40), (206, 36), (214, 112), (60, 116), (128, 124)):
        r = 3 + rnd.randrange(3)
        d.ellipse((x - r, y - r, x + r, y + r), outline=(255, 120, 90))
        d.ellipse((x - 1, y - 1, x + 1, y + 1), fill=(255, 160, 120))
    # Unfinished edge: dashes where the count is incomplete.
    for x in range(20, w - 20, 8):
        d.line((x, h - 8, x + 3, h - 8), fill=(70, 90, 120))
    return img


def overlap_terrain(w=256, h=144):
    """The station recording: the familiar horizon, and for a moment, terrain that is not theirs at the same place."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    px = img.load()
    for y in range(h):
        for x in range(w):
            sky = (int(110 + 60 * y / h), int(150 + 50 * y / h), int(200 + 30 * y / h))
            px[x, y] = sky + (255,)
    d = ImageDraw.Draw(img)
    # Their landscape: gentle green hills, the school's treeline.
    pts = [(0, 96)] + [(x, int(96 - 8 * math.sin(x / 30))) for x in range(0, w + 1, 8)] + [(w, h), (0, h)]
    d.polygon(pts, fill=(80, 130, 60))
    for x in range(10, w, 22):
        d.ellipse((x, 80, x + 14, 96), fill=(40, 90, 40))
    # The other terrain, banded through the middle: red rock spires and a different sky.
    band = (96, 0, 176, h)
    for y in range(h):
        for x in range(band[0], band[2]):
            px[x, y] = (int(150 + 40 * y / h), int(90 + 20 * y / h), int(110 + 10 * y / h), 255)
    for x in range(band[0], band[2], 10):
        top = 40 + (x * 37) % 30
        d.polygon([(x, h), (x + 5, top), (x + 10, h)], fill=(140, 60, 40))
    # The edges of the overlap.
    d.line((band[0], 0, band[0], h), fill=(255, 240, 200), width=1)
    d.line((band[2], 0, band[2], h), fill=(255, 240, 200), width=1)
    return img


def pictures():
    shuri_imagery().save(os.path.join(GUI, "shuri_imagery.png"))
    joint_display().save(os.path.join(GUI, "joint_display.png"))
    detected_events().save(os.path.join(GUI, "detected_events.png"))
    overlap_terrain().save(os.path.join(GUI, "overlap_terrain.png"))


def main():
    blocks()
    items()
    pictures()
    print("Scenes 12-16 assets written")


if __name__ == "__main__":
    main()
