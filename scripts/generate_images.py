#!/usr/bin/env python3
"""
生成 TigerFire App 的占位图片资源
使用纯 Python 标准库生成 PNG 图片
"""

import struct
import zlib
import os

def create_png(width, height, r, g, b, a=255):
    """创建纯色 PNG 图片"""

    # PNG 文件签名
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk (图片头信息)
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)  # RGBA, 8bit
    ihdr_chunk = create_chunk(b'IHDR', ihdr_data)

    # IDAT chunk (图片数据)
    # 每行添加一个过滤字节 (0 = 无过滤)
    row_data = b'\x00' + (struct.pack('4B', r, g, b, a) * width)
    raw_data = row_data * height
    compressed_data = zlib.compress(raw_data)
    idat_chunk = create_chunk(b'IDAT', compressed_data)

    # IEND chunk (文件结束)
    iend_chunk = create_chunk(b'IEND', b'')

    return png_signature + ihdr_chunk + idat_chunk + iend_chunk

def create_chunk(chunk_type, data):
    """创建 PNG chunk"""
    length = struct.pack('>I', len(data))
    crc = zlib.crc32(chunk_type + data) & 0xffffffff
    crc_bytes = struct.pack('>I', crc)
    return length + chunk_type + data + crc_bytes

def create_gradient_png(width, height, start_color, end_color, direction='horizontal'):
    """创建渐变 PNG 图片"""
    pixels = []
    r1, g1, b1, a1 = start_color
    r2, g2, b2, a2 = end_color

    for y in range(height):
        for x in range(width):
            if direction == 'horizontal':
                t = x / width
            else:  # vertical
                t = y / height

            r = int(r1 + (r2 - r1) * t)
            g = int(g1 + (g2 - g1) * t)
            b = int(b1 + (b2 - b1) * t)
            a = int(a1 + (a2 - a1) * t)
            pixels.append(struct.pack('4B', r, g, b, a))

    # PNG 文件签名
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)  # RGBA, 8bit
    ihdr_chunk = create_chunk(b'IHDR', ihdr_data)

    # IDAT chunk (带过滤)
    row_data = b''
    for y in range(height):
        row_start = y * width * 4
        row_end = row_start + width * 4
        row_pixels = pixels[row_start:row_end]
        row_data += b'\x00' + b''.join(row_pixels)

    compressed_data = zlib.compress(row_data)
    idat_chunk = create_chunk(b'IDAT', compressed_data)

    # IEND chunk
    iend_chunk = create_chunk(b'IEND', b'')

    return png_signature + ihdr_chunk + idat_chunk + iend_chunk

def create_badge_icon(width, height):
    """创建徽章基础图标（金色星形背景）"""
    # 创建外圈渐变背景
    pixels = []
    center_x, center_y = width // 2, height // 2

    for y in range(height):
        for x in range(width):
            dx = x - center_x
            dy = y - center_y
            dist = (dx * dx + dy * dy) ** 0.5
            max_dist = (width * width + height * height) ** 0.5 / 2

            # 径向渐变：金色到深金色
            t = min(dist / max_dist, 1.0)
            r = int(255 + (200 - 255) * t)
            g = int(215 + (150 - 215) * t)
            b = int(0 + (50 - 0) * t)
            a = 255

            pixels.append(struct.pack('4B', r, g, b, a))

    # PNG 文件签名
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_chunk = create_chunk(b'IHDR', ihdr_data)

    # IDAT chunk
    row_data = b''
    for y in range(height):
        row_start = y * width * 4
        row_end = row_start + width * 4
        row_pixels = pixels[row_start:row_end]
        row_data += b'\x00' + b''.join(row_pixels)

    compressed_data = zlib.compress(row_data)
    idat_chunk = create_chunk(b'IDAT', compressed_data)

    # IEND chunk
    iend_chunk = create_chunk(b'IEND', b'')

    return png_signature + ihdr_chunk + idat_chunk + iend_chunk

