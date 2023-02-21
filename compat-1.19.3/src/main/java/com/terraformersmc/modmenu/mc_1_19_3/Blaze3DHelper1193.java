package com.terraformersmc.modmenu.mc_1_19_3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.util.compat.Blaze3DHelper;

public class Blaze3DHelper1193 extends Blaze3DHelper {
	@Override
	public void enableTexture() {
		RenderSystem.enableTexture();
	}

	@Override
	public void disableTexture() {
		RenderSystem.disableTexture();
	}
}
