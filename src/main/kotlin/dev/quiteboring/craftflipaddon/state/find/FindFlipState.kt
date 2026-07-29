package dev.quiteboring.craftflipaddon.state.find

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import org.cobalt.module.impl.script.ScriptState

class FindFlipState : ScriptState() {

  override fun enter() {
    CraftFlipScript.chosenFlip = FlipData.findFlip()
  }

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val flip = CraftFlipScript.chosenFlip

    if (flip == null) {
      CraftFlipScript.chosenFlip = FlipData.findFlip(true)
      CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
      return
    }

    val bazaarProduct = BazaarData.getProduct(flip.id) ?: return
    CraftFlipScript.changeState(ValidateRecipeState(bazaarProduct))
  }

}
