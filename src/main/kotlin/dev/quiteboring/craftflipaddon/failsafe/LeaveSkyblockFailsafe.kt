package dev.quiteboring.craftflipaddon.failsafe

import dev.quiteboring.craftflipaddon.util.SkyblockUtils
import kotlin.random.Random
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.module.type.Failsafe
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.failsafe.FailsafeManager
import org.cobalt.util.scheduling.Clock

object LeaveSkyblockFailsafe : Failsafe(
  name = "Leave Skyblock",
  priority = 2
) {

  private var state = ReactionState.SEND_LOBBY_COMMAND
  private var leftSkyblock = false

  private val reactionDelay = Clock()
  private val checkDelay = Clock()
  private val verifyDelay = Clock()

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (minecraft.player == null) {
      return
    }

    if (SkyblockUtils.isInSkyblock) {
      leftSkyblock = false
      checkDelay.reset()
      return
    }

    if (!leftSkyblock) {
      checkDelay.schedule(10_000)
      leftSkyblock = true
    }

    if (checkDelay.passed()) {
      FailsafeManager.addToQueue(this)
    }
  }

  override fun resetStates() {
    state = ReactionState.SEND_LOBBY_COMMAND
    reactionDelay.reset()
    checkDelay.reset()
  }

  override fun performReaction(): ReactionResult {
    if (minecraft.player == null) {
      return ReactionResult.CONTINUE
    }

    if (!reactionDelay.passed()) {
      return ReactionResult.CONTINUE
    }

    when (state) {
      ReactionState.SEND_LOBBY_COMMAND -> {
        ChatUtils.sendCommand("lobby")
        scheduleReactionDelay()
        state = ReactionState.SEND_SKYBLOCK_COMMAND
      }

      ReactionState.SEND_SKYBLOCK_COMMAND -> {
        ChatUtils.sendCommand("play skyblock")
        scheduleReactionDelay()

        verifyDelay.schedule(genDelay())
        state = ReactionState.VERIFY_IN_SKYBLOCK
      }

      ReactionState.VERIFY_IN_SKYBLOCK -> {
        if (verifyDelay.passed()) {
          if (SkyblockUtils.isInSkyblock) {
            return ReactionResult.FINISHED
          }

          verifyDelay.reset()
          state = ReactionState.SEND_LOBBY_COMMAND
        }
      }
    }

    return ReactionResult.CONTINUE
  }

  fun scheduleReactionDelay() {
    reactionDelay.schedule(genDelay())
  }

  fun genDelay(): Long {
    return Random.nextLong(10_000L, 15_000L)
  }

  enum class ReactionState {
    SEND_LOBBY_COMMAND,
    SEND_SKYBLOCK_COMMAND,
    VERIFY_IN_SKYBLOCK
  }

}
