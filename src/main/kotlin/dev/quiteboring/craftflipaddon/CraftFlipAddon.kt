package dev.quiteboring.craftflipaddon

import org.cobalt.addon.Addon
import org.slf4j.LoggerFactory

class CraftFlipAddon : Addon {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun onLoad() {
        logger.info("Loaded!")
    }

    override fun onUnload() {
        logger.info("Unloaded!")
    }

}
