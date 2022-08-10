package com.github.nayasis.kotlin.javafx.misc

import com.github.nayasis.kotlin.javafx.common.TestFxJunitAppRunner
import javafx.application.Application
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

class ImagesTest: TestFxJunitAppRunner() {

    @Test
    fun isAcceptable() {
        assertEquals(false, Images.isAcceptable(null) )
    }

    @Test
    fun download() {
        assertNotNull(Images.toImage("https://ac2-p2.namu.la/20220728sac2/1ce40cda8132e008a62514d3b9b0bea85b42b58e91183a8dcac5e3cc89f8eb87.png"))
        assertNotNull(Images.toImage("https://dcimg1.dcinside.com/viewimage.php?id=2ca9d52aecd72ab56baddfb005&amp;no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22a7621b9279e7fc9c67a5cbaab9bec61a057088ceb5fc34ac2b0f71&amp;orgExt\\\" style=\\\"cursor:pointer;\\\" onclick=\\\"javascript:imgPop('https://image.dcinside.com/viewimagePop.php?no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22e2374d622871e86f185a5a79072289598aa8b22b2cc27c','image','fullscreen=yes,scrollbars=yes,resizable=no,menubar=no,toolbar=no,location=no,status=no');\\\" onerror=\\\"reload_img(this)\\\" alt=\\\"viewimage.php?id=2ca9d52aecd72ab56baddfb005&amp;no=24b0d769e1d32ca73feb86fa11d02831b7cca0f2855e21730c724febbe0b6d522f09a5b95c4115cf4010f9be22a7621b9279e7fc9c67a5cbaab9bec61a057088ceb5fc34ac2b0f71"))
    }
}