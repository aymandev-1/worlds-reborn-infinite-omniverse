"""Generates the Marvel Scenes 1-5 effect art: animated portal and TVA door blocks, and the cinematic layers.

Editable outputs (PNG, plus .mcmeta for the animated blocks) land in the wrmarvel assets. Run from the project root:
    python3 tools/gen_fx.py
"""
import json
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.join(os.path.dirname(__file__), "..")
ASSETS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/textures")
BLOCK = os.path.join(ASSETS, "block")
CIN = os.path.join(ASSETS, "gui/cinematic")


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(len(a)))


def clamp(x, lo=0.0, hi=1.0):
    return max(lo, min(hi, x))


def save_anim(name, frames, frametime):
    w = frames[0].width
    sheet = Image.new("RGBA", (w, w * len(frames)))
    for i, f in enumerate(frames):
        sheet.paste(f, (0, i * w))
    sheet.save(os.path.join(BLOCK, name + ".png"))
    with open(os.path.join(BLOCK, name + ".png.mcmeta"), "w") as fh:
        json.dump({"animation": {"frametime": frametime, "interpolate": True}}, fh)


def portal_frames(n=32, size=160):
    """One big silver-blue vortex for the whole 5x5 opening (each block shows a 1/5 x 1/5 piece of it): spiral arms
    turning around a bright white core, deep indigo between them, a glowing rim along the frame and twinkling
    sparks drifting inwards."""
    import numpy as np
    ys, xs = np.mgrid[0:size, 0:size]
    dx = (xs + 0.5) / size - 0.5
    dy = (ys + 0.5) / size - 0.5
    r = np.hypot(dx, dy)
    a = np.arctan2(dy, dx)
    edge = np.minimum(0.5 - np.abs(dx), 0.5 - np.abs(dy))  # distance to the frame
    rng = random.Random(5)
    sparks = [(rng.random(), rng.uniform(0.05, 0.5), rng.uniform(0.6, 1.0)) for _ in range(70)]
    deep = np.array([12, 18, 60], float)
    blue = np.array([46, 100, 210], float)
    ice = np.array([150, 205, 255], float)
    white = np.array([250, 252, 255], float)
    frames = []
    for f in range(n):
        phase = f / n * math.tau
        logr = np.log(r + 0.02)
        arms = 0.5 + 0.5 * np.sin(3 * a + 5.0 * logr + phase * 3)
        streaks = 0.5 + 0.5 * np.sin(12 * a + 20.0 * logr + phase * 3)
        t = np.clip(0.6 * arms ** 2 + 0.12 * streaks * arms + 0.6 * np.exp(-r * 8) - 0.15 * r + 0.05, 0, 1)
        col = np.where(t[..., None] < 0.35, deep + (blue - deep) * (t / 0.35)[..., None],
                       np.where(t[..., None] < 0.7, blue + (ice - blue) * ((t - 0.35) / 0.35)[..., None],
                                ice + (white - ice) * ((t - 0.7) / 0.3)[..., None]))
        core = np.exp(-(r / 0.07) ** 2)[..., None]
        col = col * (1 - core) + white * core
        rim = np.exp(-edge / 0.035)[..., None]
        col = col * (1 - rim * 0.6) + ice * rim * 0.6
        alpha = np.clip(225 + 30 * t, 0, 255)
        img = np.dstack([np.clip(col, 0, 255), alpha]).astype(np.uint8)
        for ang0, rad0, bright in sparks:
            # Sparks spiral inwards over the loop, so the animation repeats seamlessly.
            k = (f / n + ang0) % 1.0
            rad = rad0 * (1 - k)
            ang = ang0 * math.tau + k * math.tau * 0.75 + phase
            sx = int((0.5 + rad * math.cos(ang)) * size)
            sy = int((0.5 + rad * math.sin(ang)) * size)
            tw = bright * (0.5 + 0.5 * math.sin(phase * 3 + ang0 * 40))
            for ox, oy, w in ((0, 0, 1.0), (1, 0, 0.4), (-1, 0, 0.4), (0, 1, 0.4), (0, -1, 0.4)):
                x, y = sx + ox, sy + oy
                if 0 <= x < size and 0 <= y < size:
                    px = img[y, x, :3].astype(float)
                    img[y, x, :3] = np.clip(px + (255 - px) * tw * w, 0, 255).astype(np.uint8)
        frames.append(Image.fromarray(img, "RGBA"))
    return frames


