#!/usr/bin/env kotlin
@file:DependsOn("org.jetbrains.kotlin:kotlin-compiler:2.1.0")
@file:DependsOn("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.0")
@file:DependsOn("org.jetbrains.kotlin:kotlin-main-kts:2.1.0")
@file:DependsOn("org.jetbrains.kotlin:kotlin-scripting-compiler:2.1.0")


import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files.walk
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isRegularFile

walk(Paths.get("scripts/"))
    .filter { it.isRegularFile() }
    .forEach { it.tryCompiling() }

private fun Path.tryCompiling() {
    val compilationOutput = createTempDirectory()
    val args = K2JVMCompilerArguments().apply {
            destination = compilationOutput.toString()
            freeArgs = listOf(this@tryCompiling.absolutePathString())
            noStdlib = true
            noReflect = true
            allowAnyScriptsInSourceRoots = true
            useFirLT = false
        }
    val compilerMessagesOutputStream = ByteArrayOutputStream()
    val compilerMessageCollector = PrintingMessageCollector(
            PrintStream(compilerMessagesOutputStream),
            MessageRenderer.GRADLE_STYLE,
            false,
        )
    val exitCode = K2JVMCompiler().exec(
            messageCollector = compilerMessageCollector,
            services = Services.EMPTY,
            arguments = args,
        )
    check(exitCode == ExitCode.OK) {
        "Compilation failed! Compiler messages:\n$compilerMessagesOutputStream"
    }
}
