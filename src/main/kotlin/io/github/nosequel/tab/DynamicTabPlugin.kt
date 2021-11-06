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

        val imports = Config<ImportData>(
            name = "TabImportData",
            path = "io.github.nosequel.tab.TabImportData",
            defaultCode =
            """
                "import io.github.nosequel.tab.shared.entry.TabElement;",
                "import io.github.nosequel.tab.shared.entry.TabElementHandler;",
                "import org.bukkit.entity.Player;"
            """.trimIndent(),
            classLoader = this.classLoader,
            options = this.options,
            file = File(this.dataFolder, "imports"),
            wrapCode = {
                """
                     package io.github.nosequel.tab;
                     
                     import io.github.nosequel.tab.ImportData;
                     import org.jetbrains.annotations.NotNull;
                     import java.util.Arrays;
                     import java.util.List;
                     
                     public class TabImportData implements ImportData {
                        @NotNull
                        @Override
                        public List<String> getImports() {
                            return Arrays.asList(
                                $it
                            );
                        }
                     }
                """.trimIndent()
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
                """
                    package io.github.nosequel.tab
                    
                    ${imports!!.imports.joinToString { str -> "$str\n" }.replace(",", "")}
                    
                    public class TabElementConfiguration implements TabElementHandler {
                        public TabElement getElement(Player player) {
                            final TabElement element = new TabElement();
                            $it
                            return element;
                        }
                    }
                """.trimIndent()
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