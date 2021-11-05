package io.github.nosequel.tab

import io.github.nosequel.tab.shared.TabHandler
import io.github.nosequel.tab.shared.entry.TabElementHandler
import org.bukkit.Bukkit
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

        Bukkit.getOnlinePlayers().size

        val imports = Config<ImportData>(
            name = "TabImportData",
            path = "io.github.nosequel.tab.TabImportData",
            defaultCode = StringBuilder()
                .append("\"import io.github.nosequel.tab.shared.entry.TabElement;\",\n")
                .append("\"import io.github.nosequel.tab.shared.entry.TabElementHandler;\",\n")
                .append("\"import org.bukkit.entity.Player;\"\n")
                .toString(),
            classLoader = this.classLoader,
            options = this.options,
            file = File(this.dataFolder, "imports"),
            wrapCode = {
                "package io.github.nosequel.tab;\n" +
                        "import io.github.nosequel.tab.ImportData;\n" +
                        "import org.jetbrains.annotations.NotNull;\n" +
                        "\n" +
                        "import java.util.Arrays;\n" +
                        "import java.util.List;\n" +
                        "\n" +
                        "public class TabImportData implements ImportData {\n" +
                        "    @NotNull\n" +
                        "    @Override\n" +
                        "    public List<String> getImports() {\n" +
                        "        return Arrays.asList(\n" +
                        "                $it" +
                        "        );\n" +
                        "    }\n" +
                        "}\n"
            }
        )
            .checkFile()
            .writeSource()
            .construct()

        val tab = Config<TabElementHandler>(
            name = "TabElementConfiguration",
            path = "io.github.nosequel.tab.TabElementConfiguration",
            defaultCode = "element.add(0, 0, \"hey\");",
            classLoader = this.classLoader,
            options = this.options,
            file = File(this.dataFolder, "tab"),
            wrapCode = {
                StringBuilder().append("package io.github.nosequel.tab;\n")
                    .append(imports!!.imports.joinToString { str -> "$str\n" }.replace(",", ""))
                    .append("public class TabElementConfiguration implements TabElementHandler {\n")
                    .append("   public TabElement getElement(Player player) {\n")
                    .append("       final TabElement element = new TabElement();\n")
                    .append("       $it")
                    .append("\n     return element;\n")
                    .append("   }\n")
                    .append("}\n")
                    .toString()
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