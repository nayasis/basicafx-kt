package io.github.nayasis.kotlin.javafx.common

import io.github.nayasis.kotlin.javafx.app.FxView
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * minimal launcher for the manual demos in this source set.
 *
 * shows an [FxView] on the primary stage without the DI container and
 * environment that [io.github.nayasis.kotlin.javafx.app.FxApp] sets up.
 */
abstract class DemoApp(private val view: FxView, private val stylesheet: String? = null): Application() {

    override fun start(stage: Stage) {
        stage.scene = Scene(view.root).apply {
            stylesheet?.let { stylesheets.add(it) }
        }
        view.title?.let { stage.title = it }
        view.onBeforeShow()
        stage.setOnHidden { view.onUndock() }
        stage.show()
    }

}
