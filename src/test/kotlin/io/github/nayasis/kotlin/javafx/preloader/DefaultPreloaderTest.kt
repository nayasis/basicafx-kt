package io.github.nayasis.kotlin.javafx.preloader

import io.github.nayasis.kotlin.javafx.misc.runAwait
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.label
import tornadofx.launch
import tornadofx.vbox
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    DefaultPreloader.set(TestSplash::class)
    launch<DefaultPreloaderTest>(*args)

}

class DefaultPreloaderTest: App(MainView::class)

class MainView: View("Hello world!") {

    var count = SimpleIntegerProperty(0)

    override val root = vbox() {
        button( "click" ) {
            action {
                count.set( count.get() + 1 )
            }
        }
        label( "Hello world" ) {
            bind( count )
        }
    }

    override fun onBeforeShow() {
        runAwait {
            sleep(1000)
        }
        DefaultPreloader.close()
    }
}

class TestSplash: DefaultSplash(527,297,"/splash/splash.jpg")