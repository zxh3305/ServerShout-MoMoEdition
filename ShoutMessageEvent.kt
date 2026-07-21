package io.github.theramu.servershout.common.api.event

import java.util.UUID

/**
 * 喊话消息事件。
 *
 * 在 ServerShout 即将发送喊话消息时触发，允许监听器修改消息内容。
 * 例如：由第三方过滤插件对内容进行违禁词替换。
 *
 * @author TheRamU
 */
class ShoutMessageEvent(
    /** 发送者 UUID */
    val senderUuid: UUID,
    /** 发送者名称 */
    val senderName: String,
    /** 消息内容（可修改） */
    var content: String,
    /** 频道名称 */
    val channelName: String
)
