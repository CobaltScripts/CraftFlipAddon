package dev.quiteboring.craftflipaddon.state.sell

import dev.quiteboring.craftflipaddon.CraftFlipScript
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

class RelistSellState : ScriptState() {

  private var currState = State.OPEN_BAZAAR
  private var canceled = false

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val screen = minecraft.gui.screen()
    val screenTitle = screen?.title?.string.orEmpty()

    when (currState) {
      State.OPEN_BAZAAR -> {
        ChatUtils.sendCommand("bz")
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLICK_MANAGE_ORDERS
      }

      State.CLICK_MANAGE_ORDERS -> {
        val slot = InventoryUtils.findItemInContainer("Manage Orders")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLAIM_COIN
      }

      State.CLAIM_COIN -> {
        if (screenTitle.startsWith("Order options", ignoreCase = true)) {
          CraftFlipScript.scheduleGlobalDelay()
          canceled = true
          currState = State.CANCEL_ORDER
          return
        }

        if (!screenTitle.contains("Bazaar Orders", ignoreCase = true)) {
          return
        }

        val flip = CraftFlipScript.chosenFlip!!
        val slot = InventoryUtils.findItemInContainer("[SELL ${flip.name}]", true)

        if (slot == -1) {
          if (canceled) {
            currState = State.INVOKE_SELL_ORDER
            return
          }

          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
      }

      State.CANCEL_ORDER -> {
        if (screen != null && !screenTitle.startsWith("Order options", ignoreCase = true)) {
          CraftFlipScript.scheduleGlobalDelay()
          currState = State.CLAIM_COIN
          return
        }

        val slot = InventoryUtils.findItemInContainer("Cancel Order")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
      }

      State.INVOKE_SELL_ORDER -> {
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(SellOfferState())
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_MANAGE_ORDERS,
    CLAIM_COIN,
    CANCEL_ORDER,
    INVOKE_SELL_ORDER
  }

}

