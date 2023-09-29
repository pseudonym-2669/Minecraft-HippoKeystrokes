package com.hippo.keystrokeshippo;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MouseClickEvent {
    private final KeystrokesRenderer keystrokesRenderer;

    public MouseClickEvent(KeystrokesRenderer keystrokesRenderer) {
        this.keystrokesRenderer = keystrokesRenderer;
    }

    @SubscribeEvent
    public void onMouseClick(InputEvent.MouseInputEvent event) {
        if (event.getAction() == 1) {  // Mouse pressed
            if (event.getButton() == 0) {  // Left click
                keystrokesRenderer.incrementLeftClicks();
            } else if (event.getButton() == 1) {  // Right click
                keystrokesRenderer.incrementRightClicks();
            }
        }
    }
}
