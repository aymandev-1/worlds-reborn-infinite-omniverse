"""Block, item and cinematic art for Act 1 Scenes 6-11, plus their block/item model JSON.

Everything here is drawn procedurally (no outside art): the rift doorway, fallen supplies and debris, the TVA repair
panel, the recording display, the supply shelf, Spider-Man's web anchor points, the reception supply packages,
Peter's web-shooters and scanner, and the art for the Scenes 6-11 cinematics and the other Earth in the sky.
Run from the project root: python3 tools/gen_assets_6_11.py
"""
import json
import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
A = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
BLOCK_TEX = os.path.join(A, "textures/block")
ITEM_TEX = os.path.join(A, "textures/item")
GUI = os.path.join(A, "textures/gui/cinematic")
SKINS = os.path.join(A, "textures/entity")


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=1)


def noise_tile(rnd, base, spread, size=16):
    img = Image.new("RGBA", (size, size))
    px = img.load()
    for x in range(size):
        for y in range(size):
            k = rnd.uniform(-spread, spread)
            px[x, y] = tuple(max(0, min(255, int(c + k))) for c in base) + (255,)
    return img


# ------------------------------------------------------------------------------------------------ blocks

def rift_frames(n=16, size=16):
    """Red-violet unstable doorway: drifting cells of colour, different from the calm orange TVA door."""
    frames = []
    rnd = np.random.default_rng(7)
    seeds = rnd.random((6, 2)) * size
    for f in range(n):
        t = f / n * 2 * math.pi
        img = np.zeros((size, size, 4), dtype=np.float32)
        yy, xx = np.mgrid[0:size, 0:size]
        v = np.zeros((size, size))
        for i, (sx, sy) in enumerate(seeds):
            cx = (sx + math.sin(t + i) * 3) % size
            cy = (sy + math.cos(t * 1.3 + i) * 3) % size
            d = np.hypot(xx - cx, yy - cy)
            v += np.exp(-d * d / 10)
        v = v / v.max()
        img[..., 0] = 120 + 120 * v
        img[..., 1] = 20 + 40 * v
        img[..., 2] = 120 + 90 * (1 - v)
        img[..., 3] = 150 + 90 * v
        frames.append(Image.fromarray(img.clip(0, 255).astype(np.uint8), "RGBA"))
    strip = Image.new("RGBA", (size, size * n))
    for i, fr in enumerate(frames):
        strip.paste(fr, (0, i * size))
    strip.save(os.path.join(BLOCK_TEX, "rift_doorway.png"))
    write_json(os.path.join(BLOCK_TEX, "rift_doorway.png.mcmeta"), {"animation": {"frametime": 2, "interpolate": True}})


def crate(rnd):
    img = noise_tile(rnd, (150, 112, 70), 10)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(92, 66, 40, 255))
    d.line([0, 0, 15, 15], fill=(108, 78, 46, 255))
    d.line([0, 15, 15, 0], fill=(108, 78, 46, 255))
    d.rectangle([5, 6, 10, 9], fill=(230, 120, 30, 255))  # amber TVA label
    return img


def debris(rnd):
    img = noise_tile(rnd, (110, 104, 98), 20)
    d = ImageDraw.Draw(img)
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        d.line([x, y, x + rnd.randint(-5, 5), y + rnd.randint(-5, 5)], fill=(60, 56, 52, 255))
    d.rectangle([3, 3, 6, 4], fill=(200, 110, 40, 255))  # a scrap of amber panel
    return img


def repair_panel(rnd):
    img = noise_tile(rnd, (196, 120, 46), 6)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(120, 70, 24, 255))
    d.rectangle([2, 2, 13, 13], outline=(232, 160, 70, 255))
    for p in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        img.putpixel(p, (70, 60, 50, 255))
    return img


def display_tile(rnd):
    img = Image.new("RGBA", (16, 16), (12, 14, 18, 255))
    d = ImageDraw.Draw(img)
    for y in range(1, 15, 2):
        d.line([1, y, 14, y], fill=(24, 30, 36, 255))
    d.rectangle([0, 0, 15, 15], outline=(196, 110, 40, 255))
    d.rectangle([5, 5, 10, 10], outline=(230, 150, 60, 255))
    d.polygon([(7, 6), (7, 9), (9, 7)], fill=(240, 180, 90, 255))  # play symbol
    return img


def shelf(rnd):
    img = noise_tile(rnd, (120, 86, 52), 8)
    d = ImageDraw.Draw(img)
    for y in (0, 7, 15):
        d.line([0, y, 15, y], fill=(80, 56, 32, 255))
    d.rectangle([2, 2, 5, 6], fill=(60, 140, 210, 255))   # water
    d.rectangle([7, 2, 12, 6], fill=(150, 170, 200, 255))  # blankets
    d.rectangle([3, 9, 7, 14], fill=(240, 240, 235, 255))  # medical
    d.line([5, 10, 5, 13], fill=(200, 40, 40, 255))
    d.line([4, 11, 6, 11], fill=(200, 40, 40, 255))
    d.rectangle([9, 9, 13, 14], fill=(60, 140, 210, 255))
    return img


