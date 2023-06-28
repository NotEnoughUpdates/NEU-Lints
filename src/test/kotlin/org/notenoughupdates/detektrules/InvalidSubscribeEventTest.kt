package org.notenoughupdates.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test
import org.notenoughupdates.detektrules.CoreEnvironmentUtils.loadForge

@KotlinCoreEnvironmentTest
internal class InvalidSubscribeEventTest(env: KotlinCoreEnvironment) {
    private val envWithForge = CoreEnvironmentUtils.recreateEnvironment(env) {
        loadForge()
    }

    @Test
    fun `reports missing @SubscribeEvent`() {
        val code = """
        class A {
            class SomeEvent : net.minecraftforge.fml.common.eventhandler.Event()
            fun missingANnotation(event: SomeEvent) {
            }
        }
        """
        val findings = InvalidSubscribeEvent(Config.empty).compileAndLintWithContext(envWithForge, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report valid function`() {
        val code = """
        import net.minecraftforge.fml.common.eventhandler.*
        class A {
            class SomeEvent : Event()
            @SubscribeEvent
            fun validFUnc(e : SomeEvent) {
            }
        }
        """
        val findings = InvalidSubscribeEvent(Config.empty).compileAndLintWithContext(envWithForge, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports missing arguments`() {
        val code = """
            import net.minecraftforge.fml.common.eventhandler.*
            class A {
                @SubscribeEvent
                fun missingArg() {
                }
            }
        """.trimIndent()
        val findings = InvalidSubscribeEvent(Config.empty).compileAndLintWithContext(envWithForge, code)
        findings shouldHaveSize 1
    }
}
