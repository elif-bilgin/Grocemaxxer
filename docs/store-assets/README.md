# Branding assets

## Launcher icon

`androidApp/src/main/res/drawable/ic_launcher_foreground.png` is the basket
from `grocemaxxer_logo.png`, placed whole on the 108dp adaptive canvas.
Regenerate it with:

```python
# pip install pillow
from PIL import Image

CANVAS = 1456                      # 108dp adaptive-icon canvas
SAFE = round(CANVAS * 66 / 108)    # 890px: the safe-zone circle

basket = Image.open(
    "composeApp/src/commonMain/composeResources/drawable/grocemaxxer_logo.png"
).convert("RGBA").resize((SAFE, SAFE), Image.LANCZOS)
foreground = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
off = (CANVAS - SAFE) // 2
foreground.paste(basket, (off, off), basket)
foreground.save("androidApp/src/main/res/drawable/ic_launcher_foreground.png")
```

Adaptive icons show only the central 72dp of the 108dp canvas, and guarantee
only the central 66dp circle. Sizing the basket to the safe zone is what keeps
the whole basket visible: dropping the artwork in at full canvas size instead
makes every launcher crop into the middle of it, which zooms past the basket
shape entirely and clips the milk carton at the edge. Re-run the Play Store
icon below after changing this.

The icons deliberately have no `<monochrome>` layer. A themed icon is tinted
flat, which would reduce this artwork to a featureless silhouette; without the
layer, launchers fall back to the full-colour icon.

## Play Store graphics

Generated from the branding assets in the repo, so they stay in sync if the
branding changes. Regenerate with:

```python
# pip install pillow
from PIL import Image

CREAM = (0xFD, 0xF3, 0xE7)

# 512x512 Play Store icon: the central 72dp of the 108dp adaptive foreground,
# flattened onto the icon background colour (Play rejects transparency).
fg = Image.open("androidApp/src/main/res/drawable/ic_launcher_foreground.png").convert("RGBA")
view = round(fg.width * 72 / 108)
off = (fg.width - view) // 2
cropped = fg.crop((off, off, off + view, off + view))
icon = Image.new("RGB", (view, view), CREAM)
icon.paste(cropped, (0, 0), cropped)
icon.resize((512, 512), Image.LANCZOS).save("docs/store-assets/play-icon-512.png")

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
