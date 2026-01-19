package com.cryallen.tigerfire.presentation.parent

/**
 * 家长模式副作用（Effect）
 *
 * 表示一次性执行的操作，不影响 State 持久状态
 * 通过 Channel 发送，确保只消费一次
 */
sealed class ParentEffect {
    /**
     * 播放点击音效
     */
    data object PlayClickSound : ParentEffect()

    /**
     * 导航返回主地图
     */
    data object NavigateToMap : ParentEffect()

    /**
     * 显示设置保存成功提示
     */
    data object ShowSettingsSavedHint : ParentEffect()

    /**
     * 显示重置成功提示
     */
    data object ShowResetSuccessHint : ParentEffect()

    /**
     * 显示验证失败提示（答案错误）
     */
    data object ShowVerificationFailedHint : ParentEffect()
}
