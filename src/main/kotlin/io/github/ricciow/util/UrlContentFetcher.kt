package io.github.ricciow.util

import kotlinx.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets

object UrlContentFetcher {
    /**
     * Fetches the raw text content from a given URL.
     *
     * @param urlString The complete URL from which to fetch content.
     * @return The content of the URL as a String.
     */
    fun fetchContentFromURL(urlString: String): String? {
        return try {
            URI(urlString).toURL().readText(StandardCharsets.UTF_8)
        } catch (e: URISyntaxException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}