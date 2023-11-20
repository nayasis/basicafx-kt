package com.github.nayasis.kotlin.javafx.app

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.github.nayasis.kotlin.basica.core.extension.ifEmpty
import org.slf4j.LoggerFactory

class LoggerConfig(
    private val environment: Environment,
) {
    fun initialize() {

        if(hasNoConfig()) return

        val ctx = (LoggerFactory.getILoggerFactory() as LoggerContext).apply {
            stop()
            reset()
        }

        val appenderConsole = createConsoleAppender(ctx)
        val appenderFile = createFileAppender(ctx)

        environment.all.filter { it.key.startsWith("logging.level.") }.map {
            it.key.removePrefix("logging.level.") to Level.toLevel("${it.value}", Level.OFF)
        }.toMap().forEach { key, level ->
            ctx.getLogger(key)?.let { logger ->
                appenderConsole?.let { appender ->
                    logger.level = level
                    logger.addAppender(appender)
                    logger.isAdditive = false
                }
                appenderFile?.let { appender ->
                    logger.level = level
                    logger.addAppender(appender)
                    logger.isAdditive = false
                }
            }
        }

        println(">> end logger setting")
    }

    private fun createConsoleAppender(ctx: LoggerContext): ConsoleAppender<ILoggingEvent>? {
        return environment.get<String>("logging.pattern.console")?.let { pattern ->
            ConsoleAppender<ILoggingEvent>().apply {
                this.context = ctx
                this.name = "console"
                this.encoder = PatternLayoutEncoder().apply {
                    this.context = ctx
                    this.pattern = pattern
                    this.start()
                }
                this.start()
            }
        }
    }

    private fun createFileAppender(ctx: LoggerContext): FileAppender<ILoggingEvent>? {
        return environment.get<String>("logging.file.path")?.let { path ->
            val appender = RollingFileAppender<ILoggingEvent>().apply {
                this.context = ctx
                this.name = "file"
                this.file = path
                this.encoder = PatternLayoutEncoder().apply {
                    this.context = ctx
                    this.pattern = environment.get<String>("logging.pattern.file").ifEmpty { "%d{HH:mm:ss.SSS} %msg%n" }
                    this.start()
                }
            }
            appender.rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>().apply {
                this.setParent(appender)
                this.context = ctx
                this.maxHistory = environment.get<String>("logging.file.max-history").ifEmpty {"30"}.toInt()
                this.setMaxFileSize(
                    environment.get<String>("logging.file.max-size").ifEmpty {"10mb"}.let {
                        FileSize.valueOf(it)
                    }
                )
                this.fileNamePattern = "${this.parentsRawFileProperty}_%d{yyyy-MM-dd}.%i.log"
                this.start()
            }
            appender.apply { start() }
        }
    }

    private fun hasNoConfig() = environment.all.filter { it.key.startsWith("logging.") }.isEmpty()
}