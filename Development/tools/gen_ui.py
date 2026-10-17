"""Generates the Worlds Reborn menu look: themed vanilla widget sprites and a seamless multiverse panorama.

The sprites override vanilla's (assets/minecraft/...), so every menu, including Select World, Options and the pause
menu, gets the same dark-slate and emerald style as the Worlds Reborn screens. Run: python3 tools/gen_ui.py
"""
import json
import math
import os

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MC = os.path.join(ROOT, "interface/src/main/resources/assets/minecraft/textures/gui")
WIDGET = os.path.join(MC, "sprites/widget")
PANORAMA = os.path.join(MC, "title/background")

SLATE = (16, 22, 28)
SLATE_HI = (22, 40, 34)
EDGE = (74, 85, 96)
EMERALD = (46, 204, 113)
CYAN = (63, 208, 224)
DISABLED = (12, 14, 17)


def nine_slice(path, w, h, border):
    with open(path + ".mcmeta", "w") as f:
        json.dump({"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": border}}}, f, indent=2)


def button(fill, edge, accent=None, alpha=225, glow=False):
    w, h = 200, 20
    img = Image.new("RGBA", (w, h))
    for y in range(h):
        t = y / (h - 1)
        c = tuple(int(fill[i] * (1.12 - 0.24 * t)) for i in range(3))
        for x in range(w):
            img.putpixel((x, y), c + (alpha,))
    for x in range(w):
        img.putpixel((x, 0), edge + (255,))
        img.putpixel((x, h - 1), tuple(int(v * 0.7) for v in edge) + (255,))
    for y in range(h):
        img.putpixel((0, y), edge + (255,))
        img.putpixel((w - 1, y), edge + (255,))
    if accent:
        # bright bar on the left edge and a soft top highlight
        for y in range(1, h - 1):
            img.putpixel((1, y), accent + (255,))
            img.putpixel((2, y), accent + (255,))
        for x in range(1, w - 1):
            img.putpixel((x, 1), tuple(min(255, int(v * 0.55 + 40)) for v in accent) + (200,))
    if glow:
        for x in range(1, w - 1):
            img.putpixel((x, h - 2), accent + (160,))
    return img


def widget_sprites():
    os.makedirs(WIDGET, exist_ok=True)
    sprites = {
        "button": button(SLATE, EDGE),
        "button_highlighted": button(SLATE_HI, EMERALD, accent=EMERALD, glow=True),
        "button_disabled": button(DISABLED, (40, 44, 50), alpha=200),
        "slider": button((10, 14, 18), EDGE),
        "slider_highlighted": button((14, 22, 24), CYAN),
    }
    for name, img in sprites.items():
        p = os.path.join(WIDGET, name + ".png")
        img.save(p)
        nine_slice(p, 200, 20, 3)
    for name, edge in (("slider_handle", EMERALD), ("slider_handle_highlighted", CYAN)):
        img = Image.new("RGBA", (8, 20))
        for y in range(20):
            for x in range(8):
                border = x in (0, 7) or y in (0, 19)
                img.putpixel((x, y), (edge if border else tuple(int(v * 0.55) for v in edge)) + (255,))
        img.save(os.path.join(WIDGET, name + ".png"))
        nine_slice(os.path.join(WIDGET, name + ".png"), 8, 20, 2)
    for name, edge in (("text_field", EDGE), ("text_field_highlighted", EMERALD)):
        img = Image.new("RGBA", (200, 20), (6, 9, 12, 235))
        for x in range(200):
            img.putpixel((x, 0), edge + (255,))
            img.putpixel((x, 19), edge + (255,))
        for y in range(20):
            img.putpixel((0, y), edge + (255,))
            img.putpixel((199, y), edge + (255,))
        p = os.path.join(WIDGET, name + ".png")
        img.save(p)
        nine_slice(p, 200, 20, 1)


def menu_backgrounds():
    """Tiled backdrops behind lists (Select World etc.) and the separators above and below them."""
    rng = np.random.default_rng(7)
    for name, base, alpha in (("menu_background", (14, 18, 24), 255), ("menu_list_background", (10, 13, 18), 255),
                              ("inworld_menu_background", (14, 18, 24), 170),
                              ("inworld_menu_list_background", (8, 10, 14), 170)):
        n = rng.normal(0, 2.2, (32, 32))
        arr = np.zeros((32, 32, 4), dtype=np.uint8)
        for i in range(3):
            arr[..., i] = np.clip(base[i] + n, 0, 255)
        arr[..., 3] = alpha
        Image.fromarray(arr, "RGBA").save(os.path.join(MC, name + ".png"))
    for name in ("header_separator", "inworld_header_separator", "footer_separator", "inworld_footer_separator"):
        img = Image.new("RGBA", (32, 2), (0, 0, 0, 0))
        for x in range(32):
            img.putpixel((x, 0 if "header" in name else 1), EMERALD + (200,))
            img.putpixel((x, 1 if "header" in name else 0), (10, 30, 20, 160))
        img.save(os.path.join(MC, name + ".png"))


# ---------------------------------------------------------------- panorama


def _hash3(ix, iy, iz, seed):
    h = (ix * 374761393 + iy * 668265263 + iz * 2147483647 + seed * 144665) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0


