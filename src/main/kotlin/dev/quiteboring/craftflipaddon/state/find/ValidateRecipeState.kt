package dev.quiteboring.craftflipaddon.state.find

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.buy.BuyOrderState
import dev.quiteboring.craftflipaddon.util.SearchUtils
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

class ValidateRecipeState(val flip: FlipData.FlipProduct) : ScriptState() {

  private var currState = State.OPEN_RECIPE
  private val recipe = mutableMapOf<String, Int>()

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val player = minecraft.player ?: return
    val screen = minecraft.gui.screen()

    when (currState) {
      State.OPEN_RECIPE -> {
        ChatUtils.sendCommand("recipe ${flip.name.lowercase()}")
        currState = State.VERIFY_OPEN_GUI
      }

      State.VERIFY_OPEN_GUI -> {
        if (screen == null) {
          return
        }

        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLICK_PRODUCT
      }

      State.CLICK_PRODUCT -> {
        val itemSlot = InventoryUtils.findItemInContainer("[${flip.name}]", true)

        if (itemSlot == -1) {
          return
        }

        InventoryUtils.clickSlot(itemSlot, MouseButton.MIDDLE, ContainerInput.CLONE)
        currState = State.VALIDATE_CRAFT
      }

      State.VALIDATE_CRAFT -> {
        val invTitle = screen?.title?.string.orEmpty()

        if (!invTitle.startsWith(flip.name, ignoreCase = true)) {
          return
        }

        val menu = player.containerMenu

        val craftSlot = InventoryUtils.findItemInContainer("Supercraft")
        val loreLines = ItemUtils.getLoreLines(menu.slots[craftSlot].item)

        if (loreLines.any { it.string.contains("Recipe not unlocked!", ignoreCase = true) }) {
          CraftFlipScript.scheduleGlobalDelay()
          currState = State.INVOKE_REFIND
          return
        }

        for (slotIndex in craftSlots) {
          val itemStack = menu.slots[slotIndex].item

          if (itemStack.isEmpty) {
            continue
          }

          val id = SearchUtils.getProductId(itemStack)

          if (id == null || BazaarData.getProduct(id) == null) {
            CraftFlipScript.scheduleGlobalDelay()
            currState = State.INVOKE_REFIND
            return
          }

          val name = itemStack.displayName.string.replace(Regex("[\\[\\]]"), "")
          val key = "$id:$name"

          recipe[key] = (recipe[key] ?: 0) + itemStack.count
        }

        CraftFlipScript.scheduleGlobalDelay()
        currState = State.START_BUY_ORDER
      }

      State.INVOKE_REFIND -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.blacklistedFlips.add(flip.id)
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(FindFlipState())
      }

      State.START_BUY_ORDER -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()
        ChatUtils.sendSystemMessage("Chosen Flip: ${flip.id}", MessageType.DEBUG)
        CraftFlipScript.changeState(BuyOrderState(flip, genBuyAmounts()))
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
    CraftFlipScript.amountToCraft = maxCrafts

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