def anchor(rnd, webbed):
    img = noise_tile(rnd, (90, 92, 98), 8)
    d = ImageDraw.Draw(img)
    d.rectangle([5, 5, 10, 10], fill=(150, 152, 160, 255), outline=(60, 60, 66, 255))
    d.ellipse([6, 6, 9, 9], outline=(200, 200, 210, 255))
    if webbed:
        for a in range(8):
            ang = a * math.pi / 4
            d.line([7.5, 7.5, 7.5 + math.cos(ang) * 9, 7.5 + math.sin(ang) * 9], fill=(240, 240, 245, 255))
        for r in (3, 5, 7):
            d.ellipse([7.5 - r, 7.5 - r, 7.5 + r, 7.5 + r], outline=(225, 225, 232, 255))
    return img


def blocks():
    rnd = random.Random(611)
    rift_frames()
    tiles = {
        "fallen_crate": crate(rnd), "debris": debris(rnd), "repair_panel": repair_panel(rnd),
        "recording_display": display_tile(rnd), "supply_shelf": shelf(rnd),
        "web_anchor": anchor(rnd, False), "web_anchor_webbed": anchor(rnd, True),
    }
    for name, img in tiles.items():
        img.save(os.path.join(BLOCK_TEX, name + ".png"))
    simple = ["fallen_crate", "debris", "repair_panel", "recording_display", "supply_shelf"]
    for name in simple:
        write_json(os.path.join(A, "models/block", name + ".json"),
                   {"parent": "minecraft:block/cube_all", "textures": {"all": "wrmarvel:block/" + name}})
        write_json(os.path.join(A, "blockstates", name + ".json"), {"variants": {"": {"model": "wrmarvel:block/" + name}}})
    write_json(os.path.join(A, "models/block/rift_doorway.json"),
               {"parent": "minecraft:block/cube_all", "render_type": "minecraft:translucent",
                "textures": {"all": "wrmarvel:block/rift_doorway", "particle": "wrmarvel:block/rift_doorway"}})
    write_json(os.path.join(A, "blockstates/rift_doorway.json"), {"variants": {"": {"model": "wrmarvel:block/rift_doorway"}}})
    for w in ("", "_webbed"):
        write_json(os.path.join(A, "models/block", "web_anchor" + w + ".json"),
                   {"parent": "minecraft:block/cube_all", "textures": {"all": "wrmarvel:block/web_anchor" + w}})
    write_json(os.path.join(A, "blockstates/web_anchor.json"), {"variants": {
        "webbed=false": {"model": "wrmarvel:block/web_anchor"}, "webbed=true": {"model": "wrmarvel:block/web_anchor_webbed"}}})
    # The repair panel item shows the block.
    write_json(os.path.join(A, "models/item/repair_panel.json"), {"parent": "wrmarvel:block/repair_panel"})


# ------------------------------------------------------------------------------------------------ items

def item_canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def water_crate():
    img = item_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([2, 6, 13, 14], fill=(150, 112, 70, 255), outline=(92, 66, 40, 255))
    for x in (4, 7, 10):
        d.rectangle([x, 2, x + 1, 6], fill=(80, 160, 230, 255))
        d.point((x, 1), fill=(220, 220, 230, 255))
    return img


def blankets():
    img = item_canvas()
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([1, 4, 14, 8], 2, fill=(150, 170, 200, 255), outline=(90, 100, 130, 255))
    d.rounded_rectangle([2, 8, 13, 12], 2, fill=(120, 140, 180, 255), outline=(80, 90, 120, 255))
    d.line([7, 4, 7, 12], fill=(230, 120, 30, 255))
    return img


def medical():
    img = item_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 13, 13], fill=(240, 240, 235, 255), outline=(150, 150, 150, 255))
    d.rectangle([6, 2, 9, 4], outline=(150, 150, 150, 255))
    d.rectangle([7, 6, 8, 11], fill=(200, 40, 40, 255))
    d.rectangle([5, 8, 10, 9], fill=(200, 40, 40, 255))
    return img


def web_shooters():
    img = item_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([3, 5, 12, 10], fill=(170, 30, 36, 255), outline=(90, 14, 18, 255))
    d.rectangle([5, 6, 10, 9], fill=(190, 192, 200, 255))
    d.ellipse([7, 7, 8, 8], fill=(40, 40, 50, 255))
    d.line([12, 7, 15, 4], fill=(240, 240, 245, 255))
    d.line([12, 8, 15, 11], fill=(240, 240, 245, 255))
    return img


def scanner():
    img = item_canvas()
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([3, 2, 12, 14], 2, fill=(60, 62, 70, 255), outline=(30, 30, 36, 255))
    d.rectangle([5, 4, 10, 8], fill=(40, 200, 170, 255))
    d.line([5, 7, 7, 5, 8, 7, 10, 5], fill=(220, 255, 240, 255))
    d.rectangle([5, 10, 6, 11], fill=(200, 60, 50, 255))
    d.rectangle([9, 10, 10, 11], fill=(230, 200, 60, 255))
    d.line([12, 3, 14, 1], fill=(150, 150, 160, 255))  # antenna
    return img


