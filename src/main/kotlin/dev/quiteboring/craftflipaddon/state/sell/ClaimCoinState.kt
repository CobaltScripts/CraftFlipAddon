package dev.quiteboring.craftflipaddon.state.sell

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.buy.ClaimItemState
import dev.quiteboring.craftflipaddon.state.buy.ClaimItemState.ClaimItem
import dev.quiteboring.craftflipaddon.state.find.FindFlipState
import dev.quiteboring.craftflipaddon.util.helper.OrderMode
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.client.PlayerUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils
import org.cobalt.util.scheduling.Clock

class ClaimCoinState(val unitPrice: Double) : ScriptState() {

  private var currState = State.WAITING
  private val relistDelay = Clock()

  override fun enter() {
    relistDelay.schedule(30_000)
  }

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val flip = CraftFlipScript.chosenFlip!!

    when (currState) {
      State.WAITING -> {
        if (!relistDelay.passed()) {
          return
        }

        if (!BazaarData.isOutdatedList(flip.id, unitPrice, OrderMode.SELL_ORDER)) {
          return
        }

        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(RelistSellState())
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
        currState = State.CLAIM_COIN
      }

      State.CLAIM_COIN -> {
        val slot = InventoryUtils.findItemInContainer("[SELL ${flip.name}]", true)

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.scheduleGlobalDelay()
        currState = State.CLOSE_SCREEN
      }

      State.CLOSE_SCREEN -> {
        PlayerUtils.closeScreen()
        CraftFlipScript.scheduleGlobalDelay()
        CraftFlipScript.changeState(FindFlipState())
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

    if (regex.find(unformattedText) == null) {
      return
    }

    currState = State.OPEN_BAZAAR
  }

  enum class State {
    WAITING,

    OPEN_BAZAAR,
    CLICK_MANAGE_ORDERS,
    CLAIM_COIN,
    CLOSE_SCREEN
  }

  companion object {
    private val regex = Regex(
      """Your Sell Offer for ([\d,]+)x (.+?) was filled!""",
      RegexOption.IGNORE_CASE
    )
  }

}
