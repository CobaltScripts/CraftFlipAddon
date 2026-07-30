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
import org.cobalt.util.inventory.ItemUtils
import dev.quiteboring.craftflipaddon.util.helper.ItemOrder

class BuyOrderState(
  val flip: FlipData.FlipProduct,
  val buyAmounts: Map<String, Int>
) : ScriptState() {

  private val items = buyAmounts.keys.toList()

  private var currState = State.OPEN_BAZAAR
  private var itemIndex = 0
  private var unitPrice = 0.0

  private val orderedItems = mutableListOf<ItemOrder>()

  override fun enter() {
    if (buyAmounts.isEmpty() || buyAmounts.values.any { it == 0 }) {
      ChatUtils.sendSystemMessage("<red>Not enough inventory space to craft..</red>")
      ModuleManager.stopScript()
      return
    }

    ChatUtils.sendSystemMessage("Buy Amounts: $buyAmounts", MessageType.DEBUG)
  }

  override fun onTick() {
    val player = minecraft.player ?: return
    val screen = minecraft.gui.screen()

    when (currState) {
      State.OPEN_BAZAAR -> {
        val itemName = items[itemIndex].split(":")[1].lowercase()

        ChatUtils.sendCommand("bz $itemName")
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_ITEM
      }

      State.CLICK_ITEM -> {
        val itemName = items[itemIndex].split(":")[1]
        val slot = InventoryUtils.findItemInContainer("[$itemName]", true)

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

        val stack = player.containerMenu.getSlot(slot).item
        val loreLines = ItemUtils.getLoreLines(stack)

        unitPrice = loreLines.firstNotNullOfOrNull { line ->
          regex.find(line.string)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        } ?: return

        ChatUtils.sendSystemMessage("Unit Price: $unitPrice", MessageType.DEBUG)

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

        val name = items[itemIndex].split(":")[1]
        orderedItems += ItemOrder(name, items[itemIndex], buyAmounts[items[itemIndex]] ?: 0, unitPrice)
        currState = State.NEXT_ITEM
      }

      State.NEXT_ITEM -> {
        if (screen != null) {
          return
        }

        itemIndex++

        if (itemIndex >= items.size) {
          CraftFlipScript.changeState(ClaimItemState(flip, orderedItems))
          return
        }

        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.OPEN_BAZAAR
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_ITEM,
    CLICK_BUY_ORDER,
    CLICK_CUSTOM_AMOUNT,
    INPUT_CUSTOM_AMOUNT,
    SUBMIT_CUSTOM_AMOUNT,
    CLICK_TOP_ORDER,
    FINISH_BUY_ORDER,
    NEXT_ITEM
  }

  companion object {
    private val regex = Regex(
      """Unit price:\s*([\d,]+(?:\.\d+)?)\s*coins""",
      RegexOption.IGNORE_CASE
    )
  }

}
