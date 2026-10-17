"""HD (256x256) skin: Spider-Man in a modern red-and-blue suit. Bright red head, chest, shoulders, forearms and
boots; royal-blue sides, under-arms and thighs; thin black web lines on every red area; black piping where the
colours meet; big white lenses in thick black frames; a small black spider on the chest and a large one on the back.

Same UV layout as a 64x64 player skin at 4x resolution. Run from the project root:
    python3 tools/gen_spiderman_hd.py
Preview with: python3 tools/preview_skin.py <skin> <out.png>
"""
import math
import os

from PIL import Image

S = 4
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# The game uses the skin the user supplied (canon/marvel/art/spiderman_skin_user.png); this draft goes to tools/out.
OUT = os.path.join(ROOT, "tools/out/tom_spiderman_generated.png")

RED = (214, 28, 36)
RED_DARK = (160, 16, 26)
BLUE = (28, 62, 168)
BLUE_DARK = (18, 40, 118)
BLACK = (12, 10, 14)
WEBLINE = (24, 8, 12)
WHITE = (246, 248, 252)
LENS_SHADE = (196, 210, 232)

PARTS = {
    "head": ((0, 0), (8, 8, 8)),
    "body": ((16, 16), (8, 12, 4)),
    "rarm": ((40, 16), (4, 12, 4)),
    "larm": ((32, 48), (4, 12, 4)),
    "rleg": ((0, 16), (4, 12, 4)),
    "lleg": ((16, 48), (4, 12, 4)),
}

img = Image.new("RGBA", (64 * S, 64 * S), (0, 0, 0, 0))


def faces(part):
    (u, v), (w, h, d) = PARTS[part]
    f = {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }
    return {k: (x * S, y * S, ww * S, hh * S) for k, (x, y, ww, hh) in f.items()}


def region(rect, fn):
    x0, y0, w, h = rect
    for i in range(w):
        for j in range(h):
            c = fn(i, j, w, h)
            if c is not None:
                img.putpixel((x0 + i, y0 + j), tuple(c) + (255,))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def red(j, h):
    """Red with a soft top-to-bottom falloff."""
    return shade(RED, 1.04 - 0.14 * j / max(1, h))


def blue(j, h):
    return shade(BLUE, 1.03 - 0.16 * j / max(1, h))


def web(i, j, cx, cy, spokes=8, spacing=9.0, h=32):
    """Red with a web: straight spokes from (cx, cy) and polygon rings every `spacing` texture pixels."""
    dx, dy = i - cx, j - cy
    r = math.hypot(dx, dy)
    a = math.atan2(dy, dx)
    sector = 2 * math.pi / spokes
    k = (a % sector) / sector
    spoke_d = min(k, 1 - k) * sector * r  # pixels to the nearest spoke
    ring_r = r * math.cos((k - 0.5) * sector)  # rings run straight between spokes
    ring_d = abs((ring_r % spacing) - spacing / 2)
    if (r > 2.5 and spoke_d < 0.5) or (r > spacing * 0.8 and ring_d > spacing / 2 - 0.5):
        return WEBLINE
    return red(j, h)


# ------------------------------------------------------------------------------------------- head
def lens(i, j, w, h):
    """Large framed lenses. Returns a colour inside a lens or its frame, else None."""
    u = w / 32.0
    for side in (-1, 1):
        ex, ey = w / 2 + side * 7.8 * u, 13.5 * u
        dx, dy = (i - ex) / u, (j - ey) / u
        ang = 0.5 * side
        rx = dx * math.cos(ang) + dy * math.sin(ang)
        ry = -dx * math.sin(ang) + dy * math.cos(ang)
        squash = 1.0 + 0.25 * (side * rx / 6.0)  # wider at the outer edge, narrowing towards the nose
        d = (rx / 7.0) ** 2 + (ry / (5.8 * squash)) ** 2
        if d <= 1.0:
            return LENS_SHADE if ry > 2.8 or (side * rx) < -4.8 else WHITE
        if d <= 1.6:
            return BLACK
    return None


for name, rect in faces("head").items():
    if name == "front":
        def head_front(i, j, w, h):
            c = lens(i, j, w, h)
            return c if c is not None else web(i, j, w / 2 - 0.5, h * 0.66, spokes=10, spacing=7.0, h=h)
        region(rect, head_front)
    elif name == "back":
        region(rect, lambda i, j, w, h: web(i, j, w / 2 - 0.5, h * 0.55, 10, 7.0, h=h))
    elif name in ("left", "right"):
        region(rect, lambda i, j, w, h: web(i, j, w / 2 - 0.5, h * 0.6, 8, 7.0, h=h))
    elif name == "top":
        region(rect, lambda i, j, w, h: web(i, j, w / 2 - 0.5, h / 2, 10, 6.0, h=h))
    else:
        region(rect, lambda i, j, w, h: RED_DARK)


