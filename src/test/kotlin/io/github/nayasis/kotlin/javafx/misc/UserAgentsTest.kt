package io.github.nayasis.kotlin.javafx.misc

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.lang.Thread.sleep
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UserAgentsTest {

    @Test
    fun pickShouldReuseUserAgentWithinCacheWindow() {
        val userAgents = UserAgents(5.minutes)

        val first = userAgents.pick()
        sleep(5.seconds.inWholeMilliseconds)
        val second = userAgents.pick()

        first shouldBe second
    }

    @Test
    fun pickShouldRotateUserAgentAfterCacheWindowExpires() {
        val userAgents = UserAgents(1.seconds)

        val first = userAgents.pick()
        sleep(5.seconds.inWholeMilliseconds)
        val second = userAgents.pick()

        first shouldNotBe second
    }

}