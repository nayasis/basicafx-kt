@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.io.isFile
import com.github.nayasis.kotlin.basica.core.io.makeDir
import com.github.nayasis.kotlin.basica.core.string.decodeBase64
import com.github.nayasis.kotlin.basica.core.string.encodeBase64
import com.github.nayasis.kotlin.basica.core.string.find
import com.github.nayasis.kotlin.basica.core.string.toFile
import com.github.nayasis.kotlin.basica.core.string.toUrl
import com.github.nayasis.kotlin.basica.core.url.toFile
import com.github.nayasis.kotlin.basica.etc.error
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
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
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

private val log = KotlinLogging.logger {}

private val CARRIAGE_RETURN = "[\n\r]".toRegex()

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

fun Image?.toBufferedImage(another: BufferedImage? = null): BufferedImage? {
    return this?.let { SwingFXUtils.fromFXImage(it,another) }
}

fun ByteArray?.toBufferedImage(): BufferedImage? {
    return this?.let {
        try {
            ByteArrayInputStream(it).use { bis ->
                ImageIO.read(bis)
            }
        } catch (e: Exception) {
            null
        }
    }
}

fun File?.toBufferedImage(): BufferedImage? {
    return this.toImage().toBufferedImage()
}

fun Path?.toBufferedImage(): BufferedImage? {
    return this.toImage().toBufferedImage()
}

fun URL?.toBufferedImage(): BufferedImage? {
    return when {
        this == null -> null
        this.protocol == "file" -> this.toFile().toBufferedImage()
        else -> httpClient.use{ it.execute(HttpGet("$this"))?.use { response ->
            try {
                ImageIO.read(response.entity.content)
            } catch (e: Exception) {
                log.error(e)
                null
            }
        }}
    }
}

fun File?.toImage(): Image? {
    return this?.let { if(it.isFile) Image("${it.toURI()}") else null }
}

fun Path?.toImage(): Image? {
    return this?.let { if(it.isFile()) Image("${it.toFile().toURI()}") else null }
}

fun URL?.toImage(): Image? {
    return when {
        this == null -> null
        this.protocol == "file" -> this.toFile().toImage()
        else -> httpClient.use{ it.execute(HttpGet("$this"))?.use { response ->
            try {
                ImageIO.read(response.entity.content).toImage()
            } catch (e: Exception) {
                log.error(e)
                null
            }
        }}
    }
}

fun String?.toImage(): Image? {
    return this?.let { url -> when {
        url.find("^http(s?)://".toRegex()) -> url.toUrl().toImage()
        url.find("^data:.*?;base64,".toRegex()) -> {
            val encoded = url.replaceFirst("^data:.*?;base64,".toRegex(), "")
            encoded.decodeBase64<ByteArray>().toImage()
        }
        url.toFile().exists() -> url.toFile().toImage()
        else -> null
    }}
}

fun ImageIcon?.toImage(): Image? {
    return this?.let {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (it as BufferedImage).toImage()
    }
}

fun ByteArray?.toImage(another: WritableImage? = null): Image? {
    return this.toBufferedImage().toImage(another)
}

fun BufferedImage?.toImage(another: WritableImage? = null): Image? {
    return this?.let { SwingFXUtils.toFXImage(it,another) }
}

fun Image?.toBinary(format: String = "jpg"): ByteArray {
    return this.toBufferedImage().toBinary(format)
}

fun BufferedImage?.toBinary(format: String = "jpg"): ByteArray {
    return this?.let { bimg ->
        ByteArrayOutputStream().use { bos ->
            ImageIO.write(bimg,format,bos)
            bos.toByteArray()
        }
    } ?: byteArrayOf()
}

fun BufferedImage?.removeAlpha(): BufferedImage? {
    return this?.let { src ->
        val newCanvas = BufferedImage(src.width,src.height,BufferedImage.TYPE_INT_RGB)
        newCanvas.createGraphics().run {
            composite = AlphaComposite.Src
            drawImage(src,0,0,null)
            dispose()
        }
        return newCanvas
    }
}

fun Image?.toJpgBinary(): ByteArray {
    return this.toBufferedImage().toJpgBinary()
}

fun BufferedImage?.toJpgBinary(): ByteArray {
    return this.removeAlpha().toBinary("jpg")
}

fun File?.toJpgBinary(): ByteArray {
    return this.toImage().toJpgBinary()
}

fun Path?.toJpgBinary(): ByteArray {
    return this.toImage().toJpgBinary()
}

