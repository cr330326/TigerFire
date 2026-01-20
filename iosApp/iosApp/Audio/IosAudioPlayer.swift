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
 */
@objc public class IosAudioPlayer: NSObject {

    // MARK: - 单例

    @objc public static let shared = IosAudioPlayer()

    // MARK: - 私有属性

    /// 音效播放器池（音效名 -> AVAudioPlayer）
    private var soundPlayers: [String: AVAudioPlayer] = [:]

    /// 语音播放器池（路径 -> AVAudioPlayer）
    private var voicePlayers: [String: AVAudioPlayer] = [:]

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

    // MARK: - 公开方法

    /// 播放点击音效（场景差异化）
    @objc public func playClickSound(sceneValue: Int) {
        let scene = SceneType.fromValue(sceneValue)
        playSoundEffect(.click, scene: scene)
    }

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
        // 可以同时播放语音
        playVoice("audio/voice/all_completed.mp3")
    }

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

    /// 释放所有音频资源
    @objc public func release() {
        stopAlertSound()

        // 停止并释放所有音效播放器
        soundPlayers.values.forEach { $0.stop() }
        soundPlayers.removeAll()

        // 停止并释放所有语音播放器
        voicePlayers.values.forEach { $0.stop() }
        voicePlayers.removeAll()
    }

    // MARK: - 私有方法

    /// 音效资源映射
    private func getSoundFileName(type: SoundType, scene: SceneType? = nil) -> String {
        switch type {
        case .click:
            switch scene {
            case .fireStation:
                return "fire_click"
            case .school:
                return "school_click"
            case .forest:
                return "forest_click"
            case .none:
                return "click"
            }
        case .success:
            return "success"
        case .hint:
            return "hint"
        case .drag:
            return "drag"
        case .snap:
            return "snap"
        case .badge:
            return "badge"
        case .allCompleted:
            return "all_completed"
        case .alert:
            return "alert"
        case .voice:
            return "voice"
        }
    }

    /// 获取音频文件路径
    private func getSoundFilePath(fileName: String) -> String {
        return "audio/\(fileName).mp3"
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
        let fileName = getSoundFileName(type: type, scene: scene)

        // 检查缓存
        if let cachedPlayer = soundPlayers[fileName] {
            cachedPlayer.currentTime = 0  // 重置到开头
            return cachedPlayer
        }

        // 创建新播放器
        let filePath = getSoundFilePath(fileName: fileName)
        guard let url = Bundle.main.url(forResource: filePath, withExtension: nil) else {
            NSLog("Sound file not found: \(filePath)")
            return nil
        }

        guard let player = try? AVAudioPlayer(contentsOf: url) else {
            NSLog("Failed to create AVAudioPlayer for: \(filePath)")
            return nil
        }

        // 缓存播放器
        soundPlayers[fileName] = player
        return player
    }

    /// 创建语音播放器（不缓存，每次创建新的）
    private func createVoicePlayer(forPath path: String) -> AVAudioPlayer? {
        // 先检查是否已有该路径的播放器
        if let existingPlayer = voicePlayers[path] {
            existingPlayer.currentTime = 0
            return existingPlayer
        }

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
    IosAudioPlayer.shared.release()
}
