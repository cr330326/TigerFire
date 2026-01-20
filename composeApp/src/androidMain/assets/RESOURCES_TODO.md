# TigerFire 资源文件清单

> 本文件列出所有需要的资源文件及其规格要求
> 请根据此清单准备和放置资源文件

---

## 📁 目录结构

```
assets/
├── audio/
│   ├── voices/           # 小火语音文件
│   └── sound_effects/    # 场景音效文件
├── videos/               # MP4 教学视频文件（已存在）
├── lottie/               # Lottie 动画文件
│   ├── welcome/
│   ├── firestation/
│   ├── school/
│   ├── forest/
│   ├── collection/
│   └── easter_egg/
└── images/               # 静态图片资源（待添加）
```

---

## 🔊 音频资源清单

### 语音文件 (audio/voices/)

| 文件名 | 用途 | 时长 | 规范 |
|--------|------|------|------|
| `welcome_greeting.mp3` | 启动页："HI！今天和我一起救火吧！" | ~3秒 | 正常语速，带停顿 |
| `firestation_complete.mp3` | 消防站完成："你真棒！记住，小火用它就能搞定！" | ~4秒 | 鼓励型 |
| `school_start.mp3` | 学校开始："学校着火啦！快叫消防车！" | ~3秒 | 紧急语调 |
| `school_complete.mp3` | 学校完成："你真棒！记住，着火要找大人帮忙！" | ~4秒 | 鼓励型 |
| `forest_start.mp3` | 森林开始："小羊被困啦！快开直升机救它们！" | ~3秒 | 紧急语调 |
| `forest_complete.mp3` | 森林完成："直升机能从天上救人，真厉害！" | ~4秒 | 鼓励型 |
| `collection_egg.mp3` | 集齐彩蛋："恭喜你收集了所有徽章！" | ~3秒 | 庆祝型 |
| `hint_idle.mp3` | 无操作提示："需要帮忙吗？" | ~2秒 | 温柔提示 |
| `hint_slow_click.mp3` | 快速点击提示："慢慢来，不着急~" | ~2秒 | 安抚型 |
| `time_up.mp3` | 时间到："时间到啦！我们明天再玩吧！" | ~3秒 | 提醒型 |

**音频规范**：
- 格式：MP3
- 采样率：44.1kHz
- 比特率：128kbps
- 声道：单声道
- 音量：-3dB（避免爆音）

---

### 音效文件 (audio/sound_effects/)

| 文件名 | 用途 | 长度 | 规范 |
|--------|------|------|------|
| `truck_horn.mp3` | 消防车鸣笛 | ~1秒 | 汽笛声 |
| `click_firestation.mp3` | 消防站点击音 | ~0.2秒 | "咔"声 |
| `click_school.mp3` | 学校点击音 | ~0.2秒 | "哔"声 |
| `click_forest.mp3` | 森林点击音 | ~0.2秒 | "呼"声 |
| `hint叮咚.mp3` | 可点击提示音 | ~0.3秒 | "叮咚" |
| `success.mp3` | 成功音效 | ~0.5秒 | "叮！" |
| `alarm.mp3` | 学校警报 | ~2秒 | 柔和警报 |
| `helicopter.mp3` | 直升机螺旋桨 | 循环 | 背景音 |
| `water_spray.mp3` | 喷水音效 | ~1秒 | 水流声 |
| `badge_collect.mp3` | 徽章收集 | ~1秒 | 魔法音 |

---

## 🎬 Lottie 动画资源清单

### 已有动画
- ✅ `anim_truck_enter.json` - 消防车入场
- ✅ `anim_xiaohuo_wave.json` - 小火挥手

### 待补充动画

| 目录 | 文件名 | 用途 | 时长 |
|------|--------|------|------|
| `welcome/` | `anim_truck_enter.json` | ✅ 已有 | 3-5秒 |
| `welcome/` | `anim_xiaohuo_wave.json` | ✅ 已有 | 2-3秒 |
| `firestation/` | `anim_device_click.json` | 设备点击反馈 | ~0.3秒 |
| `firestation/` | `anim_badge_appear.json` | 徽章弹出 | ~1秒 |
| `school/` | `anim_xiaohuo_thumbsup.json` | 小火点赞 | ~2秒 |
| `forest/` | `anim_helicopter_fly.json` | 直升机悬停 | 循环 |
| `collection/` | `anim_badge_shine.json` | 徽章闪光 | 循环 |
| `easter_egg/` | `anim_xiaohuo_dance.json` | 小火跳舞 | ~5秒 |
| `easter_egg/` | `anim_fireworks.json` | 烟花效果 | ~3秒 |

