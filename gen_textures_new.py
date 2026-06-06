import struct, zlib, os

def write_png(filename, pixels):
    width = height = 16
    def chunk(name, data):
        c = name + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
    sig = b'\x89PNG\r\n\x1a\n'
    ihdr_data = struct.pack('>II', width, height) + bytes([8, 6, 0, 0, 0])
    ihdr = chunk(b'IHDR', ihdr_data)
    raw = b''
    for row in pixels:
        raw += b'\x00'
        for (r,g,b,a) in row:
            raw += bytes([r,g,b,a])
    compressed = zlib.compress(raw, 9)
    idat = chunk(b'IDAT', compressed)
    iend = chunk(b'IEND', b'')
    with open(filename, 'wb') as f:
        f.write(sig + ihdr + idat + iend)

def grid_to_pixels(grid, palette):
    pixels = []
    for row_str in grid:
        row = []
        for ch in row_str:
            row.append(palette.get(ch, (255, 0, 255, 255)))
        pixels.append(row)
    return pixels

def fix(r, n=16):
    if len(r) < n:
        return r + '.' * (n - len(r))
    return r[:n]

OUT = r'C:\Users\Giamat13\code\more-totems\src\main\resources\assets\more-totems\textures\item'
os.makedirs(OUT, exist_ok=True)

# ── 1. CHEST (brown/gold) ──────────────────────────────────────────────────
chest_pal = {
    '.': (  0,  0,  0,  0),
    'D': ( 70, 40, 10,255),
    'B': (139, 90, 43,255),
    'W': (185,130, 65,255),
    'G': (212,175, 55,255),
    'L': (255,215,  0,255),
    'H': (100, 60, 20,255),
}
chest = [
    fix('................'),
    fix('................'),
    fix('.DDDDDDDDDDDD...'),
    fix('.DWWWWWWWWWWD...'),
    fix('.DBBBBBBBBBBD...'),
    fix('.DGGGGGGGGGGD...'),
    fix('.DDDDDDDDDDDD...'),
    fix('.DBBBBLBBBBD....'),
    fix('.DBBBBBBBBBD....'),
    fix('.DBBBBBBBBBD....'),
    fix('.DHBBBBBBBHD....'),
    fix('.DGGGGGGGGGGD...'),
    fix('.DDDDDDDDDDDD...'),
    fix('................'),
    fix('................'),
    fix('................'),
]

# ── 2. STEEL CHEST (gray) ──────────────────────────────────────────────────
chest_iron_pal = {
    '.': (  0,  0,  0,  0),
    'D': ( 50, 50, 55,255),
    'B': (125,125,135,255),
    'W': (190,190,200,255),
    'G': (170,175,185,255),
    'L': (220,225,235,255),
    'H': ( 80, 80, 90,255),
}

# ── 3. FEATHER (green) ────────────────────────────────────────────────────
feather_pal = {
    '.': (  0,  0,  0,  0),
    'S': ( 20,100, 20,255),
    'V': ( 70,170, 70,255),
    'L': (140,220,100,255),
    'T': ( 45,130, 45,255),
    'Q': (200,240,120,255),
    'X': ( 10, 70, 10,255),
}
feather = [
    fix('..............SQ'),
    fix('.............SVL'),
    fix('............SVLL'),
    fix('...........STVLL'),
    fix('..........STVLL.'),
    fix('.........LSTVL..'),
    fix('........LLSTV...'),
    fix('.......VLLST....'),
    fix('......VVLLS.....'),
    fix('.....TVVLS......'),
    fix('....TTVLS.......'),
    fix('...VTVLS........'),
    fix('..SVTLS.........'),
    fix('.XSVLS..........'),
    fix('XS..............'),
    fix('X...............'),
]

# ── 4. SILVER FEATHER ─────────────────────────────────────────────────────
feather_iron_pal = {
    '.': (  0,  0,  0,  0),
    'S': ( 55, 55, 65,255),
    'V': (120,120,135,255),
    'L': (185,185,200,255),
    'T': ( 85, 85, 98,255),
    'Q': (220,225,235,255),
    'X': ( 30, 30, 38,255),
}

