package com.example.refurbishedcams.client;

import com.example.refurbishedcams.data.CameraEntry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

/**
 * Live view through a camera.
 * The client camera is swapped for an invisible client-only entity placed just in front of the lens,
 * looking exactly where the lens points.
 *
 * Controls: hold LEFT MOUSE BUTTON and drag, or use arrow keys / WASD. ESC exits.
 */
public class CameraViewScreen extends Screen {
    private static final float DRAG_SENSITIVITY = 0.3F;   // degrees per pixel
    private static final float KEY_SPEED = 90F;           // degrees per second

    private final CameraEntry entry;
    private final String label;
    private final Screen parent;

    private ArmorStand camEntity;
    private Entity previousCamera;
    private boolean previousHideGui;

    private float yaw;
    private float pitch;
    private long lastMillis;

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
        camEntity.noPhysics = true;

        // Put the eye just OUTSIDE the front face of the block, in line with the lens,
        // so we never see the inside of the camera model.
        Direction facing = Direction.fromYRot(entry.yaw());
        double x = entry.pos().getX() + 0.5 + facing.getStepX() * 0.52;
        double z = entry.pos().getZ() + 0.5 + facing.getStepZ() * 0.52;
        double y = entry.pos().getY() + 0.5 - camEntity.getEyeHeight();
        camEntity.setPos(x, y, z);
        camEntity.xo = x;
        camEntity.yo = y;
        camEntity.zo = z;

        yaw = entry.yaw();   // exactly where the lens points
        pitch = 0F;
        applyRotation();
        lastMillis = Util.getMillis();

        mc.setCameraEntity(camEntity);
        mc.options.hideGui = true;
    }

    /**
     * Living entities use their HEAD rotation as the view direction, so every rotation
     * field (current AND previous) is set to the same value: no interpolation = no jitter.
     */
    private void applyRotation() {
        if (camEntity == null) return;
        pitch = Math.max(-90F, Math.min(90F, pitch));
        camEntity.setYRot(yaw);
        camEntity.setXRot(pitch);
        camEntity.yRotO = yaw;
        camEntity.xRotO = pitch;
        camEntity.setYHeadRot(yaw);
        camEntity.yHeadRotO = yaw;
        camEntity.yBodyRot = yaw;
        camEntity.yBodyRotO = yaw;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // Smooth keyboard rotation (polled every frame, no key-repeat delay)
        long now = Util.getMillis();
        float dt = Math.min(0.1F, (now - lastMillis) / 1000F);
        lastMillis = now;
        long window = Minecraft.getInstance().getWindow().getWindow();
        float step = KEY_SPEED * dt;
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_A)) yaw -= step;
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_D)) yaw += step;
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_UP) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_W)) pitch -= step;
        if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_DOWN) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S)) pitch += step;
        applyRotation();

        // No background: we want to see the world through the camera.
        boolean blink = (now / 500) % 2 == 0;
        if (blink) g.fill(10, 10, 18, 18, 0xFFFF2222);
        g.drawString(font, "REC  " + label + "  [" + entry.pos().getX() + ", " + entry.pos().getY() + ", "
                + entry.pos().getZ() + "]", 24, 10, 0xFFFFFF);
        g.drawString(font, Component.translatable("gui.refurbishedcams.view_hint"), 10, height - 16, 0xCCCCCC);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0) {
            yaw += (float) dx * DRAG_SENSITIVITY;
            pitch += (float) dy * DRAG_SENSITIVITY;
            applyRotation();
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
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