def portal_models():
    """Blockstate and 25 thin-pane models; each block maps its own fifth of the big portal texture."""
    models = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/models/block")
    states = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel/blockstates")
    variants = {}
    u = 16 / 5
    for tx in range(5):
        for ty in range(5):
            south = [tx * u, ty * u, (tx + 1) * u, (ty + 1) * u]
            north = [(4 - tx) * u, ty * u, (5 - tx) * u, (ty + 1) * u]
            name = f"silver_portal_{tx}_{ty}"
            light = {"block_light": 15, "sky_light": 15, "ambient_occlusion": False}
            model = {
                "render_type": "minecraft:translucent",
                "ambientocclusion": False,
                "textures": {"particle": "wrmarvel:block/silver_portal", "portal": "wrmarvel:block/silver_portal"},
                "elements": [{
                    "from": [0, 0, 7.5], "to": [16, 16, 8.5], "shade": False, "neoforge_data": light,
                    "faces": {"south": {"uv": south, "texture": "#portal"}, "north": {"uv": north, "texture": "#portal"}},
                }],
            }
            with open(os.path.join(models, name + ".json"), "w") as fh:
                json.dump(model, fh, indent=1)
            variants[f"tile_x={tx},tile_y={ty}"] = {"model": f"wrmarvel:block/{name}"}
    with open(os.path.join(states, "silver_portal.json"), "w") as fh:
        json.dump({"variants": variants}, fh, indent=1)
    # The plain model stays for the item/particle.
    with open(os.path.join(models, "silver_portal.json"), "w") as fh:
        json.dump({"parent": "wrmarvel:block/silver_portal_2_2"}, fh)


def tva_door_frames(n=16, size=16):
    """Warm orange light with slow vertical shimmer, brighter towards the middle."""
    edge = (200, 80, 10)
    mid = (255, 150, 40)
    core = (255, 236, 190)
    frames = []
    for f in range(n):
        phase = f / n * math.tau
        img = Image.new("RGBA", (size, size))
        px = img.load()
        for y in range(size):
            for x in range(size):
                band = 0.5 + 0.5 * math.sin(x * 0.9 + phase) * math.sin(y * 0.35 - phase * 2)
                t = clamp(0.45 + 0.4 * band)
                c = lerp(edge, mid, t / 0.7) if t < 0.7 else lerp(mid, core, (t - 0.7) / 0.3)
                px[x, y] = c + (int(200 + 40 * t),)
        frames.append(img)
    return frames


