package net.toshimichi.kzeplus.options;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.toshimichi.kzeplus.KzePlusMod;

public class KzeOptionsScreen extends GameOptionsScreen {

    private OptionListWidget list;

    public KzeOptionsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, Text.translatable("kze_plus.options.title"));
    }

    @Override
    protected void init() {
        KzeOptions options = KzePlusMod.getInstance().getOptions();
        SimpleOption<?>[] arr = {
                KzeOptions.VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sneak", options.getHideOnSneak(), options::setHideOnSneak),
                KzeOptions.VisibilityMode.createSimpleOption("kze_plus.options.visibility_on_sprint", options.getHideOnSprint(), options::setHideOnSprint),
                SimpleOption.ofBoolean("kze_plus.options.full_bright", options.isFullBright(), options::setFullBright),
        };

        this.list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        list.addAll(arr);

        addSelectableChild(list);
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (buttonWidget) -> close())
                .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) return true;
        if (this.list.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.render(matrices, this.list, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        KzePlusMod.getInstance().saveOptions();
        super.close();
    }
}
