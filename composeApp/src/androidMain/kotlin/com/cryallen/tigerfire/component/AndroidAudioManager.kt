package com.cryallen.tigerfire.component

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.cryallen.tigerfire.domain.model.SceneType
import com.cryallen.tigerfire.presentation.audio.AudioManager
import com.cryallen.tigerfire.presentation.audio.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 音频管理器实现
 *
 * 使用 SoundPool 管理短音效，MediaPlayer 播放语音
 * 支持同时播放 BGM 与音效
 *
 * @param context Android Context
 */
class AndroidAudioManager(
    private val context: Context
) : AudioManager {

    companion object {
        /** 单例实例 */
        @Volatile
        private var INSTANCE: AndroidAudioManager? = null

        /**
         * 获取单例实例
         *
         * @param context Android Context
         * @return AudioManager 实例
         */
        fun getInstance(context: Context): AndroidAudioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidAudioManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // ==================== SoundPool 配置 ====================

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10) // 最多同时播放 10 个音效
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // 音效 ID 缓存（资源名 -> SoundPool ID）
    private val soundIdCache = ConcurrentHashMap<String, Int>()

    // MediaPlayer 实例池（用于播放语音等长音频）
    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    // 当前正在播放的警报音效
    private var alertMediaPlayer: MediaPlayer? = null

    // 协程作用域
    private val scope = CoroutineScope(Dispatchers.IO)

    // ==================== 音效资源映射 ====================

    /**
     * 获取音效资源名称
     */
    private fun getSoundResourceName(type: SoundType, scene: SceneType? = null): String {
        return when (type) {
            SoundType.CLICK -> when (scene) {
                SceneType.FIRE_STATION -> "click"
                SceneType.SCHOOL -> "click"
                SceneType.FOREST -> "click"
                null -> "click"
            }
            SoundType.SUCCESS -> "success"
            SoundType.HINT -> "hint"
            SoundType.DRAG -> "helicopter"
            SoundType.SNAP -> "water"
            SoundType.BADGE -> "collect"
            SoundType.ALL_COMPLETED -> "truck_horn"
            SoundType.ALERT -> "alert"
            SoundType.VOICE -> "voice" // 语音需要额外路径
        }
    }

    /**
     * 获取音效文件路径
     */
    private fun getSoundFilePath(soundName: String): String {
        // 音效文件在 audio/sound_effects/ 目录下，格式为 .wav
        return "audio/sound_effects/$soundName.wav"
    }

    // ==================== 公共方法实现 ====================

    /**
     * 预加载音效（静音加载，不播放）
     * 用于页面初始化时预加载音效，避免首次播放无声音
     */
    fun preloadSounds() {
        scope.launch {
            try {
                // 预加载点击音效（三个场景）
                loadSound(getSoundResourceName(SoundType.CLICK, SceneType.FIRE_STATION))
                loadSound(getSoundResourceName(SoundType.CLICK, SceneType.SCHOOL))
                loadSound(getSoundResourceName(SoundType.CLICK, SceneType.FOREST))
                // 预加载其他常用音效
                loadSound(getSoundResourceName(SoundType.SUCCESS))
                loadSound(getSoundResourceName(SoundType.HINT))
                loadSound(getSoundResourceName(SoundType.BADGE))
                loadSound(getSoundResourceName(SoundType.ALL_COMPLETED))
                loadSound(getSoundResourceName(SoundType.ALERT))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun playClickSound(scene: SceneType?) {
        playSoundEffect(SoundType.CLICK, scene)
    }

    override fun playVoice(voicePath: String) {
        scope.launch {
            try {
                // 检查是否已有该语音的 MediaPlayer
                mediaPlayers[voicePath]?.let {
                    it.reset()
                }

                // 创建新的 MediaPlayer
                val mediaPlayer = createMediaPlayer(voicePath)
                mediaPlayers[voicePath] = mediaPlayer
                mediaPlayer.start()

                // 播放完成后释放资源
                mediaPlayer.setOnCompletionListener {
                    it.release()
                    mediaPlayers.remove(voicePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun playSuccessSound() {
        playSoundEffect(SoundType.SUCCESS)
    }

    override fun playHintSound() {
        playSoundEffect(SoundType.HINT)
    }

    override fun playDragSound() {
        playSoundEffect(SoundType.DRAG)
    }

    override fun playSnapSound() {
        playSoundEffect(SoundType.SNAP)
    }

    override fun playBadgeSound() {
        playSoundEffect(SoundType.BADGE)
    }

    override fun playAllCompletedSound() {
        playSoundEffect(SoundType.ALL_COMPLETED)
        // 可以同时播放语音
        playVoice("audio/voices/collection_egg.mp3")
    }

    override fun playAlertSound() {
        if (alertMediaPlayer?.isPlaying == true) {
            return // 已在播放
        }

        scope.launch {
            try {
                val mediaPlayer = createMediaPlayer("audio/sound_effects/alert.wav")
                alertMediaPlayer = mediaPlayer

                // 循环播放警报
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stopAlertSound() {
        alertMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        alertMediaPlayer = null
    }

    override fun release() {
        // 停止警报
        stopAlertSound()

        // 释放所有 MediaPlayer
        mediaPlayers.values.forEach { it.release() }
        mediaPlayers.clear()

        // 释放 SoundPool
        soundPool.release()
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 播放短音效
     *
     * @param type 音效类型
     * @param scene 场景类型（可选）
     */
    private fun playSoundEffect(type: SoundType, scene: SceneType? = null) {
        scope.launch {
            try {
                val soundName = getSoundResourceName(type, scene)
                val soundId = loadSound(soundName)

                // 播放音效（左侧声道，音量 1.0）
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
            } catch (e: Exception) {
                e.printStackTrace()
                // 静默失败，避免影响用户体验
            }
        }
    }

    /**
     * 加载音效到 SoundPool
     *
     * @param soundName 音效名称
     * @return SoundPool ID
     */
    private fun loadSound(soundName: String): Int {
        return soundIdCache.getOrPut(soundName) {
            val afd = context.assets.openFd(getSoundFilePath(soundName))
            soundPool.load(afd.fileDescriptor, afd.startOffset, afd.length, 1)
        }
    }

    /**
     * 创建 MediaPlayer
     *
     * @param assetPath 资源路径
     * @return MediaPlayer 实例
     */
    private fun createMediaPlayer(assetPath: String): MediaPlayer {
        val afd = context.assets.openFd(assetPath)
        return MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            prepare()
            setOnCompletionListener {
                it.release()
                mediaPlayers.remove(assetPath)
            }
        }
    }
}

/**
 * Android Audio 扩展函数
 */
fun Context.getAudioManager(): AndroidAudioManager {
    return AndroidAudioManager.getInstance(this)
}