**动画规范**：
- 格式：Lottie JSON (5.0+)
- 帧率：30-60 FPS
- 文件大小：≤500KB/个
- 背景透明：支持 RGBA

---

## 🎥 视频资源清单

### 已有视频
| 文件名 | 用途 | 时长 | 大小 |
|--------|------|------|------|
| `firehydrant_cartoon.mp4` | ✅ 消防栓教学 | ~15秒 | 15MB |
| `fireladder_truck_cartoon.mp4` | ✅ 云梯教学 | ~15秒 | 12MB |
| `firefighter_cartoon.mp4` | ✅ 灭火器教学 | ~15秒 | 14MB |
| `firenozzle_cartoon.mp4` | ✅ 水枪教学 | ~15秒 | 11MB |
| `School_Fire_Safety_Knowledge.mp4` | ✅ 学校剧情 | ~45秒 | 38MB |

### 待补充视频

| 文件名 | 用途 | 时长 | 优先级 |
|--------|------|------|--------|
| `rescue_sheep_1.mp4` | 救援小羊1 | ~10秒 | 高 |
| `rescue_sheep_2.mp4` | 救援小羊2 | ~10秒 | 高 |

**视频规范**：
- 格式：MP4 (H.264)
- 分辨率：1920×1080 (16:9) 或 1280×720
- 帧率：30 FPS
- 比特率：2-4 Mbps
- 音频：AAC 128kbps

---

## 🖼️ 图片资源清单（✅ 已完成 - 2025-01-20）

| 类型 | 文件名 | 用途 | 尺寸 | 状态 |
|------|--------|------|------|------|
| 背景 | `bg_map.png` | 地图背景 | 1920×1080 | ✅ 已生成 |
| 背景 | `bg_firestation.png` | 消防站背景 | 1920×1080 | ✅ 已生成 |
| 背景 | `bg_school.png` | 学校背景 | 1920×1080 | ✅ 已生成 |
| 背景 | `bg_forest.png` | 森林背景 | 1920×1080 | ✅ 已生成 |
| 图标 | `icon_badge_base.png` | 徽章基础图标 | 512×512 | ✅ 已生成 |
| UI | `btn_parent.png` | 家长模式按钮 | 200×200 | ✅ 已生成 |
| UI | `btn_collection.png` | 收藏按钮 | 200×200 | ✅ 已生成 |

**生成方式**: 使用 `scripts/generate_images.py` Python 脚本自动生成

---

## 📋 资源制作外包清单

### 需要外部制作的内容

| 资源类型 | 数量 | 制作方 | 交付格式 |
|---------|------|--------|----------|
| 语音文件 | 10段 | 配音工作室 | MP3 |
| 音效文件 | 10段 | 音效库/自制 | MP3 |
| Lottie 动画 | 7个 | 动画设计师 | JSON |
| MP4 视频 | 2个 | 动画工作室 | MP4 |
| 静态图片 | 7个 | UI设计师 | PNG |

---

## 🔍 资源验证清单

### 使用前检查

- [ ] 所有音频文件可正常播放
- [ ] Lottie 动画在 Android 设备上正常显示
- [ ] MP4 视频在 ExoPlayer 中正常播放
- [ ] 视频时长符合规格要求
- [ ] 音频音量一致且无爆音
- [ ] 所有资源文件名与代码中的引用一致

---

## 📝 资源文件命名规范

1. **全部小写字母**
2. **使用下划线分隔单词** (snake_case)
3. **描述性命名**：`用途_描述.扩展名`
4. **示例**：
   - ✅ `badge_collect.mp3`
   - ✅ `anim_device_click.json`
   - ❌ `BadgeCollect.mp3`
   - ❌ `animation.json`

---

## 🔄 更新记录

| 日期 | 更新内容 |
|------|----------|
| 2025-01-20 | 创建资源清单，标记已有资源 |
