package io.github.ricciow.util

import java.net.URI
import java.nio.charset.StandardCharsets

object UrlContentFetcher {
    /**
     * Fetches the raw text content from a given URL.
     *
     * @param urlString The complete URL from which to fetch content.
     * @return The content of the URL as a String.
     */
    fun fetchContentFromURL(urlString: String): String {
       return URI(urlString).toURL().readText(StandardCharsets.UTF_8)
    }
}