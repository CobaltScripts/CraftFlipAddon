package dev.quiteboring.craftflipaddon.state.buy

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.craft.CraftState
import dev.quiteboring.craftflipaddon.util.helper.OrderMode
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.ModuleManager
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.scheduling.Clock

class ClaimItemState : ScriptState() {

  private var currState = State.WAITING
  private val scheduledItemsToClaim = mutableListOf<ClaimItem>()
  private val relistDelay = Clock()

  override fun enter() {
    relistDelay.schedule(30_000)
  }

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    when (currState) {
      State.WAITING -> {
        if (scheduledItemsToClaim.isNotEmpty()) {
          currState = State.OPEN_BAZAAR
          return
        }

        if (!relistDelay.passed()) {
          return
        }

        val outdatedItem = CraftFlipScript.orderedItems.firstOrNull { item ->
          BazaarData.isOutdatedList(item.id, item.unitPrice, OrderMode.BUY_ORDER)
        }

        if (outdatedItem == null) {
          return
        }

        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(RelistBuyState(flip, outdatedItem))
      }

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
        val itemAndSlot = scheduledItemsToClaim.firstNotNullOfOrNull { item ->
          val slot = InventoryUtils.findItemInContainer("[BUY ${item.name}]", true)
          if (slot != -1) item to slot else null
        } ?: return

        val (item, slot) = itemAndSlot

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()

        scheduledItemsToClaim.remove(item)
        CraftFlipScript.orderedItems.removeAll { it.name == item.name }

        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()

        if (scheduledItemsToClaim.isEmpty()) {
          CraftFlipScript.changeState(CraftState(flip))
        } else {
          currState = State.WAITING
        }
      }

      State.CLOSE_SCREEN -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(CraftState(flip))
      }
    }
  }

  override fun onPacketReceive(packet: Packet<*>) {
    if (packet !is ClientboundSystemChatPacket) {
      return
    }

    val text = packet.content.string
    val unformattedText = ChatFormatting.stripFormatting(text).orEmpty()

    if (unformattedText.contains(":")) {
      return
    }

    val match = regex.find(unformattedText) ?: return
    val quantity = match.groupValues[1].replace(",", "").toInt()
    val itemName = match.groupValues[2]

    scheduledItemsToClaim.add(ClaimItem(itemName, quantity))
  }

  enum class State {
    WAITING,

    OPEN_BAZAAR,
    CLICK_MANAGE_ORDERS,
    CLAIM_ITEM,
    CLOSE_SCREEN
  }

  data class ClaimItem(val name: String, val quantity: Int)

  companion object {
    private val regex = Regex(
      """Your Buy Order for ([\d,]+)x (.+?) was filled!""",
      RegexOption.IGNORE_CASE
    )
  }
}
