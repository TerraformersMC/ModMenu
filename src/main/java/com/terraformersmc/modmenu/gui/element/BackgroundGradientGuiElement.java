package com.terraformersmc.modmenu.gui.element;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record BackgroundGradientGuiElement(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x,
        int y,
        int height,
        int width,
        int right,
        int bottom,
        float scale,
        int color1,
        int color2,
        @Nullable ScreenRect scissorArea,
        @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {
    public BackgroundGradientGuiElement(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x, int y, int height, int width, int right, int bottom, float scale, int color1, int color2, @Nullable ScreenRect scissorArea) {
        this(pipeline, textureSetup, pose, x, y, height, width, right, bottom, scale, color1, color2, scissorArea, createBounds(x, y, (x + width), (y + height), scale, pose, scissorArea));
    }

    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
        final float a1 = (float) (this.color1() >> 24 & 255) / 255.0F;
        final float r1 = (float) (this.color1() >> 16 & 255) / 255.0F;
        final float g1 = (float) (this.color1() >> 8 & 255) / 255.0F;
        final float b1 = (float) (this.color1() & 255) / 255.0F;
        final float a2 = (float) (this.color2() >> 24 & 255) / 255.0F;
        final float r2 = (float) (this.color2() >> 16 & 255) / 255.0F;
        final float g2 = (float) (this.color2() >> 8 & 255) / 255.0F;
        final float b2 = (float) (this.color2() & 255) / 255.0F;

        vertices.vertex(this.pose(), this.x() * this.scale(), (this.y() + 4) * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.right() * this.scale(), (this.y() + 4) * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.right() * this.scale(), this.y() * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), this.x() * this.scale(), this.y() * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), this.x() * this.scale(), this.bottom() * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), this.right() * this.scale(), this.bottom() * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), this.right() * this.scale(), (this.bottom() - 4) * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.x() * this.scale(), (this.bottom() - 4) * this.scale(), depth).color(r1, g1, b1, a1);
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = new ScreenRect(x0, y0, (int) (x1 * scale) - x0, (int) (y1 * scale) - y0).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
