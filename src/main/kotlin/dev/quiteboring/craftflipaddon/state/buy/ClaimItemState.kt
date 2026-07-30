package dev.quiteboring.craftflipaddon.state.buy

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.craft.CraftState
import dev.quiteboring.craftflipaddon.util.helper.ItemOrder
import dev.quiteboring.craftflipaddon.util.helper.OrderMode
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

class ClaimItemState(
  val flip: FlipData.FlipProduct,
  val orderedItems: MutableList<ItemOrder>
) : ScriptState() {

  private var currState = State.WAITING
  private val scheduledItemsToClaim = mutableListOf<ClaimItem>()

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

//        val outdatedItem = orderedItems.firstOrNull { item ->
//          BazaarData.isOutdatedList(item.id, item.unitPrice, OrderMode.BUY_ORDER)
//        }
//
//        if (outdatedItem == null) {
//          return
//        }
//
//        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
//        CraftFlipScript.changeState(RelistBuyState(flip, orderedItems, outdatedItem))
      }

      State.OPEN_BAZAAR -> {
        ChatUtils.sendCommand("bz")
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_BOOK
      }

      State.CLICK_BOOK -> {
        val slot = InventoryUtils.findItemInContainer("Manage Orders")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLAIM_ITEM
      }

      State.CLAIM_ITEM -> {
        val itemAndSlot = scheduledItemsToClaim.firstNotNullOfOrNull { item ->
          val slot = InventoryUtils.findItemInContainer("BUY ${item.name}", true)
          if (slot != -1) item to slot else null
        } ?: return

        val (item, slot) = itemAndSlot

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())

        scheduledItemsToClaim.remove(item)
        orderedItems.removeAll { it.name == item.name }

        if (orderedItems.isEmpty()) {
          CraftFlipScript.changeState(CraftState(flip))
        } else if (scheduledItemsToClaim.isEmpty()) {
          currState = State.WAITING
        }
      }
    }
  }

  override fun onPacketReceive(packet: Packet<*>) {
    if (packet !is ClientboundSystemChatPacket) {
      return
    }

    val text = packet.content.string
    val unformattedText = ChatFormatting.stripFormatting(text).orEmpty()
    val match = regex.find(unformattedText) ?: return

    val quantity = match.groupValues[1].replace(",", "").toInt()
    val itemName = match.groupValues[2]

    scheduledItemsToClaim.add(ClaimItem(itemName, quantity))
  }

  enum class State {
    WAITING,

    OPEN_BAZAAR,
    CLICK_BOOK,
    CLAIM_ITEM,
  }

  data class ClaimItem(val name: String, val quantity: Int)

  companion object {
    private val regex = Regex(
      """Your Buy Order for ([\d,]+)x (.+?) was filled!""",
      RegexOption.IGNORE_CASE
    )
  }
}
