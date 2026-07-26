package dev.quiteboring.craftflipaddon.state

import org.cobalt.module.impl.script.ScriptState

class BuyOrderState(val recipe: Map<String, Int>) : ScriptState() {

  val buyAmounts = mutableMapOf<String, Int>()

  override fun enter() {
    println(recipe)
  }

}
