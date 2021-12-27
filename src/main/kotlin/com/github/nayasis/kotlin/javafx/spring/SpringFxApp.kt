package com.github.nayasis.kotlin.javafx.spring

import com.github.nayasis.kotlin.basica.core.extention.ifEmpty
import com.github.nayasis.kotlin.basica.etc.error
import com.github.nayasis.kotlin.basica.exception.rootCause
import com.github.nayasis.kotlin.basica.model.Messages
import com.github.nayasis.kotlin.javafx.misc.runAndWait
import com.github.nayasis.kotlin.javafx.preloader.CloseNotificator
import com.github.nayasis.kotlin.javafx.preloader.NPreloader
import com.github.nayasis.kotlin.javafx.preloader.Notificator
import com.github.nayasis.kotlin.javafx.preloader.ProgressNotificator
import com.github.nayasis.kotlin.javafx.stage.Dialog
import com.github.nayasis.kotlin.javafx.stage.Stages
import com.sun.javafx.application.LauncherImpl
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import mu.KotlinLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.NoPrimaryViewSpecified
import tornadofx.Scope
import tornadofx.Stylesheet
import tornadofx.UIComponent
import tornadofx.runLater
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

private lateinit var ctx: ConfigurableApplicationContext

@Suppress("SpringJavaConstructorAutowiringInspection")
abstract class SpringFxApp: App {

    constructor(primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, vararg stylesheet: KClass<out Stylesheet>) : super(primaryView, *stylesheet)
    constructor(primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, stylesheet: KClass<out Stylesheet>, scope: Scope = FX.defaultScope) : super(primaryView, stylesheet, scope)
    constructor(icon: Image, primaryView: KClass<out UIComponent> = NoPrimaryViewSpecified::class, vararg stylesheet: KClass<out Stylesheet>) : super(icon, primaryView, *stylesheet)

    private val options = Options()

    override fun init() {
        try {
            setOptions(options)
            ctx = SpringApplication.run(this.javaClass, *parameters.raw.toTypedArray())
            ctx.autowireCapableBeanFactory.autowireBean(this)
            FX.dicontainer = object: DIContainer {
                override fun <T: Any> getInstance(type: KClass<T>): T = ctx.getBean(type.java)
                override fun <T: Any> getInstance(type: KClass<T>, name: String): T = ctx.getBean(name, type.java)
            }
            setupDefaultExceptionHandler()
        } catch (e: Throwable) {
            closePreloader()
            runAndWait {
                Dialog.error(e)
                stop()
            }
        }
    }

    private fun setupDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            if (Platform.isFxApplicationThread()) {
                runCatching {
                    runLater {
                        Dialog.error(e.rootCause)
                    }
                }.onFailure { logger.error(it) }
            } else {
                logger.error(e)
            }
        }
    }

    override fun start(stage: Stage) {
        try {
            onStart(DefaultParser().parse(options, parameters.raw.toTypedArray()))
            onStart(stage)
        } catch (e: Exception) {
            if (Platform.isFxApplicationThread()) {
                Dialog.error(e.rootCause)
                throw e
            } else {
                logger.error(e)
                throw e
            }
        }
        super.start(stage)
    }

    override fun stop() {
        try {
            onStop(ctx)
        } catch (e: Exception) {
            logger.error(e)
        }
        runCatching { ctx.close() }.onFailure { logger.error(it) }
        runCatching { super.stop() }.onFailure { logger.error(it) }
        exitProcess(0)
    }

    open fun setOptions(options: Options) {}
    open fun onStart(command: CommandLine) {}
    open fun onStart(stage: Stage) {}
    open fun onStop(context: ConfigurableApplicationContext) {}

    companion object {

        fun setPreloader(preloader: KClass<out NPreloader>) {
            System.setProperty("javafx.preloader", preloader.jvmName)
            System.setProperty("java.awt.headless", "false")
        }

        fun loadDefaultIcon(resourcePath: String) = Stages.defaultIcons.add(resourcePath)

        fun loadMessage(resourcePath: String) = Messages.loadFromResource(resourcePath)

        @Suppress("MemberVisibilityCanBePrivate")
        fun notifyPreloader( notificator: Notificator ) {
            LauncherImpl.notifyPreloader(null,notificator)
        }

        fun notifyProgress(percent: Double, message: String? = null) {
            notifyPreloader(ProgressNotificator(percent,message))
        }

        fun notifyProgress(index: Number, max: Number, message: String? = null) {
            notifyPreloader(ProgressNotificator(index,max,message))
        }

        fun closePreloader() = notifyPreloader(CloseNotificator())

        val context: ConfigurableApplicationContext
            get() = ctx

        val environment: Environment
            get() = ctx.environment

        fun environment(key: String, default: String = ""): String =
            environment[key].ifEmpty { default }

    }

}