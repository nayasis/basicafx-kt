package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.javafx.preloader.DefaultSplash
import io.github.nayasis.kotlin.javafx.preloader.DefaultPreloader
import tornadofx.*

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
//        runSync {
//            sleep(1000)
////            NPreloader.notifyError("test", RuntimeException("test error"))
//            throw RuntimeException("test error")
//        }
//        BasePreloader.close()
    }

}