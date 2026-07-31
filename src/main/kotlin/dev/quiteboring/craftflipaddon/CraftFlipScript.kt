package dev.quiteboring.craftflipaddon

import dev.quiteboring.craftflipaddon.api.FlipData
import dev.quiteboring.craftflipaddon.failsafe.LeaveSkyblockFailsafe
import dev.quiteboring.craftflipaddon.state.find.FindFlipState
import dev.quiteboring.craftflipaddon.util.SkyblockUtils
import dev.quiteboring.craftflipaddon.util.helper.BuffState
import dev.quiteboring.craftflipaddon.util.helper.ItemOrder
import kotlin.random.Random
import org.cobalt.event.annotation.SubscribeEvent
import org.cobalt.event.impl.PacketEvent
import org.cobalt.event.impl.TickEvent
import org.cobalt.event.impl.WorldEvent
import org.cobalt.module.ModuleCategory
import org.cobalt.module.ModuleManager
import org.cobalt.module.impl.script.ScriptState
import org.cobalt.module.type.Script
import org.cobalt.ui.component.setting.impl.CheckboxSetting
import org.cobalt.ui.component.setting.impl.InfoSetting
import org.cobalt.ui.component.setting.impl.ModeSetting
import org.cobalt.ui.component.setting.impl.SliderSetting
import org.cobalt.util.chat.ChatUtils
import org.cobalt.util.chat.MessageType
import org.cobalt.util.scheduling.Clock

object CraftFlipScript : Script(
  name = "CraftFlip",
  category = ModuleCategory.MISC,
  backgroundResourcePath = "/assets/craftflipaddon/script.png",
  failsafes = listOf(LeaveSkyblockFailsafe)
) {

  val ignored by InfoSetting(
    text = "You need a Hypixel rank in order to use!",
    type = InfoSetting.Type.ERROR
  )

  val minMargin by SliderSetting(
    name = "Min Margin",
    description = "Min margin in thousands",
    min = 4,
    max = 2000,
    defaultValue = 4
  )

  val maxCraftCost by SliderSetting(
    name = "Max Craft Cost",
    description = "Max craft cost in thousands",
    min = 0,
    max = 2000,
    defaultValue = 500
  )

  val maxCoinsPerHour by SliderSetting(
    name = "Max Coins Per Hour",
    description = "Max coins per hour in millions",
    min = 1,
    max = 200,
    defaultValue = 25
  )

  val instaSellProduct by CheckboxSetting(
    name = "Instasell Product",
    description = "Instasell final product to bazaar",
    defaultValue = false
  )

  val sortMode by ModeSetting(
    name = "Sort Mode",
    description = "How to sort flips",
    defaultValue = 0,
    options = arrayOf("Coins Per Hour", "Margin", "Mix")
  )

  val updateFlipsInterval by SliderSetting(
    name = "Update Flips Interval",
    description = "Updates best flips every x amount of minutes",
    min = 1,
    max = 60,
    defaultValue = 20
  )

  val updateBazaarDataInterval by SliderSetting(
    name = "Update Bazaar Data Interval",
    description = "Updates bazaar data every x amount of seconds",
    min = 15,
    max = 3600,
    defaultValue = 3600
  )

  var state: ScriptState? = null
  val globalDelay = Clock()

  var chosenFlip: FlipData.FlipProduct? = null
  var amountToCraft: Int = 0
  val orderedItems = mutableListOf<ItemOrder>()
  val blacklistedFlips = mutableSetOf<String>()

  override fun onEnable() {
    if (SkyblockUtils.cookieBuffState != BuffState.ACTIVE) {
      ChatUtils.sendSystemMessage("<red>You need a booster cookie in order to use this macro.</red>")
      ModuleManager.stopScript()
      return
    }

    chosenFlip = null
    amountToCraft = 0
    orderedItems.clear()
    globalDelay.schedule(1000)
    changeState(FindFlipState())

    super.onEnable()
  }

  override fun onDisable() {
    changeState(null)
    chosenFlip = null
    super.onDisable()
  }

  @SubscribeEvent
  fun onTick(ignored: TickEvent.Start) {
    if (!enabled) {
      return
    }

    if (minecraft.level == null || minecraft.player == null) {
      return
    }

    if (SkyblockUtils.cookieBuffState != BuffState.ACTIVE) {
      ChatUtils.sendSystemMessage("<red>You need a booster cookie in order to use this macro.</red>")
      ModuleManager.stopScript()
      return
    }

    if (!globalDelay.passed()) {
      return
    }

    state?.onTick()
  }

  @SubscribeEvent
  fun onRender(ignored: WorldEvent.BeforeGizmos) {
    if (!enabled) {
      return
    }

    state?.onRender()
  }

  fun changeState(newState: ScriptState?) {
    state?.exit()

    newState?.enter()
    state = newState

    ChatUtils.sendSystemMessage("Current State: ${state?.javaClass?.simpleName}", MessageType.DEBUG)
  }

  @SubscribeEvent()
  fun onPacketSend(event: PacketEvent.Send) {
    if (!enabled) {
      return
    }

    state?.onPacketSend(event.packet)
  }

  @SubscribeEvent
  fun onPacketReceive(event: PacketEvent.Receive) {
    if (!enabled) {
      return
    }

    state?.onPacketReceive(event.packet)
  }

  fun genDelay(): Long {
    return Random.nextLong(500L, 700L)
  }

}
