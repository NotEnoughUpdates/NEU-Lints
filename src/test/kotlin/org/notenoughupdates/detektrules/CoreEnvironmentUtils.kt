package org.notenoughupdates.detektrules

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File

object CoreEnvironmentUtils {

    fun CompilerConfiguration.loadForge() {
        addJvmClasspathRoot(File(System.getProperty("dependency.forge")))
    }

    fun CompilerConfiguration.loadMinecraft() {
        addJvmClasspathRoot(File(System.getProperty("dependency.minecraft")))
    }

    fun CompilerConfiguration.loadLWJLG() {
        addJvmClasspathRoot(File(System.getProperty("dependency.lwjgl")))
    }

    fun recreateEnvironment(
        env: KotlinCoreEnvironment,
        modify: CompilerConfiguration.() -> Unit
    ): KotlinCoreEnvironment {
        val c = env.configuration.copy()
        modify(c)
        return KotlinCoreEnvironment.createForTests(
            env.projectEnvironment.parentDisposable,
            c,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

}
