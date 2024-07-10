package pkg.tool.paster

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

class PasterPlugin : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        val processedContent = FileProcessor.processFiles(selectedFiles.toList(), project.name)

        // Copy the processed content to clipboard
        val selection = StringSelection(processedContent)
        CopyPasteManager.getInstance().setContents(selection)
        // TODO: Add a notification to inform the user that the content has been copied
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
