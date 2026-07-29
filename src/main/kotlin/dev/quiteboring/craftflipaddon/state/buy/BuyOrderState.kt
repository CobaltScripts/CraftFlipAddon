package dev.quiteboring.craftflipaddon.state.buy

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.util.interfaces.IAbstractSignEditScreen
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.ModuleManager
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

class BuyOrderState(val buyAmounts: Map<String, Int>) : ScriptState() {

  private val items = buyAmounts.keys.toList()
  private var itemIndex = 0
  private var currState = State.OPEN_BAZAAR

  override fun enter() {
    if (buyAmounts.isEmpty() || buyAmounts.values.any { it == 0 }) {
      ChatUtils.sendSystemMessage("<red>Not enough inventory space to craft..</red>")
      ModuleManager.stopScript()
      return
    }

    ChatUtils.sendSystemMessage("Buy Amounts: $buyAmounts", MessageType.DEBUG)
  }

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val screen = minecraft.gui.screen()

    when (currState) {
      State.OPEN_BAZAAR -> {
        val item = items[itemIndex]
        val itemName = FlipData.findItemName(item)

        ChatUtils.sendCommand("bz $itemName")
        currState = State.VERIFY_OPEN_BAZAAR
      }

      State.VERIFY_OPEN_BAZAAR -> {
        if (screen == null) {
          return
        }

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_ITEM
      }

      State.CLICK_ITEM -> {
        val item = items[itemIndex]
        val itemName = FlipData.findItemName(item).lowercase()
        val slot = InventoryUtils.findItemInContainer(itemName)

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_BUY_ORDER
      }

      State.CLICK_BUY_ORDER -> {
        val slot = InventoryUtils.findItemInContainer("Create Buy Order")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_CUSTOM_AMOUNT
      }

      State.CLICK_CUSTOM_AMOUNT -> {
        val slot = InventoryUtils.findItemInContainer("Custom Amount")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.INPUT_CUSTOM_AMOUNT
      }

      State.INPUT_CUSTOM_AMOUNT -> {
        val item = items[itemIndex]
        val amount = buyAmounts[item]

        if (screen !is AbstractSignEditScreen) {
          return
        }

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        (screen as IAbstractSignEditScreen).`craftflipaddon$setFirstMessage`(amount.toString())
        currState = State.SUBMIT_CUSTOM_AMOUNT
      }

      State.SUBMIT_CUSTOM_AMOUNT -> {
        minecraft.gui.setScreen(null)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_TOP_ORDER
      }

      State.CLICK_TOP_ORDER -> {
        val slot = InventoryUtils.findItemInContainer("Top Order")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.FINISH_BUY_ORDER
      }

      State.FINISH_BUY_ORDER -> {
        val slot = InventoryUtils.findItemInContainer("Buy Order")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.NEXT_ITEM
      }

      State.NEXT_ITEM -> {
        if (screen != null) {
          return
        }

        itemIndex++

        if (itemIndex >= items.size) {
          CraftFlipScript.changeState(ClaimItemState())
          return
        }

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.OPEN_BAZAAR
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    VERIFY_OPEN_BAZAAR,
    CLICK_ITEM,
    CLICK_BUY_ORDER,
    CLICK_CUSTOM_AMOUNT,
    INPUT_CUSTOM_AMOUNT,
    SUBMIT_CUSTOM_AMOUNT,
    CLICK_TOP_ORDER,
    FINISH_BUY_ORDER,
    NEXT_ITEM
  }

}
