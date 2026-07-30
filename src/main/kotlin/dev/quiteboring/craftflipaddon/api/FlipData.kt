package dev.quiteboring.craftflipaddon.api

import com.google.gson.annotations.SerializedName
import dev.quiteboring.craftflipaddon.CraftFlipAddon
import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.util.WebUtils
import org.cobalt.util.scheduling.Multithreading
import org.slf4j.LoggerFactory

object FlipData {

  private val logger = LoggerFactory.getLogger(this::class.java)
  private var lastUpdate = 0L

  var products: Array<FlipProduct> = emptyArray()
    private set

  fun findFlip(force: Boolean = false): FlipProduct? {
    updateData(force)

    val minMargin = CraftFlipScript.minMargin * 1000.0
    val maxCraftCost = CraftFlipScript.maxCraftCost * 1000.0
    val maxCoinsPerHour = CraftFlipScript.maxCoinsPerHour * 1_000_000.0

    val filtered = products
      .filter { it.id !in CraftFlipScript.blacklistedFlips }
      .filter { it.margin >= minMargin }
      .filter { it.craftCost <= maxCraftCost }
      .filter {
        if (CraftFlipScript.instaSellProduct) it.instaCoinsPerHour <= maxCoinsPerHour
        else it.coinsPerHour <= maxCoinsPerHour
      }

    if (filtered.isEmpty()) {
      return null
    }

    val coinsMetric = { product: FlipProduct ->
      if (CraftFlipScript.instaSellProduct) product.instaCoinsPerHour else product.coinsPerHour
    }

    return when (CraftFlipScript.sortMode) {
      1 -> filtered.maxByOrNull { it.margin }
      2 -> filtered.maxByOrNull { it.margin * coinsMetric(it) }
      else -> filtered.maxByOrNull { coinsMetric(it) }
    }
  }

  fun findProduct(productId: String): FlipProduct? {
    return products.firstOrNull { it.id == productId }
  }

  fun updateData(force: Boolean = false) {
    if (!force && System.currentTimeMillis() - lastUpdate < CraftFlipScript.updateFlipsInterval) {
      return
    }

    Multithreading.runAsync {
      val body = WebUtils.fetchBody(API_URL)
      val response = CraftFlipAddon.gson.fromJson(body, Array<FlipProduct>::class.java)

      products = response ?: emptyArray()

      lastUpdate = System.currentTimeMillis()
      logger.info("Fetched craft flipping data and found ${products.size} flip(s).")
    }
  }

  data class FlipProduct(
    @SerializedName("bottleneck")
    val bottleneck: Double = 0.0,

    @SerializedName("buyprice")
    val buyPrice: Double = 0.0,

    @SerializedName("buysperhour")
    val buysPerHour: Double = 0.0,

    @SerializedName("coinsperhour")
    val coinsPerHour: Double = 0.0,

    @SerializedName("craftcost")
    val craftCost: Double = 0.0,

    @SerializedName("icon")
    val icon: String = "",

    @SerializedName("id")
    val id: String = "",

    @SerializedName("instacoinsperhour")
    val instaCoinsPerHour: Double = 0.0,

    @SerializedName("margin")
    val margin: Double = 0.0,

    @SerializedName("name")
    val name: String = "",
  )

  private const val API_URL = "https://api.skyblock.bz/api/crafts"

}
