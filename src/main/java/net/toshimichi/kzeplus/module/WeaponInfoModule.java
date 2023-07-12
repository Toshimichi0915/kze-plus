package net.toshimichi.kzeplus.module;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Data;
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
import net.minecraft.item.ItemStack;
import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ClientTickEvent;
import net.toshimichi.kzeplus.event.EventTarget;
import net.toshimichi.kzeplus.event.InGameHudRenderEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class WeaponInfoModule implements Module {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static final int DEG_STEP = 3;
    private static final int INNER_CIRCLE_SIZE = 3;
    private static final int OUTER_CIRCLE_SIZE = 5;

    private static final int RELOAD_COLOR = 0xfc5454;

    private final Map<String, WeaponInfo> weapons = new HashMap<>();
    private final WeaponContext mainWeaponContext = new WeaponContext(0);
    private final WeaponContext subWeaponContext = new WeaponContext(1);

    @Override
    public void onEnable() {
        KzePlusMod.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlusMod.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    public void updateContext(ClientTickEvent e) {
        mainWeaponContext.update();
        subWeaponContext.update();
    }

    private WeaponContext getSelectedWeaponContext() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;

        int slot = player.getInventory().selectedSlot;
        if (mainWeaponContext.getSlot() == slot) return mainWeaponContext;
        if (subWeaponContext.getSlot() == slot) return subWeaponContext;
        return null;
    }

    public void updateWeaponInfo(WeaponInfo now) {
        WeaponInfo old = weapons.get(now.getName());
        if (now.equals(old)) return;

        weapons.put(now.getName(), now);
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
        WeaponStatus status = context.getWeaponStatus();
        if (status == null) return "";
        return status.getName() + ": " +
                status.getCurrentAmmo() +
                "/" +
                context.getWeaponInfo().getMagazineSize() +
                " (" +
                status.getRemainingAmmo() +
                ")";
    }

    private void drawWeaponStatus(MatrixStack matrices) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String main = getWeaponStatus(mainWeaponContext);
        String sub = getWeaponStatus(subWeaponContext);
        boolean mainReloading = mainWeaponContext.getWeaponStatus() != null && mainWeaponContext.getWeaponStatus().isReloading();
        boolean subReloading = subWeaponContext.getWeaponStatus() != null && subWeaponContext.getWeaponStatus().isReloading();

        int weaponContextLength = Math.max(
                textRenderer.getWidth(main),
                textRenderer.getWidth(sub)
        );

        if (weaponContextLength > 0) {
            InGameHud.fill(matrices, 20, 100, weaponContextLength + 30, 130, 0x80000000);
        }

        textRenderer.drawWithShadow(matrices, main, 25, 105, mainReloading ? RELOAD_COLOR : 0xffffff);
        textRenderer.drawWithShadow(matrices, sub, 25, 115, subReloading ? RELOAD_COLOR : 0xffffff);
    }

    private void drawReloadProgress(MatrixStack matrices, WeaponContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;

        double remainingTicks = context.getReloadRemainingTicks();
        if (remainingTicks == 0) return;

        String text = "残り" + FORMAT.format(remainingTicks * 0.05) + "秒";
        int width = textRenderer.getWidth(text) + 10;

        double reloadTicks = context.getReloadTicks();
        InGameHud.fill(matrices, centerX - width / 2 - 5, centerY + 15, centerX + width / 2 + 15, centerY + 35, 0x80000000);
        drawProgressCircle((reloadTicks - remainingTicks) / reloadTicks, centerX - width / 2 + 5, centerY + 25);
        textRenderer.drawWithShadow(matrices, text, centerX - width / 2 + 15, centerY + 20, 0xffffff);
    }

    @EventTarget
    public void renderWeaponInfo(InGameHudRenderEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        WeaponContext context = getSelectedWeaponContext();
        if (context != null) {
            drawReloadProgress(e.getMatrices(), context);
        }

        drawWeaponStatus(e.getMatrices());
    }

    @Data
    private class WeaponContext {

        private final int slot;

        private WeaponStatus weaponStatus;
        private WeaponInfo weaponInfo;
        private int reloadTicks;

        public void update() {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            ItemStack itemStack = player.getInventory().getStack(slot);

            // get weapon status
            WeaponStatus now = WeaponStatus.fromItemStack(itemStack);
            if (now == null) {
                weaponStatus = null;
                weaponInfo = null;
                reloadTicks = 0;
                return;
            }

            WeaponStatus old = this.weaponStatus;
            if (old == null) old = now;
            this.weaponStatus = now;

            // set weapon info
            if (weaponInfo == null) {
                weaponInfo = weapons.get(now.getName());
            }

            if (weaponInfo == null) {
                weaponInfo = new WeaponInfo(now.getName(), now.getCurrentAmmo(), 0, 0);
                updateWeaponInfo(weaponInfo);
            }

            // update reload related info
            int ammoDiff = now.getCurrentAmmo() - old.getCurrentAmmo();
            if (ammoDiff > 0) {
                weaponInfo = new WeaponInfo(weaponInfo.getName(), weaponInfo.getMagazineSize(), reloadTicks, ammoDiff);
                reloadTicks = 0;
                updateWeaponInfo(weaponInfo);
            }

            if (now.isReloading() && player.getInventory().selectedSlot == slot) {
                reloadTicks++;
            }
        }

        private boolean isDataLoaded() {
            if (weaponInfo == null) return false;
            if (weaponStatus == null) return false;
            if (weaponInfo.getBulletPerReload() == 0) return false;

            return true;
        }

        public double getReloadRemainingTicks() {
            if (!isDataLoaded()) return 0;
            if (!weaponStatus.isReloading()) return 0;
            int remainingAmmo = weaponInfo.getMagazineSize() - weaponStatus.getCurrentAmmo();
            double timesToReload = Math.max((double) remainingAmmo / weaponInfo.getBulletPerReload(), 1);
            return Math.max(timesToReload * weaponInfo.getReloadTicks() - reloadTicks, 0);
        }

        public double getReloadTicks() {
            if (!isDataLoaded()) return 0;
            double timesToReload = Math.max((double) weaponInfo.getMagazineSize() / weaponInfo.getBulletPerReload(), 1);
            return Math.max(timesToReload * weaponInfo.getReloadTicks(), 0);
        }
    }
}
