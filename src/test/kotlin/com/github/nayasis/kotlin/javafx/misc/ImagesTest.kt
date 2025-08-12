package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.string.toResource
import com.github.nayasis.kotlin.javafx.common.createTempFile
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import java.awt.AlphaComposite
import java.io.IOException
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

@ExtendWith(ApplicationExtension::class)
class ImagesTest {

    @Test
    fun basic() {

        assertThrows<IOException> {
            "https://ac-p2.namu.la/20230429sac/de5cef79e0b5e5c3f1d0fad7ea7f57b1ef615fa3620091d4fd39a1e82e132c90.jpg?expires=1683836424&key=AKiVXiYnEvwkfCK18G9nHA&type=orig".toImage()
        }

        assertNotNull("https://www.suruga-ya.jp/database/pics_light/game/186017828.jpg".toImage())

        val file = Files.createTempFile("basicafx-imagetest-", ".png").toFile().apply { deleteOnExit() }

        "https://www.suruga-ya.jp/database/pics_light/game/186017828.jpg".toImage().write(file)

        assertTrue(file.exists())

    }

    @Test
    fun writePng() {

        val fullWidth  = 1920
        val fullHeight = 1080

        val image = "image/test.png".toResource()?.toImage()
            ?.resize(fullWidth,fullHeight)
            ?.toBufferedImage()
            ?: throw IOException("Image not found or could not be loaded.")

        val newWidth = getWidth("4:3", fullHeight)

        val x = (fullWidth - newWidth) / 2

        image.createGraphics().run {
            composite = AlphaComposite.Clear
            fillRect(x,0,newWidth,fullHeight)
            dispose()
        }

        val file = createTempFile("basicafx-image-test-", ".png")

        ImageIO.write(image, "png",file.toFile())

        assertTrue { file.exists() }

    }

    fun getWidth(ratio: String, baseHeight: Int): Int {
        val (width,height) = ratio.split(":").map { it.toInt() }.let { it[0] to it[1] }
        return (1.0 * width * baseHeight / height).toInt()
    }

    @Test
    fun rotate() {
        val file = createTempFile("basicafx-image-rotated-", ".png", deleteOnExist = false)
        val image = "image/test.png".toResource()?.toImage()?.rotate(90.0)
        image?.write(file)

        assertTrue { file.exists() }
    }

}