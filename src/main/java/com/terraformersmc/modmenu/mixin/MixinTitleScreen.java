package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.SmallModMenuButtonWidget;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.terraformersmc.modmenu.gui.widget.SmallModMenuButtonWidget.MODS_SPRITE_ENABLED;
import static com.terraformersmc.modmenu.gui.widget.SmallModMenuButtonWidget.MODS_SPRITE_DISABLED;
import static com.terraformersmc.modmenu.gui.widget.SmallModMenuButtonWidget.MODS_SPRITE_FOCUSED;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen {
    @Shadow
    protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(II)V"), method = "init", index = 1)
    private int adjustRealmsHeight(int height) {
        if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
            return height - 51;
        } else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
            return -99999;
        } else {
            return height;
        }
    }

    @Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
    @Expression("numberOfButtons = ?")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void adjustAmountOfIconButtons(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons, @Share("addModMenuIconWidget") LocalBooleanRef addModMenuIconWidget) {
        if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
            addModMenuIconWidget.set(true);
            numberOfButtons.set(numberOfButtons.get() + 1);
        }
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;getHorizontalPosition(III)I"))
    private int replaceInlinedConstant(TitleScreen instance, int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original, @Local(name = "numberOfButtons") int actualNumberOfButtons) {
        return original.call(instance, currentButton, actualNumberOfButtons, buttonWidth);
    }

    @Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/TitleScreen;width:I")
    @Expression("this.width / 2 - 100")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private void addModMenuIconWidget(CallbackInfo ci, @Local(name = "currentButton") LocalIntRef currentButton, @Local(name = "topPos") int topPos, @Local(name = "numberOfButtons") int numberOfButtons, @Share("addModMenuIconWidget") LocalBooleanRef addModMenuIconWidget) {
        if (!addModMenuIconWidget.get()) return;
        currentButton.set(currentButton.get()+1);
        Screen screen = (TitleScreen) (Object) this;
        Screens.getWidgets(screen).add(new SmallModMenuButtonWidget(
                this.getHorizontalPosition(currentButton.get(), numberOfButtons, 20),
                topPos,
                20,
                20,
                ModMenuApi.createModsButtonText(),
                20,
                20,
                0,
                0,
                new WidgetSprites(
                        MODS_SPRITE_ENABLED,
                        MODS_SPRITE_DISABLED,
                        MODS_SPRITE_FOCUSED
                ),
                _ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(screen)),
                ModMenuApi.createModsButtonText(),
                null,
                false
        ));
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/I18n;get(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
    String modifyModdedVersionString(String id, Object[] args, Operation<String> original) {
        if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnTitleScreen()) {
            String count = ModMenu.getDisplayedModCount();
            String specificKey = "modmenu.mods." + count;
            String replacementKey = Language.getInstance().has(specificKey) ? specificKey : "modmenu.mods.n";
            if (ModMenuConfig.EASTER_EGGS.getValue() && Language.getInstance().has(specificKey + ".secret")) {
                replacementKey = specificKey + ".secret";
            }

            return I18n.get(replacementKey, count);
        }

        return original.call(id, args);
    }
}
