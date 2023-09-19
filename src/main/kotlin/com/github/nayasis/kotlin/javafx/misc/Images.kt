@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.io.isFile
import com.github.nayasis.kotlin.basica.core.io.makeDir
import com.github.nayasis.kotlin.basica.core.string.decodeBase64
import com.github.nayasis.kotlin.basica.core.string.encodeBase64
import com.github.nayasis.kotlin.basica.core.string.find
import com.github.nayasis.kotlin.basica.core.string.toFile
import com.github.nayasis.kotlin.basica.core.string.toPath
import com.github.nayasis.kotlin.basica.core.string.toUrl
import com.github.nayasis.kotlin.basica.core.url.toFile
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition.CENTER
import javafx.scene.layout.BackgroundRepeat.ROUND
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.BackgroundSize.AUTO
import mu.KotlinLogging
import net.sf.image4j.codec.ico.ICODecoder
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_CUSTOM
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.Math.toRadians
import java.net.URL
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private val logger = KotlinLogging.logger {}

private val CARRIAGE_RETURN = "[\n\r]".toRegex()

const val DEFAULT_USER_AGENT  = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36"

fun Image?.isValid(): Boolean {
    return this != null && this.width > 0 && this.height > 0
}

fun Image.cropTop(pixel: Int): Image {
    val (width, height) = width.toInt() to height.toInt()
    return if( pixel > height ) this else
        WritableImage(pixelReader, 0, pixel, width, height - pixel)
}

fun Image.cropBottom(pixel: Int): Image {
    val (width, height) = width.toInt() to height.toInt()
    return if( pixel > height ) this else
        WritableImage(pixelReader,0,0, width, height - pixel)
}

fun Image.cropLeft(pixel: Int): Image {
    val (width, height) = width.toInt() to height.toInt()
    return if( pixel > width ) this else
        WritableImage(pixelReader,pixel,0, width - pixel, height)
}

fun Image.cropRight(pixel: Int): Image {
    val (width, height) = width.toInt() to height.toInt()
    return if( pixel > width ) this else
        WritableImage(pixelReader,0,0, width - pixel, height)
}

fun Image.toBufferedImage(another: BufferedImage? = null): BufferedImage {
    return this.let { SwingFXUtils.fromFXImage(it,another) }
}

fun ByteArray.toBufferedImage(): BufferedImage {
    return this.let {
        ByteArrayInputStream(it).use { bis ->
            ImageIO.read(bis)
        }
    }
}

fun File.toBufferedImage(): BufferedImage {
    return this.toImage().toBufferedImage()
}

fun Path.toBufferedImage(): BufferedImage {
    return this.toImage().toBufferedImage()
}

/**
 * convert to buffered image
 *
 * @param connectionTimeout         timeout to establish a connection with a remote host
 * @param socketTimeout             timeout to wait for response data
 * @param connectionRequestTimeout  timeout pulling out of a connection pool
 * @return BufferedImage
 */
fun URL.toBufferedImage(
    connectionTimeout: Int = 1 * 1000,
    socketTimeout: Int = 3 * 1000,
    connectionRequestTimeout: Int = 1 * 1000,
): BufferedImage {
    return when {
        this.protocol == "file" -> this.toFile().toBufferedImage()
        else -> httpClient.use{ it.execute(HttpGet("$this").apply {
            setHeader("User-Agent", DEFAULT_USER_AGENT)
            config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout) // 원격 호스트와의 연결을 설정하는 시간
                .setSocketTimeout(socketTimeout) // 데이터를 기다리는 시간
                .setConnectionRequestTimeout(connectionRequestTimeout) // 커넥션 풀로부터 꺼내올 때의 타임아웃
                .build()
        }).use { response ->
            ImageIO.read(response.entity.content)
        }}
    }
}

fun File.toImage(): Image {
    if(this.isFile) {
        return Image("${toURI()}")
    } else {
        throw IOException("only file could be converted to image. ($this)")
    }
}

fun File.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

fun Path.toImage(): Image {
    if(this.isFile()) {
        return Image("${toFile().toURI()}")
    } else {
        throw IOException("only file could be converted to image. ($this)")
    }
}

fun Path.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

