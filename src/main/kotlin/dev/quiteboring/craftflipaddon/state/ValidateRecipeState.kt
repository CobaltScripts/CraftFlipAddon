package dev.quiteboring.craftflipaddon.state

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils
import org.slf4j.LoggerFactory

class ValidateRecipeState(val bazaarProduct: BazaarData.BazaarProduct) : ScriptState() {

  private var currState = State.OPEN_RECIPE
  private val search = bazaarProduct.productId
    .replace("_", " ")
    .lowercase()

  private val recipe = mutableMapOf<String, Int>()

  override fun onTick() {
    if (minecraft.level == null) {
      return
    }

    val player = minecraft.player ?: return
    val screen = minecraft.gui.screen()

    when (currState) {
      State.OPEN_RECIPE -> {
        ChatUtils.sendCommand("recipe $search")
        currState = State.VERIFY_OPEN_GUI
      }

      State.VERIFY_OPEN_GUI -> {
        if (screen == null) {
          return
        }

        CraftFlipScript.globalDelay.schedule(700)
        currState = State.CLICK_PRODUCT
      }

      State.CLICK_PRODUCT -> {
        val itemSlot = InventoryUtils.findItemInContainer(search)

        if (itemSlot == -1) {
          return
        }

        InventoryUtils.clickSlot(itemSlot, MouseButton.MIDDLE, ContainerInput.CLONE)
        currState = State.VALIDATE_CRAFT
      }

      State.VALIDATE_CRAFT -> {
        val invTitle = screen?.title?.string.orEmpty().lowercase()

        if (!invTitle.startsWith(search)) {
          return
        }

        val menu = player.containerMenu

        for (slotIndex in craftSlots) {
          val itemStack = menu.slots[slotIndex].item

          if (itemStack.isEmpty) {
            continue
          }

          val id = itemStack.get(DataComponents.CUSTOM_DATA)
            ?.copyTag()
            ?.getString("id")
            ?.get()

          if (id == null || BazaarData.getProduct(id) == null) {
            CraftFlipScript.globalDelay.schedule(700)
            currState = State.INVOKE_REFIND
            return
          }

          recipe[id] = (recipe[id] ?: 0) + itemStack.count
        }

        val craftSlot = InventoryUtils.findItemInContainer(Items.GOLDEN_PICKAXE)
        val loreLines = ItemUtils.getLoreLines(menu.slots[craftSlot].item)

        if (loreLines.any { it.string.contains("Recipe not unlocked!", ignoreCase = true) }) {
          CraftFlipScript.globalDelay.schedule(700)
          currState = State.INVOKE_REFIND
          return
        }

        CraftFlipScript.globalDelay.schedule(1000)
        currState = State.START_BUY_ORDER
      }

      State.INVOKE_REFIND -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.blacklistedFlips.add(bazaarProduct.productId)
        CraftFlipScript.globalDelay.schedule(1000)
        CraftFlipScript.changeState(FindFlipState())
      }

      State.START_BUY_ORDER -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.globalDelay.schedule(1000)

        ChatUtils.sendSystemMessage("Chosen Flip: ${bazaarProduct.productId}", MessageType.DEBUG)
        CraftFlipScript.changeState(BuyOrderState(recipe))
      }
    }
  }

  enum class State {
    OPEN_RECIPE,
    VERIFY_OPEN_GUI,
    CLICK_PRODUCT,
    VALIDATE_CRAFT,
    INVOKE_REFIND,
    START_BUY_ORDER
  }

  companion object {
    // sorry for the hardcoded slot numbers </3
    private val craftSlots = arrayOf(
      10, 11, 12,
      19, 20, 21,
      28, 29, 30
    )
  }

}
