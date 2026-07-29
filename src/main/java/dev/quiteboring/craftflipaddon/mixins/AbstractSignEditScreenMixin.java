package dev.quiteboring.craftflipaddon.mixins;

import dev.quiteboring.craftflipaddon.util.interfaces.IAbstractSignEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenMixin implements IAbstractSignEditScreen {

  @Shadow
  @Final
  private String[] messages;

  @Shadow
  @Final
  protected SignBlockEntity sign;

  @Override
  public void craftflipaddon$setFirstMessage(@NonNull String message) {
    Font font = Minecraft.getInstance().font;
    int maxWidth = sign.getMaxTextLineWidth();

    messages[0] = font.plainSubstrByWidth(message, maxWidth);
  }
}
