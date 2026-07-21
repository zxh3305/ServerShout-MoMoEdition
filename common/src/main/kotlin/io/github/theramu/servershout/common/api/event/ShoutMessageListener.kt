package io.github.theramu.servershout.common.api.event

/**
 * 喊话消息监听器。
 *
 * 第三方插件可实现此接口，注册到 [io.github.theramu.servershout.common.service.ShoutChannelService]
 * 以在喊话消息发送前进行处理（例如违禁词过滤）。
 *
 * @author TheRamU
 */
fun interface ShoutMessageListener {
    /**
     * 处理喊话消息事件。
     *
     * @param event 消息事件，可修改其 [ShoutMessageEvent.content]
     */
    fun onShoutMessage(event: ShoutMessageEvent)
}
