@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.io.isFile
import com.github.nayasis.kotlin.basica.core.io.makeDir
import com.github.nayasis.kotlin.basica.core.string.*
import com.github.nayasis.kotlin.basica.core.url.toFile
import com.microsoft.playwright.Page
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition.CENTER
import javafx.scene.layout.BackgroundRepeat.ROUND
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.BackgroundSize.AUTO
import net.sf.image4j.codec.ico.ICODecoder
import java.awt.AlphaComposite
import java.awt.RenderingHints.*
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_CUSTOM
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.*
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

private var webBrowser: WebBrowser? = null

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

/**
 * convert to buffered image
 *
 * @param timeout timeout to wait response
 * @return BufferedImage
 */
fun URL.toBufferedImage(
    timeout: Double = 30_000.0,
): BufferedImage {
    return when {
        this.protocol == "file" -> this.toFile().toBufferedImage()
        else -> {
            if (webBrowser == null) {
                webBrowser = WebBrowser()
                Runtime.getRuntime().addShutdownHook(Thread {
                    webBrowser?.close()
                    webBrowser = null
                })
            }
            webBrowser!!.withPage { page ->
                page.navigate(this.toString(),
                    Page.NavigateOptions().apply {
                        this.timeout = timeout
                    }
                ).body().toBufferedImage()
            }
        }
    }
}

fun File.toImage(): Image {
    if(this.isFile) {
        return Image("${toURI()}")
    } else {
        throw IOException("only file could be converted to image. ($this)")
    }
}

fun Path.toImage(): Image {
    if(this.isFile()) {
        return Image("${toFile().toURI()}")
    } else {
        throw IOException("only file could be converted to image. ($this)")
    }
}

/**
 * convert to image
 *
 * @param timeout timeout to wait response
 * @return Image
 */
fun URL.toImage(
    timeout: Double = 30_000.0,
): Image {
    return this.toBufferedImage(timeout).toImage()
}


/**
 * convert to image
 *
 * @param timeout timeout to wait response
 * @return Image
 */
fun String.toImage(
    timeout: Double = 30_000.0,
): Image {
    return runCatching { this.let { url -> when {
        url.find("^http(s?)://".toRegex()) -> url.toUrl().toImage(timeout)
        url.find("^data:.*?;base64,".toRegex()) -> {
            val encoded = url.replaceFirst("^data:.*?;base64,".toRegex(), "")
            encoded.decodeBase64<ByteArray>().toImage()
        }
        url.toFile().exists() -> url.toFile().toImage()
        else -> throw IOException("not a valid URL or file path.")
    }}}.getOrElse {
        throw IOException("can not be converted to image. ($this)", it)
    }
}

fun ImageIcon.toBufferedImage(): BufferedImage {
    return BufferedImage(iconWidth, iconHeight, TYPE_INT_ARGB).also {
        it.createGraphics().apply {
            setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC)
            setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY)
            setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
            setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY)
            setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP)
        }.let { graphics ->
            paintIcon(null, graphics,0,0)
            graphics.dispose()
        }
    }
}

fun ImageIcon.toImage(): Image {
    return this.toBufferedImage().toImage()
}

fun ImageIcon.toImageOrNull(): Image? {
    return runCatching { toImage() }.getOrNull()
}

fun ByteArray.toBufferedImage(): BufferedImage {
    return this.let {
        if(it.isEmpty()) {
            BufferedImage(0,0, TYPE_INT_ARGB)
        } else {
            ByteArrayInputStream(it).use { bis ->
                ImageIO.read(bis)
            } ?: throw IOException("can not convert byte array to BufferedImage. (size=${it.size})")
        }
    }
}

fun ByteArray.toImage(): Image {
    return this.toBufferedImage().toImage()
}

fun BufferedImage.toImage(): Image {
    return this.let { SwingFXUtils.toFXImage(it,null) }
}

fun Image.toBufferedImage(): BufferedImage {
    return this.let { SwingFXUtils.fromFXImage(it, null) }
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

fun File.toBufferedImage(): BufferedImage {
    return this.toImage().toBufferedImage()
}

fun File.toJpgBinary(): ByteArray {
    return this.toImage().toJpgBinary()
}

fun Path.toBufferedImage(): BufferedImage {
    return this.toImage().toBufferedImage()
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
    return this.toImage().toJpgBinary().toBufferedImage().let { image ->
        ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "jpg", output)
            "data:image/jpeg;base64,${output.toByteArray().encodeBase64().replace(CARRIAGE_RETURN, "")}"
        }
    }
}

fun Image.toWritableImage(): WritableImage {
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

fun Image.copy(): Image {
    return toBufferedImage().copy().toImage()
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

