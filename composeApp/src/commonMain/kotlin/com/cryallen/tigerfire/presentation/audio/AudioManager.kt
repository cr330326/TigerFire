package com.cryallen.tigerfire.presentation.audio

import com.cryallen.tigerfire.domain.model.SceneType

/**
 * 音频管理器接口
 *
 * 定义音频播放的跨平台接口
 * 每个平台需要提供自己的实现
 */
interface AudioManager {
    /**
     * 播放点击音效（场景差异化）
     *
     * @param scene 场景类型，不同场景使用不同音效
     */
    fun playClickSound(scene: SceneType? = null)

    /**
     * 播放语音
     *
     * @param voicePath 语音文件路径（相对于 assets 目录）
     */
    fun playVoice(voicePath: String)

    /**
     * 播放成功音效
     */
    fun playSuccessSound()

    /**
     * 播放提示音效
     */
    fun playHintSound()

    /**
     * 播放拖拽音效
     */
    fun playDragSound()

    /**
     * 播放吸附成功音效
     */
    fun playSnapSound()

    /**
     * 播放徽章获得音效
     */
    fun playBadgeSound()

    /**
     * 播放全部完成音效
     */
    fun playAllCompletedSound()

    /**
     * 播放警报音效（学校场景）
     */
    fun playAlertSound()

    /**
     * 停止警报音效
     */
    fun stopAlertSound()

    /**
     * 释放音频资源
     */
    fun release()
}

/**
 * 音频类型枚举
 */
enum class SoundType {
    /** 点击音效 */
    CLICK,
    /** 语音 */
    VOICE,
    /** 成功音效 */
    SUCCESS,
    /** 提示音效 */
    HINT,
    /** 拖拽音效 */
    DRAG,
    /** 吸附音效 */
    SNAP,
    /** 徽章音效 */
    BADGE,
    /** 全部完成音效 */
    ALL_COMPLETED,
    /** 警报音效 */
    ALERT
}
