package io.github.theramu.servershout.velocity

import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import io.github.theramu.servershout.common.ServerShoutProxyApi
import io.github.theramu.servershout.velocity.command.VelocityChannelCommandAdapter
import io.github.theramu.servershout.velocity.command.VelocityCommandAdapter
import io.github.theramu.servershout.velocity.listener.PlayerEventListener
import io.github.theramu.servershout.velocity.listener.PluginChannelMessageListener
import io.github.theramu.servershout.velocity.platform.VelocityPlatform
import io.github.theramu.servershout.velocity.platform.logging.VelocityPlatformLogger
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Path

/**
 * @author TheRamU
 * @since 2024/08/19 00:44
 */
open class ServerShoutVelocityApi protected constructor(
    private val plugin: ServerShoutVelocityPlugin,
    val proxy: ProxyServer,
    dataDirectory: Path
) : ServerShoutProxyApi() {
    override val platform = VelocityPlatform(proxy)
    override val logger = VelocityPlatformLogger()
    override val dataFolder = dataDirectory.toFile()
    val identifier = MinecraftChannelIdentifier.from("servershout:main")
    private var metrics: Metrics? = null
    private val registeredDynamicCommands = mutableListOf<String>()

    override fun onEnable() {
        super.onEnable()
        proxy.channelRegistrar.register(identifier)
        proxy.eventManager.register(plugin, PlayerEventListener())
        proxy.eventManager.register(plugin, PluginChannelMessageListener())
        registerCommands()
        registerDynamicChannelCommands()
        metrics = makeMetrics()
        logger.info("&aServerShout is ready!")
    }

    override fun onDisable() {
        super.onDisable()
        unregisterDynamicChannelCommands()
        proxy.eventManager.unregisterListeners(plugin)
        metrics?.shutdown()
    }

    private fun registerCommands() {
        val proxyCommandManager = proxy.commandManager
        val commandMeta = proxyCommandManager.metaBuilder("servershoutvelocity")
            .aliases("ssv")
            .plugin(plugin)
            .build()
        proxyCommandManager.register(commandMeta, VelocityCommandAdapter())
    }

    private fun registerDynamicChannelCommands() {
        val proxyCommandManager = proxy.commandManager
        for (channel in shoutChannelService.getChannels()) {
            for (command in channel.commands) {
                if (command.isEmpty()) continue
                val cmdName = command.removePrefix("/")
                val commandMeta = proxyCommandManager.metaBuilder(cmdName)
                    .plugin(plugin)
                    .build()
                proxyCommandManager.register(commandMeta, VelocityChannelCommandAdapter(cmdName))
                registeredDynamicCommands.add(cmdName)
            }
        }
    }

    private fun unregisterDynamicChannelCommands() {
        val proxyCommandManager = proxy.commandManager
        for (cmdName in registeredDynamicCommands) {
            proxyCommandManager.unregister(cmdName)
        }
        registeredDynamicCommands.clear()
    }

    private fun makeMetrics(): Metrics? {
        return try {
            val clazz = Metrics::class.java
            val constructor = clazz.getDeclaredConstructor(Any::class.java, ProxyServer::class.java, Logger::class.java, Path::class.java, Int::class.java)
            constructor.isAccessible = true
            constructor.newInstance(plugin, proxy, logger, dataFolder.toPath(), 23116)
        } catch (e: Exception) {
            logger.warn("bStats Metrics初始化失败，已跳过: ${e.message}")
            null
        }
    }

    override fun sendUpdate(playerName: String) {
        val optional = proxy.getPlayer(playerName)
        if (!optional.isPresent) return
        optional?.get()?.let { player ->
            ByteArrayOutputStream().use { bytes ->
                DataOutputStream(bytes).use { out ->
                    out.writeUTF("UPDATE_BALANCE")
                    out.writeUTF(player.uniqueId.toString())
                }
                player.currentServer.get().sendPluginMessage(identifier, bytes.toByteArray())
            }
        }
    }
}
