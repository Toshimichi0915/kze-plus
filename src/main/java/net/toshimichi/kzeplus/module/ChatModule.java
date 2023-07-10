package net.toshimichi.kzeplus.module;

import net.toshimichi.kzeplus.KzePlusMod;
import net.toshimichi.kzeplus.event.ChatEvent;
import net.toshimichi.kzeplus.event.EventTarget;

import java.util.regex.Pattern;

public class ChatModule implements Module {

    private static final Pattern MAGAZINE_MESSAGE_PATTERN = Pattern.compile("》[^ ]+? 弾が切れた！");
    private static final Pattern KILL_MESSAGE_PATTERN = Pattern.compile("[^ ]+? killed by [^ ]+? \\(.*?\\)");

    @Override
    public void onEnable() {
        KzePlusMod.getInstance().getEventRegistry().register(this);
    }

    @Override
    public void onDisable() {
        KzePlusMod.getInstance().getEventRegistry().unregister(this);
    }

    @EventTarget
    public void hideMagazineMessage(ChatEvent e) {
        String text = e.getText().getString();
        if (!KzePlusMod.getInstance().getOptions().isHideMagazineMessage()) return;
        if (MAGAZINE_MESSAGE_PATTERN.matcher(text).find()) {
            e.setCancelled(true);
        }
    }

    @EventTarget
    public void hideKillMessage(ChatEvent e) {
        String text = e.getText().getString();
        if (!KzePlusMod.getInstance().getOptions().isHideKillMessage()) return;
        if (KILL_MESSAGE_PATTERN.matcher(text).find()) {
            e.setCancelled(true);
        }
    }
}
