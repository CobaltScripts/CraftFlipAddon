package dev.quiteboring.craftflipaddon.api

import com.google.gson.annotations.SerializedName
import dev.quiteboring.craftflipaddon.CraftFlipAddon
import dev.quiteboring.craftflipaddon.CraftFlipScript
import dev.quiteboring.craftflipaddon.util.WebUtils
import dev.quiteboring.craftflipaddon.util.helper.OrderMode
import org.cobalt.util.scheduling.Multithreading
import org.slf4j.LoggerFactory

object BazaarData {

  private val logger = LoggerFactory.getLogger(this::class.java)
  private var lastUpdate = 0L

  private var products: Array<BazaarProduct> = emptyArray()

  fun getProduct(productId: String): BazaarProduct? {
    return products.find { it.productId == productId }
  }

  fun isOutdatedList(productId: String, price: Double, orderMode: OrderMode): Boolean {
    updateData()

    val product = getProduct(productId) ?: return true

    val topPrice = when (orderMode) {
      OrderMode.BUY_ORDER -> product.buySummary.firstOrNull()?.pricePerUnit
        ?: return false

      OrderMode.SELL_ORDER -> product.sellSummary.firstOrNull()?.pricePerUnit
        ?: return false
    }

    return when (orderMode) {
      OrderMode.BUY_ORDER -> price < topPrice
      OrderMode.SELL_ORDER -> price > topPrice
    }
  }

  fun updateData() {
    if (System.currentTimeMillis() - lastUpdate < CraftFlipScript.updateBazaarDataInterval) {
      return
    }

    Multithreading.runAsync {
      val body = WebUtils.fetchBody(API_URL)
      val response = CraftFlipAddon.gson.fromJson(body, BazaarResponse::class.java)

      products = response.products?.values?.toTypedArray() ?: emptyArray()

      lastUpdate = System.currentTimeMillis()
      logger.info("Fetched bazaar data and found ${products.size} item(s).")
    }
  }

  data class BazaarResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("lastUpdated")
    val lastUpdated: Long = 0,

    @SerializedName("products")
    val products: Map<String, BazaarProduct>? = null,
  )

  data class BazaarProduct(
    @SerializedName("product_id")
    val productId: String = "",

    @SerializedName("sell_summary")
    val sellSummary: List<SummaryEntry> = emptyList(),

    @SerializedName("buy_summary")
    val buySummary: List<SummaryEntry> = emptyList(),
  )

  data class SummaryEntry(
    @SerializedName("amount")
    val amount: Int = 0,

    @SerializedName("pricePerUnit")
    val pricePerUnit: Double = 0.0,

    @SerializedName("orders")
    val orders: Int = 0,
  )

  private const val API_URL = "https://api.hypixel.net/v2/skyblock/bazaar"

}
