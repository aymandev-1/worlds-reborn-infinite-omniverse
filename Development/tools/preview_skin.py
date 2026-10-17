"""Draws a flat front/back preview of a player-layout skin (any resolution). Usage: preview_skin.py skin.png out.png"""
import sys
from PIL import Image

src = Image.open(sys.argv[1]).convert("RGBA")
S = src.width // 64


def face(u, v, w, h):
    return src.crop((u * S, v * S, (u + w) * S, (v + h) * S))


def figure(back=False):
    out = Image.new("RGBA", (16 * S, 32 * S), (60, 60, 70, 255))
    if not back:
        parts = [((8, 8, 8, 8), (4, 0)), ((20, 20, 8, 12), (4, 8)), ((44, 20, 4, 12), (0, 8)), ((36, 52, 4, 12), (12, 8)),
                 ((4, 20, 4, 12), (4, 20)), ((20, 52, 4, 12), (8, 20))]
        overlay = [((40, 8, 8, 8), (4, 0))]
    else:
        parts = [((24, 8, 8, 8), (4, 0)), ((32, 20, 8, 12), (4, 8)), ((52, 20, 4, 12), (12, 8)), ((44, 52, 4, 12), (0, 8)),
                 ((12, 20, 4, 12), (8, 20)), ((28, 52, 4, 12), (4, 20))]
        overlay = [((56, 8, 8, 8), (4, 0))]
    for (u, v, w, h), (x, y) in parts + overlay:
        f = face(u, v, w, h)
        out.alpha_composite(f, (x * S, y * S))
    return out


a, b = figure(), figure(True)
canvas = Image.new("RGBA", (a.width * 2 + S * 4, a.height), (60, 60, 70, 255))
canvas.paste(a, (0, 0))
canvas.paste(b, (a.width + S * 4, 0))
scale = max(1, 480 // canvas.height)
canvas.resize((canvas.width * scale, canvas.height * scale), Image.NEAREST).save(sys.argv[2])
