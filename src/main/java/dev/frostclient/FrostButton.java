package dev.frostclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Icy button that keeps vanilla input, focus and narration behavior. */
public final class FrostButton extends Button {
    private float hoverProgress = 0f;
    private long previousFrameNanos = System.nanoTime();

    private FrostButton(Component label, int x, int y, int width, int height, OnPress pressed) {
        super(x, y, width, height, label, pressed, DEFAULT_NARRATION);
    }

    public static FrostButton create(String label, int x, int y, int width, int height, OnPress pressed) {
        return new FrostButton(Component.literal(label), x, y, width, height, pressed);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        float dt = Math.min(0.075f, (now - previousFrameNanos) / 1_000_000_000f);
        previousFrameNanos = now;
        float target = active && isHoveredOrFocused() ? 1f : 0f;
        hoverProgress += (target - hoverProgress) * (1f - (float)Math.exp(-dt * 15f));
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        int border = (int)(35 + hoverProgress * 150);
        int shade = (int)(205 + hoverProgress * 38);
        int top = (shade << 24) | 0x10263B;
        int bottom = (shade << 24) | 0x142A40;
        // The soft glow grows from the sides while the label lifts one pixel.
        if (hoverProgress > .03f) {
            int glowAlpha = (int)(95 * hoverProgress);
            graphics.fill(x-1,y-1,x+w+1,y+h+1,(glowAlpha<<24)|0x4DC9EE);
        }
        graphics.fillGradient(x+1,y+1,x+w-1,y+h-1,top,bottom);
        graphics.fillGradient(x+1,y+1,x+w-1,y+2,(border<<24)|0x8BEBFF,(border/2<<24)|0x4AA6DE);
        int line = Math.round((w-4)*hoverProgress);
        if(line>0)graphics.fill(x+2,y+h-2,x+2+line,y+h-1,0xFF70DCF2);
        int yText = y+(h-8)/2-Math.round(hoverProgress);
        int textColor = !active ? 0xFF7490A2 : (hoverProgress > .2f ? 0xFFFFFFFF : 0xFFD5E9F3);
        graphics.drawCenteredString(Minecraft.getInstance().font,getMessage(),x+w/2,yText,textColor);
    }
}
