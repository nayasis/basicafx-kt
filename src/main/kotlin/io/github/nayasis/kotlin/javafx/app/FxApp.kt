package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.basica.exception.rootCause
import io.github.nayasis.kotlin.javafx.preloader.BasePreloader
import io.github.nayasis.kotlin.javafx.stage.Dialog
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import tornadofx.*
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

@Suppress("unused")
abstract class FxApp: App {

    constructor(primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, vararg stylesheet: KClass<out Stylesheet>) : super(primaryView, *stylesheet)
    constructor(primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, stylesheet: KClass<out Stylesheet>, scope: Scope = FX.defaultScope) : super(primaryView, stylesheet, scope)
    constructor(icon: Image, primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, vararg stylesheet: KClass<out Stylesheet>) : super(icon, primaryView, *stylesheet)

    companion object {
        val ctx = SimpleDiContainer()
        lateinit var environment: Environment
            private set
    }

    final override fun init() {
        try {
            setupDefaultExceptionHandler()
            // setup DI container and environment
            environment = Environment(parameters.raw.toTypedArray(), "application.yml")
            FX.dicontainer = ctx.apply {
                set(environment)
            }
            // setup Logger
            LoggerConfig(environment).initialize()
        } catch (e: Throwable) {
            logger.error(e)
            BasePreloader.notifyError(e.message,e)
        }
    }

    open fun setupDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (Platform.isFxApplicationThread()) {
                runCatching {
                    runLater {
                        Dialog.error(e.message,e.rootCause)
                    }
                }.onFailure { logger.error(it) }
            } else {
                logger.error(e)
            }
        }
    }

    final override fun start(stage: Stage) {
        try {
            onStart(DefaultParser().parse(setOptions() ?: Options(), parameters.raw.toTypedArray()))
            onStart(stage)
            super.start(stage)
        } catch (e: Throwable) {
            logger.error(e)
            BasePreloader.notifyError(e.message,e)
        }
    }

    open fun onStart(command: CommandLine) {}
    open fun onStart(stage: Stage) {}
    open fun setOptions(): Options? { return null }

}