/**
 * convert to image
 *
 * @param connectionTimeout         timeout to establish a connection with a remote host
 * @param socketTimeout             timeout to wait for response data
 * @param connectionRequestTimeout  timeout pulling out of a connection pool
 * @return Image
 */
fun URL.toImage(
    connectionTimeout: Int        = 1 * 1000,
    socketTimeout: Int            = 3 * 1000,
    connectionRequestTimeout: Int = 1 * 1000,
): Image {
    return this.toBufferedImage(connectionTimeout, socketTimeout, connectionRequestTimeout).toImage()
}

fun URL.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

/**
 * convert to image
 *
 * @param connectionTimeout         timeout to establish a connection with a remote host
 * @param socketTimeout             timeout to wait for response data
 * @param connectionRequestTimeout  timeout pulling out of a connection pool
 * @return Image
 */
fun String.toImage(
    connectionTimeout: Int        = 1 * 1000,
    socketTimeout: Int            = 3 * 1000,
    connectionRequestTimeout: Int = 1 * 1000,
): Image {
    return this.let { url -> when {
        url.find("^http(s?)://".toRegex()) -> url.toUrl().toImage()
        url.find("^data:.*?;base64,".toRegex()) -> {
            val encoded = url.replaceFirst("^data:.*?;base64,".toRegex(), "")
            encoded.decodeBase64<ByteArray>()!!.toImage()
        }
        url.toFile().exists() -> url.toFile().toImage()
        else -> throw IOException("can not be converted to image. ($this)")
    }}
}

fun String.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

fun ImageIcon.toImage(): Image {
    return this.image.let { (it as BufferedImage).toImage() }
}

fun ImageIcon.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

fun ByteArray.toImage(another: WritableImage? = null): Image {
    return this.toBufferedImage().toImage(another)
}

fun ByteArray.toImageOrNull(another: WritableImage? = null): Image? {
    return runCatching { this.toImage(another) }.getOrNull()
}

fun BufferedImage.toImage(another: WritableImage? = null): Image {
    return this.let { SwingFXUtils.toFXImage(it,another) }
}

fun BufferedImage.toImageOrNull(another: WritableImage? = null): Image? {
    return runCatching { this.toImage(another) }.getOrNull()
}

fun Image.toBinary(format: String = "jpg"): ByteArray {
    return this.toBufferedImage().toBinary(format)
}

fun BufferedImage.toBinary(format: String = "jpg"): ByteArray {
    return ByteArrayOutputStream().use { bos ->
        ImageIO.write(this,format,bos)
        bos.toByteArray()
    }
}

fun BufferedImage.removeAlpha(): BufferedImage {
    return this.let { src ->
        BufferedImage(src.width,src.height,BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().run {
                composite = AlphaComposite.Src
                drawImage(src,0,0,null)
                dispose()
            }
        }
    }
}

fun Image.toJpgBinary(): ByteArray {
    return this.toBufferedImage().toJpgBinary()
}

fun BufferedImage.toJpgBinary(): ByteArray {
    return this.removeAlpha().toBinary("jpg")
}

fun File.toJpgBinary(): ByteArray {
    return this.toImage().toJpgBinary()
}

fun Path.toJpgBinary(): ByteArray {
    return this.toImage().toJpgBinary()
}

fun File.toIconImage(): List<Image> {
    if( ! isFile) return emptyList()
    return try {
        this.inputStream().toIconImage()
    } catch (e: Exception) {
        val icon = FileSystemView.getFileSystemView().getSystemIcon(this) as ImageIcon
        listOfNotNull(icon.toImage())
    }
}

fun InputStream.toIconImage(): List<Image> {
    return this.use {
        ICODecoder.read(it).mapNotNull { it.toImage() }
    }
}

fun Image.toBackgroundImage(): BackgroundImage {
    val sizeProperty = BackgroundSize(AUTO, AUTO, true, true, true, false)
    return BackgroundImage(this, ROUND, ROUND, CENTER, sizeProperty)
}

fun String.toBackgroundImage(): BackgroundImage {
    return Image(this, 0.0, 0.0, false, true, true).toBackgroundImage()
}

