package net.toshimichi.kzeplus.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;

import java.text.DecimalFormat;

public class WeaponInfoModule implements Module {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static final int DEG_STEP = 3;
    private static final int INNER_CIRCLE_SIZE = 3;
    private static final int OUTER_CIRCLE_SIZE = 5;
    private static final int RELOAD_COLOR = 0xfc5454;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    private void drawProgressCircle(double progress, double centerX, double centerY) {
        double deg = 360 * progress;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < deg; i += DEG_STEP) {
            double angle = Math.toRadians(i);
            double angleNext = Math.toRadians(i + DEG_STEP);

            double x1 = Math.cos(angle) * OUTER_CIRCLE_SIZE;
            double y1 = Math.sin(angle) * OUTER_CIRCLE_SIZE;

            double x2 = Math.cos(angle) * INNER_CIRCLE_SIZE;
            double y2 = Math.sin(angle) * INNER_CIRCLE_SIZE;

            double x3 = Math.cos(angleNext) * INNER_CIRCLE_SIZE;
            double y3 = Math.sin(angleNext) * INNER_CIRCLE_SIZE;

            double x4 = Math.cos(angleNext) * OUTER_CIRCLE_SIZE;
            double y4 = Math.sin(angleNext) * OUTER_CIRCLE_SIZE;

            buffer.vertex(centerX + x1, centerY + y1, 0).color(0xffffffff).next();
            buffer.vertex(centerX + x2, centerY + y2, 0).color(0xffffffff).next();
            buffer.vertex(centerX + x3, centerY + y3, 0).color(0xffffffff).next();
            buffer.vertex(centerX + x4, centerY + y4, 0).color(0xffffffff).next();
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }

    private String getWeaponStatus(WeaponContext context) {
        return context.getName() + ": " +
                context.getCurrentAmmo() +
                "/" +
                context.getWeaponInfo().getMagazineSize() +
                " (" +
                context.getRemainingAmmo() +
                ")";
    }

    private void drawWeaponStatus(MatrixStack matrices) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        WeaponContext mainWeapon = KzePlus.getInstance().getMainWeaponContext();
        if (!mainWeapon.isValid()) return;
        boolean mainReloading = mainWeapon.isReloading();
        String mainStatus = getWeaponStatus(mainWeapon);

        WeaponContext subWeapon = KzePlus.getInstance().getSubWeaponContext();
        if (!subWeapon.isValid()) return;

        boolean subReloading = subWeapon.isReloading();
        String subStatus = getWeaponStatus(subWeapon);

        int weaponContextLength = Math.max(
                textRenderer.getWidth(mainStatus),
                textRenderer.getWidth(subStatus)
        );

        InGameHud.fill(matrices, 20, 130, Math.max(weaponContextLength + 30, 145), 160, 0x80000000);
        textRenderer.drawWithShadow(matrices, mainStatus, 25, 135, mainReloading ? RELOAD_COLOR : 0xffffff);
        textRenderer.drawWithShadow(matrices, subStatus, 25, 145, subReloading ? RELOAD_COLOR : 0xffffff);
    }

    private void drawReloadProgress(MatrixStack matrices, WeaponContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;

        double remainingTicks = context.getRemainingReloadTicks();
        if (remainingTicks == 0) return;

        double totalReloadTicks = context.getTotalReloadTicks();
        if (totalReloadTicks == 0) return;

        String text = "残り" + FORMAT.format(remainingTicks * 0.05) + "秒";
        int width = textRenderer.getWidth(text) + 10;

        InGameHud.fill(matrices, centerX - width / 2 - 5, centerY + 15, centerX + width / 2 + 15, centerY + 35, 0x80000000);
        drawProgressCircle((totalReloadTicks - remainingTicks) / totalReloadTicks, centerX - width / 2 + 5, centerY + 25);
        textRenderer.drawWithShadow(matrices, text, centerX - width / 2 + 15, centerY + 20, 0xffffff);
    }

    @EventTarget
    private void renderWeaponInfo(InGameHudRenderEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;


        int slot = player.getInventory().selectedSlot;
        WeaponContext weaponContext = null;
        if (slot == 0) weaponContext = KzePlus.getInstance().getMainWeaponContext();
        else if (slot == 1) weaponContext = KzePlus.getInstance().getSubWeaponContext();

        if (weaponContext != null && weaponContext.isValid()) {
            drawReloadProgress(e.getMatrices(), weaponContext);
        }

        drawWeaponStatus(e.getMatrices());
    }
}
