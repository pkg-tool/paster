package pkg.tool.paster

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.notification.*
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import com.intellij.openapi.actionSystem.ActionUpdateThread

class PasterPlugin : AnAction() {
    companion object {
        private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("Paster Notifications")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        val totalFiles = countTotalFiles(selectedFiles.toList())

        val processedContent = FileProcessor.processFiles(selectedFiles.toList(), project.name)

        // Copy the processed content to clipboard
        val selection = StringSelection(processedContent)
        CopyPasteManager.getInstance().setContents(selection)

        // Show notification
        NOTIFICATION_GROUP
            .createNotification(
                "Paster",
                "${selectedFiles.size} item(s) copied (${totalFiles} files in total)",
                NotificationType.INFORMATION
            ).notify(project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun countTotalFiles(files: List<VirtualFile>): Int {
        var count = 0
        for (file in files) {
            if (file.isDirectory) {
                count += countTotalFiles(file.children.toList())
            } else {
                count++
            }
        }
        return count
    }
}
