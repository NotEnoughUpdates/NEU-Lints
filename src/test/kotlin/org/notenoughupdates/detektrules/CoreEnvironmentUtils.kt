package org.notenoughupdates.detektrules

import net.minecraftforge.common.MinecraftForge
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File

object CoreEnvironmentUtils {

    fun CompilerConfiguration.loadForge() {
        addJvmClasspathRoot(File(MinecraftForge::class.java.protectionDomain.codeSource.location.toURI()))
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
