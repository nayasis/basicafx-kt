package io.github.nayasis.kotlin.javafx.app

import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.basica.exception.rootCause
import io.github.nayasis.kotlin.javafx.app.di.SimpleDiContainer
import io.github.nayasis.kotlin.javafx.misc.runLater
import io.github.nayasis.kotlin.javafx.misc.shutdownAsyncPools
import io.github.nayasis.kotlin.javafx.preloader.DefaultPreloader
import io.github.nayasis.kotlin.javafx.stage.Dialog
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * application base: sets up the DI container, environment and logging,
 * then puts [primaryView] on the primary stage.
 *
 * ```
 * class MyApp: FxApp(Main::class)
 *
 * fun main(args: Array<String>) = Application.launch(MyApp::class.java, *args)
 * ```
 */
@Suppress("unused")
abstract class FxApp(
    private val primaryView: KClass<out FxView>? = null,
    private val stylesheets: List<String> = emptyList(),
): Application() {

    companion object {

        val ctx = SimpleDiContainer()

        lateinit var environment: Environment
            private set

        lateinit var primaryStage: Stage
            private set

        private val views = ConcurrentHashMap<KClass<out FxView>, FxView>()

        /**
         * get a view, creating and caching it on first use.
         *
         * views are not DI beans: they are instantiated through their no-arg
         * constructor and held as singletons, so they need no annotation.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T: FxView> find(type: KClass<T>): T = views.getOrPut(type) {
            type.java.getDeclaredConstructor().newInstance()
        } as T

    }

    override fun init() {
        try {
            setupDefaultExceptionHandler()
            // setup DI container and environment
            environment = Environment(parameters.raw.toTypedArray(), "application.yml")
            ctx.set(environment)
            // setup Logger
            LoggerConfig(environment).initialize()
            // init bean
            beforeInitialization(ctx,environment)
            ctx.scanPackages(this::class.java.packageName)
            afterInitialization(ctx,environment)
        } catch (e: Throwable) {
            logger.error(e)
            DefaultPreloader.notifyError(e.message,e)
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

    override fun start(stage: Stage) {
        try {
            primaryStage = stage
            onStart(DefaultParser().parse(setOptions() ?: Options(), parameters.raw.toTypedArray()))
            onStart(stage)
            primaryView?.let { type ->
                val view = find(type)
                stage.scene = Scene(view.root).apply { stylesheets.addAll(this@FxApp.stylesheets) }
                view.title?.let { stage.title = it }
                stage.setOnHidden { view.onUndock() }
                view.onBeforeShow()
            }
            stage.show()
        } catch (e: Throwable) {
            logger.error(e)
            DefaultPreloader.notifyError(e.message,e)
        }
    }

    override fun stop() {
        runCatching { onStop() }.onFailure { logger.error(it) }
        runCatching { shutdownAsyncPools() }.onFailure { logger.error(it) }
        runCatching { ctx.close() }.onFailure { logger.error(it) }
        runCatching { super.stop() }.onFailure { logger.error(it) }
        exitProcess(0)
    }

    open fun beforeInitialization(context: SimpleDiContainer, environment: Environment) {}
    open fun afterInitialization(context: SimpleDiContainer, environment: Environment) {}

    open fun onStart(command: CommandLine) {}
    open fun onStart(stage: Stage) {}
    open fun setOptions(): Options? { return null }
    open fun onStop() {}

}
