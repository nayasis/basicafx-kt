package io.github.nayasis.kotlin.javafx.preloader

import io.github.nayasis.kotlin.basica.core.string.toUri
import io.github.nayasis.kotlin.javafx.control.basic.bottomAnchor
import io.github.nayasis.kotlin.javafx.control.basic.leftAnchor
import io.github.nayasis.kotlin.javafx.control.basic.rightAnchor
import io.github.nayasis.kotlin.javafx.stage.addMoveHandler
import io.github.nayasis.kotlin.javafx.stage.loadDefaultIcon
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * Default splash view for preloader
 *
 * ```
 * class Splash: DefaultSplash(500,300,"/view/splash/splash.jpg")
 *
 * fun main(args: Array<String>) {
 *   DefaultPreloader.set(Splash::class)
 * }
 * ```
 */
open class DefaultSplash(
    width: Int,
    height: Int,
    imagePath: String? = null,
    val showProgressBar: Boolean = true,
): DefaultPreloader() {

    private val progressBar = ProgressBar().apply {
        maxWidth  = Double.MAX_VALUE
        progress  = 0.0
        isVisible = false
    }

    private val label = Label().apply { ellipsisString = "..." }

    private val root = AnchorPane().apply {
        id = "splash"
        prefWidth  = width.toDouble()
        prefHeight = height.toDouble()
        children.add(VBox(progressBar, label).apply {
            alignment    = Pos.CENTER_RIGHT
            leftAnchor   = 0.0
            rightAnchor  = 0.0
            bottomAnchor = 0.0
        })
        imagePath?.toUri()?.let { style = "-fx-background-image: url('$it')" }
    }

    override fun onStart(stage: Stage) {
        stage.apply {
            initStyle(StageStyle.TRANSPARENT)
            scene = Scene(root).apply { stylesheets.add("/basicafx/css/splash.css") }
            isAlwaysOnTop = true
            loadDefaultIcon()
            addMoveHandler(root)
            show()
        }
    }

    override fun onProgress(notificator: ProgressNotificator) {
        with(notificator) {
            message?.let { label.text = it }
            if(showProgressBar && progress != null && progress.toDouble() > 0) {
                progressBar.isVisible = true
                progressBar.progress = progress.toDouble()
            }
        }
    }

}
