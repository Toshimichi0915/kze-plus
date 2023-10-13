package net.toshimichi.kzeplus.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.widget.Widget;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class WeaponInfoModule implements Module {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static final int DEG_STEP = 3;
    private static final int INNER_CIRCLE_SIZE = 3;
    private static final int OUTER_CIRCLE_SIZE = 5;
    private static final int RELOAD_COLOR = 0xfc5454;
    private boolean enabled;

    @Override
    public void onEnable() {
        enabled = true;
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        enabled = false;
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    @Override
    public Map<String, Widget> getWidgets() {
        return Map.of(
                "weapon_info", new WeaponInfoWidget(),
                "reload_info", new ReloadInfoWidget()
        );
    }

    private class WeaponInfoWidget implements Widget {

        private boolean valid;
        private boolean mainReloading;
        private String mainStatus;

        private boolean subReloading;
        private String subStatus;

        private int weaponContextLength;

        private String getWeaponStatus(WeaponContext context) {
            return context.getName() + ": " +
                    context.getCurrentAmmo() +
                    "/" +
                    context.getWeaponInfo().getMagazineSize() +
                    " (" +
                    context.getRemainingAmmo() +
                    ")";
        }

        @Override
        public void update(boolean placeholder) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            if (placeholder) {
                mainReloading = false;
                mainStatus = "BarrettM82: 6/6 (36)";

                subReloading = true;
                subStatus = "SAA: 0/6 (48)";

                weaponContextLength = Math.max(
                        textRenderer.getWidth(mainStatus),
                        textRenderer.getWidth(subStatus)
                );
                valid = true;
                return;
            }

            if (!enabled) {
                valid = false;
                return;
            }

            valid = true;

            WeaponContext mainWeapon = KzePlus.getInstance().getMainWeaponContext();
            if (mainWeapon.isValid()) {
                mainReloading = mainWeapon.isReloading();
                mainStatus = getWeaponStatus(mainWeapon);
            } else {
                valid = false;
                return;
            }

            WeaponContext subWeapon = KzePlus.getInstance().getSubWeaponContext();
            if (subWeapon.isValid()) {
                subReloading = subWeapon.isReloading();
                subStatus = getWeaponStatus(subWeapon);
            } else {
                valid = false;
                return;
            }

            weaponContextLength = Math.max(
                    textRenderer.getWidth(mainStatus),
                    textRenderer.getWidth(subStatus)
            );
        }

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);
            textRenderer.drawWithShadow(stack, mainStatus, x + 5, y + 5, mainReloading ? RELOAD_COLOR : 0xffffff);
            textRenderer.drawWithShadow(stack, subStatus, x + 5, y + 15, subReloading ? RELOAD_COLOR : 0xffffff);
        }

        @Override
        public int getWidth() {
            return Math.max(125, weaponContextLength + 10);
        }

        @Override
        public int getHeight() {
            return 30;
        }

        @Override
        public boolean isVisible() {
            return valid;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }

    private class ReloadInfoWidget implements Widget {

        private double remainingTicks;
        private double totalReloadTicks;
        private String text;
        private int textWidth;

        private boolean valid;

        @Override
        public void update(boolean placeholder) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            if (placeholder) {
                remainingTicks = 40;
                totalReloadTicks = 120;
                text = "残り" + FORMAT.format(remainingTicks * 0.05) + "秒";
                textWidth = textRenderer.getWidth(text) + 10;
                valid = true;
                return;
            }

            if (!enabled) {
                valid = false;
                return;
            }

            valid = false;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            int slot = player.getInventory().selectedSlot;
            WeaponContext weaponContext;
            if (slot == 0) {
                weaponContext = KzePlus.getInstance().getMainWeaponContext();
            } else if (slot == 1) {
                weaponContext = KzePlus.getInstance().getSubWeaponContext();
            } else {
                return;
            }

            if (!weaponContext.isValid()) return;

            remainingTicks = weaponContext.getRemainingReloadTicks();
            if (remainingTicks == 0) return;

            totalReloadTicks = weaponContext.getTotalReloadTicks();
            if (totalReloadTicks == 0) return;

            text = "残り" + FORMAT.format(remainingTicks * 0.05) + "秒";
            textWidth = textRenderer.getWidth(text) + 10;

            valid = true;
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

        @Override
        public void render(int x, int y, MatrixStack stack, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            InGameHud.fill(stack, x, y, x + getWidth(), y + getHeight(), 0x80000000);
            drawProgressCircle((totalReloadTicks - remainingTicks) / totalReloadTicks, x + 10, y + 10);
            textRenderer.drawWithShadow(stack, text, x + 20, y + 5, 0xffffff);
        }

        @Override
        public int getWidth() {
            return textWidth + 20;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public boolean isVisible() {
            return valid;
        }

        @Override
        public List<GameOptions> getOptions() {
            return List.of();
        }
    }
}