def create_round_button(width, height, bg_color, icon_emoji=''):
    """创建圆形按钮"""
    center_x, center_y = width // 2, height // 2
    radius = min(width, height) // 2 - 4

    r_bg, g_bg, b_bg = bg_color

    pixels = []
    for y in range(height):
        for x in range(width):
            dx = x - center_x
            dy = y - center_y
            dist = (dx * dx + dy * dy) ** 0.5

            if dist <= radius:
                # 圆形内部 - 背景色
                # 添加一些阴影效果
                shadow = max(0, min(1, (radius - dist) / 10))
                r = int(r_bg * shadow)
                g = int(g_bg * shadow)
                b = int(b_bg * shadow)
                a = 255
            else:
                # 圆形外部 - 透明
                r, g, b, a = 0, 0, 0, 0

            pixels.append(struct.pack('4B', r, g, b, a))

    # PNG 文件签名
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_chunk = create_chunk(b'IHDR', ihdr_data)

    # IDAT chunk
    row_data = b''
    for y in range(height):
        row_start = y * width * 4
        row_end = row_start + width * 4
        row_pixels = pixels[row_start:row_end]
        row_data += b'\x00' + b''.join(row_pixels)

    compressed_data = zlib.compress(row_data)
    idat_chunk = create_chunk(b'IDAT', compressed_data)

    # IEND chunk
    iend_chunk = create_chunk(b'IEND', b'')

    return png_signature + ihdr_chunk + idat_chunk + iend_chunk

def main():
    """生成所有图片资源"""
    base_dir = 'composeApp/src/androidMain/assets/images'
    os.makedirs(base_dir, exist_ok=True)

    print("开始生成图片资源...")

    # 1. 地图背景 (bg_map.png) - 1920x1080
    # 浅灰蓝色渐变背景
    print("生成 bg_map.png...")
    img_data = create_gradient_png(1920, 1080, (230, 240, 255, 255), (200, 220, 250, 255), 'vertical')
    with open(f'{base_dir}/bg_map.png', 'wb') as f:
        f.write(img_data)

    # 2. 消防站背景 (bg_firestation.png) - 1920x1080
    # 红色渐变背景 (#FF6B6B 到 #E63946)
    print("生成 bg_firestation.png...")
    img_data = create_gradient_png(1920, 1080, (255, 107, 107, 255), (230, 57, 70, 255), 'vertical')
    with open(f'{base_dir}/bg_firestation.png', 'wb') as f:
        f.write(img_data)

    # 3. 学校背景 (bg_school.png) - 1920x1080
    # 蓝色渐变背景 (#A8DADC 到 #457B9D)
    print("生成 bg_school.png...")
    img_data = create_gradient_png(1920, 1080, (168, 218, 220, 255), (69, 123, 157, 255), 'vertical')
    with open(f'{base_dir}/bg_school.png', 'wb') as f:
        f.write(img_data)

    # 4. 森林背景 (bg_forest.png) - 1920x1080
    # 绿色渐变背景 (#2A9D8F 到 #264653)
    print("生成 bg_forest.png...")
    img_data = create_gradient_png(1920, 1080, (42, 157, 143, 255), (38, 70, 83, 255), 'vertical')
    with open(f'{base_dir}/bg_forest.png', 'wb') as f:
        f.write(img_data)

    # 5. 徽章基础图标 (icon_badge_base.png) - 512x512
    # 金色星形背景
    print("生成 icon_badge_base.png...")
    img_data = create_badge_icon(512, 512)
    with open(f'{base_dir}/icon_badge_base.png', 'wb') as f:
        f.write(img_data)

    # 6. 家长模式按钮 (btn_parent.png) - 200x200
    # 灰色圆形按钮
    print("生成 btn_parent.png...")
    img_data = create_round_button(200, 200, (150, 150, 150))
    with open(f'{base_dir}/btn_parent.png', 'wb') as f:
        f.write(img_data)

    # 7. 收藏按钮 (btn_collection.png) - 200x200
    # 黄色圆形按钮
    print("生成 btn_collection.png...")
    img_data = create_round_button(200, 200, (244, 162, 97))
    with open(f'{base_dir}/btn_collection.png', 'wb') as f:
        f.write(img_data)

    print("\n所有图片资源生成完成！")
    print(f"图片保存位置: {base_dir}/")
    print("\n生成的文件:")
    print("  - bg_map.png (1920x1080)")
    print("  - bg_firestation.png (1920x1080)")
    print("  - bg_school.png (1920x1080)")
    print("  - bg_forest.png (1920x1080)")
    print("  - icon_badge_base.png (512x512)")
    print("  - btn_parent.png (200x200)")
    print("  - btn_collection.png (200x200)")

if __name__ == '__main__':
    main()