def value_noise(p, seed):
    """3D value noise, smooth, on an (N, 3) array of points."""
    i = np.floor(p).astype(np.int64)
    f = p - i
    u = f * f * (3 - 2 * f)
    out = 0
    for dx in (0, 1):
        for dy in (0, 1):
            for dz in (0, 1):
                v = _hash3(i[:, 0] + dx, i[:, 1] + dy, i[:, 2] + dz, seed)
                wx = u[:, 0] if dx else 1 - u[:, 0]
                wy = u[:, 1] if dy else 1 - u[:, 1]
                wz = u[:, 2] if dz else 1 - u[:, 2]
                out = out + v * wx * wy * wz
    return out


def fbm(p, seed, octaves=5):
    total, amp, freq, norm = 0, 1.0, 1.0, 0
    for o in range(octaves):
        total = total + amp * value_noise(p * freq, seed + o * 17)
        norm += amp
        amp *= 0.5
        freq *= 2.03
    return total / norm


def face_dirs(face, n):
    a = (np.arange(n) + 0.5) / n * 2 - 1
    u, v = np.meshgrid(a, a)
    u, v = u.ravel(), -v.ravel()
    one = np.ones_like(u)
    # Minecraft panorama order: 0 north(front), 1 east, 2 south, 3 west, 4 up, 5 down
    d = {
        0: (u, v, -one),
        1: (one, v, u),
        2: (-u, v, one),
        3: (-one, v, -u),
        4: (u, one, v),
        5: (u, -one, -v),
    }[face]
    vec = np.stack(d, axis=1).astype(np.float64)
    return vec / np.linalg.norm(vec, axis=1, keepdims=True)


PORTALS = [  # direction, colour (RGB 0-1), angular radius
    ((0.55, 0.12, -0.83), (0.75, 0.85, 1.0), 0.16),   # silver-blue
    ((-0.80, 0.25, -0.30), (0.25, 0.95, 0.45), 0.10),  # green
    ((0.30, 0.35, 0.89), (0.25, 0.45, 1.0), 0.12),     # blue
]


def panorama(n=1024):
    os.makedirs(PANORAMA, exist_ok=True)
    rng = np.random.default_rng(616)
    star_dirs = rng.normal(size=(9000, 3))
    star_dirs /= np.linalg.norm(star_dirs, axis=1, keepdims=True)
    star_mag = rng.uniform(0.25, 1.0, 9000) ** 3
    for face in range(6):
        d = face_dirs(face, n)
        neb = fbm(d * 2.2 + 11, 1)
        neb2 = fbm(d * 3.1 - 5, 2)
        band = np.exp(-((d[:, 1] * 2.2 + 0.4 * (neb - 0.5)) ** 2))
        dens = np.clip((neb - 0.42) * 2.4, 0, 1) ** 1.6 * (0.35 + 0.65 * band)
        col = np.zeros((d.shape[0], 3))
        col += np.array([0.015, 0.02, 0.045])
        col += np.outer(dens * (1 - neb2), [0.05, 0.55, 0.38]) * 0.55   # emerald
        col += np.outer(dens * neb2, [0.18, 0.35, 0.75]) * 0.55          # cyan-blue
        col += np.outer(np.clip(neb2 - 0.55, 0, 1) * band, [0.45, 0.15, 0.55]) * 0.6  # violet wisps
        for pd, pc, pr in PORTALS:
            pdv = np.array(pd) / np.linalg.norm(pd)
            ang = np.arccos(np.clip(d @ pdv, -1, 1))
            ring = np.exp(-((ang - pr) / 0.012) ** 2) * 1.4
            glow = np.exp(-((ang - pr) / 0.06) ** 2) * 0.35 + np.exp(-(ang / pr) ** 2) * 0.12
            col += np.outer(ring + glow, pc)
        img = np.clip(col, 0, 1)
        img = (img ** (1 / 1.8) * 255).astype(np.uint8).reshape(n, n, 3)
        # Stars: project onto this face.
        axis = {0: (2, -1), 1: (0, 1), 2: (2, 1), 3: (0, -1), 4: (1, 1), 5: (1, -1)}[face]
        k, sgn = axis
        sel = star_dirs[:, k] * sgn > 0
        sd = star_dirs[sel] / np.abs(star_dirs[sel][:, k:k + 1])
        sm = star_mag[sel]
        if face == 0:
            uu, vv = sd[:, 0], sd[:, 1]
        elif face == 1:
            uu, vv = sd[:, 2], sd[:, 1]
        elif face == 2:
            uu, vv = -sd[:, 0], sd[:, 1]
        elif face == 3:
            uu, vv = -sd[:, 2], sd[:, 1]
        elif face == 4:
            uu, vv = sd[:, 0], sd[:, 2]
        else:
            uu, vv = sd[:, 0], -sd[:, 2]
        px = ((uu + 1) / 2 * n).astype(int)
        py = ((1 - (vv + 1) / 2) * n).astype(int)
        ok = (px >= 1) & (px < n - 1) & (py >= 1) & (py < n - 1)
        for x, y, m in zip(px[ok], py[ok], sm[ok]):
            b = int(120 + 135 * m)
            img[y, x] = np.maximum(img[y, x], [b, b, min(255, b + 10)])
            if m > 0.5:
                for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    img[y + oy, x + ox] = np.maximum(img[y + oy, x + ox], [b // 2, b // 2, b // 2 + 10])
        Image.fromarray(img, "RGB").save(os.path.join(PANORAMA, f"panorama_{face}.png"))
        print("panorama face", face)


if __name__ == "__main__":
    widget_sprites()
    menu_backgrounds()
    panorama()
