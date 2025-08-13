package io.github.nayasis.kotlin.javafx.preloader

import io.github.nayasis.kotlin.basica.core.string.toUri
import io.github.nayasis.kotlin.javafx.control.basic.bottomAnchor
import io.github.nayasis.kotlin.javafx.control.basic.leftAnchor
import io.github.nayasis.kotlin.javafx.control.basic.rightAnchor
import io.github.nayasis.kotlin.javafx.stage.addMoveHandler
import io.github.nayasis.kotlin.javafx.stage.loadDefaultIcon
import io.github.nayasis.kotlin.tornadofx.extension.toScene
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.*

/**
 * base splash view for preloader
 *
 * ```
 * class Splash: BaseSplash(500,300,"/view/splash/splash.jpg")
 *
 * fun main(args: Array<String>) {
 *   SpringFxApp.setPreloader(Splash::class)
 *   launch<Simplelauncher>(*args)
 * }
 * ```
 */
open class BaseSplash(
    width: Int,
    height: Int,
    imageUrl: String? = null,
): BasePreloader() {

    private val view = SplashView(width,height,imageUrl)

    override fun onStart(stage: Stage) {
        stage.apply {
            initStyle(StageStyle.TRANSPARENT)
            scene = view.toScene("/basicafx/css/splash.css")
            isAlwaysOnTop = true
            loadDefaultIcon()
            addMoveHandler(view.root)
            show()
        }
    }

    override fun onProgress(notificator: ProgressNotificator) {
        with(notificator) {
            message?.let { view.label.text = it }
            view.progressBar.progress = progress
        }
    }

}

class SplashView(
    width: Int,
    height: Int,
    imagePath: String?,
): View() {

    lateinit var progressBar: ProgressBar
    lateinit var label: Label

    override val root = anchorpane {
        id = "splash"
        prefWidth  = width.toDouble()
        prefHeight = height.toDouble()
        vbox {
            alignment = Pos.CENTER_RIGHT
            progressBar = progressbar {
                maxWidth = Double.MAX_VALUE
                progress = 0.0
            }
            label = label { ellipsisString = "..." }
            leftAnchor = 0.0
            rightAnchor = 0.0
            bottomAnchor = 0.0
        }
        imagePath?.toUri()?.let { uri ->
            style(true) {
                backgroundImage.add(uri)
            }
        }
    }

}