package com.github.bognetprojects.gitworktreeplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.bognetprojects.gitworktreeplugin.services.WorktreeManagementService
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.panel


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
            var worktreeList: JBList<String> = getList()
            worktreeList.selectionModel.selectionMode = 0

            val buttons = panel {
                row {
                    button("Switch") {
                        service.switchToWorktree()
                    }
                    button("Remove") {
                        service.removeWorktree()
                        remove(worktreeList)
                        worktreeList = getList()
                        add(worktreeList)
                        revalidate()
                        updateUI()
                    }
                }
            }

            val branches = panel {
                row("Branches") {
                    val box = comboBox(service.getBranchList())
                    button("Add") {
                        box.component.selectedItem?.let { it1 -> service.addToWorktree(it1.toString().trim()) }
                        remove(worktreeList)
                        worktreeList = getList()
                        add(worktreeList)
                        revalidate()
                        updateUI()

                    }
                }
            }

            add(branches)
            add(buttons)
            add(worktreeList)
        }

        private fun getList(): JBList<String> {
            return JBList(service.worktreeList).apply {
                addListSelectionListener {
                    service.selected = selectedIndex
                }
            }
        }
    }
}
