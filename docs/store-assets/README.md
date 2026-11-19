# Branding assets

## Launcher icon

`androidApp/src/main/res/drawable/ic_launcher_foreground.png` is the milk
carton from `grocemaxxer_logo.png`, cropped and placed on the 108dp adaptive
canvas. Regenerate it with:

```python
# pip install pillow
from PIL import Image, ImageFilter

CANVAS = 1456                      # 108dp adaptive-icon canvas
ART = 1120                         # oversized on purpose -- see below
CROP = (535, 40, 805, 310)         # the carton, plus a little of the swirl
SS = 4                             # supersample factor -- see "Sharpening" below
USM = dict(radius=2.4, percent=70, threshold=2)

src = Image.open(
    "composeApp/src/commonMain/composeResources/drawable/grocemaxxer_logo.png"
).convert("RGBA")

# Sharpen at SS x the target size, then downsample: this crisps the edges
# without leaving the halo a same-size unsharp mask would.
big = src.resize((ART * SS, ART * SS), Image.LANCZOS, box=CROP)
rgb, alpha = big.convert("RGB"), big.getchannel("A")
rgb = rgb.filter(ImageFilter.UnsharpMask(**USM))   # colour only...
rgb.putalpha(alpha)                                # ...alpha stays plain Lanczos
carton = rgb.resize((ART, ART), Image.LANCZOS)

foreground = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
offset = (CANVAS - ART) // 2
foreground.paste(carton, (offset, offset), carton)
foreground.save("androidApp/src/main/res/drawable/ic_launcher_foreground.png",
                optimize=True)
```

Sharpen the colour channels only. Running the unsharp mask over the alpha
channel as well puts a light fringe around the artwork where it meets the
transparent canvas, which the launcher then composites onto the icon
background as a visible outline.

Adaptive icons show only the central 72dp of the 108dp canvas, and guarantee
only the central 66dp circle.

`ART` is deliberately larger than the 971px visible window. The crop slices
through the swirl on its left and bottom, and at any size that fits inside the
window those straight cut edges are visible as hard lines against the icon
background. At 1120 the artwork overhangs the window by 74px on every side, so
the swirl bleeds off the edge the way the rest of the illustration does, while
the carton itself still sits whole inside the safe-zone circle. Shrinking
`ART` back below 971 brings the cut edges back; growing it much past 1200
starts clipping the carton's cap. Re-run the Play Store icon below after
changing this.

The icons deliberately have no `<monochrome>` layer. A themed icon is tinted
flat, which would reduce this artwork to a featureless silhouette; without the
layer, launchers fall back to the full-colour icon.

## Play Store graphics

Generated from the branding assets in the repo, so they stay in sync if the
branding changes. Regenerate with:

```python
# pip install pillow
from PIL import Image, ImageFilter

CREAM = (0xFD, 0xF3, 0xE7)

# 512x512 Play Store icon: the same framing the launcher shows -- the central
# 72dp of the 108dp canvas -- but resampled straight from the original art in
# one step, flattened onto the icon background (Play rejects transparency).
CANVAS, ART, CROP = 1456, 1120, (535, 40, 805, 310)
SS = 4
USM = dict(radius=2.4, percent=70, threshold=2)

src = Image.open(
    "composeApp/src/commonMain/composeResources/drawable/grocemaxxer_logo.png"
).convert("RGBA")

# Work out which slice of the original the launcher's 72dp window exposes,
# then go to 512 in a single resample instead of via the 1456px foreground.
scale = ART / (CROP[2] - CROP[0])
view = round(CANVAS * 72 / 108)
lo = ((CANVAS - view) // 2 - (CANVAS - ART) // 2) / scale
box = (CROP[0] + lo, CROP[1] + lo, CROP[0] + lo + view / scale, CROP[1] + lo + view / scale)

sup = src.resize((512 * SS, 512 * SS), Image.LANCZOS, box=box)
flat = Image.new("RGB", sup.size, CREAM)
flat.paste(sup, (0, 0), sup)
flat.filter(ImageFilter.UnsharpMask(**USM)) \
    .resize((512, 512), Image.LANCZOS) \
    .save("docs/store-assets/play-icon-512.png", optimize=True)

# 1024x500 feature graphic: the wordmark centred on the same cream background.
mark = Image.open(
    "composeApp/src/commonMain/composeResources/drawable/grocemaxxer_title_no_background.png"
).convert("RGBA")
W, H = 1024, 500
feature = Image.new("RGB", (W, H), CREAM)
scale = min((W * 0.82) / mark.width, (H * 0.62) / mark.height)
w, h = int(mark.width * scale), int(mark.height * scale)
resized = mark.resize((w, h), Image.LANCZOS)
feature.paste(resized, ((W - w) // 2, (H - h) // 2), resized)
feature.save("docs/store-assets/play-feature-graphic-1024x500.png")
```

Run it from the repository root.

## Why the icon is only so sharp

`grocemaxxer_logo.png` is 833x831, and the carton occupies about 270x270 of
it. The slice the Play icon shows is 234x234 real pixels, displayed at 512 --
a 2.19x upscale. The sharpening above recovers the edge definition that the
upscale costs, but it cannot invent detail that was never in the file.

A genuinely sharp icon needs a higher-resolution original. If the artwork
exists somewhere as vector or as a large export, re-export the carton region
at 512px or more, drop it in, and the upscale disappears entirely.
