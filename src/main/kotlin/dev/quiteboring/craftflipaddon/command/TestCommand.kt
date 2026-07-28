package dev.quiteboring.craftflipaddon.command

import dev.quiteboring.craftflipaddon.util.SkyblockUtils
import org.cobalt.command.Command
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.util.chat.ChatUtils

object TestCommand : Command("cftest") {

  @DefaultHandler
  fun main() {
    ChatUtils.sendSystemMessage("In Skyblock?: ${SkyblockUtils.isInSkyblock}")
    ChatUtils.sendSystemMessage("Booster Cookie Buff State: ${SkyblockUtils.cookieBuffState}")
  }

}
