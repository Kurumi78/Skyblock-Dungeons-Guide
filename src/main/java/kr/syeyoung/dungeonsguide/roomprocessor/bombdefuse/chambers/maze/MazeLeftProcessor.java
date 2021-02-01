package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.maze;

import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MazeLeftProcessor extends GeneralDefuseChamberProcessor {
    public MazeLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
    }

    @Override
    public String getName() {
        return "mazeLeft";
    }


    @Override
    public void drawScreen(float partialTicks) {
        if (Minecraft.getMinecraft().objectMouseOver == null ) return;
        MovingObjectPosition pos = Minecraft.getMinecraft().objectMouseOver;
        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos block = pos.getBlockPos();
        Block b = getChamber().getRoom().getContext().getWorld().getBlockState(block).getBlock();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        String str = "Press "+ Keyboard.getKeyName(Keybinds.sendBombdefuse.getKeyCode()) + " to request open "+b.getLocalizedName();
        fr.drawString(str, (sr.getScaledWidth() - fr.getStringWidth(str)) / 2, (sr.getScaledHeight() - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onSendData() {
        if (Minecraft.getMinecraft().objectMouseOver == null ) return;
        MovingObjectPosition pos = Minecraft.getMinecraft().objectMouseOver;
        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos block = pos.getBlockPos();
        Block b = getChamber().getRoom().getContext().getWorld().getBlockState(block).getBlock();

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("a", "e");
        nbt.setInteger("b", Block.getIdFromBlock(b));
        getSolver().communicate(nbt);
    }
}
