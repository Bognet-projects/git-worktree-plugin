package com.github.bognetprojects.gitworktreeplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.bognetprojects.gitworktreeplugin.services.WorktreeManagementService
import com.intellij.ui.components.JBList


class WorkTreeWindowFactory : ToolWindowFactory {

    init {}

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val worktreeWindow = WorktreeWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(worktreeWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    class WorktreeWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<WorktreeManagementService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val worktreeList = JBList(service.worktreeList).apply {
                addListSelectionListener {
                    service.selected = selectedValue
                }
            }
            worktreeList.selectionModel.selectionMode = 0
            add(worktreeList)
        }
    }
}
