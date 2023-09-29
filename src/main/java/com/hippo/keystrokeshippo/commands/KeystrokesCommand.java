package com.hippo.keystrokeshippo.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import com.hippo.keystrokeshippo.KeystrokesRenderer;

public class KeystrokesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hippoKeystrokes_toggle")
                .requires((commandSource) -> commandSource.hasPermission(0))
                .executes((context) -> {
                    boolean isNowDisabled = KeystrokesRenderer.toggleDisabled();

                    String message = isNowDisabled ? "Keystrokes disabled" : "Keystrokes enabled";
                    context.getSource().sendSuccess(new TextComponent(message), false);

                    return 1;
                }));
    }

}
