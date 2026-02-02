import Foundation
import AVFoundation

/**
 * iOS 音频播放器
 *
 * 使用 AVAudioPlayer 播放音效和语音
 * 支持同时播放 BGM 与音效（AVAudioSession 多音频混音）
 *
 * 设计原则：
 * - 使用单例模式管理共享资源
 * - 短音效使用 AVAudioPlayer，语音也使用 AVAudioPlayer
 * - 支持 AVAudioSessionCategory.ambient 以支持混音
 * - 自动管理播放器池，避免资源泄漏
 *
 * 音频资源路径规范（与 Android 共享 assets 目录）：
 * - 音效文件：assets/audio/sound_effects/{name}.wav
 * - 语音文件：assets/audio/voices/{name}.mp3
 * - 背景音乐：assets/audio/music/{name}.mp3
 */
@objc public class IosAudioPlayer: NSObject {

    // MARK: - 单例

    @objc public static let shared = IosAudioPlayer()

    // MARK: - 私有属性

    /// 音效播放器池（音效名 -> AVAudioPlayer）
    private var soundPlayers: [String: AVAudioPlayer] = [:]

    /// 语音播放器池（路径 -> AVAudioPlayer）
    private var voicePlayers: [String: AVAudioPlayer] = [:]

    /// BGM 播放器
    private var bgmPlayer: AVAudioPlayer?

    /// 当前正在播放的警报播放器
    private var alertPlayer: AVAudioPlayer?

    // MARK: - 初始化

    private override init() {
        super.init()
        setupAudioSession()
    }

    /// 配置音频会话
    private func setupAudioSession() {
        do {
            let session = AVAudioSession.sharedInstance()
            // 使用 ambient 模式，支持与其他音频混音
            try session.setCategory(.ambient, mode: .default)
            try session.setActive(true)
        } catch {
            NSLog("Failed to setup AVAudioSession: \(error)")
        }
    }

    // MARK: - 公开方法 - 场景差异化音效

    /// 播放点击音效（场景差异化）
    @objc public func playClickSound(sceneValue: Int) {
        let scene = SceneType.fromValue(sceneValue)
        playSoundEffect(.click, scene: scene)
    }

    // MARK: - 公开方法 - 语音播放

    /// 播放语音
    @objc public func playVoice(voicePath: String) {
        guard let player = createVoicePlayer(forPath: voicePath) else {
            NSLog("Failed to create voice player for: \(voicePath)")
            return
        }

        voicePlayers[voicePath] = player
        player.play()

        // 播放完成后释放资源
        player.delegate = self
    }

    /// 播放欢迎语音
    @objc public func playWelcomeVoice() {
        playVoice(voicePath: "audio/voices/welcome_greeting.mp3")
    }

    /// 播放学校警报语音
    @objc public func playSchoolAlertVoice() {
        playVoice(voicePath: "audio/voices/school_fire.mp3")
    }

    /// 播放学校夸奖语音
    @objc public func playSchoolPraiseVoice() {
        playVoice(voicePath: "audio/voices/school_praise.mp3")
    }

    /// 播放森林完成语音
    @objc public func playForestCompletionVoice() {
        playVoice(voicePath: "audio/voices/forest_complete.mp3")
    }

    /// 播放时间到语音
    @objc public func playTimeUpVoice() {
        playVoice(voicePath: "audio/voices/time_up.mp3")
    }

    // MARK: - 公开方法 - 音效播放

    /// 播放成功音效
    @objc public func playSuccessSound() {
        playSoundEffect(.success)
    }

    /// 播放提示音效
    @objc public func playHintSound() {
        playSoundEffect(.hint)
    }

    /// 播放拖拽音效
    @objc public func playDragSound() {
        playSoundEffect(.drag)
    }

    /// 播放吸附音效
    @objc public func playSnapSound() {
        playSoundEffect(.snap)
    }

    /// 播放徽章音效
    @objc public func playBadgeSound() {
        playSoundEffect(.badge)
    }

