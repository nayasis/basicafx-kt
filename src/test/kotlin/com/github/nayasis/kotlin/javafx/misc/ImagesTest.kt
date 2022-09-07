package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.basica.core.string.toFile
import com.github.nayasis.kotlin.basica.core.string.toResource
import com.github.nayasis.kotlin.javafx.test.JavaFxJunitRunner
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.AlphaComposite
import javax.imageio.ImageIO

private val logger = KotlinLogging.logger {  }

@Disabled
class ImagesTest {

    @Test
    fun download() {
        JavaFxJunitRunner {
            assertNotNull("https://www.myabandonware.com/media/screenshots/b/blue-forest-story-kaze-no-fuin-mco/blue-forest-story-kaze-no-fuin_9.png".toImage())
//            assertNotNull("https://ac2-p2.namu.la/20220728sac2/1ce40cda8132e008a62514d3b9b0bea85b42b58e91183a8dcac5e3cc89f8eb87.png".toImage())
//            assertNotNull("https://dcimg1.dcinside.com/viewimage.php?id=2ca9d52aecd72ab56baddfb005&amp;no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22a7621b9279e7fc9c67a5cbaab9bec61a057088ceb5fc34ac2b0f71&amp;orgExt\\\" style=\\\"cursor:pointer;\\\" onclick=\\\"javascript:imgPop('https://image.dcinside.com/viewimagePop.php?no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22e2374d622871e86f185a5a79072289598aa8b22b2cc27c','image','fullscreen=yes,scrollbars=yes,resizable=no,menubar=no,toolbar=no,location=no,status=no');\\\" onerror=\\\"reload_img(this)\\\" alt=\\\"viewimage.php?id=2ca9d52aecd72ab56baddfb005&amp;no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22a7621b9279e7fc9c67a5cbaab9bec61a057088ceb5fc34ac2b0f71".toImage())
//            assertNotNull("http://www.getchu.com/brandnew/1150459/c1150459table3.jpg".toImage()) // forbiddn
//            assertNotNull("https://ac-p2.namu.la/20210625/1748e18670f551eb3fac3e24c94a82901fd20f2d2a996c3673d48047398944f5.png".toImage())
//            assertNotNull("http://www.getchu.com/campaign/lump2022summer/img/pop_02.jpg".toImage())
//            assertNotNull("http://pds11.egloos.com/pds/200810/07/43/d0034443_48eb36738f77e.jpg".toImage())
        }
    }

    val fullWidth  = 1920
    val fullHeight = 1080

    @Test
    fun writePng() {
        JavaFxJunitRunner {

            val image = "image/test.png".toResource().toImage()
                .resize(fullWidth,fullHeight)
                .toBufferedImage()
                ?: return@JavaFxJunitRunner

            val newWidth = getWidth("4:3", fullHeight)

            val x = (fullWidth - newWidth) / 2

            image.createGraphics().run {
                composite = AlphaComposite.Clear
                fillRect(x,0,newWidth,fullHeight)
                dispose()
            }

            ImageIO.write(image, "png","test.png".toFile())
        }
    }

    fun getWidth(ratio: String, baseHeight: Int): Int {
        val (width,height) = ratio.split(":").map { it.toInt() }.let { it[0] to it[1] }
        return (1.0 * width * baseHeight / height).toInt()
    }

    @Test
    fun rotate() {
        JavaFxJunitRunner {
            val image = "image/test.png".toResource().toImage().rotate(90.0)
            image.write("test.png".toFile())
        }
    }

}