package dev.quiteboring.craftflipaddon.util

import dev.quiteboring.craftflipaddon.mixins.PlayerTabOverlayAccessor
import dev.quiteboring.craftflipaddon.util.helper.BuffState
import net.minecraft.client.Minecraft

object SkyblockUtils {

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

}