fun File?.toIconImage(): List<Image> {
    if( this?.isFile != true ) return emptyList()
    return try {
        this.inputStream().toIconImage()
    } catch (e: Exception) {
        val icon = FileSystemView.getFileSystemView().getSystemIcon(this) as ImageIcon
        listOfNotNull(icon.toImage())
    }
}

fun InputStream?.toIconImage(): List<Image> {
    return this?.let { bis -> bis.use {
        ICODecoder.read(it).mapNotNull { it.toImage() }
    }} ?: emptyList()
}

fun Image?.toBackgroundImage(): BackgroundImage? {
    return this?.let { image ->
        val sizeProperty = BackgroundSize(AUTO, AUTO, true, true, true, false)
        return BackgroundImage(image, ROUND, ROUND, CENTER, sizeProperty)
    }
}

fun String?.toBackgroundImage(): BackgroundImage? {
    return this?.let { url ->
        Image("$url", 0.0, 0.0, false, true, true).toBackgroundImage()
    }
}

fun String?.toBase64Image() {
    this.toImage()?.let { it.toJpgBinary().toBufferedImage() }?.let { image ->
        val output = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", output)
        "data:image/jpeg;base64,${output.toByteArray().encodeBase64().replace(CARRIAGE_RETURN, "")}"
    }
}

fun Image?.copy(): WritableImage? {
    return this?.let { src ->
        val width  = src.width.toInt()
        val height = src.height.toInt()
        val pixelReader = src.pixelReader
        WritableImage(width, height).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color = pixelReader.getColor(x, y)
                    pixelWriter.setColor(x, y, color)
                }
            }
        }
    }
}

fun BufferedImage?.copy(): BufferedImage? {
    return this?.let { src ->
        BufferedImage(src.width,src.height,src.type).apply {
            graphics.run {
                drawImage(src,0,0,null)
                dispose()
            }
        }
    }
}

fun BufferedImage?.resize(width: Int, height: Int): BufferedImage? {
    return this?.let { image ->
        BufferedImage(width, height, getType(image)).apply {
            createGraphics().run {
                drawImage(image,0,0,width,height,null)
                dispose()
            }
        }
    }
}

fun BufferedImage?.resize(maxPixel: Int): BufferedImage? {
    return this?.let { image ->
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

fun Image?.resize(width: Int, height: Int): Image? {
    return this.toBufferedImage().resize(width,height).toImage()
}

fun Image?.resize(maxPixel: Int): Image? {
    return this.toBufferedImage().resize(maxPixel).toImage()
}

fun BufferedImage?.rotate(angle: Double): BufferedImage? {
    return this?.let { image ->

        val radian = toRadians(angle)
        val sin = abs(sin(radian))
        val cos = abs(cos(radian))

        val w = floor(image.width * cos + image.height * sin).toInt()
        val h = floor(image.height * cos + image.width * sin).toInt()

        log.trace { """
            >> rotate image
            - src : ${image.width} x ${image.height}
            - trg : ${w} x ${h}
        """.trimIndent() }

        BufferedImage(w, h, getType(image)).apply {
            createGraphics().run {
                translate((w - width) / 2.0, (h - height) / 2.0)
                rotate(radian, image.width.toDouble() / 2, image.height.toDouble() / 2)
                drawRenderedImage(image, null)
                dispose()
            }
        }
    }
}

fun Image?.rotate(angle: Double): Image? {
    return this.toBufferedImage().rotate(angle).toImage()
}

fun Image?.write(path: Path) {
    toBufferedImage().write(path)
}

fun Image?.write(file: File) {
    toBufferedImage().write(file)
}

fun BufferedImage?.write(path: Path) {
    this.write(path.toFile())
}

fun BufferedImage?.write(file: File) {
    this?.let { image ->
        val extension = file.extension
        if(extension.equals("jpg",true))
            image.removeAlpha()
        file.toPath().parent.makeDir()
        ImageIO.write(image,extension,file)
    }
}

private fun getType(image: BufferedImage?): Int {
    return if (image!!.type == 0) BufferedImage.TYPE_INT_ARGB else image.type
}

private val sslSocket = SSLConnectionSocketFactory(SSLContexts.custom()
    .loadTrustMaterial(null) { _, _ -> true }
    .build(), NoopHostnameVerifier.INSTANCE)

private val httpClient: CloseableHttpClient
    get() {
        return HttpClients.custom().setSSLSocketFactory(sslSocket).build()
    }

