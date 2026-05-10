package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.widget.Widget;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class WeaponInfoModule implements Module {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static final double CIRCLE_OUTER_RADIUS = 5.5;
    private static final double CIRCLE_INNER_RADIUS = 3.5;
    private static final int RELOAD_COLOR = 0xfffc5454;
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

            if (!KzePlus.getInstance().getOptions().isShowWeaponInfo()) {
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
        public void render(int x, int y, DrawContext context, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            context.fill(x, y, x + getWidth(), y + getHeight(), 0x80000000);
            context.drawTextWithShadow(textRenderer, mainStatus, x + 5, y + 5, mainReloading ? RELOAD_COLOR : 0xffffffff);
            context.drawTextWithShadow(textRenderer, subStatus, x + 5, y + 15, subReloading ? RELOAD_COLOR : 0xffffffff);
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

            if (!KzePlus.getInstance().getOptions().isShowReloadProgress()) {
                valid = false;
                return;
            }

            valid = false;

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) return;

            int slot = player.getInventory().getSelectedSlot();
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

        private void drawProgressCircle(DrawContext context, double progress, int centerX, int centerY) {
            if (progress <= 0) return;
            double endAngle = 2 * Math.PI * progress;
            int bound = (int) Math.ceil(CIRCLE_OUTER_RADIUS) + 1;

            for (int dy = -bound; dy <= bound; dy++) {
                for (int dx = -bound; dx <= bound; dx++) {
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    double radialCoverage;
                    if (dist <= CIRCLE_INNER_RADIUS - 0.5 || dist >= CIRCLE_OUTER_RADIUS + 0.5) continue;
                    else if (dist < CIRCLE_INNER_RADIUS) radialCoverage = dist - (CIRCLE_INNER_RADIUS - 0.5);
                    else if (dist > CIRCLE_OUTER_RADIUS) radialCoverage = (CIRCLE_OUTER_RADIUS + 0.5) - dist;
                    else radialCoverage = 1.0;

                    double angle = Math.atan2(dy, dx);
                    if (angle < 0) angle += 2 * Math.PI;
                    if (angle > endAngle) continue;

                    double angleCoverage = 1.0;
                    double pixelAngleWidth = 1.0 / Math.max(dist, 1.0);
                    double angleDelta = endAngle - angle;
                    if (angleDelta < pixelAngleWidth) {
                        angleCoverage = angleDelta / pixelAngleWidth;
                    }

                    double coverage = Math.max(0, Math.min(1, radialCoverage * angleCoverage));
                    int alpha = (int) Math.round(coverage * 255);
                    if (alpha <= 0) continue;
                    int color = (alpha << 24) | 0x00ffffff;

                    context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }

        @Override
        public void render(int x, int y, DrawContext context, float tickDelta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            context.fill(x, y, x + getWidth(), y + getHeight(), 0x80000000);
            drawProgressCircle(context, (totalReloadTicks - remainingTicks) / totalReloadTicks, x + 10, y + 10);
            context.drawTextWithShadow(textRenderer, text, x + 20, y + 5, 0xffffffff);
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
