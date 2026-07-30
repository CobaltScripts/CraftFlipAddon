package dev.quiteboring.craftflipaddon.state.sell

import dev.quiteboring.craftflipaddon.api.FlipData
import org.cobalt.module.impl.script.ScriptState

// TODO
class ClaimCoinState(val flip: FlipData.FlipProduct, val pricePerUnit: Double) : ScriptState()
