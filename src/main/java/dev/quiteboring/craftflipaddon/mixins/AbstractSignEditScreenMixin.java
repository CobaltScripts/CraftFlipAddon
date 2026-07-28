package dev.quiteboring.craftflipaddon.mixins;

import dev.quiteboring.craftflipaddon.util.interfaces.IAbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenMixin implements IAbstractSignEditScreen {

  @Shadow
  @Final
  private String[] messages;

  @Override
  public void craftflipaddon$setFirstMessage(@NonNull String message) {
    messages[0] = message;
  }
}
