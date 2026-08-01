package dev.quiteboring.craftflipaddon.state.buy

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.util.helper.ItemOrder
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.inventory.ItemUtils

class RelistBuyState(val itemToRelist: ItemOrder) : ScriptState() {

  private var currState = State.OPEN_BAZAAR
  private var amountToBuy = 1
  private var canceled = false

  override fun onTick() {
    val player = minecraft.player ?: return

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
        currState = State.CLAIM_ITEM
      }

      State.CLAIM_ITEM -> {
        if (screenTitle.startsWith("Order options", ignoreCase = true)) {
          CraftFlipScript.scheduleGlobalDelay()
          canceled = true
          currState = State.CANCEL_ORDER
          return
        }

        if (!screenTitle.contains("Bazaar Orders", ignoreCase = true)) {
          return
        }

        val slot = InventoryUtils.findItemInContainer("[BUY ${itemToRelist.name}]", true)

        if (slot == -1) {
          if (canceled) {
            currState = State.CLOSE_SCREEN
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
          currState = State.CLAIM_ITEM
          return
        }

        val slot = InventoryUtils.findItemInContainer("Cancel Order")

        if (slot == -1) {
          return
        }

        val itemStack = player.containerMenu.getSlot(slot).item
        val loreLines = ItemUtils.getLoreLines(itemStack)

        amountToBuy = loreLines.firstNotNullOfOrNull { line ->
          regex.find(line.string)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull()
        } ?: return

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
      }

      State.CLOSE_SCREEN -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.INVOKE_BUY_ORDER
      }

      State.INVOKE_BUY_ORDER -> {
        CraftFlipScript.orderedItems.remove(itemToRelist)
        CraftFlipScript.scheduleGlobalDelay()

        val pair = Pair("${itemToRelist.id}:${itemToRelist.name}", amountToBuy)
        CraftFlipScript.changeState(BuyOrderState(mapOf(pair)))
      }
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_MANAGE_ORDERS,
    CLAIM_ITEM,
    CANCEL_ORDER,
    CLOSE_SCREEN,
    INVOKE_BUY_ORDER
  }

  companion object {
    private val regex = Regex(
      """from ([\d,]+)x missing items""",
      RegexOption.IGNORE_CASE
    )
  }

}
