package io.github.ricciow;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the registration of client-side commands.
 * Commands are added to a list and then registered all at once.
 */
public class CommandManager {

    private final List<LiteralArgumentBuilder<FabricClientCommandSource>> commands = new ArrayList<>();

    public void addCommand(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        commands.add(command);
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (LiteralArgumentBuilder<FabricClientCommandSource> command : commands) {
                dispatcher.register(command);
            }
        });
    }

    public static class StringListSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        private final List<String> stringList;

        public StringListSuggestionProvider(List<String> stringList) {
            this.stringList = stringList;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
            for (String type : stringList) {
                builder.suggest(type);
            }
            return builder.buildFuture();
        }
    }
}