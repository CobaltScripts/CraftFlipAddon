package dev.quiteboring.craftflipaddon.util

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

object SearchUtils {

  fun getProductId(itemStack: ItemStack): String? {
    return itemStack.get(DataComponents.CUSTOM_DATA)
      ?.copyTag()
      ?.getString("id")
      ?.get()
  }

}
