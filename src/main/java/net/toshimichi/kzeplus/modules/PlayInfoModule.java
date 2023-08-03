package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.InGameHudRenderEvent;
import net.toshimichi.kzeplus.events.SoundPlayEvent;
import net.toshimichi.kzeplus.utils.GameRole;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayInfoModule implements Module {

    private static final Pattern REWARD_PATTERN = Pattern.compile("》 +合計 \\+?(\\d+?)円");
    private static final Pattern EXP_PATTERN = Pattern.compile("》 +取得パーク経験値 \\+?(\\d+?) \\(ナイフ武器\\)");
    private double prevHealth;

    private int reward;
    private int exp;
    private int playTime;

    private int mainDefenseCount;
    private int subDefenseCount;
    private int defenseBonus;

    private int hitCount;
    private double hitBonus;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
        reward = 0;
        exp = 0;
        playTime = 0;
        resetBonus();
    }

    private void resetBonus() {
        mainDefenseCount = 0;
        subDefenseCount = 0;
        defenseBonus = 0;
        hitCount = 0;
        hitBonus = 0;
    }

    @EventTarget
    private void updateReward(ChatEvent e) {
        String text = e.getText().getString();
        Matcher matcher = REWARD_PATTERN.matcher(text);
        if (matcher.find()) {
            reward += Integer.parseInt(matcher.group(1));
        }

        matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            exp += Integer.parseInt(matcher.group(1));
        }
    }

    @EventTarget
    private void updatePlayTime(ClientTickEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (!KzeUtils.isInGame()) return;

        playTime++;
    }

    @EventTarget
    private void updateDefenseBonus(SoundPlayEvent e) {
        if (!KzeUtils.isInGame()) return;
        if (e.getSound() != SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP) return;
        if (e.getPitch() != 0) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int slot = player.getInventory().selectedSlot;
        WeaponStatus weaponStatus = WeaponStatus.fromItemStack(player.getInventory().getStack(slot));
        if (weaponStatus == null) return;

        if (slot == 0) mainDefenseCount++;
        else if (slot == 1) subDefenseCount++;

        WeaponReward weaponReward = WeaponReward.fromName(weaponStatus.getName());
        if (weaponReward == null) return;
        defenseBonus += weaponReward.getReward();
    }

    @EventTarget
    private void updateHitBonus(ClientTickEvent e) {
        if (!KzeUtils.isInGame()) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        if (GameRole.fromEntity(player) != GameRole.ZOMBIE) return;

        double health = player.getHealth();

        double prevHealth = this.prevHealth;
        this.prevHealth = health;
        if (prevHealth <= health) return;
        if (player.getMaxHealth() - health < 0.5) return;

        hitCount++;
        hitBonus += (prevHealth - health) + 4;
    }

    @EventTarget
    private void resetBonus(ClientTickEvent e) {
        if (!KzeUtils.isInGame()) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        GameRole role = GameRole.fromEntity(player);
        if (role != null) return;
        resetBonus();
    }

    @EventTarget
    private void renderInfo(InGameHudRenderEvent e) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        InGameHud.fill(e.getMatrices(), 20, 20, 145, 120, 0x80000000);

        // reward
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得金額: " + reward + "円", 25, 25, 0xffffff);

        // exp
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "取得経験値: " + exp, 25, 35, 0xffffff);

        // play time
        Duration duration = Duration.ofSeconds(playTime / 20);
        String time = "%02d:%02d:%02d".formatted(duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "プレイ時間: " + time, 25, 45, 0xffffff);

        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "生存者: " + KzeUtils.getSurvivorCount() + "人", 25, 65, 0xffffff);
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "ゾンビ: " + KzeUtils.getZombieCount() + "人", 25, 75, 0xffffff);

        // bonus
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "防衛: " + Math.min(defenseBonus, 10000) + "円 (" + mainDefenseCount + ", " + subDefenseCount + ")", 25, 95, 0xffffff);
        InGameHud.drawTextWithShadow(e.getMatrices(), textRenderer, "被弾: " + (int) Math.min(hitBonus, 4000) + "円 (" + hitCount + ")", 25, 105, 0xffffff);
    }
}