# ------------------------------------------------------------------------------------------- spiders
def seg_dist(px, py, ax, ay, bx, by):
    vx, vy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px - ax) * vx + (py - ay) * vy) / (vx * vx + vy * vy)))
    return math.hypot(px - (ax + t * vx), py - (ay + t * vy))


def spider(x, y, scale):
    """True if (x, y), relative to the emblem centre and divided by `scale`, is on the spider."""
    x, y = x / scale, y / scale
    if (x / 0.8) ** 2 + ((y - 1.1) / 1.8) ** 2 <= 1 or (x / 0.65) ** 2 + ((y + 1.1) / 0.8) ** 2 <= 1:
        return True
    legs = [((0.5, -0.7), (2.1, -2.0), (2.6, -3.4)), ((0.6, -0.2), (2.5, -0.7), (3.4, -1.7)),
            ((0.6, 0.4), (2.5, 1.0), (3.2, 2.2)), ((0.5, 0.9), (1.9, 2.2), (2.3, 3.6))]
    for (ax, ay), (bx, by), (cx, cy) in legs:
        for sx in (-1, 1):
            if seg_dist(x, y, sx * ax, ay, sx * bx, by) < 0.3 or seg_dist(x, y, sx * bx, by, sx * cx, cy) < 0.26:
                return True
    return False


# ------------------------------------------------------------------------------------------- body
def torso(i, j, w, h, back=False):
    """Red centre that narrows towards the waist, blue sides, black piping, black belt line."""
    t = j / h
    half_red = w * (0.40 - 0.16 * max(0.0, (t - 0.35) / 0.65))  # narrows below the chest
    if t < 0.28:
        half_red = w * 0.5  # full red across the chest and shoulders
    dx = abs(i - (w / 2 - 0.5))
    belt = h - 2.2 * S
    if belt <= j < belt + 0.6 * S:
        return BLACK
    if j >= belt + 0.6 * S:
        return blue(j, h)
    if dx > half_red + 0.5 * S:
        return blue(j, h)
    if dx > half_red - 0.1 * S:
        return BLACK
    cx, cy = w / 2 - 0.5, h * 0.24
    sy, scale = (h * 0.36, 2.4 * S / 2) if back else (cy, 1.2 * S / 2)
    if spider(i - cx, j - sy, scale):
        return BLACK
    if spider(i - cx, j - sy, scale * 1.35):
        return red(j, h)  # clear halo so the emblem reads against the web
    return web(i, j, cx, cy, 10, 10.0, h=h)


region(faces("body")["front"], lambda i, j, w, h: torso(i, j, w, h))
region(faces("body")["back"], lambda i, j, w, h: torso(i, j, w, h, back=True))
for n in ("left", "right"):
    region(faces("body")[n], lambda i, j, w, h: (web(i, j, w / 2, 0, 6, 8.0, h=h) if j < 2.5 * S
                                                 else BLACK if j < 3 * S else blue(j, h)))
region(faces("body")["top"], lambda i, j, w, h: web(i, j, w / 2, h, 8, 7.0, h=h))
region(faces("body")["bottom"], lambda i, j, w, h: BLUE_DARK)


# ------------------------------------------------------------------------------------------- arms
def arm(i, j, w, h, face):
    """Red shoulder cap, blue upper arm, red forearm and glove, black piping between."""
    cap = 3 * S if face != "inner" else 0
    elbow = int(6.5 * S)
    if face == "top":
        return web(i, j, w / 2, h / 2, 6, 7.0, h=h)
    if face == "bottom":
        return RED_DARK
    if j < cap:
        return web(i, j, w / 2, 0, 6, 7.0, h=h)
    if cap and j < cap + 0.5 * S:
        return BLACK
    if j < elbow:
        return blue(j, h)
    if j < elbow + 0.5 * S:
        return BLACK
    return web(i, j, w / 2, h + S, 6, 8.0, h=h)


for name in ("rarm", "larm"):
    inner = "left" if name == "rarm" else "right"
    for n, rect in faces(name).items():
        kind = "inner" if n == inner else n
        region(rect, lambda i, j, w, h, kind=kind: arm(i, j, w, h, kind))


# ------------------------------------------------------------------------------------------- legs
def leg(i, j, w, h, face):
    """Blue thighs with a red stripe down the front, then red boots from just above the knee."""
    boot = int(5.5 * S)
    if face == "top":
        return BLUE_DARK
    if face == "bottom":
        return RED_DARK
    if j >= boot + 0.5 * S:
        return web(i, j, -S, boot, 5, 9.0, h=h)
    if j >= boot:
        return BLACK
    if face == "front":
        dx = abs(i - (w / 2 - 0.5))
        if dx < 1.1 * S:
            return web(i, j, w / 2, 0, 4, 9.0, h=h)
        if dx < 1.6 * S:
            return BLACK
    return blue(j, h)


for name in ("rleg", "lleg"):
    for n, rect in faces(name).items():
        region(rect, lambda i, j, w, h, n=n: leg(i, j, w, h, n))

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT)
print("wrote", OUT)