    /// 播放全部完成音效
    @objc public func playAllCompletedSound() {
        playSoundEffect(.allCompleted)
    }

    // MARK: - 公开方法 - 警报音效

    /// 播放警报音效（循环播放）
    @objc public func playAlertSound() {
        guard let player = createSoundPlayer(forType: .alert) else {
            return
        }

        // 循环播放
        player.numberOfLoops = -1

        alertPlayer = player
        player.play()
    }

    /// 停止警报音效
    @objc public func stopAlertSound() {
        alertPlayer?.stop()
        alertPlayer = nil
    }

    // MARK: - 公开方法 - BGM 播放

    /// 播放欢迎页 BGM
    @objc public func playWelcomeBGM() {
        playBGM("audio/music/fire_engine.mp3")
    }

    /// 播放主地图 BGM
    @objc public func playMapBGM() {
        playBGM("audio/music/fire_engine.mp3")
    }

    /// 停止 BGM
    @objc public func stopBGM() {
        bgmPlayer?.stop()
        bgmPlayer = nil
    }

    /// 播放 BGM
    private func playBGM(_ path: String) {
        // 停止当前 BGM
        stopBGM()

        guard let url = Bundle.main.url(forResource: path, withExtension: nil) else {
            NSLog("BGM file not found: \(path)")
            return
        }

        guard let player = try? AVAudioPlayer(contentsOf: url) else {
            NSLog("Failed to create AVAudioPlayer for BGM: \(path)")
            return
        }

        // 循环播放
        player.numberOfLoops = -1

        bgmPlayer = player
        player.play()
    }

    // MARK: - 公开方法 - 资源管理

    /// 释放所有音频资源
    @objc public func releaseAudioResources() {
        stopAlertSound()
        stopBGM()

        // 停止并释放所有音效播放器
        soundPlayers.values.forEach { $0.stop() }
        soundPlayers.removeAll()

        // 停止并释放所有语音播放器
        voicePlayers.values.forEach { $0.stop() }
        voicePlayers.removeAll()
    }

    // MARK: - 私有方法 - 音效资源映射

    /**
     * 音效资源文件名映射
     *
     * 根据 assets/audio/sound_effects/ 目录下的实际文件
     * 文件格式：.wav
     */
    private func getSoundFileName(type: SoundType, scene: SceneType? = nil) -> String {
        switch type {
        case .click:
            // 点击音效（所有场景共用）
            return "audio/sound_effects/click.wav"
        case .success:
            return "audio/sound_effects/success.wav"
        case .hint:
            return "audio/sound_effects/hint.wav" // "叮咚"
        case .drag:
            return "audio/sound_effects/helicopter.wav" // 拖拽音效：直升机声音
        case .snap:
            return "audio/sound_effects/water.wav" // 吸附音效：水流声音
        case .badge:
            return "audio/sound_effects/collect.wav" // 徽章获得：收集音效
        case .allCompleted:
            return "audio/sound_effects/truck_horn.wav" // 全部完成：卡车喇叭
        case .alert:
            return "audio/sound_effects/alert.wav" // 警报音效
        case .voice:
            return "audio/voices/voice"
        }
    }

    /// 获取音频文件路径（保持原有格式兼容）
    private func getSoundFilePath(fileName: String) -> String {
        return fileName
    }

    /// 播放短音效
    private func playSoundEffect(_ type: SoundType, scene: SceneType? = nil) {
        guard let player = createSoundPlayer(forType: type, scene: scene) else {
            NSLog("Failed to create sound player for type: \(type)")
            return
        }

        player.play()
    }

