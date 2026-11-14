package com.example.refurbishedcams.client;

import com.example.refurbishedcams.data.CameraEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

/**
 * Live view: swaps the client camera entity for an invisible, client-only entity
 * placed at the camera block. Arrow keys rotate the view, ESC exits.
 * NOTE: the world only renders where the client has chunks loaded.
 */
public class CameraViewScreen extends Screen {
    private final CameraEntry entry;
    private final String label;
    private final Screen parent;

    private ArmorStand camEntity;
    private Entity previousCamera;
    private boolean previousHideGui;

    public CameraViewScreen(CameraEntry entry, String label, Screen parent) {
        super(Component.literal(label));
        this.entry = entry;
        this.label = label;
        this.parent = parent;
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        if (camEntity != null || mc.level == null) return;
        previousCamera = mc.getCameraEntity();
        previousHideGui = mc.options.hideGui;

        camEntity = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
        camEntity.setInvisible(true);
        camEntity.setNoGravity(true);
        double y = entry.pos().getY() + 0.5 - camEntity.getEyeHeight();
        camEntity.moveTo(entry.pos().getX() + 0.5, y, entry.pos().getZ() + 0.5, entry.yaw(), 15F);

        mc.setCameraEntity(camEntity);
        mc.options.hideGui = true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // No background: we want to see the world through the camera.
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (blink) g.fill(10, 10, 18, 18, 0xFFFF2222);
        g.drawString(font, "REC  " + label + "  [" + entry.pos().getX() + ", " + entry.pos().getY() + ", "
                + entry.pos().getZ() + "]", 24, 10, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.refurbishedcams.view_hint"), 10, height - 16, 0xCCCCCC);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (camEntity != null) {
            float yaw = camEntity.getYRot(), pitch = camEntity.getXRot();
            switch (key) {
                case GLFW.GLFW_KEY_LEFT -> yaw -= 5;
                case GLFW.GLFW_KEY_RIGHT -> yaw += 5;
                case GLFW.GLFW_KEY_UP -> pitch -= 5;
                case GLFW.GLFW_KEY_DOWN -> pitch += 5;
                default -> { return super.keyPressed(key, scan, mods); }
            }
            pitch = Math.max(-90F, Math.min(90F, pitch));
            camEntity.setYRot(yaw);
            camEntity.setXRot(pitch);
            camEntity.yRotO = yaw;
            camEntity.xRotO = pitch;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void removed() {
        Minecraft mc = Minecraft.getInstance();
        mc.setCameraEntity(previousCamera != null ? previousCamera : mc.player);
        mc.options.hideGui = previousHideGui;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
