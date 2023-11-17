package com.github.nayasis.kotlin.javafx.app

import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.kotlin.basica.exception.rootCause
import com.github.nayasis.kotlin.javafx.preloader.BasePreloader
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import tornadofx.*
import kotlin.reflect.KClass
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

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
            FX.dicontainer = ctx
            environment = Environment(parameters.raw.toTypedArray(), "application.yml")
        } catch (e: Throwable) {
            logger.error(e)
            throw e
            stop()
        }
    }

    open fun setupDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (Platform.isFxApplicationThread()) {
                runCatching {
                    runLater {
                        BasePreloader.notifyError(e.message, e.rootCause)
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
            throw e
            stop()
        }
    }

    final override fun stop() {
        runCatching { stop() }.onFailure { logger.error(it) }
        runCatching { super.stop() }.onFailure { logger.error(it) }
        exitProcess(0)
    }

    open fun onStart(command: CommandLine) {}
    open fun onStart(stage: Stage) {}
    open fun onStop() {}
    open fun setOptions(): Options? { return null }

}