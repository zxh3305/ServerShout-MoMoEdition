package io.github.theramu.servershout.common.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * @author TheRamU
 * @since 2024/8/25 5:09
 */
object ColorUtil {

    private val legacySection = LegacyComponentSerializer.legacySection()
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * 基础颜色转换（仅转换 &x 格式，不转换 &#RRGGBB 和 MiniMessage）
     * 用于在消息处理阶段保持原始格式
     */
    fun translateColor(str: String): String {
        if (str.isEmpty()) return str
        val ary = str.toCharArray()
        val colors = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr"
        for (i in 0 until ary.size - 1) {
            if (ary[i] == '&' && colors.indexOf(ary[i + 1]) > -1) {
                ary[i] = 167.toChar()
                ary[i + 1] = ary[i + 1].lowercaseChar()
            }
        }
        return String(ary)
    }

    /**
     * 将颜色代码转换回 & 格式
     */
    fun translateColorBack(str: String): String {
        return str.replace(167.toChar(), '&')
    }

    /**
     * 移除颜色代码（包括所有格式）
     */
    fun stripColor(str: String): String {
        if (str.isEmpty()) return str
        var result = str
        // 移除 &#RRGGBB 格式
        result = result.replace(Regex("&#[0-9A-Fa-f]{6}"), "")
        // 移除 &x&R&R&G&G&B&B 格式
        result = result.replace(Regex("&x&[0-9A-Fa-f]&[0-9A-Fa-f]&[0-9A-Fa-f]&[0-9A-Fa-f]&[0-9A-Fa-f]&[0-9A-Fa-f]"), "")
        // 移除 <#RRGGBB> 格式
        result = result.replace(Regex("<#[0-9A-Fa-f]{6}>"), "")
        // 移除基础颜色代码
        val sb = StringBuilder(result.length)
        val colors = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr"
        var i = 0
        while (i < result.length) {
            if (i < result.length - 1
                && (result[i] == '&' || result[i] == 167.toChar())
                && colors.indexOf(result[i + 1]) > -1
            ) {
                i += 2
            } else {
                sb.append(result[i])
                i++
            }
        }
        return sb.toString()
    }

    /**
     * 在最后发送消息时调用，转换所有颜色格式
     * 支持：
     * - MiniMessage 格式 (<#RRGGBB>, <bold>, <color:red>, 等)
     * - &#RRGGBB 格式（转换为 <#RRGGBB>）
     * - &x&R&R&G&G&B&B 格式（转换为 <#RRGGBB>）
     * - 基础颜色代码 (&0-&f, &k-&r)
     */
    fun deserializeComponent(text: String): Component {
        return try {
            deserializeComponentMiniMessage(text)
        } catch (e: Throwable) {
            // MiniMessage 解析失败或不可用，回退到 LegacySerializer
            legacySection.deserialize(translateColor(text))
        }
    }

    private fun deserializeComponentMiniMessage(text: String): Component {
        // 0. 将 § 归一化为 &，统一处理（translateColor 可能已将 & 转为 §）
        var processed = text.replace(167.toChar(), '&')

        // 1. 转换 &#RRGGBB 为 <#RRGGBB>
        processed = processed.replace(Regex("&#([0-9A-Fa-f]{6})"), "<#$1>")

        // 2. 转换 &x&R&R&G&G&B&B 格式为 <#RRGGBB>
        val hexPattern = Regex("&x&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])")
        processed = hexPattern.replace(processed) { match ->
            val r1 = match.groupValues[1]
            val r2 = match.groupValues[2]
            val g1 = match.groupValues[3]
            val g2 = match.groupValues[4]
            val b1 = match.groupValues[5]
            val b2 = match.groupValues[6]
            "<#$r1$r2$g1$g2$b1$b2>"
        }

        // 3. 转换基础颜色代码为 MiniMessage 格式
        processed = translateLegacyToMiniMessage(processed)

        // 4. 使用 MiniMessage 序列化器解析
        return miniMessage.deserialize(processed)
    }

    /**
     * 将基础颜色代码转换为 MiniMessage 格式
     */
    private fun translateLegacyToMiniMessage(str: String): String {
        val colorMap = mapOf(
            '0' to "<black>",
            '1' to "<dark_blue>",
            '2' to "<dark_green>",
            '3' to "<dark_aqua>",
            '4' to "<dark_red>",
            '5' to "<dark_purple>",
            '6' to "<gold>",
            '7' to "<gray>",
            '8' to "<dark_gray>",
            '9' to "<blue>",
            'a' to "<green>",
            'b' to "<aqua>",
            'c' to "<red>",
            'd' to "<light_purple>",
            'e' to "<yellow>",
            'f' to "<white>",
            'k' to "<obfuscated>",
            'l' to "<bold>",
            'm' to "<strikethrough>",
            'n' to "<underlined>",
            'o' to "<italic>",
            'r' to "<reset>"
        )

        val sb = StringBuilder()
        var i = 0
        while (i < str.length) {
            if (i < str.length - 1 && str[i] == '&') {
                val nextChar = str[i + 1].lowercaseChar()
                if (colorMap.containsKey(nextChar)) {
                    sb.append(colorMap[nextChar])
                    i += 2
                    continue
                }
            }
            sb.append(str[i])
            i++
        }
        return sb.toString()
    }

    /**
     * 使用 Legacy 序列化器（保留原逻辑）
     */
    fun deserializeComponentLegacy(text: String): Component {
        return legacySection.deserialize(translateColor(text))
    }
}
