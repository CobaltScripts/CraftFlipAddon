package dev.quiteboring.craftflipaddon.state.sell

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.FlipData
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.ModuleManager
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

class SellOfferState(val flip: FlipData.FlipProduct) : ScriptState() {

  private var currState = State.OPEN_BAZAAR

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    when (currState) {
      State.OPEN_BAZAAR -> {
        ChatUtils.sendCommand("bz")
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_ITEM
      }

      State.CLICK_ITEM -> {
        val slot = InventoryUtils.findItemInInventory(flip.name.lowercase())

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CREATE_SELL_OFFER
      }

      State.CREATE_SELL_OFFER -> {
        val slot = InventoryUtils.findItemInContainer("Create Sell Offer")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_BEST_OFFER
      }

      State.CLICK_BEST_OFFER -> {
        val slot = InventoryUtils.findItemInContainer("Best Offer")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.FINISH_SELL_ORDER
      }


      State.FINISH_SELL_ORDER -> {
        val slot = InventoryUtils.findItemInContainer("Sell Offer")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        CraftFlipScript.changeState(ClaimCoinState())
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_ITEM,
    CREATE_SELL_OFFER,
    CLICK_BEST_OFFER,
    FINISH_SELL_ORDER
  }

}
