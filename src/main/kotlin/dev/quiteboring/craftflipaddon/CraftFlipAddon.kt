package dev.quiteboring.craftflipaddon

import com.google.gson.Gson
import dev.quiteboring.craftflipaddon.api.BazaarData
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.command.TestCommand
import org.cobalt.addon.Addon
import org.cobalt.command.CommandManager
import org.cobalt.module.ModuleManager
import org.slf4j.LoggerFactory

object CraftFlipAddon : Addon {

  private val logger = LoggerFactory.getLogger(this::class.java)

  val gson = Gson()

  override fun onLoad() {
    CommandManager.registerCommand(TestCommand)
    ModuleManager.addModule(CraftFlipScript)

    BazaarData.updateData()
    FlipData.updateData()

    logger.info("Loaded CraftFlipAddon!")
  }

  override fun onUnload() {
    logger.info("Unloaded CraftFlipAddon!")
  }

}
