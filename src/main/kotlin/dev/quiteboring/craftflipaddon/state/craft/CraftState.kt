package dev.quiteboring.craftflipaddon.state.craft

import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.state.sell.SellOfferState
import dev.quiteboring.craftflipaddon.util.interfaces.IAbstractSignEditScreen
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
import net.minecraft.world.inventory.ContainerInput
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.input.MouseButton
import org.cobalt.util.inventory.InventoryUtils

class CraftState(
  val flip: FlipData.FlipProduct,
  val amountToCraft: Int
) : ScriptState() {

  private val flipName = flip.name
  private var currState = State.SEND_COMMAND

  override fun onTick() {
    if (minecraft.player == null) {
      return
    }

    val screen = minecraft.gui.screen()

    when (currState) {
      State.SEND_COMMAND -> {
        ChatUtils.sendCommand("recipe ${flipName.lowercase()}")
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_ITEM
      }

      State.CLICK_ITEM -> {
        val slot = InventoryUtils.findItemInContainer(flipName)

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.OPEN_SIGN
      }

      State.OPEN_SIGN -> {
        val slot = InventoryUtils.findItemInContainer("Supercraft")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.RIGHT, ContainerInput.PICKUP)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.INPUT_SIGN
      }

      State.INPUT_SIGN -> {
        if (screen !is AbstractSignEditScreen) {
          return
        }

        (screen as IAbstractSignEditScreen).`craftflipaddon$setFirstMessage`(amountToCraft.toString())
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.EXIT_SIGN
      }

      State.EXIT_SIGN -> {
        minecraft.gui.setScreen(null)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.CLICK_SUPERCRAFT
      }

      State.CLICK_SUPERCRAFT -> {
        val slot = InventoryUtils.findItemInContainer("Supercraft")

        if (slot == -1) {
          return
        }

        InventoryUtils.clickSlot(slot, MouseButton.MIDDLE, ContainerInput.CLONE)
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        currState = State.EXIT_SCREEN
      }

      State.EXIT_SCREEN -> {
        CraftFlipScript.globalDelay.schedule(CraftFlipScript.genDelay())
        CraftFlipScript.changeState(SellOfferState(flip))
      }
    }
  }

  enum class State {
    SEND_COMMAND,
    CLICK_ITEM,
    OPEN_SIGN,
    INPUT_SIGN,
    EXIT_SIGN,
    CLICK_SUPERCRAFT,
    EXIT_SCREEN
  }

}