fun String.toBase64Image(): String {
    return this.toImage().let { it.toJpgBinary().toBufferedImage() }.let { image ->
        ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "jpg", output)
            "data:image/jpeg;base64,${output.toByteArray().encodeBase64().replace(CARRIAGE_RETURN, "")}"
        }
    }
}

fun Image.copy(): WritableImage {
    val width  = width.toInt()
    val height = height.toInt()
    return WritableImage(width, height).also {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixelReader.getColor(x, y)
                it.pixelWriter.setColor(x, y, color)
            }
        }
    }
}

fun BufferedImage.copy(): BufferedImage {
    return this.let { src ->
        BufferedImage(src.width,src.height,src.type).apply {
            graphics.run {
                drawImage(src,0,0,null)
                dispose()
            }
        }
    }
}

fun BufferedImage.resize(width: Int, height: Int): BufferedImage {
    return this.let { image ->
        BufferedImage(width, height, getType(image)).apply {
            createGraphics().run {
                drawImage(image,0,0,width,height,null)
                dispose()
            }
        }
    }
}

fun BufferedImage.resize(maxPixel: Int): BufferedImage {
    return this.let { image ->
        var w = image.width.coerceAtLeast(1).toDouble()
        var h = image.height.coerceAtLeast(1).toDouble()
        when {
            w < maxPixel && h < maxPixel -> image
            w > h -> {
                h = h * maxPixel / width
                w = maxPixel.toDouble()
                image.resize(w.toInt(),h.toInt())
            }
            else -> {
                w = w * maxPixel / height
                h = maxPixel.toDouble()
                image.resize(w.toInt(),h.toInt())
            }
        }
    }
}

fun Image.resize(width: Int, height: Int): Image {
    return this.toBufferedImage().resize(width,height).toImage()
}

fun Image.resize(maxPixel: Int): Image {
    return this.toBufferedImage().resize(maxPixel).toImage()
}

fun BufferedImage.rotate(angle: Double): BufferedImage {
    return this.let { image ->

        val radian = toRadians(angle)
        val sin    = abs(sin(radian))
        val cos    = abs(cos(radian))

        val srcWidth  = image.width
        val srcHeight = image.height
        val trgWidth  = floor(srcWidth * cos + srcHeight * sin).toInt()
        val newHeight = floor(srcHeight * cos + srcWidth * sin).toInt()

        logger.trace { """
            >> rotate image
               - src : $srcWidth x $srcHeight
               - trg : $trgWidth x $newHeight
        """.trimIndent() }

        BufferedImage(trgWidth, newHeight, getType(image)).apply {
            createGraphics().run {
                translate((trgWidth - srcWidth) / 2.0, (newHeight - srcHeight) / 2.0)
                rotate(radian, srcWidth.toDouble() / 2, srcHeight.toDouble() / 2)
                drawRenderedImage(image, null)
                dispose()
            }
        }
    }
}

fun Image.rotate(angle: Double): Image {
    return this.toBufferedImage().rotate(angle).toImage()
}

fun Image.write(path: String) {
    toBufferedImage().write(path)
}

fun Image.write(path: Path) {
    toBufferedImage().write(path)
}

fun Image.write(file: File) {
    toBufferedImage().write(file)
}

fun BufferedImage.write(path: String) {
    this.write(path.toPath())
}

fun BufferedImage.write(path: Path) {
    this.write(path.toFile())
}

fun BufferedImage.write(file: File) {
    this.let { image ->
        val extension = file.extension
        if(extension.equals("jpg",true))
            image.removeAlpha()
        file.toPath().toAbsolutePath().parent.makeDir()
        ImageIO.write(image,extension,file)
    }
}

private fun getType(image: BufferedImage?): Int {
    return when(image?.type) {
        null -> TYPE_CUSTOM
        0    -> TYPE_INT_ARGB
        else -> image.type
    }
}

private val sslSocket = SSLConnectionSocketFactory(SSLContexts.custom()
    .loadTrustMaterial(null) { _, _ -> true }
    .build(), NoopHostnameVerifier.INSTANCE)

private val httpClient: CloseableHttpClient
    get() {
        return HttpClients.custom().apply {
            setSSLSocketFactory(sslSocket)
        }.build()
    }

