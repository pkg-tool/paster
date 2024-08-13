package pkg.tool.paster

import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

object FileProcessor {
    fun processFiles(files: List<VirtualFile>, projectName: String): String {
        val result = StringBuilder()
        result.append("""<project name="$projectName" format-version="1.2">
            |<generated_at>${Instant.now()}</generated_at>
            |""".trimMargin())

        val queue: Queue<Pair<VirtualFile, String>> = LinkedList()
        files.forEach { queue.offer(it to it.path) }

        while (queue.isNotEmpty()) {
            val (file, path) = queue.poll()
            processFile(file, result, path, queue)
        }

        result.append("</project>\n")
        return result.toString()
    }

    private fun processFile(file: VirtualFile, result: StringBuilder, relativePath: String, queue: Queue<Pair<VirtualFile, String>>) {
        if (file.isDirectory) {
            result.append("""<folder path="$relativePath">
                |
            """.trimMargin())
            
            file.children.forEach { child -> 
                queue.offer(child to "$relativePath/${child.name}")
            }
            
            result.append("</folder>")
        } else {
            val modifiedTime = Instant.ofEpochMilli(file.timeStamp).atOffset(ZoneOffset.UTC)
            result.append("""<file path="$relativePath" size="${file.length}" modified="$modifiedTime">
                |    <content>
                |""".trimMargin())
            
            try {
                val lines = String(file.contentsToByteArray()).lines()
                lines.forEachIndexed { index, line ->
                    result.append("""      <line number="${index + 1}"><![CDATA[$line]]></line>
                    |""".trimMargin())
                }
                // Add a newline after the last line if the file doesn't end with one
                if (lines.isNotEmpty() && !lines.last().isBlank()) {
                    result.append("""      <line number="${lines.size + 1}"><![CDATA[]]></line>
                    |""".trimMargin())
                }
            } catch (e: IOException) {
                result.append("""      <line number="1"><![CDATA[Error reading file content: ${e.message}]]></line>
                |""".trimMargin())
            }
            result.append("""    </content>
                |</file>""".trimMargin())
        }
    }
}