def vortex(size=256):
    """Tunnel of silver-blue light streaks; the cinematic spins and scales it."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(616)
    arms = [(rnd.uniform(0, math.tau), rnd.uniform(0.6, 1.0)) for _ in range(7)]
    for y in range(size):
        for x in range(size):
            dx = (x + 0.5) / size - 0.5
            dy = (y + 0.5) / size - 0.5
            r = math.hypot(dx, dy) * 2
            if r > 1:
                continue
            a = math.atan2(dy, dx)
            v = 0
            for off, strength in arms:
                d = math.sin(a * 2 + off + math.log(r + 0.02) * 4)
                v += strength * max(0.0, d) ** 6
            v = clamp(v * 0.7)
            glow = clamp(1 - r) ** 2
            t = clamp(v + glow * 0.8)
            c = lerp((30, 60, 140), (215, 235, 255), t)
            alpha = int(255 * clamp(t * (1 - r ** 4)))
            px[x, y] = c + (alpha,)
    return img.filter(ImageFilter.GaussianBlur(0.8))


def world_panel(kind, w=96, h=60, seed=0):
    """A glimpse of another reality, framed like a shard of glass."""
    rnd = random.Random(seed)
    img = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(img)
    skies = {
        "city": ((20, 24, 50), (80, 60, 110)),
        "burning": ((60, 10, 5), (230, 90, 30)),
        "forest": ((90, 150, 210), (190, 220, 240)),
        "desert": ((210, 140, 70), (250, 210, 140)),
        "cosmic": ((5, 5, 20), (60, 20, 90)),
        "ocean": ((40, 90, 160), (150, 200, 230)),
    }
    top, bottom = skies[kind]
    for y in range(h):
        d.line([(0, y), (w, y)], fill=lerp(top, bottom, y / h) + (255,))
    if kind == "city":
        x = 0
        while x < w:
            bw = rnd.randint(6, 14)
            bh = rnd.randint(18, 50)
            d.rectangle([x, h - bh, x + bw - 1, h], fill=(15, 15, 30, 255))
            for wy in range(h - bh + 3, h - 2, 5):
                for wx in range(x + 2, x + bw - 2, 4):
                    if rnd.random() < 0.5:
                        d.point((wx, wy), fill=(250, 220, 120, 255))
            x += bw + 1
    elif kind == "burning":
        for i in range(5):
            bx = rnd.randint(0, w)
            d.rectangle([bx, h - rnd.randint(10, 35), bx + rnd.randint(5, 10), h], fill=(25, 10, 8, 255))
        for i in range(40):
            fx, fy = rnd.randint(0, w), rnd.randint(h // 2, h)
            d.ellipse([fx - 2, fy - 4, fx + 2, fy], fill=(255, rnd.randint(120, 200), 40, 220))
        d.ellipse([w * 0.6, -20, w * 1.3, 30], fill=(120, 20, 10, 200))
    elif kind == "forest":
        d.rectangle([0, h - 12, w, h], fill=(60, 130, 50, 255))
        for i in range(9):
            tx = rnd.randint(0, w)
            d.rectangle([tx, h - 26, tx + 2, h - 12], fill=(90, 60, 30, 255))
            d.rectangle([tx - 5, h - 38, tx + 7, h - 24], fill=(40, 110, 40, 255))
    elif kind == "desert":
        for i in range(3):
            cx = rnd.randint(0, w)
            d.polygon([(cx - 30, h), (cx, h - rnd.randint(12, 24)), (cx + 30, h)], fill=(200, 150, 80, 255))
        d.ellipse([w * 0.7, 6, w * 0.7 + 12, 18], fill=(255, 245, 200, 255))
    elif kind == "cosmic":
        for i in range(60):
            d.point((rnd.randint(0, w - 1), rnd.randint(0, h - 1)), fill=(255, 255, 255, rnd.randint(120, 255)))
        d.ellipse([w * 0.3, h * 0.2, w * 0.3 + 30, h * 0.2 + 30], fill=(140, 60, 200, 255))
        d.arc([w * 0.3 - 10, h * 0.2 + 10, w * 0.3 + 40, h * 0.2 + 20], 0, 360, fill=(220, 180, 255, 255))
    elif kind == "ocean":
        d.rectangle([0, h * 0.55, w, h], fill=(30, 70, 140, 255))
        for i in range(12):
            wx = rnd.randint(0, w)
            wy = rnd.randint(int(h * 0.6), h - 2)
            d.line([(wx, wy), (wx + 6, wy)], fill=(180, 220, 255, 255))
    # Cracked-glass frame.
    d.rectangle([0, 0, w - 1, h - 1], outline=(200, 225, 255, 255))
    for i in range(2):
        x0 = rnd.randint(0, w)
        d.line([(x0, 0), (x0 + rnd.randint(-20, 20), h)], fill=(220, 235, 255, 90))
    return img


def watcher(size=192):
    """A vast bald head and shoulders in shadow, two faint pale eyes: a watcher at the edge of reality."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx = size // 2
    d.ellipse([cx - 120, size - 70, cx + 120, size + 120], fill=(8, 10, 24, 235))  # shoulders and robe
    d.ellipse([cx - 52, 18, cx + 52, 140], fill=(10, 12, 28, 245))  # head
    d.rectangle([cx - 26, 120, cx + 26, size - 50], fill=(10, 12, 28, 245))  # neck
    for ex in (cx - 22, cx + 22):
        d.ellipse([ex - 8, 74, ex + 8, 82], fill=(210, 230, 255, 230))
    img = img.filter(ImageFilter.GaussianBlur(1.2))
    rim = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(rim).ellipse([cx - 54, 16, cx + 54, 142], outline=(120, 160, 230, 120), width=2)
    return Image.alpha_composite(img, rim.filter(ImageFilter.GaussianBlur(1.5)))


