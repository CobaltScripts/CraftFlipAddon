package dev.quiteboring.craftflipaddon.state.buy

import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.util.helper.ItemOrder
import org.cobalt.module.impl.script.ScriptState

class RelistBuyState(
  flip: FlipData.FlipProduct,
  val orderedItems: MutableList<ItemOrder>,
  val itemToRelist: ItemOrder
) : ScriptState() {

  private var currState = State.OPEN_BAZAAR

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    when (currState) {
      State.OPEN_BAZAAR -> TODO()
      State.CLICK_BOOK -> TODO()
      State.CLAIM_ITEM -> TODO()
      State.CANCEL_ORDER -> TODO()
      State.INVOKE_BUY_ORDER -> TODO()
    }
  }

  enum class State {
    OPEN_BAZAAR,
    CLICK_BOOK,
    CLAIM_ITEM,
    CANCEL_ORDER,
    INVOKE_BUY_ORDER
  }

}
