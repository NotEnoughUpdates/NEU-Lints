package org.notenoughupdates.detektrules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test
import org.notenoughupdates.detektrules.CoreEnvironmentUtils.loadLWJLG
import org.notenoughupdates.detektrules.CoreEnvironmentUtils.loadMinecraft

@KotlinCoreEnvironmentTest
internal class GlStateManagerWarningsTest(env: KotlinCoreEnvironment) {
    private val envWithForge = CoreEnvironmentUtils.recreateEnvironment(env) {
        loadMinecraft()
        loadLWJLG()
    }

    @Test
    fun `reports push attrib violations`() {
        val code = """
            import net.minecraft.client.renderer.GlStateManager
            object GL11 {
                val SHADE_SMOOTH = 0
            }
            fun whatEver() {
                GlStateManager.pushAttrib()
                GlStateManager.shadeModel(GL11.SHADE_SMOOTH)
                GlStateManager.color(1f,1f,1f,1f)
                GlStateManager.translate(1f, 1f, 1f)
                GlStateManager.popMatrix()
            }
            fun whatEverTwo() {
                GlStateManager.pushMatrix()
                GlStateManager.color(1f,1f,1f,1f)
            }
            
        """.trimIndent()
        val lints = GlStateManagerWarnings(Config.empty).compileAndLintWithContext(envWithForge, code)
        assert(lints.count { it.id == "GlStackDepthViolation" } == 3) { "$lints should contain a GlStackDepthViolation" }
        assert(lints.count { it.id == "PushAttribStateViolation" } == 2) { "$lints should contain a PushAttribStateViolation" }
    }

    @Test
    fun `should not report regular gl calls`() {
        val code = """
            import org.lwjgl.opengl.GL11
            import net.minecraft.client.renderer.GlStateManager
            fun whatEver() {
                GlStateManager.pushAttrib()
                GL11.glShadeModel(GL11.GL_SMOOTH)
                GlStateManager.translate(1f, 1f, 1f)
                GlStateManager.popAttrib()
            }
            
        """.trimIndent()
        val lints = GlStateManagerWarnings(Config.empty).compileAndLintWithContext(envWithForge, code)
        lints should beEmpty()
    }
}
