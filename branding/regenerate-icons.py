#!/usr/bin/env python3
"""Regenerate PriceGrab launcher icons from `branding/icon-source.png`.

Outputs are written under `android/app/src/main/res/mipmap-*/` and are intended
to be committed to the repository so the build is fully reproducible -- F-Droid
verifies the published APK byte-for-byte against an upstream-signed build, and
re-running an icon pipeline at compile time would introduce drift between
toolchains. Re-run this script only when the source art changes.

Usage:
    python3 branding/regenerate-icons.py

Determinism notes:
- Pillow's PNG encoder does NOT emit a `tIME` chunk by default, so byte output
  is stable across machines for a given Pillow version.
- All resampling uses LANCZOS so antialiasing is identical across runs.
- We avoid `image.thumbnail()` / chained transforms; every resize is a single
  explicit call from the cropped square to the final bucket size.

If you bump Pillow and a `git diff` shows pixel changes you didn't expect,
that's the encoder version drift; pin Pillow in the agent environment or
regenerate icons in the same environment that produced the previous run.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

REPO_ROOT = Path(__file__).resolve().parent.parent
SOURCE = Path(__file__).resolve().parent / "icon-source.png"
RES_DIR = REPO_ROOT / "android" / "app" / "src" / "main" / "res"

# Pixel-coordinate box of the rounded-square frame inside `icon-source.png`.
# Measured by walking the central column / row until the dark-teal background
# starts. Keep these in sync with the source image; if you replace the source
# with a different crop, re-measure with `branding/measure-source.py` (TODO if
# we ever need it again -- the values here come from the original Gemini PNG).
FRAME_BOX = (844, 188, 1972, 1350)  # (left, top, right, bottom), right/bottom exclusive

# Adaptive-icon background colour, sampled from inside the rounded-square frame.
# Must match `values/ic_launcher_background.xml` exactly so the device mask sees
# a uniform colour field with the design centred inside the safe zone.
BG_HEX = "#2F5C73"

# Foreground inset: the cropped frame is rendered at this fraction of the
# 108-dp adaptive-icon canvas, surrounded by transparent padding. The padding
# falls onto the same `BG_HEX` colour, so the seam is invisible. 0.95 leaves
# ~5 dp of margin around the frame, which keeps the design's outermost
# elements (top-right arrow, bottom-right price tag) inside the 72-dp visible
# square that all device masks must respect.
FG_SCALE = 0.95

# Density buckets and their pixel-per-dp multipliers.
DENSITIES: dict[str, float] = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

FG_DP = 108  # adaptive-icon canvas size in dp
LEGACY_DP = 48  # legacy launcher icon size in dp


def crop_to_square_frame(im: Image.Image) -> Image.Image:
    """Crop the source to the rounded-square frame and pad to a perfect square.

    The Gemini-generated source frame is slightly taller than wide (1128x1162);
    rather than distorting the art with a non-uniform resize we pad to the
    larger side with the frame's own background colour, which is invisible.

    We also flood-fill the four corners with the frame colour. The source PNG
    paints the rounded-square frame on a light-grey halo, and the boundary
    pixels are anti-aliased between the two. When that crop is resized down
    aggressively (e.g. 1128 -> 48 for mdpi legacy) the halo bleeds into the
    icon's outer ring as a visible grey tint. Filling the exterior with the
    frame colour eliminates that bleed without touching the design, because
    the dark-teal frame separates the exterior corner pixels from any design
    content via a continuous opaque ring.
    """
    cropped = im.crop(FRAME_BOX).convert("RGB")
    w, h = cropped.size
    bg = tuple(int(BG_HEX[i : i + 2], 16) for i in (1, 3, 5))

    # Flood-fill the exterior of the rounded square from each corner.
    # `thresh=80` is comfortably below the contrast between the light-grey
    # halo (~#D4D4D4, sum ~636) and the dark-teal frame (~#2F5C73, sum ~338).
    cropped = cropped.copy()  # floodfill mutates in place
    for corner in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)):
        ImageDraw.floodfill(cropped, corner, bg, thresh=80)

    side = max(w, h)
    canvas = Image.new("RGB", (side, side), bg)
    canvas.paste(cropped, ((side - w) // 2, (side - h) // 2))
    return canvas


def render_foreground(square: Image.Image, canvas_px: int) -> Image.Image:
    """Render the adaptive-icon foreground: square art on a transparent canvas."""
    art_side = int(round(canvas_px * FG_SCALE))
    art = square.resize((art_side, art_side), Image.LANCZOS).convert("RGBA")
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    offset = (canvas_px - art_side) // 2
    canvas.paste(art, (offset, offset))
    return canvas


def render_legacy(square: Image.Image, canvas_px: int) -> Image.Image:
    """Render a legacy (pre-Android-8) launcher icon: full square at canvas size."""
    return square.resize((canvas_px, canvas_px), Image.LANCZOS)


def save_png(im: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, format="PNG", optimize=True)


def main() -> None:
    src = Image.open(SOURCE)
    square = crop_to_square_frame(src)

    for bucket, mult in DENSITIES.items():
        out_dir = RES_DIR / f"mipmap-{bucket}"

        fg_size = int(round(FG_DP * mult))
        save_png(render_foreground(square, fg_size), out_dir / "ic_launcher_foreground.png")

        legacy_size = int(round(LEGACY_DP * mult))
        legacy = render_legacy(square, legacy_size)
        save_png(legacy, out_dir / "ic_launcher.png")
        # The "round" variant historically targeted Android 7.1's circular
        # launchers (Pixel/Nexus). Modern launchers always pick adaptive when
        # available; the round PNG is only a fallback. We ship the same square
        # frame -- its rounded corners are friendly enough under any mask, and
        # double-masking would clip the design.
        save_png(legacy, out_dir / "ic_launcher_round.png")

    print(f"Wrote launcher icons under {RES_DIR}")


if __name__ == "__main__":
    main()
