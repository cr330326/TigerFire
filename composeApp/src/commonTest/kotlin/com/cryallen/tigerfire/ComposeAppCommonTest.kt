package com.cryallen.tigerfire

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TigerFire 项目通用测试示例
 *
 * 此文件作为测试的入口点，运行基本的验证测试。
 * 实际的业务逻辑测试分散在各个模块的测试文件中。
 */
class ComposeAppCommonTest {

    @Test
    fun testBasicArithmetic() {
        // 基础算术测试，验证测试环境正常工作
        assertEquals(3, 1 + 2, "1 + 2 应该等于 3")
        assertEquals(10, 5 * 2, "5 * 2 应该等于 10")
    }

    @Test
    fun testStringLength() {
        // 基础字符串测试
        val text = "TigerFire"
        assertEquals(9, text.length, "TigerFire 的长度应该是 9")
    }
}
