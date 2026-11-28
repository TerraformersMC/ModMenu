package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.ModMenu;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class TranslationUtil {
    public static Component translateNumeric(String key, int[]@Nullable... args) {
        Object[] realArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            int[] arg = args[i];
            assert arg != null;
            NumberFormat nf = NumberFormat.getInstance(Locale.ROOT);
            if (arg.length == 1) {
                realArgs[i] = nf.format(arg[0]);
            } else {
                assert arg.length == 2;
                realArgs[i] = nf.format(arg[0]) + "/" + nf.format(arg[1]);
            }
        }

        int[] override = new int[args.length];
        Arrays.fill(override, -1);
        for (int i = 0; i < args.length; i++) {
            int[] arg = args[i];
            if (arg == null) {
                throw new NullPointerException("args[" + i + "]");
            }
            if (arg.length == 1) {
                override[i] = arg[0];
            }
        }

        String lastKey = key;
        for (int flags = (1 << args.length) - 1; flags >= 0; flags--) {
            StringBuilder fullKey = new StringBuilder(key);
            for (int i = 0; i < args.length; i++) {
                fullKey.append('.');
                if (((flags & (1 << i)) != 0) && override[i] != -1) {
                    fullKey.append(override[i]);
                } else {
                    fullKey.append('n');
                }
            }
            lastKey = fullKey.toString();
            if (Language.getInstance().has(lastKey)) {
                return Component.translatable(lastKey, realArgs);
            }
        }
        return Component.translatable(lastKey, realArgs);
    }

    public static String translationKeyOf(String type, String id) {
        return type + "." + ModMenu.MOD_ID + "." + id;
    }
}
