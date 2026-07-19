#!/usr/bin/env python3
"""Regenerate PriceGrab launcher icons from `branding/icon-source.png`.

Outputs are written to two destinations:

1. `android/app/src/main/res/mipmap-*/` — the launcher icons baked into
   the APK (adaptive-icon foreground + legacy + round, per density bucket).
2. `fastlane/metadata/android/{en-US,es-ES}/images/icon.png` — the 512x512
   store icon F-Droid auto-discovers from the fastlane tree. Required by
   F-Droid review (see MR !37136 reviewer feedback, 2026-04-26).

All outputs are committed to the repository so the build is fully
reproducible -- F-Droid verifies the published APK byte-for-byte against
an upstream-signed build, and re-running an icon pipeline at compile time
would introduce drift between toolchains. Re-run this script only when
the source art changes.

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
# Measured by walking the central column / row from each edge inward until the
# dark-teal frame starts. Keep these in sync with the source image; if the
# source is recropped, re-measure (a one-shot script using
# `PIL.Image.getpixel` + a teal predicate is enough -- see the PR #15
# description for the recipe). Right/bottom are exclusive (PIL.crop convention).
#
# Source dimensions: 1286 x 1313 (post-crop, April 2026). Inner rounded-square
# bounding box: 1128 x 1162. Same physical art as the original 2816 x 1536
# source; the crop only trimmed the surrounding light-grey halo.
FRAME_BOX = (79, 75, 1207, 1237)

# Adaptive-icon background colour, sampled from inside the rounded-square frame.
# Must match `values/ic_launcher_background.xml` exactly so the device mask sees
# a uniform colour field with the design centred inside the safe zone.
BG_HEX = "#2F5C73"

# Art inset: the cropped frame is rendered at this fraction of every output
# canvas (adaptive-icon foreground, legacy mipmaps, and the 512 px fastlane
# store icon). The surrounding band is filled with `BG_HEX` (opaque outputs)
# or left transparent (adaptive foreground only). 0.86 leaves ~7 % margin
# per side — enough to keep the rounded-square frame and its outermost
# elements (top-right arrow, bottom-right price tag) inside the circular
# masks F-Droid and most launchers apply. v0.1.7 started at 0.82; 0.76
# adds more safe-zone inset after on-device review of circular masks.
# Optical centering: the illustration's weighted centroid in the 1162 px
# square frame sits ~17 px right and ~74 px low of geometric centre.
# Negative values shift art left/up on the output canvas. Re-measure if
# FRAME_BOX changes.
ART_OFFSET_X_PX = -17
# v0.1.8 QA: -95 shifted art too high (excess bottom margin). Move down vs
# that cut so circular masks see more even top/bottom air (-95 → -45).
ART_OFFSET_Y_PX = -45

ART_SCALE = 0.72

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

# F-Droid's fastlane convention: a 512x512 PNG at
# `fastlane/metadata/android/<locale>/images/icon.png` is the store
# icon used by the F-Droid client list view. F-Droid auto-discovers
# it on every metadata sync. We render with the same ``ART_SCALE`` inset
# as the launcher mipmaps so every surface shows identical proportions.
FASTLANE_ICON_PX = 512
FASTLANE_LOCALES = ("en-US", "es-ES")


def crop_to_square_frame(im: Image.Image) -> Image.Image:
    """Crop the source to the rounded-square frame and pad to a perfect square.

    The source frame is slightly taller than wide (1128x1162);
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


def _bg_rgb() -> tuple[int, int, int]:
    return tuple(int(BG_HEX[i : i + 2], 16) for i in (1, 3, 5))


def render_padded_square(
    square: Image.Image,
    canvas_px: int,
    *,
    transparent: bool,
) -> Image.Image:
    """Centre the frame at ``ART_SCALE`` inside a square canvas."""
    art_side = int(round(canvas_px * ART_SCALE))
    art = square.resize((art_side, art_side), Image.LANCZOS)
    offset_x = (canvas_px - art_side) // 2 + int(
        round(ART_OFFSET_X_PX * art_side / square.width),
    )
    offset_y = (canvas_px - art_side) // 2 + int(
        round(ART_OFFSET_Y_PX * art_side / square.height),
    )
    if transparent:
        art = art.convert("RGBA")
        canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    else:
        art = art.convert("RGB")
        canvas = Image.new("RGB", (canvas_px, canvas_px), _bg_rgb())
    canvas.paste(art, (offset_x, offset_y))
    return canvas


def render_foreground(square: Image.Image, canvas_px: int) -> Image.Image:
    """Render the adaptive-icon foreground: padded art on a transparent canvas."""
    return render_padded_square(square, canvas_px, transparent=True)


def render_legacy(square: Image.Image, canvas_px: int) -> Image.Image:
    """Render a legacy (pre-Android-8) launcher icon with safe-zone padding."""
    return render_padded_square(square, canvas_px, transparent=False)


def save_png(im: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, format="PNG", optimize=True)


def render_fastlane_icon(square: Image.Image) -> Image.Image:
    """Render the 512x512 fastlane store icon with the same inset as launchers."""
    return render_padded_square(square, FASTLANE_ICON_PX, transparent=False)


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

    fastlane_icon = render_fastlane_icon(square)
    for locale in FASTLANE_LOCALES:
        save_png(
            fastlane_icon,
            REPO_ROOT / "fastlane" / "metadata" / "android" / locale / "images" / "icon.png",
        )

    print(f"Wrote launcher icons under {RES_DIR}")
    print(
        f"Wrote {FASTLANE_ICON_PX}x{FASTLANE_ICON_PX} fastlane icon under "
        f"{REPO_ROOT / 'fastlane' / 'metadata' / 'android'}/{{{','.join(FASTLANE_LOCALES)}}}/images/icon.png"
    )


if __name__ == "__main__":
    main()
