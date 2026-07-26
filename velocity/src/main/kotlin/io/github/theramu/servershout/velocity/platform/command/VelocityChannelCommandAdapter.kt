package io.github.theramu.servershout.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import io.github.theramu.servershout.common.ServerShoutApi
import io.github.theramu.servershout.velocity.ServerShoutVelocityApi
import io.github.theramu.servershout.velocity.platform.player.VelocityPlatformPlayer

/**
 * 频道命令适配器，将命令转发到 ShoutChannelService
 * @author TheRamU
 */
class VelocityChannelCommandAdapter(private val commandName: String) : SimpleCommand {

    private val api get() = ServerShoutApi.api as ServerShoutVelocityApi
    private val shoutChannelService get() = api.shoutChannelService

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        if (source !is Player) return

        val args = invocation.arguments()
        val message = if (args.isEmpty()) {
            commandName
        } else {
            "$commandName ${args.joinToString(" ")}"
        }

        shoutChannelService.shout(VelocityPlatformPlayer(source), message)
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return true
    }
}
