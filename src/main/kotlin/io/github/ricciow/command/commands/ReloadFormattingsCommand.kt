package io.github.ricciow.command.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.github.ricciow.command.Command
import io.github.ricciow.command.arguments.StringListArgumentType
import io.github.ricciow.format.FormatManager
import io.github.ricciow.util.TextParser
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ReloadFormattingsCommand : Command("reloadformattings") {
    private var lastReloadType: String? = null

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            doReload(context)
            return@executes SINGLE_SUCCESS
        }.then(arg("type", StringListArgumentType("assets", "github", "config", "default")).executes { context ->
            val reloadType = StringArgumentType.getString(context, "type")
            doReload(context, reloadType)
            return@executes SINGLE_SUCCESS
        })
    }

    private fun doReload(context: CommandContext<FabricClientCommandSource>, reloadType: String? = lastReloadType ?: "default") {
        when (reloadType) {
            "assets" -> FormatManager.loadFromDefaultAssetAndSave()
            "github" -> FormatManager.loadFromGithubAndSave()
            "config" -> FormatManager.loadFromConfigAndSave()
            "default" -> FormatManager.loadDefault()
        }
        context.source.sendFeedback(TextParser.parse("&a&lReloaded Formattings with '$reloadType'"))
        lastReloadType = reloadType
        FormatManager.config.postLoad()
    }
}