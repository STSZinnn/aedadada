package dev.frostclient;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Places each waypoint above its real projected XYZ location, not in a static compass row. */
public final class WorldWaypointRenderer {
    private WorldWaypointRenderer() {}

    public static void render(GuiGraphics graphics, Minecraft mc, FrostConfig config) {
        Camera camera = mc.gameRenderer.getMainCamera();
        if(!camera.isInitialized() || mc.player == null || mc.level == null) return;
        Vec3 cameraPos = camera.position();
        Vector3f facing = camera.rotation().transform(new Vector3f(0f, 0f, -1f));
        Vec3 forward = new Vec3(facing.x, facing.y, facing.z);
        String dimension = mc.level.dimension().identifier().toString();
        List<FrostConfig.Waypoint> markers = new ArrayList<>();
        for (FrostConfig.Waypoint wp : config.waypoints) {
            if (wp != null && dimension.equals(wp.dimension)) markers.add(wp);
        }
        markers.sort(Comparator.comparingDouble(w -> cameraPos.distanceToSqr(new Vec3(w.x + .5, w.y + 1.5, w.z + .5))));

        int drawn = 0;
        for(FrostConfig.Waypoint wp : markers) {
            if(drawn >= 16) break;
            Vec3 point = new Vec3(wp.x + .5, wp.y + 1.5, wp.z + .5);
            Vec3 delta = point.subtract(cameraPos);
            double distance = delta.length();
            // A point behind the camera must never be mirrored onto the front of the HUD.
            if(distance < .1 || delta.dot(forward) < .15) continue;
            Vec3 projected = mc.gameRenderer.projectPointToScreen(point);
            if(!Double.isFinite(projected.x) || !Double.isFinite(projected.y)) continue;

            int screenWidth = graphics.guiWidth(), screenHeight = graphics.guiHeight();
            double targetX = (projected.x + 1.0) * screenWidth * .5;
            double targetY = (1.0 - projected.y) * screenHeight * .5;
            boolean offscreen = targetX < 14 || targetX > screenWidth - 14 || targetY < 16 || targetY > screenHeight - 30;
            int x = (int)Math.round(Math.max(14,Math.min(screenWidth-14,targetX)));
            int y = (int)Math.round(Math.max(16,Math.min(screenHeight-30,targetY)));
            int rgb = parseColor(wp.color);
            int color = 0xFF000000 | rgb;
            String name = wp.name == null || wp.name.isBlank() ? "Waypoint" : wp.name;
            if(name.length()>20) name = name.substring(0,19)+"…";
            String dist = String.format(Locale.ROOT,"%d m",Math.round(distance));
            int labelWidth = Math.max(mc.font.width(name),mc.font.width(dist))+15;
            int labelX = Math.max(3,Math.min(screenWidth-labelWidth-3,x-labelWidth/2));
            int labelY = offscreen ? Math.max(2,Math.min(screenHeight-28,y-28)) : Math.max(2,y-39);
            int opacity = Math.max(110, 205 - (int)(distance/55));
            graphics.fill(labelX,labelY,labelX+labelWidth,labelY+26,(opacity<<24)|0x071725);
            graphics.fill(labelX,labelY,labelX+labelWidth,labelY+2,color);
            graphics.drawCenteredString(mc.font,name,labelX+labelWidth/2,labelY+5,0xFFF1FBFF);
            graphics.drawCenteredString(mc.font,dist,labelX+labelWidth/2,labelY+15,0xFFACDAE9);
            // Pixel-shaped pin, with an outline so it remains visible over bright snow.
            graphics.fill(x-2,y-6,x+3,y+7,0xE0061822);
            graphics.fill(x-6,y-2,x+7,y+3,0xE0061822);
            graphics.fill(x-1,y-5,x+2,y+6,color);
            graphics.fill(x-5,y-1,x+6,y+2,color);
            if(offscreen) {
                int arrowY=Math.min(screenHeight-10,y+10);
                String arrow = targetX < 14 ? "◀" : targetX > screenWidth-14 ? "▶" : targetY < 16 ? "▲" : "▼";
                graphics.drawCenteredString(mc.font,arrow,x,arrowY,color);
            }
            drawn++;
        }
    }

    private static int parseColor(String hex) {
        try {
            if(hex != null && hex.matches("#[0-9a-fA-F]{6}")) return Integer.parseInt(hex.substring(1),16);
        } catch(NumberFormatException ignored) {}
        return 0x69DDF5;
    }
}
