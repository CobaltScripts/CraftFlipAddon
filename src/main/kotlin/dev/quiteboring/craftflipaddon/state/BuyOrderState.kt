package dev.quiteboring.craftflipaddon.state

import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType

class BuyOrderState(val recipe: Map<String, Int>) : ScriptState() {

  private val buyAmounts = mutableMapOf<String, Int>()

  override fun enter() {
    val inventorySpace = calculateInventorySpace()
    val totalPerCraft = recipe.values.sum()

    if (totalPerCraft == 0) {
      return
    }

    val maxCrafts = inventorySpace / totalPerCraft

    buyAmounts.clear()

    for ((ingredient, count) in recipe) {
      buyAmounts[ingredient] = count * maxCrafts
    }

    ChatUtils.sendSystemMessage("Buy Amounts: $buyAmounts", MessageType.DEBUG)
  }

  override fun onTick() {

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
    CHOOSE_ITEM,
    SEND_COMMAND,
    CLICK_ITEM,
    CLICK_BUY_ORDER
  }

}
