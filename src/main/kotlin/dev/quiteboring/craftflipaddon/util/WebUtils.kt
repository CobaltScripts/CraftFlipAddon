package dev.quiteboring.craftflipaddon.util

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object WebUtils {

  private val client = HttpClient.newHttpClient()

  fun fetchBody(url: String): String {
    val request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .GET()
      .build()

    return client.send(request, HttpResponse.BodyHandlers.ofString()).body()
  }

}
