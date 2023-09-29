package com.hippo.keystrokeshippo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class KeystrokesRenderer extends GuiComponent {

    private static boolean isDisabled = false;
    private final AtomicInteger frameCount = new AtomicInteger(0);
    private final Deque<Long> leftClickTimestamps = new LinkedList<>();
    private final Deque<Long> rightClickTimestamps = new LinkedList<>();

    private int leftCPS = 0, rightCPS = 0, fps = 0;

    private static final int KEY_SIZE = 18;
    private static final int KEY_SPACING = 5;
    private static final int START_X = 25;
    private static final int START_Y = 22;

    private static final int SPACEBAR_Y_OFFSET = 2 * KEY_SPACING;
    private static final int LMB_RMB_Y_OFFSET = (KEY_SPACING + KEY_SIZE)-3;

    private static final int INDICATOR_WIDTH = 45;
    private static final int INDICATOR_HEIGHT = 11;
    private static final float TEXT_SCALE = 0.75f;
    private static final float CPS_TEXT_SCALE = 0.65f;

    private double huePhase = 0;
    private static final double HUE_INCREMENT = Math.PI / 75;

    private boolean leftClickTransition = false;
    private boolean rightClickTransition = false;

    public KeystrokesRenderer() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::updateState, 0, 50, TimeUnit.MILLISECONDS);
    }

    private void updateState() {
        long currentTime = System.currentTimeMillis();

        while (!leftClickTimestamps.isEmpty() && currentTime - leftClickTimestamps.getFirst() > 1000) {
            leftClickTimestamps.pollFirst();
        }
        while (!rightClickTimestamps.isEmpty() && currentTime - rightClickTimestamps.getFirst() > 1000) {
            rightClickTimestamps.pollFirst();
        }

        leftCPS = leftClickTimestamps.size();
        rightCPS = rightClickTimestamps.size();

        fps = frameCount.getAndSet(0) * 20;

        huePhase += HUE_INCREMENT;
        if (huePhase > 2 * Math.PI) huePhase -= 2 * Math.PI;

        leftClickTransition = false;
        rightClickTransition = false;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (isDisabled) {
            return;
        }

        frameCount.incrementAndGet();

        PoseStack matrixStack = event.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        Color rgb = Color.getHSBColor((float) (0.5 * (1 + Math.sin(huePhase))), 1, 1);
        int rgbColor = rgb.getRGB();

        int screenHeight = mc.getWindow().getScreenHeight();
        int fpsBoxTop = Math.min(5, screenHeight - INDICATOR_HEIGHT - 5);

        renderFPS(matrixStack, font, "FPS: " + fps, START_X+4 - KEY_SIZE - KEY_SPACING, fpsBoxTop, rgbColor);
        renderKeys(matrixStack, font, mc, rgbColor);
        renderSpaceBar(matrixStack, mc, rgbColor);
        renderCPS(matrixStack, font, "LMB", leftCPS + " CPS", START_X - KEY_SIZE - KEY_SPACING, START_Y + 2 * KEY_SIZE + LMB_RMB_Y_OFFSET, rgbColor);
        renderCPS(matrixStack, font, "RMB", rightCPS + " CPS", START_X + KEY_SIZE + KEY_SPACING, START_Y + 2 * KEY_SIZE + LMB_RMB_Y_OFFSET, rgbColor);
    }

    private void renderKeys(PoseStack matrixStack, Font font, Minecraft mc, int rgbColor) {
        int textColor = 0xFFFFFF;

        renderKeyBar(matrixStack, font, "W", START_X, START_Y, KEY_SIZE, KEY_SIZE, mc.options.keyUp.isDown(), rgbColor, textColor);
        renderKeyBar(matrixStack, font, "A", START_X - KEY_SIZE - KEY_SPACING, START_Y + KEY_SIZE + KEY_SPACING, KEY_SIZE, KEY_SIZE, mc.options.keyLeft.isDown(), rgbColor, textColor);
        renderKeyBar(matrixStack, font, "S", START_X, START_Y + KEY_SIZE + KEY_SPACING, KEY_SIZE, KEY_SIZE, mc.options.keyDown.isDown(), rgbColor, textColor);
        renderKeyBar(matrixStack, font, "D", START_X + KEY_SIZE + KEY_SPACING, START_Y + KEY_SIZE + KEY_SPACING, KEY_SIZE, KEY_SIZE, mc.options.keyRight.isDown(), rgbColor, textColor);
    }

    private void renderSpaceBar(PoseStack matrixStack, Minecraft mc, int rgbColor) {
        int bgColor = mc.options.keyJump.isDown() ? rgbColor : 0x80000000;
        int borderColor = rgbColor;

        int spaceBarWidth = 3 * KEY_SIZE + 2 * KEY_SPACING;
        int spaceBarHeight = KEY_SIZE / 4;
        int spaceBarX = START_X+1 - KEY_SIZE - KEY_SPACING;
        int spaceBarY = START_Y + 2 * KEY_SIZE + SPACEBAR_Y_OFFSET;

        fill(matrixStack, spaceBarX, spaceBarY, spaceBarX + spaceBarWidth, spaceBarY + spaceBarHeight, bgColor);
        drawBorder(matrixStack, spaceBarX, spaceBarY, spaceBarX + spaceBarWidth, spaceBarY + spaceBarHeight, borderColor);
    }


    private void renderFPS(PoseStack matrixStack, Font font, String fpsText, int x, int y, int borderColor) {
        int adjustedX = x + 5;
        fill(matrixStack, adjustedX, y, adjustedX + INDICATOR_WIDTH, y + INDICATOR_HEIGHT, 0x80000000);
        drawBorder(matrixStack, adjustedX, y, adjustedX + INDICATOR_WIDTH, y + INDICATOR_HEIGHT, borderColor);

        matrixStack.pushPose();
        matrixStack.scale(TEXT_SCALE, TEXT_SCALE, 1f);

        int textColor = 0xFFFFFF;
        if (borderColor == Color.WHITE.getRGB()) {
            textColor = 0x000000;
        }

        int centeredX = (int) ((adjustedX + (INDICATOR_WIDTH / 2)) / TEXT_SCALE);
        int centeredY = (int) ((y + (INDICATOR_HEIGHT / 2) - (font.lineHeight * TEXT_SCALE / 2)) / TEXT_SCALE);

        drawString(matrixStack, font, fpsText, centeredX - font.width(fpsText) / 2, centeredY, textColor);

        matrixStack.popPose();
    }


    private void renderCPS(PoseStack matrixStack, Font font, String label, String cps, int x, int y, int borderColor) {
        int bgColor = 0x80000000;
        int textColor = 0xFFFFFF;

        if ((label.equals("LMB") && leftClickTransition) || (label.equals("RMB") && rightClickTransition)) {
            bgColor = Color.WHITE.getRGB();
            textColor = 0x000000;
        }

        fill(matrixStack, x, y, x + INDICATOR_WIDTH, y + INDICATOR_HEIGHT * 2, bgColor);
        drawBorder(matrixStack, x, y, x + INDICATOR_WIDTH, y + INDICATOR_HEIGHT * 2, borderColor);

        matrixStack.pushPose();
        matrixStack.scale(CPS_TEXT_SCALE, CPS_TEXT_SCALE, 1f);
        drawCenteredString(matrixStack, font, label, (int) ((x + INDICATOR_WIDTH / 2) / CPS_TEXT_SCALE), (int) ((y + 3) / CPS_TEXT_SCALE), textColor);
        drawCenteredString(matrixStack, font, cps, (int) ((x + INDICATOR_WIDTH / 2) / CPS_TEXT_SCALE), (int) ((y + INDICATOR_HEIGHT + 2) / CPS_TEXT_SCALE), textColor);
        matrixStack.popPose();
    }


    private void renderKeyBar(PoseStack matrixStack, Font font, String key, int x, int y, int width, int height, boolean isPressed, int borderColor, int textColor) {
        int bgColor = isPressed ? 0xFFFFFFFF : 0x80000000;
        fill(matrixStack, x, y, x + width, y + height, bgColor);

        if (bgColor == 0xFFFFFFFF) {
            textColor = 0x000000;
        }

        drawBorder(matrixStack, x, y, x + width, y + height, borderColor);
        drawCenteredString(matrixStack, font, key, x + width / 2, y + (height / 2) - 4, textColor);
    }

    private void renderKey(PoseStack matrixStack, Font font, String key, int x, int y, boolean isPressed, int borderColor, int textColor) {
        int bgColor = isPressed ? 0xFFFFFFFF : 0x80000000;
        fill(matrixStack, x, y, x + KEY_SIZE, y + KEY_SIZE, bgColor);

        if (bgColor == 0xFFFFFFFF) {
            textColor = 0x000000;
        }

        drawBorder(matrixStack, x, y, x + KEY_SIZE, y + KEY_SIZE, borderColor);
        drawCenteredString(matrixStack, font, key, x + KEY_SIZE / 2, y + (KEY_SIZE / 2) - 4, textColor);
    }

    private void drawBorder(PoseStack matrixStack, int x1, int y1, int x2, int y2, int color) {
        fill(matrixStack, x1 - 1, y1 - 1, x2 + 1, y1, color);
        fill(matrixStack, x1 - 1, y2, x2 + 1, y2 + 1, color);
        fill(matrixStack, x1 - 1, y1, x1, y2, color);
        fill(matrixStack, x2, y1, x2 + 1, y2, color);
    }



    public void incrementLeftClicks() {
        leftClickTimestamps.addLast(System.currentTimeMillis());
        leftClickTransition = true;
    }

    public void incrementRightClicks() {
        rightClickTimestamps.addLast(System.currentTimeMillis());
        rightClickTransition = true;
    }

    public static boolean toggleDisabled() {
        isDisabled = !isDisabled;
        return isDisabled;
    }


}
