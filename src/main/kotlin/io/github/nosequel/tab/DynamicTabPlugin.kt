package io.github.nosequel.tab

import io.github.nosequel.tab.shared.TabHandler
import io.github.nosequel.tab.shared.entry.TabElementHandler
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.StringBuilder

class DynamicTabPlugin : JavaPlugin() {

    private val options = listOf(
        "-classpath",
        System.getProperty("java.class.path") + File.pathSeparator + this.file.absolutePath
    )

    override fun onEnable() {
        this.dataFolder.mkdirs()

        val tab = Config<TabElementHandler>(
            path = "io.github.nosequel.tab.TabElementConfiguration",
            defaultCode = "",
            classLoader = this.classLoader,
            options = this.options,
            file = File(this.dataFolder, "tab"),
            wrapCode = {
                StringBuilder().append("package io.github.nosequel.tab;\n")
                    .append("import io.github.nosequel.tab.shared.entry.TabElement;\n")
                    .append("import io.github.nosequel.tab.shared.entry.TabElementHandler;\n")
                    .append("import org.bukkit.entity.Player;\n")
                    .append("public class TabElementConfiguration implements TabElementHandler {\n")
                    .append("public TabElement getElement(Player player) {\n")
                    .append("final TabElement element = new TabElement();\n")
                    .append(it)
                    .append("\nreturn element;\n}}").toString()
            }
        )
            .checkFile()
            .writeSource()
            .construct()

        TabHandler(
            tab,
            this,
            20L
        )
    }
}