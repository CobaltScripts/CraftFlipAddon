package dev.quiteboring.craftflipaddon.util

import dev.quiteboring.craftflipaddon.mixins.PlayerTabOverlayAccessor
import dev.quiteboring.craftflipaddon.util.helper.BuffState
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import org.cobalt.event.EventBus
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.event.impl.WorldEvent

object SkyblockUtils {

  var isInSkyblock: Boolean = false
    private set

  val cookieBuffState: BuffState
    get() {
      val playerListHud = Minecraft.getInstance().gui.hud.tabList
      val footer = (playerListHud as PlayerTabOverlayAccessor).footer
      val lines = footer.string.lines()

      val cookieIndex = lines.indexOfFirst {
        it.contains("Cookie Buff", ignoreCase = true)
      }

      if (cookieIndex == -1 || cookieIndex + 1 >= lines.size) {
        return BuffState.UNKNOWN
      }

      val status = lines[cookieIndex + 1]

      return when {
        status.contains("Not active!", ignoreCase = true) -> BuffState.INACTIVE
        else -> BuffState.ACTIVE
      }
    }

  init {
    EventBus.register(this)
  }

  @SubscribeEvent
  fun onPacketReceive(event: PacketEvent.Receive) {
    val packet = event.packet

    if (packet !is ClientboundSetObjectivePacket) {
      return
    }

    if (!isInSkyblock) {
      val objName = packet.objectiveName
      isInSkyblock = objName == "SBScoreboard"
    }
  }

  @SubscribeEvent
  fun onWorldChange(event: WorldEvent.Change) {
    isInSkyblock = false
  }

}
