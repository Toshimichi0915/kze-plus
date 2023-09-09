package net.toshimichi.kzeplus.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.toshimichi.kzeplus.KzePlus;
import net.toshimichi.kzeplus.context.game.GameContext;
import net.toshimichi.kzeplus.context.weapon.WeaponContext;
import net.toshimichi.kzeplus.context.weapon.WeaponInfo;
import net.toshimichi.kzeplus.events.ChatEvent;
import net.toshimichi.kzeplus.events.ClientTickEvent;
import net.toshimichi.kzeplus.events.EventTarget;
import net.toshimichi.kzeplus.events.SoundPlayEvent;
import net.toshimichi.kzeplus.utils.GameRole;
import net.toshimichi.kzeplus.utils.KzeUtils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameContextModule implements Module {

    private static final Identifier GAME_START_SOUND = Identifier.of("minecraft", "count.siren");
    private static final Identifier HUMAN_WIN_SOUND = Identifier.of("minecraft", "count.hwin");
    private static final Identifier ZOMBIE_WIN_SOUND = Identifier.of("minecraft", "count.zwin");
    private static final Pattern REWARD_PATTERN = Pattern.compile("》 +?合計 +?\\+?(\\d+?)円");
    private static final Pattern EXP_PATTERN = Pattern.compile("》 +?取得パーク経験値 +?\\+?(\\d+?) \\(ナイフ武器\\)");
    private static final Pattern DEFENSE_BONUS_PATTERN = Pattern.compile("》 +?防衛ボーナス +?\\+?(\\d+?)円");
    private static final Pattern HIT_BONUS_PATTERN = Pattern.compile("》 +?被弾ボーナス +?\\+?(\\d+?)円");
    private double prevHealth;

    @Override
    public void onEnable() {
        KzePlus.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlus.getInstance().getEventRegistry().unregister(this);
    }

    private void updateRewardPerHit(WeaponInfo info, int rewardPerHit) {
        if (info.getName() == null) throw new IllegalArgumentException("This weapon is a placeholder.");

        KzePlus.getInstance().getWeaponRegistry().save(new WeaponInfo(
                info.getName(),
                info.getMagazineSize(),
                info.getReloadTicks(),
                info.getBulletPerReload(),
                rewardPerHit)
        );
    }

    private void updateRewardPerHit(GameContext current, GameContext last) {
        if (current.getDefenseBonus() >= 10000) return;

        WeaponInfo main = current.getMainWeapon();
        WeaponInfo sub = current.getSubWeapon();

        if (current.getMainHitCount() > 0 && main.getRewardPerHit() == 0 && (sub.getRewardPerHit() != 0 || current.getSubHitCount() == 0)) {
            int rewardPerHit = (current.getDefenseBonus() - sub.getRewardPerHit() * current.getSubHitCount()) / current.getMainHitCount();
            updateRewardPerHit(main, rewardPerHit);
        } else if (current.getSubHitCount() > 0 && sub.getRewardPerHit() == 0 && (main.getRewardPerHit() != 0 || current.getMainHitCount() == 0)) {
            int rewardPerHit = (current.getDefenseBonus() - main.getRewardPerHit() * current.getMainHitCount()) / current.getSubHitCount();
            updateRewardPerHit(sub, rewardPerHit);
        }

        if (last == null) return;
        if (last.getDefenseBonus() >= 10000) return;
        if (!main.getName().equals(last.getMainWeaponName())) return;
        if (!sub.getName().equals(last.getSubWeaponName())) return;

        // https://en.wikipedia.org/wiki/Cramer%27s_rule
        int det = current.getMainHitCount() * last.getSubHitCount() - current.getSubHitCount() * last.getMainHitCount();
        if (det == 0) return;

        int mainRewardPerHit = (current.getDefenseBonus() * last.getSubHitCount() - last.getDefenseBonus() * current.getSubHitCount()) / det;
        int subRewardPerHit = (last.getDefenseBonus() * current.getMainHitCount() - current.getDefenseBonus() * last.getMainHitCount()) / det;

        updateRewardPerHit(main, mainRewardPerHit);
        updateRewardPerHit(sub, subRewardPerHit);
    }

    @EventTarget
    private void createGameContext(SoundPlayEvent e) {
        if (!e.getSound().getId().equals(GAME_START_SOUND)) return;
        KzePlus.getInstance().getGameContextRegistry().startGameContext();
    }

    @EventTarget
    private void updateWeapon(ClientTickEvent e) {
        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        WeaponContext main = KzePlus.getInstance().getMainWeaponContext();
        WeaponContext sub = KzePlus.getInstance().getSubWeaponContext();

        if (main.getName() != null) context.setMainWeaponName(main.getName());
        if (sub.getName() != null) context.setSubWeaponName(sub.getName());
    }

    @EventTarget
    private void updateGameEnd(SoundPlayEvent e) {
        Identifier id = e.getSound().getId();
        if (!id.equals(HUMAN_WIN_SOUND) && !id.equals(ZOMBIE_WIN_SOUND)) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        context.setHumanWin(id.equals(HUMAN_WIN_SOUND));
    }

    @EventTarget
    private void resetGameContext(ClientTickEvent e) {
        if (KzeUtils.isInGame()) return;
        KzePlus.getInstance().getGameContextRegistry().endGameContext(false);
    }

    @EventTarget
    private void updateDefenseBonus(SoundPlayEvent e) {
        if (e.getSound() != SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP) return;
        if (e.getPitch() != 0) return;
        if (e.getVolume() != 0.5) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int slot = player.getInventory().selectedSlot;

        if (slot == 0) context.setMainHitCount(context.getMainHitCount() + 1);
        else if (slot == 1) context.setSubHitCount(context.getSubHitCount() + 1);
    }

    @EventTarget
    private void updateHitBonus(ClientTickEvent e) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        double health = player.getHealth();
        double prevHealth = this.prevHealth;

        this.prevHealth = health;
        if (prevHealth <= health) return;

        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        if (GameRole.fromEntity(player) != GameRole.ZOMBIE) return;

        context.setShotCount(context.getShotCount() + 1);
        context.setDamage(context.getDamage() + prevHealth - health);
    }

    @EventTarget
    private void updateReward(ChatEvent e) {
        GameContext context = KzePlus.getInstance().getGameContextRegistry().getCurrentGameContext();
        if (context == null) return;

        String text = e.getText().getString();
        boolean end = false;

        Matcher matcher = REWARD_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setReward(context.getReward() + Integer.parseInt(matcher.group(1)));
        }

        matcher = DEFENSE_BONUS_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setDefenseBonus(context.getDefenseBonus() + Integer.parseInt(matcher.group(1)));
        }

        matcher = HIT_BONUS_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setHitBonus(context.getHitBonus() + Integer.parseInt(matcher.group(1)));
        }

        matcher = EXP_PATTERN.matcher(text);
        if (matcher.find()) {
            context.setExp(context.getExp() + Integer.parseInt(matcher.group(1)));
            end = true;
        }

        if (end) {
            context.setEndedAt(System.currentTimeMillis());
            context.setEnded(true);

            GameContext last = KzePlus.getInstance().getGameContextRegistry().getGameContextHistories()
                    .stream()
                    .filter(GameContext::isEnded)
                    .filter(it -> it.getMainHitCount() > 0 || it.getSubHitCount() > 0)
                    .filter(it -> it.getMainWeaponName().equals(context.getMainWeaponName()) && it.getSubWeaponName().equals(context.getSubWeaponName()))
                    .max(Comparator.comparingLong(GameContext::getStartedAt))
                    .orElse(null);

            if (last != null && last.isEnded()) {
                updateRewardPerHit(context, last);
            } else {
                updateRewardPerHit(context, null);
            }

            KzePlus.getInstance().getGameContextRegistry().endGameContext(true);
        }
    }
}
