package dev.quiteboring.craftflipaddon.state.sell

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.state.find.FindFlipState
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils

class SellOfferState : ScriptState() {

  private var currState = State.OPEN_BAZAAR
  private var unitPrice = 0.0

  override fun enter() {
    val screen = minecraft.gui.screen()
    val screenTitle = screen?.title?.string.orEmpty()

    if (screenTitle.contains("Bazaar", ignoreCase = true)) {
      currState = State.CLICK_ITEM
    }
  }

  override fun onTick() {
    val player = minecraft.player ?: return

    when (currState) {
      State.OPEN_BAZAAR -> {
        ChatUtils.sendCommand("bz")
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLICK_ITEM
      }

      State.CLICK_ITEM -> {
        val flip = CraftFlipScript.chosenFlip!!
        val slot = InventoryUtils.findItemInInventory(flip.name.lowercase())

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()

        currState = if (CraftFlipScript.instaSellProduct) State.INSTASELL else State.CREATE_SELL_OFFER
      }

      State.INSTASELL -> {
        val slot = InventoryUtils.findItemInContainer("Sell Instantly")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLOSE_CONTAINER
      }

      State.CLOSE_CONTAINER -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(FindFlipState())
      }

      State.CREATE_SELL_OFFER -> {
        val slot = InventoryUtils.findItemInContainer("Create Sell Offer")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLICK_BEST_OFFER
      }

      State.CLICK_BEST_OFFER -> {
        val slot = InventoryUtils.findItemInContainer("Best Offer")

        if (slot == -1) {
          return
        }

        val stack = player.containerMenu.getSlot(slot).item
        val loreLines = ItemUtils.getLoreLines(stack)

        unitPrice = loreLines.firstNotNullOfOrNull { line ->
          regex.find(line.string)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        } ?: return

        ChatUtils.sendSystemMessage("Unit Price: $unitPrice", MessageType.DEBUG)

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.FINISH_SELL_ORDER
      }


      State.FINISH_SELL_ORDER -> {
        val slot = InventoryUtils.findItemInContainer("Sell Offer")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(ClaimCoinState(unitPrice))
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_ITEM,

    INSTASELL,
    CLOSE_CONTAINER,

    CREATE_SELL_OFFER,
    CLICK_BEST_OFFER,
    FINISH_SELL_ORDER
  }

  companion object {
    private val regex = Regex(
      """Unit price:\s*([\d,]+(?:\.\d+)?)\s*coins""",
      RegexOption.IGNORE_CASE
    )
  }

}
