package dev.quiteboring.craftflipaddon.state.find

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.buy.BuyOrderState
import net.minecraft.core.component.DataComponents
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils
import kotlin.collections.iterator

class ValidateRecipeState(val bazaarProduct: BazaarData.BazaarProduct) : ScriptState() {

  private var currState = State.OPEN_RECIPE
  private val search = FlipData.findItemName(bazaarProduct.productId)
    .lowercase()

  private val recipe = mutableMapOf<String, Int>()

  override fun onTick() {
    if (minecraft.player == null) {
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

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
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

        val craftSlot = InventoryUtils.findItemInContainer("Supercraft")
        val loreLines = ItemUtils.getLoreLines(menu.slots[craftSlot].item)

        if (loreLines.any { it.string.contains("Recipe not unlocked!", ignoreCase = true) }) {
          CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
          currState = State.INVOKE_REFIND
          return
        }

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
            CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
            currState = State.INVOKE_REFIND
            return
          }

          recipe[id] = (recipe[id] ?: 0) + itemStack.count
        }

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.START_BUY_ORDER
      }

      State.INVOKE_REFIND -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.blacklistedFlips.add(bazaarProduct.productId)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        CraftFlipScript.changeState(FindFlipState())
      }

      State.START_BUY_ORDER -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        ChatUtils.sendSystemMessage("Chosen Flip: ${bazaarProduct.productId}", MessageType.DEBUG)
        CraftFlipScript.changeState(BuyOrderState(genBuyAmounts()))
      }
    }
  }

  private fun genBuyAmounts(): Map<String, Int> {
    val buyAmounts = mutableMapOf<String, Int>()
    val inventorySpace = calculateInventorySpace()
    val totalPerCraft = recipe.values.sum()

    if (totalPerCraft == 0) {
      return emptyMap()
    }

    val maxCrafts = inventorySpace / totalPerCraft

    for ((ingredient, count) in recipe) {
      buyAmounts[ingredient] = count * maxCrafts
    }

    return buyAmounts
  }

  private fun calculateInventorySpace(): Int {
    val player = minecraft.player ?: return 0
    val menu = player.containerMenu
    var emptySlots = 0

    for (i in 0 until 36) {
      if (menu.slots[i].item.isEmpty) {
        emptySlots++
      }
    }

    return emptySlots * 64
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