def items():
    for name, fn in {"water_crate": water_crate, "blanket_bundle": blankets, "medical_supplies": medical,
                     "web_shooters": web_shooters, "peter_scanner": scanner}.items():
        fn().save(os.path.join(ITEM_TEX, name + ".png"))
        write_json(os.path.join(A, "models/item", name + ".json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": "wrmarvel:item/" + name}})


# ------------------------------------------------------------------------------------------------ cinematic art

def earth(size=256):
    """Another Earth: oceans, continents unlike ours, clouds, a thin bright limb. Transparent outside the disc."""
    rnd = np.random.default_rng(616)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    c = (size - 1) / 2
    r = size * 0.47
    dx, dy = (xx - c) / r, (yy - c) / r
    d = np.hypot(dx, dy)
    z = np.sqrt(np.clip(1 - d * d, 0, 1))

    def fbm(scale, oct_, seed):
        g = np.random.default_rng(seed)
        out = np.zeros((size, size), np.float32)
        amp = 1.0
        for o in range(oct_):
            n = 4 * 2 ** o
            grid = g.random((n + 1, n + 1)).astype(np.float32)
            img = Image.fromarray((grid * 255).astype(np.uint8)).resize((size, size), Image.BICUBIC)
            out += np.asarray(img, np.float32) / 255 * amp
            amp *= 0.5
        return out / out.max()

    land = fbm(1, 6, 11)
    cloud = fbm(1, 5, 23)
    img = np.zeros((size, size, 4), np.float32)
    ocean = np.stack([20 + 30 * z, 60 + 60 * z, 120 + 90 * z], -1)
    is_land = land > 0.58
    green = np.stack([70 + 60 * land, 110 + 50 * land, 50 + 20 * land], -1)
    col = np.where(is_land[..., None], green, ocean)
    cl = np.clip((cloud - 0.55) * 3, 0, 1)[..., None]
    col = col * (1 - cl) + 235 * cl
    light = np.clip(0.25 + 0.9 * (z * 0.6 + dx * -0.35 + dy * -0.25), 0.1, 1.1)[..., None]
    col = col * light
    limb = np.clip(1 - np.abs(d - 0.985) * 40, 0, 1)[..., None]
    col = col * (1 - limb) + np.array([150, 200, 255]) * limb
    alpha = np.clip((1.0 - d) * size * 0.5, 0, 1) * 255
    img[..., :3] = col
    img[..., 3] = alpha
    Image.fromarray(img.clip(0, 255).astype(np.uint8), "RGBA").save(os.path.join(GUI, "other_earth.png"))


def ops_screen(w=256, h=144):
    """The operations feed frame: an extraction doorway seen from a field camera, people coming through."""
    img = Image.new("RGBA", (w, h), (14, 10, 8, 255))
    d = ImageDraw.Draw(img)
    for y in range(h):
        k = y / h
        d.line([0, y, w, y], fill=(int(40 + 30 * k), int(26 + 16 * k), int(18 + 8 * k), 255))
    # Ground and a ruined skyline of another world.
    rnd = random.Random(6)
    x = 0
    while x < w:
        bw = rnd.randint(10, 24)
        bh = rnd.randint(20, 60)
        d.rectangle([x, h * 0.62 - bh, x + bw, h * 0.62], fill=(30, 22, 20, 255))
        x += bw + rnd.randint(2, 6)
    d.rectangle([0, int(h * 0.62), w, h], fill=(36, 30, 28, 255))
    # The doorway: an orange frame of light.
    cx, top, bot = w // 2, int(h * 0.22), int(h * 0.78)
    d.rectangle([cx - 22, top, cx + 22, bot], fill=(255, 200, 120, 255), outline=(200, 100, 30, 255), width=3)
    img = img.filter(ImageFilter.GaussianBlur(0.6))
    img.save(os.path.join(GUI, "ops_feed.png"))


def observatory_window(w=256, h=144):
    """A great round window of the Watchers' observatory looking out on the void and distant realities."""
    img = Image.new("RGBA", (w, h), (6, 6, 14, 255))
    d = ImageDraw.Draw(img)
    rnd = random.Random(10)
    for _ in range(220):
        x, y = rnd.randrange(w), rnd.randrange(h)
        v = rnd.randint(120, 255)
        d.point((x, y), fill=(v, v, min(255, v + 30), 255))
    for i in range(5):
        x, y, r = rnd.randrange(w), rnd.randrange(h // 2), rnd.randint(4, 10)
        d.ellipse([x - r, y - r, x + r, y + r], outline=(180, 200, 255, 120))
    img.save(os.path.join(GUI, "observatory_view.png"))


def cinematics():
    os.makedirs(GUI, exist_ok=True)
    earth()
    ops_screen()
    observatory_window()


def main():
    for p in (BLOCK_TEX, ITEM_TEX):
        os.makedirs(p, exist_ok=True)
    blocks()
    items()
    cinematics()
    print("assets for scenes 6-11 written")


if __name__ == "__main__":
    main()
