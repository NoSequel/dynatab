package io.github.nosequel.tab

import java.io.*
import java.net.URLClassLoader
import java.util.stream.Collectors
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class Config<T>(
    private val file: File,
    private var sourceFile: File? = null,
    private val path: String,
    private val defaultCode: String,
    private val options: List<String>,
    private val classLoader: ClassLoader,
    val wrapCode: (String) -> String = { it }
) {

    fun checkFile(): Config<T> {
        if (!file.exists()) {
            file.createNewFile()

            BufferedWriter(FileWriter(file)).also {
                it.write(defaultCode)
                it.close()
            }
        }

        return this
    }

    fun writeSource(): Config<T> {
        val code = wrapCode(
            BufferedReader(FileReader(file)).lines()
                .collect(Collectors.joining(" "))
        )

        this.sourceFile = File("${this.path.replace(".", "/")}.java")

        if (sourceFile!!.exists()) {
            sourceFile!!.delete()
        }

        if (!sourceFile!!.parentFile.exists()) {
            sourceFile!!.parentFile.mkdirs()
        }

        sourceFile!!.writeBytes(code.toByteArray())

        return this
    }

    fun construct(): T? {
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()

        val fileManager = compiler.getStandardFileManager(null, null, null)
        val units = fileManager.getJavaFileObjectsFromFiles(listOf(sourceFile!!))

        compiler.getTask(null, fileManager, null, options, null, units).call()
        fileManager.close()

        val classLoader = URLClassLoader(arrayOf(File("").toURI().toURL()), this.classLoader)
        val clazz = classLoader.loadClass("io.github.nosequel.tab.TabElementConfiguration")

        return clazz.newInstance() as T?
    }
}