def skyline(w=256, h=96):
    """Night-time New York rooftops for the TVA door cinematic."""
    rnd = random.Random(1962)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    x = 0
    while x < w:
        bw = rnd.randint(10, 26)
        bh = rnd.randint(30, 90)
        d.rectangle([x, h - bh, x + bw, h], fill=(12, 14, 26, 255))
        if rnd.random() < 0.3:
            d.rectangle([x + bw // 2, h - bh - 12, x + bw // 2 + 1, h - bh], fill=(12, 14, 26, 255))
        for wy in range(h - bh + 4, h - 3, 6):
            for wx in range(x + 3, x + bw - 2, 5):
                if rnd.random() < 0.35:
                    d.rectangle([wx, wy, wx + 1, wy + 2], fill=(240, 200, 110, 255))
        x += bw + rnd.randint(0, 3)
    return img


def tva_hall(w=256, h=144):
    """The TVA lobby in warm amber: tall arches, hanging lamps, a polished floor."""
    img = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        d.line([(0, y), (w, y)], fill=lerp((60, 30, 12), (170, 95, 35), y / h) + (255,))
    floor = int(h * 0.68)
    for y in range(floor, h):
        d.line([(0, y), (w, y)], fill=lerp((120, 70, 30), (60, 32, 14), (y - floor) / (h - floor)) + (255,))
    for i, ax in enumerate(range(-20, w + 40, 48)):
        d.rectangle([ax - 6, 18, ax + 6, floor], fill=(70, 38, 16, 255))
        d.arc([ax, 0, ax + 48, 70], 180, 360, fill=(80, 44, 18, 255), width=6)
        lx = ax + 24
        d.line([(lx, 0), (lx, 34)], fill=(40, 22, 10, 255))
        d.ellipse([lx - 5, 32, lx + 5, 40], fill=(255, 210, 130, 255))
    for x in range(0, w, 16):
        d.line([(w / 2 + (x - w / 2) * 0.3, floor), (x, h)], fill=(150, 90, 40, 120))
    # Lettering panel over the far doors.
    d.rectangle([w / 2 - 34, 46, w / 2 + 34, 60], fill=(40, 22, 10, 255))
    d.rectangle([w / 2 - 32, 48, w / 2 + 32, 58], outline=(250, 190, 90, 255))
    return img.filter(ImageFilter.GaussianBlur(0.6))


def fbm(size, seed, octaves=6):
    """Fractal noise in 0..1 (numpy), tileable enough for a slowly turning backdrop."""
    import numpy as np
    rng = np.random.default_rng(seed)
    out = np.zeros((size, size))
    amp, total = 1.0, 0.0
    for o in range(octaves):
        n = 2 ** (o + 2)
        grid = rng.random((n + 1, n + 1))
        img = Image.fromarray((grid * 255).astype("uint8")).resize((size, size), Image.BICUBIC)
        out += np.asarray(img, dtype=float) / 255 * amp
        total += amp
        amp *= 0.55
    return out / total


def nebula(size=512):
    """Deep-space backdrop: violet and teal gas clouds, dark lanes, dense stars."""
    import numpy as np
    a = fbm(size, 3)
    b = fbm(size, 9)
    c = fbm(size, 21, 4)
    a = np.clip((a - 0.35) * 2.2, 0, 1)
    b = np.clip((b - 0.4) * 2.5, 0, 1)
    rgb = np.zeros((size, size, 3))
    rgb += a[..., None] * np.array([90, 40, 150])
    rgb += b[..., None] * np.array([20, 120, 150])
    rgb += (c[..., None] ** 3) * np.array([60, 60, 110])
    rgb = rgb * (0.35 + 0.65 * c[..., None])
    rng = np.random.default_rng(616)
    for _ in range(1400):
        x, y = rng.integers(0, size, 2)
        v = rng.random()
        rgb[y, x] = np.maximum(rgb[y, x], [200 + 55 * v, 200 + 55 * v, 255])
    img = Image.fromarray(np.clip(rgb, 0, 255).astype("uint8")).convert("RGBA")
    glow = img.filter(ImageFilter.GaussianBlur(2))
    return Image.blend(img, glow, 0.35)


def world_scene(kind, size, seed):
    """A richer view into another universe, for the inside of a bubble."""
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size))
    d = ImageDraw.Draw(img)
    skies = {
        "city": ((10, 12, 40), (70, 50, 110)), "forest": ((80, 150, 220), (200, 230, 250)),
        "desert": ((220, 140, 70), (255, 220, 150)), "ocean": ((30, 80, 160), (160, 210, 240)),
        "ice": ((150, 190, 230), (235, 245, 255)), "volcano": ((40, 10, 10), (200, 70, 20)),
        "islands": ((120, 90, 200), (250, 180, 200)), "cosmic": ((5, 5, 25), (50, 20, 90)),
    }
    top, bottom = skies[kind]
    for y in range(size):
        d.line([(0, y), (size, y)], fill=lerp(top, bottom, y / size) + (255,))
    h = size
    if kind == "city":
        x = 0
        while x < size:
            bw, bh = rnd.randint(8, 18), rnd.randint(30, 95)
            d.rectangle([x, h - bh, x + bw, h], fill=(12, 12, 28, 255))
            for wy in range(h - bh + 4, h - 2, 6):
                for wx in range(x + 2, x + bw - 2, 4):
                    if rnd.random() < 0.45:
                        d.rectangle([wx, wy, wx + 1, wy + 2], fill=(250, 215, 120, 255))
            x += bw + 1
        d.ellipse([size * 0.7, 12, size * 0.7 + 18, 30], fill=(240, 240, 220, 255))
    elif kind == "forest":
        d.polygon([(0, h * 0.55), (size * 0.3, h * 0.35), (size * 0.6, h * 0.55)], fill=(110, 140, 170, 255))
        d.rectangle([0, h * 0.72, size, h], fill=(70, 140, 55, 255))
        for _ in range(16):
            tx = rnd.randint(0, size)
            ty = rnd.randint(int(h * 0.7), int(h * 0.95))
            d.rectangle([tx, ty - 18, tx + 3, ty], fill=(90, 60, 30, 255))
            d.rectangle([tx - 8, ty - 34, tx + 11, ty - 16], fill=(35 + rnd.randint(0, 30), 110, 40, 255))
    elif kind == "desert":
        d.ellipse([size * 0.6, 14, size * 0.6 + 26, 40], fill=(255, 245, 210, 255))
        for i in range(4):
            cx = rnd.randint(0, size)
            d.polygon([(cx - 60, h), (cx, h - rnd.randint(25, 50)), (cx + 60, h)], fill=(205 - i * 10, 150, 80, 255))
        d.polygon([(size * 0.2, h * 0.75), (size * 0.3, h * 0.45), (size * 0.4, h * 0.75)], fill=(180, 130, 70, 255))
    elif kind == "ocean":
        d.rectangle([0, h * 0.55, size, h], fill=(25, 65, 135, 255))
        for _ in range(30):
            wx, wy = rnd.randint(0, size), rnd.randint(int(h * 0.58), h - 2)
            d.line([(wx, wy), (wx + 8, wy)], fill=(170, 215, 255, 255))
        d.polygon([(size * 0.55, h * 0.55), (size * 0.62, h * 0.4), (size * 0.7, h * 0.55)], fill=(80, 110, 70, 255))
    elif kind == "ice":
        for i in range(5):
            cx = rnd.randint(0, size)
            d.polygon([(cx - 40, h), (cx, h - rnd.randint(40, 80)), (cx + 40, h)], fill=(220, 235, 250, 255))
            d.line([(cx, h - 60), (cx + 10, h)], fill=(170, 200, 230, 255))
        for _ in range(40):
            d.point((rnd.randint(0, size - 1), rnd.randint(0, size - 1)), fill=(255, 255, 255, 255))
    elif kind == "volcano":
        d.polygon([(size * 0.15, h), (size * 0.5, h * 0.35), (size * 0.85, h)], fill=(45, 20, 15, 255))
        d.polygon([(size * 0.45, h * 0.37), (size * 0.5, h * 0.33), (size * 0.55, h * 0.37), (size * 0.52, h * 0.8)],
                  fill=(255, 120, 30, 255))
        for _ in range(30):
            fx, fy = rnd.randint(0, size), rnd.randint(0, int(h * 0.5))
            d.ellipse([fx - 1, fy - 1, fx + 1, fy + 1], fill=(255, 170, 60, 255))
    elif kind == "islands":
        for i in range(4):
            cx, cy = rnd.randint(10, size - 30), rnd.randint(20, int(h * 0.75))
            w = rnd.randint(18, 34)
            d.rectangle([cx, cy, cx + w, cy + 5], fill=(80, 160, 70, 255))
            d.polygon([(cx, cy + 5), (cx + w, cy + 5), (cx + w // 2, cy + 22)], fill=(110, 80, 60, 255))
    elif kind == "cosmic":
        for _ in range(90):
            d.point((rnd.randint(0, size - 1), rnd.randint(0, size - 1)), fill=(255, 255, 255, rnd.randint(120, 255)))
        d.ellipse([size * 0.25, size * 0.25, size * 0.7, size * 0.7], fill=(150, 70, 210, 255))
        d.arc([size * 0.1, size * 0.4, size * 0.85, size * 0.55], 0, 360, fill=(230, 190, 255, 255), width=2)
    return img


def bubble(kind, size=160, seed=0, burning=False):
    """A universe as a glass sphere: its world inside, a glowing rim, a highlight. Burning ones are cracked and red."""
    import numpy as np
    scene = world_scene(kind, size, seed)
    if burning:
        arr = np.asarray(scene, dtype=float)
        arr[..., 0] = np.clip(arr[..., 0] * 0.8 + 120, 0, 255)
        arr[..., 1] *= 0.45
        arr[..., 2] *= 0.3
        scene = Image.fromarray(arr.astype("uint8"), "RGBA")
        d = ImageDraw.Draw(scene)
        rnd = random.Random(seed + 99)
        for _ in range(5):
            x, y = size / 2, size / 2
            for _ in range(6):
                nx, ny = x + rnd.uniform(-30, 30), y + rnd.uniform(-30, 30)
                d.line([(x, y), (nx, ny)], fill=(255, 220, 140, 255), width=2)
                x, y = nx, ny
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse([4, 4, size - 5, size - 5], fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(scene, (0, 0), mask)
    # Spherical shading: darker towards the edge.
    yy, xx = np.mgrid[0:size, 0:size]
    r = np.hypot(xx - size / 2, yy - size / 2) / (size / 2)
    arr = np.asarray(out, dtype=float)
    arr[..., :3] *= np.clip(1.15 - r ** 3 * 0.75, 0, 1.2)[..., None]
    out = Image.fromarray(np.clip(arr, 0, 255).astype("uint8"), "RGBA")
    rim = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    rd = ImageDraw.Draw(rim)
    rim_col = (255, 140, 60, 230) if burning else (190, 225, 255, 220)
    rd.ellipse([3, 3, size - 4, size - 4], outline=rim_col, width=3)
    rim = rim.filter(ImageFilter.GaussianBlur(2))
    out = Image.alpha_composite(out, rim)
    hi = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ImageDraw.Draw(hi).ellipse([size * 0.22, size * 0.12, size * 0.48, size * 0.3], fill=(255, 255, 255, 90))
    return Image.alpha_composite(out, hi.filter(ImageFilter.GaussianBlur(4)))


BUBBLE_KINDS = ["city", "forest", "desert", "ocean", "ice", "volcano", "islands", "cosmic"]


def main():
    os.makedirs(CIN, exist_ok=True)
    save_anim("silver_portal", portal_frames(), 2)
    portal_models()
    save_anim("tva_doorway", tva_door_frames(), 3)
    vortex(512).save(os.path.join(CIN, "vortex.png"))
    nebula().save(os.path.join(CIN, "nebula.png"))
    for i, kind in enumerate(BUBBLE_KINDS):
        bubble(kind, seed=i).save(os.path.join(CIN, "bubble_%d.png" % i))
        bubble(kind, seed=i, burning=True).save(os.path.join(CIN, "bubble_burn_%d.png" % i))
    for i, kind in enumerate(["city", "burning", "forest", "desert", "cosmic", "ocean"]):
        world_panel(kind, seed=i).save(os.path.join(CIN, "world_%d.png" % i))
    watcher().save(os.path.join(CIN, "watcher.png"))
    skyline().save(os.path.join(CIN, "skyline.png"))
    tva_hall().save(os.path.join(CIN, "tva_hall.png"))
    print("generated portal and TVA door animations, vortex, 6 world panels, watcher, skyline, TVA hall")


if __name__ == "__main__":
    main()
