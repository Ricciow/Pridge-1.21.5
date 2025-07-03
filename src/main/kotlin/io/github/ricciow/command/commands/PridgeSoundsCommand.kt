package io.github.ricciow.command.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.ricciow.command.Command
import io.github.ricciow.command.arguments.StringListArgumentType
import io.github.ricciow.sounds.DynamicSoundPlayer
import io.github.ricciow.sounds.DynamicSoundPlayer.getSoundNames
import io.github.ricciow.sounds.DynamicSoundPlayer.isSound
import io.github.ricciow.util.TextParser
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object PridgeSoundsCommand : Command("sounds") {
    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.executes { context ->
            val builder = StringBuilder("&6&lAvailable Sounds: &e")

            builder.append(getSoundNames().joinToString("&f, &e") { name ->
                name.replace("_", " ")
            })

            context.source.sendFeedback(TextParser.parse(builder.toString()))
            return@executes SINGLE_SUCCESS
        }.then(arg("sound name", StringListArgumentType(getSoundNames())).executes { context ->
            val argument = StringArgumentType.getString(context, "sound name")
            if (isSound(argument)) {
                DynamicSoundPlayer.play(argument.replace(" ", "_"))
                context.source.sendFeedback(TextParser.parse("&6&lPlaying sound: &e$argument"))
            } else {
                context.source.sendFeedback(TextParser.parse("&c&lSound not found"))
            }
            return@executes SINGLE_SUCCESS
        })
    }
}