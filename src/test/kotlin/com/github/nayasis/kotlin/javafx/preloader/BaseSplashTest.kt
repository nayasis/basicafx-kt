package com.github.nayasis.kotlin.javafx.preloader

import com.github.nayasis.kotlin.javafx.spring.SpringFxApp
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.label
import tornadofx.launch
import tornadofx.vbox

fun main(args: Array<String>) {
    SpringFxApp.setPreloader(TestSplash::class)
    launch<BaseSplashTest>(*args)

}

class BaseSplashTest: App(MainView::class)

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
}

class TestSplash: BaseSplash(527,297,"/splash/splash.jpg")