package dev.frostclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WaypointScreen extends Screen {
    private final int initialX,initialY,initialZ;
    private EditBox name, coords, color;
    private int listScroll=0;
    private String info="";
    public WaypointScreen(){this(currentX(), currentY(), currentZ());}
    public WaypointScreen(int x,int y,int z){super(Component.literal("Frost Waypoints"));initialX=x;initialY=y;initialZ=z;}
    private static int currentX(){var mc=Minecraft.getInstance(); return mc.player==null?0:(int)Math.floor(mc.player.getX());}
    private static int currentY(){var mc=Minecraft.getInstance(); return mc.player==null?64:(int)Math.floor(mc.player.getY());}
    private static int currentZ(){var mc=Minecraft.getInstance(); return mc.player==null?0:(int)Math.floor(mc.player.getZ());}
    @Override protected void init(){
        int x=width/2-165,y=height/2-142;
        name=new EditBox(font,x+14,y+46,305,20,Component.literal("Waypoint name"));name.setMaxLength(44);name.setValue("New Waypoint");addRenderableWidget(name);
        coords=new EditBox(font,x+14,y+88,305,20,Component.literal("XYZ"));coords.setMaxLength(100);coords.setValue(initialX+" "+initialY+" "+initialZ);addRenderableWidget(coords);
        color=new EditBox(font,x+14,y+130,115,20,Component.literal("Color"));color.setMaxLength(7);color.setValue("#69DDF5");addRenderableWidget(color);
        addRenderableWidget(FrostButton.create("USE CURRENT POS",x+136,y+130,183,20,b->{coords.setValue(currentX()+" "+currentY()+" "+currentZ());}));
        addRenderableWidget(FrostButton.create("SAVE WAYPOINT",x+14,y+158,305,22,b->saveWaypoint()));
        addRenderableWidget(FrostButton.create("PREV",x+14,y+274,84,20,b->{listScroll=Math.max(0,listScroll-4);refresh();}));
        addRenderableWidget(FrostButton.create("NEXT",x+104,y+274,84,20,b->{listScroll+=4;refresh();}));
        addRenderableWidget(FrostButton.create("BACK",x+222,y+274,97,20,b->minecraft.setScreen(new FrostScreen())));
        int max=Math.min(4,Math.max(0,FrostClient.CONFIG.waypoints.size()-listScroll));
        for(int i=0;i<max;i++){
            int index=listScroll+i;
            addRenderableWidget(FrostButton.create("X",x+300,y+201+i*18,19,17,b->{FrostClient.CONFIG.waypoints.remove(index);FrostClient.CONFIG.save();refresh();}));
        }
    }
    private void refresh(){clearWidgets();init();}
    private void saveWaypoint(){
        try{
            String[] pieces=coords.getValue().trim().split("[\\s,]+");
            if(pieces.length<3){info="Provide three coordinates (X Y Z)";return;}
            int x=(int)Math.floor(Double.parseDouble(pieces[0])),y=(int)Math.floor(Double.parseDouble(pieces[1])),z=(int)Math.floor(Double.parseDouble(pieces[2]));
            String hex=color.getValue().trim();if(!hex.matches("#[0-9a-fA-F]{6}")){info="Color must be #RRGGBB";return;}
            FrostClient.addWaypoint(name.getValue().isBlank()?"Waypoint":name.getValue(),x,y,z,hex);
            info="Saved waypoint";listScroll=Math.max(0,FrostClient.CONFIG.waypoints.size()-4);
            refresh();
        }catch(Exception ignored){info="Invalid coordinates";}
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderInGameBackground(g);
        g.fill(0,0,width,height,0xB0091322);
        int x=width/2-165,y=height/2-142;
        g.fillGradient(x-12,y-14,x+332,y+304,0xD4162B43,0xC0091425);
        g.fillGradient(x-12,y-14,x+332,y-10,0xFF7CE7FF,0xFF428CDE);
        g.drawString(font,"WAYPOINT MANAGER",x+14,y,0xFF79DEFA,true);
        g.drawString(font,"Auto-fills with your current coordinates",x+14,y+14,0xFFB9DDE8,false);
        g.drawString(font,"Name",x+14,y+34,0xFFEAFBFF,false);
        g.drawString(font,"X Y Z (paste allowed)",x+14,y+76,0xFFEAFBFF,false);
        g.drawString(font,"Color",x+14,y+118,0xFFEAFBFF,false);
        g.drawString(font,"SAVED WAYPOINTS",x+14,y+186,0xFF8DE5F7,true);
        for(int i=0;i<4 && listScroll+i<FrostClient.CONFIG.waypoints.size();i++){
            var wp=FrostClient.CONFIG.waypoints.get(listScroll+i);
            int rowY=y+202+i*18;
            g.fillGradient(x+12,rowY-2,x+298,rowY+13,0x65304967,0x40233549);
            String line=wp.name+"  ("+wp.x+", "+wp.y+", "+wp.z+")";
            if(font.width(line)>252)line=line.substring(0,Math.min(30,line.length()))+"...";
            g.drawString(font,line,x+16,rowY,0xFFD0E5F1,false);
        }
        g.drawString(font,info,x+14,y+256,0xFF79DEFA,false);
        super.render(g,mx,my,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