# ── 5. EXPLOSION BURST (orange/red/yellow) ─────────────────────────────────
burst_pal = {
    '.': (  0,  0,  0,  0),
    'W': (255,255,220,255),
    'C': (255,240, 80,255),
    'Y': (255,190,  0,255),
    'O': (255,110,  0,255),
    'R': (210, 40,  0,255),
    'D': (140, 20,  0,255),
}
burst = [
    fix('...D..........D.'),
    fix('....O........O..'),
    fix('D....R......R..D'),
    fix('.O....Y....Y..O.'),
    fix('..R..YOOOOY..R..'),
    fix('...YOOCCCOO.Y...'),
    fix('..ROOCWWWCOOR...'),
    fix('..YOCCWWWCCOYR..'),
    fix('...YOOCCCOO.Y...'),
    fix('..R..YOOOOY..R..'),
    fix('.O....Y....Y..O.'),
    fix('D....R......R..D'),
    fix('....O........O..'),
    fix('...D..........D.'),
    fix('................'),
    fix('................'),
]

# ── 6. GRAY BURST ─────────────────────────────────────────────────────────
burst_iron_pal = {
    '.': (  0,  0,  0,  0),
    'W': (245,245,255,255),
    'C': (210,210,225,255),
    'Y': (170,170,185,255),
    'O': (130,130,145,255),
    'R': ( 85, 85,100,255),
    'D': ( 45, 45, 55,255),
}

# ── 7. MAGIC BOOK (purple + gold star) ────────────────────────────────────
book_pal = {
    '.': (  0,  0,  0,  0),
    'D': ( 50,  0, 70,255),
    'P': (140, 40,180,255),
    'L': (200,110,230,255),
    'N': ( 80, 10,110,255),
    'T': (170, 80,210,255),
    'G': (200,155, 30,255),
    'Y': (255,215,  0,255),
    'W': (255,255,160,255),
    'K': (255,245, 80,255),
}
book = [
    fix('.....WKYKW......'),
    fix('....WKYYYYYYW...'),
    fix('.....WKYKW......'),
    fix('................'),
    fix('.DDDDDNDDDDDDD..'),
    fix('.DPLLLLNLLLLPD..'),
    fix('.DPLLLLNLLLLPD..'),
    fix('.DPTTTTNTTTTPD..'),
    fix('.DPLLLLNLLLLPD..'),
    fix('.DPTTTTNTTTTPD..'),
    fix('.DPLLLLNLLLLPD..'),
    fix('.DGGGGGNGGGGGGD.'),
    fix('.DDDDDNDDDDDDD..'),
    fix('................'),
    fix('................'),
    fix('................'),
]

# ── 8. GRAY BOOK ─────────────────────────────────────────────────────────
book_iron_pal = {
    '.': (  0,  0,  0,  0),
    'D': ( 35, 35, 42,255),
    'P': (125,125,138,255),
    'L': (180,180,195,255),
    'N': ( 55, 55, 65,255),
    'T': (145,145,158,255),
    'G': (155,155,165,255),
    'Y': (215,218,228,255),
    'W': (235,237,245,255),
    'K': (200,202,215,255),
}

textures = [
    ('totem_of_keep_inventory',      chest,   chest_pal),
    ('totem_of_keep_inventory_iron', chest,   chest_iron_pal),
    ('totem_of_no_fall',             feather, feather_pal),
    ('totem_of_no_fall_iron',        feather, feather_iron_pal),
    ('totem_of_shockwave',           burst,   burst_pal),
    ('totem_of_shockwave_iron',      burst,   burst_iron_pal),
    ('totem_of_enchant',             book,    book_pal),
    ('totem_of_enchant_iron',        book,    book_iron_pal),
]

for name, grid, pal in textures:
    bad = set()
    for row in grid:
        for ch in row:
            if ch not in pal:
                bad.add(ch)
    if bad:
        print(f'WARNING {name}: unknown chars {bad}')
    pixels = grid_to_pixels(grid, pal)
    path = os.path.join(OUT, f'{name}.png')
    write_png(path, pixels)
    print(f'\n=== {name} ===')
    for row in grid:
        print('   ', row)
    print(f'   -> {path}')

print('\nDone - 8 textures written.')
