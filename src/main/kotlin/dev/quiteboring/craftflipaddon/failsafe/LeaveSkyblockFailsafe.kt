package dev.quiteboring.craftflipaddon.failsafe

import org.cobalt.module.type.Failsafe

object LeaveSkyblockFailsafe : Failsafe(
  name = "Leave Skyblock",
  priority = 2
) {

  override fun resetStates() = Unit
  override fun performReaction() = ReactionResult.FINISHED

}
