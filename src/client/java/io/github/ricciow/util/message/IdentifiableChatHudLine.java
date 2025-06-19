package io.github.ricciow.util.message;

import org.jetbrains.annotations.Nullable;

public interface IdentifiableChatHudLine {
    @Nullable
    default String pridge$getIdentifier() {
        return "None";
    };

    default void pridge$setIdentifier(String identifier) {};
}
