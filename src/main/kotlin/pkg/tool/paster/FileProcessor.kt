package pkg.tool.paster

import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset

object FileProcessor {
    fun processFiles(files: List<VirtualFile>, projectName: String): String {
        val result = StringBuilder()
        result.append("""<project name="$projectName" format-version="1.0">
            |<generated_at>${Instant.now()}</generated_at>
            |""".trimMargin())

        for (file in files) {
            processFile(file, result, "")
        }

        result.append("</project>\n")
        return result.toString()
    }

    private fun processFile(file: VirtualFile, result: StringBuilder, relativePath: String) {
        if (file.isDirectory) {
            val newRelativePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
            result.append("""<folder path="$newRelativePath">
                |
            """.trimMargin())
            file.children.forEach { processFile(it, result, newRelativePath) }
            result.append("</folder>")
        } else {
            val filePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
            val modifiedTime = Instant.ofEpochMilli(file.timeStamp).atOffset(ZoneOffset.UTC)
            result.append("""<file path="$filePath" size="${file.length}" modified="$modifiedTime">
                |    <content><![CDATA[""".trimMargin())
            try {
                result.append(String(file.contentsToByteArray()))
            } catch (e: IOException) {
                result.append("Error reading file content: ${e.message}")
            }
            result.append("""    ]]></content>
                |</file>""".trimMargin())
        }
    }
}