    /// 创建音效播放器（带缓存）
    private func createSoundPlayer(forType type: SoundType, scene: SceneType? = nil) -> AVAudioPlayer? {
        let filePath = getSoundFileName(type: type, scene: scene)

        // 检查缓存
        if let cachedPlayer = soundPlayers[filePath] {
            cachedPlayer.currentTime = 0  // 重置到开头
            return cachedPlayer
        }

        // 创建新播放器（filePath 已包含完整路径和扩展名）
        guard let url = Bundle.main.url(forResource: filePath, withExtension: nil) else {
            NSLog("Sound file not found: \(filePath)")
            return nil
        }

        guard let player = try? AVAudioPlayer(contentsOf: url) else {
            NSLog("Failed to create AVAudioPlayer for: \(filePath)")
            return nil
        }

        // 缓存播放器
        soundPlayers[filePath] = player
        return player
    }

    /// 创建语音播放器（不缓存，每次创建新的）
    private func createVoicePlayer(forPath path: String) -> AVAudioPlayer? {
        // 先检查是否已有该路径的播放器
        if let existingPlayer = voicePlayers[path] {
            existingPlayer.currentTime = 0
            return existingPlayer
        }

        // path 已包含完整路径和扩展名（如 "audio/voices/welcome_greeting.mp3"）
        // 直接使用 withExtension: nil 来加载完整路径
        guard let url = Bundle.main.url(forResource: path, withExtension: nil) else {
            NSLog("Voice file not found: \(path)")
            return nil
        }

        guard let player = try? AVAudioPlayer(contentsOf: url) else {
            NSLog("Failed to create AVAudioPlayer for voice: \(path)")
            return nil
        }

        return player
    }
}

// MARK: - AVAudioPlayerDelegate

extension IosAudioPlayer: AVAudioPlayerDelegate {
    public func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        // 播放完成后从池中移除
        if let voicePath = voicePlayers.first(where: { $0.value === player })?.key {
            voicePlayers.removeValue(forKey: voicePath)
        }
    }

    public func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
        if let error = error {
            NSLog("Audio player decode error: \(error)")
        }

        // 发生错误时移除播放器
        if let voicePath = voicePlayers.first(where: { $0.value === player })?.key {
            voicePlayers.removeValue(forKey: voicePath)
        }
    }
}

// MARK: - 音频类型枚举

private enum SoundType {
    case click
    case voice
    case success
    case hint
    case drag
    case snap
    case badge
    case allCompleted
    case alert
}

// MARK: - 场景类型枚举

public enum SceneType: Int {
    case fireStation = 0
    case school = 1
    case forest = 2
    case none = -1

    static func fromValue(_ value: Int) -> SceneType? {
        switch value {
        case 0: return .fireStation
        case 1: return .school
        case 2: return .forest
        case -1: return .none
        default: return nil
        }
    }
}

// MARK: - C 风格函数导出（供 Kotlin 调用）

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playClickSoundI")
public func playClickSoundC(sceneValue: Int32) {
    IosAudioPlayer.shared.playClickSound(sceneValue: Int(sceneValue))
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playVoiceV")
public func playVoiceC(voicePath: UnsafePointer<CChar>) {
    let path = String(cString: voicePath)
    IosAudioPlayer.shared.playVoice(voicePath: path)
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playSuccessSoundV")
public func playSuccessSoundC() {
    IosAudioPlayer.shared.playSuccessSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playHintSoundV")
public func playHintSoundC() {
    IosAudioPlayer.shared.playHintSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playDragSoundV")
public func playDragSoundC() {
    IosAudioPlayer.shared.playDragSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playSnapSoundV")
public func playSnapSoundC() {
    IosAudioPlayer.shared.playSnapSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playBadgeSoundV")
public func playBadgeSoundC() {
    IosAudioPlayer.shared.playBadgeSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playAllCompletedSoundV")
public func playAllCompletedSoundC() {
    IosAudioPlayer.shared.playAllCompletedSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_playAlertSoundV")
public func playAlertSoundC() {
    IosAudioPlayer.shared.playAlertSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_stopAlertSoundV")
public func stopAlertSoundC() {
    IosAudioPlayer.shared.stopAlertSound()
}

@_cdecl("com_cryallen_tigerfire_presentation_audio_IosAudioPlayerHelper_releaseV")
public func releaseC() {
    IosAudioPlayer.shared.releaseAudioResources()
}
