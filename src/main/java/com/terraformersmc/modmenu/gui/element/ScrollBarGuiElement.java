package com.terraformersmc.modmenu.gui.element;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ScrollBarGuiElement(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int startX,
        int y,
        int endX,
        int height,
        int width,
        int bottom,
        int q,
        int p,
        float scale,
        int firstColor,
        int lastColor,
        int bgColor,
        @Nullable ScreenRect scissorArea,
        @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState
{
    public ScrollBarGuiElement(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int startX, int y, int endX, int height, int width, int bottom, int q, int p, float scale, int firstColor, int lastColor, int bgColor, @Nullable ScreenRect scissorArea) {
        this(pipeline, textureSetup, pose, startX, y, endX, height, width, bottom, q, p, scale, firstColor, lastColor, bgColor, scissorArea, createBounds(startX, y, (startX + width), (y + height), scale, pose, scissorArea));
    }

    @Override
    public void setupVertices(VertexConsumer vertices, float depth) {
        final float a1 = (float) (this.firstColor() >> 24 & 255) / 255.0F;
        final float r1 = (float) (this.firstColor() >> 16 & 255) / 255.0F;
        final float g1 = (float) (this.firstColor() >> 8 & 255) / 255.0F;
        final float b1 = (float) (this.firstColor() & 255) / 255.0F;
        final float a2 = (float) (this.lastColor() >> 24 & 255) / 255.0F;
        final float r2 = (float) (this.lastColor() >> 16 & 255) / 255.0F;
        final float g2 = (float) (this.lastColor() >> 8 & 255) / 255.0F;
        final float b2 = (float) (this.lastColor() & 255) / 255.0F;
        final float bgA = (float) (this.bgColor() >> 24 & 255) / 255.0F;
        final float bgR = (float) (this.bgColor() >> 16 & 255) / 255.0F;
        final float bgG = (float) (this.bgColor() >> 8 & 255) / 255.0F;
        final float bgB = (float) (this.bgColor() & 255) / 255.0F;
        
        vertices.vertex(this.pose(), this.startX() * this.scale(), this.bottom() * this.scale(), depth).color(bgR, bgG, bgB, bgA);
        vertices.vertex(this.pose(), this.endX() * this.scale(), this.bottom() * this.scale(), depth).color(bgR, bgG, bgB, bgA);
        vertices.vertex(this.pose(), this.endX() * this.scale(), this.y() * this.scale(), depth).color(bgR, bgG, bgB, bgA);
        vertices.vertex(this.pose(), this.startX() * this.scale(), this.y() * this.scale(), depth).color(bgR, bgG, bgB, bgA);
        vertices.vertex(this.pose(), this.startX() * this.scale(), (this.q() + this.p()) * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.endX() * this.scale(), (this.q() + this.p()) * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.endX() * this.scale(), this.q() * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.startX() * this.scale(), this.q() * this.scale(), depth).color(r1, g1, b1, a1);
        vertices.vertex(this.pose(), this.startX() * this.scale(), (this.q() + this.p() - 1) * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), (this.endX() - 1) * this.scale(), (this.q() + this.p() - 1) * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), (this.endX() - 1) * this.scale(), this.q() * this.scale(), depth).color(r2, g2, b2, a2);
        vertices.vertex(this.pose(), this.startX() * this.scale(), this.q() * this.scale(), depth).color(r2, g2, b2, a2);
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRect scissorArea) {
        ScreenRect screenRect = new ScreenRect(x0, y0, (int) (x1 * scale) - x0, (int) (y1 * scale) - y0).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }
}
