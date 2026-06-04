import struct
import zlib
import os

def write_png(filename, pixels):
    """Write a 16x16 RGBA PNG file using only struct and zlib."""
    width = 16
    height = 16

    def pack_chunk(chunk_type, data):
        c = chunk_type + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)

    # PNG signature
    signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr = pack_chunk(b'IHDR', ihdr_data)

    # IDAT chunk - raw image data
    raw_rows = []
    for row in pixels:
        row_bytes = b'\x00'  # filter type None
        for (r, g, b, a) in row:
            row_bytes += struct.pack('BBBB', r, g, b, a)
        raw_rows.append(row_bytes)
    raw_data = b''.join(raw_rows)
    compressed = zlib.compress(raw_data, 9)
    idat = pack_chunk(b'IDAT', compressed)

    # IEND chunk
    iend = pack_chunk(b'IEND', b'')

    with open(filename, 'wb') as f:
        f.write(signature + ihdr + idat + iend)


T = (0, 0, 0, 0)  # transparent

def make_totem(X, H, D, E, icon_fn):
    """
    Build a 16x16 pixel grid for a totem.
    X = body color, H = highlight, D = dark/shadow, E = eye white
    icon_fn(grid) -> mutates rows 9-11 to add icon
    """
    # Base layout using characters
    layout = [
        "................",  # 0
        "....XXXXX.......",  # 1
        "....HXXXHX......",  # 2  (fixed: HXXXHX -> 6 chars for cols 4-9)
        "....XXEXXEX.....",  # 3
        "....XXXXXXX.....",  # 4  (7 wide cols 4-10)
        ".....XXXXX......",  # 5
        "..XXXXXXXXXXX...",  # 6
        "..XXXXXXXXXXX...",  # 7
        "..XXX.XXX.XXX...",  # 8
        ".....XXXXX......",  # 9
        ".....XXXXX......",  # 10
        ".....XXXXX......",  # 11
        "....XX...XX.....",  # 12
        "....XX...XX.....",  # 13
        "....XX...XX.....",  # 14
        "................",  # 15
    ]

    # Map characters to colors
    color_map = {
        '.': T,
        'X': X,
        'H': H,
        'D': D,
        'E': E,
    }

    grid = []
    for row_str in layout:
        row = []
        for ch in row_str:
            row.append(color_map[ch])
        grid.append(row)

    # Apply icon
    icon_fn(grid)

    return grid


def icon_chest(grid):
    """Small square (chest icon) in center of body rows 9-11, cols 5-9."""
    # Draw a small chest: outline square rows 9-11, cols 6-8
    # Top bar row 9
    for c in range(6, 9):
        grid[9][c] = (255, 255, 255, 200)
    # Sides rows 10
    grid[10][6] = (255, 255, 255, 200)
    grid[10][8] = (255, 255, 255, 200)
    # Bottom row 11
    for c in range(6, 9):
        grid[11][c] = (255, 255, 255, 200)


def icon_feather(grid):
    """V shape (feather/wings) in center of body rows 9-11."""
    # V shape: row9 cols 6,8; row10 cols 6,8; row11 col 7
    grid[9][6] = (255, 255, 255, 200)
    grid[9][8] = (255, 255, 255, 200)
    grid[10][6] = (255, 255, 255, 200)
    grid[10][8] = (255, 255, 255, 200)
    grid[11][7] = (255, 255, 255, 200)


def icon_lightning(grid):
    """Lightning bolt shape in center of body rows 9-11."""
    # Lightning: row9 col 8, row10 cols 6-8, row11 col 6
    grid[9][8] = (255, 255, 255, 200)
    grid[10][6] = (255, 255, 255, 200)
    grid[10][7] = (255, 255, 255, 200)
    grid[10][8] = (255, 255, 255, 200)
    grid[11][6] = (255, 255, 255, 200)


def icon_star(grid):
    """Small star/sparkle shape in center of body rows 9-11."""
    # Star: center col7, top/bottom/left/right arms
    grid[9][7] = (255, 255, 255, 200)
    grid[10][6] = (255, 255, 255, 200)
    grid[10][7] = (255, 255, 255, 200)
    grid[10][8] = (255, 255, 255, 200)
    grid[11][7] = (255, 255, 255, 200)


# Color palettes
# Blue theme - keep_inventory
BLUE_X = (30, 80, 200, 255)
BLUE_H = (100, 160, 255, 255)
BLUE_D = (10, 40, 120, 255)
BLUE_E = (255, 255, 255, 255)

# Green theme - no_fall
GREEN_X = (40, 160, 60, 255)
GREEN_H = (120, 220, 100, 255)
GREEN_D = (20, 80, 30, 255)
GREEN_E = (255, 255, 255, 255)

# Orange-red theme - shockwave
ORANGE_X = (220, 100, 20, 255)
ORANGE_H = (255, 180, 60, 255)
ORANGE_D = (160, 40, 10, 255)
ORANGE_E = (255, 255, 255, 255)

# Purple theme - enchant
PURPLE_X = (120, 40, 200, 255)
PURPLE_H = (200, 80, 255, 255)
PURPLE_D = (60, 10, 120, 255)
PURPLE_E = (255, 255, 255, 255)

# Gray/iron theme
GRAY_X = (140, 140, 150, 255)
GRAY_H = (200, 200, 210, 255)
GRAY_D = (80, 80, 90, 255)
GRAY_E = (255, 255, 255, 255)

OUT_DIR = "C:/Users/Giamat13/code/more-totems/src/main/resources/assets/more-totems/textures/item"

totems = [
    ("totem_of_keep_inventory.png",      BLUE_X,   BLUE_H,   BLUE_D,   BLUE_E,   icon_chest),
    ("totem_of_no_fall.png",             GREEN_X,  GREEN_H,  GREEN_D,  GREEN_E,  icon_feather),
    ("totem_of_shockwave.png",           ORANGE_X, ORANGE_H, ORANGE_D, ORANGE_E, icon_lightning),
    ("totem_of_enchant.png",             PURPLE_X, PURPLE_H, PURPLE_D, PURPLE_E, icon_star),
    ("totem_of_keep_inventory_iron.png", GRAY_X,   GRAY_H,   GRAY_D,   GRAY_E,   icon_chest),
    ("totem_of_no_fall_iron.png",        GRAY_X,   GRAY_H,   GRAY_D,   GRAY_E,   icon_feather),
]

for fname, X, H, D, E, icon_fn in totems:
    grid = make_totem(X, H, D, E, icon_fn)
    path = os.path.join(OUT_DIR, fname)
    write_png(path, grid)
    print(f"Written: {path}")

print("Done!")
