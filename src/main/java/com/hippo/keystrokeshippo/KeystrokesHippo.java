package com.hippo.keystrokeshippo;

import com.hippo.keystrokeshippo.commands.KeystrokesCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(KeystrokesHippo.MODID)
public class KeystrokesHippo {
    public static final String MODID = "keystrokeshippo";
    public static final Logger LOGGER = LogManager.getLogger();

    public KeystrokesHippo() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLClientSetupEvent event) {
        KeystrokesRenderer keystrokesRenderer = new KeystrokesRenderer();
        MinecraftForge.EVENT_BUS.register(keystrokesRenderer);
        MinecraftForge.EVENT_BUS.register(new MouseClickEvent(keystrokesRenderer));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        KeystrokesCommand.register(event.getDispatcher());
    }
}
