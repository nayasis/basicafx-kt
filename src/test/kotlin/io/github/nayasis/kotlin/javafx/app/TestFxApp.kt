package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.javafx.preloader.DefaultPreloader
import io.github.nayasis.kotlin.javafx.preloader.DefaultSplash
import tornadofx.View
import tornadofx.label
import tornadofx.launch
import tornadofx.textfield
import tornadofx.vbox

fun main() {
    DefaultPreloader.set(TestSplash::class)
    launch<TestFxApp>()
}

class TestSplash: DefaultSplash(527,297,"/image/test.png")

class TestFxApp: FxApp(TestFxView::class)

class TestFxView: View("test view") {
    override val root = vbox {
        label("test label")
        textfield {}
    }

    override fun onBeforeShow() {
        throw RuntimeException("test error")
//        runAwait {
//            sleep(1000)
////            NPreloader.notifyError("test", RuntimeException("test error"))
//            throw RuntimeException("test error")
//        }
//        DefaultPreloader.close()
    }

}