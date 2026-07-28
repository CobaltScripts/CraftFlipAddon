package dev.quiteboring.craftflipaddon.state

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipFinder
import kotlin.random.Random
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.client.PlayerUtils

class FindFlipState : ScriptState() {

  override fun enter() {
    CraftFlipScript.chosenFlip = FlipFinder.findFlip()
  }

  override fun onTick() {
    val flip = CraftFlipScript.chosenFlip

    if (flip == null) {
      CraftFlipScript.chosenFlip = FlipFinder.findFlip(true)
      CraftFlipScript.globalDelay.schedule(1000)
      return
    }

    val bazaarProduct = BazaarData.getProduct(flip.id) ?: return
    CraftFlipScript.changeState(ValidateRecipeState(bazaarProduct))
  }

}
