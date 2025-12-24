package io.github.nayasis.kotlin.javafx.misc

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.Header
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.message.BasicHeader
import org.apache.hc.core5.util.Timeout
import java.io.Closeable
import java.io.IOException
import java.net.URL

class WebBrowser(
    timeout: Int = 30_000,
    userAgent: String? = null,
): Closeable {

    private val httpClient = HttpClients.custom()
        .setUserAgent(userAgent ?: UserAgents.pick())
        .setDefaultHeaders(listOf(
            BasicHeader("Accept"         , "image/webp,image/apng,image/*,*/*;q=0.8"),
            BasicHeader("Accept-Language", "ko-KR,ko;q=0.9,ja-JP,ja;q=0.8,en-US;q=0.7,en;q=0.6"),
            BasicHeader("Accept-Encoding", "gzip, deflate, br"),
            BasicHeader("Cache-Control"  , "no-cache"),
            BasicHeader("Pragma"         , "no-cache"),
            BasicHeader("Sec-Fetch-Dest" , "image"),
            BasicHeader("Sec-Fetch-Mode" , "no-cors"),
            BasicHeader("Sec-Fetch-Site" , "cross-site"),
        ))
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout.toLong()))
                .setResponseTimeout(Timeout.ofMilliseconds(timeout.toLong()))
                .build()
        )
        .build()

    /**
     * Downloads content from a URI with custom headers.
     *
     * @param url the URI to download from
     * @param headers additional headers to include
     * @return byte array of the downloaded content
     * @throws IOException if the download fails
     */
    fun downloadContent(url: URL, headers: List<Header>? = null): ByteArray {
        val httpGet = HttpGet(url.toURI())
        headers?.forEach { httpGet.addHeader(it) }
        return httpClient.execute(httpGet) { response ->
            if (response.code in 200..<300) {
                EntityUtils.toByteArray(response.entity)
            } else {
                throw IOException("HTTP error: ${response.code} ${response.reasonPhrase}")
            }
        }
    }

    override fun close() {
        runCatching { httpClient.close() }
    }
}

