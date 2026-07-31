package dev.quiteboring.craftflipaddon.mixins;

import dev.quiteboring.craftflipaddon.event.HoverSlotEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.cobalt.event.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

  @Shadow
  @Nullable
  protected Slot hoveredSlot;

  @Unique
  private Slot craftflip$lastHoveredSlot;

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
    if (hoveredSlot != craftflip$lastHoveredSlot) {
      craftflip$lastHoveredSlot = hoveredSlot;

      if (hoveredSlot != null) {
        EventBus.post(new HoverSlotEvent(hoveredSlot));
      }
    }
  }

}
