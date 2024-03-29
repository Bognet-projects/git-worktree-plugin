package com.github.bognetprojects.gitworktreeplugin.services

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class WorktreeManagementService(project: Project) {
    var worktreeList: List<String>
    var selected: Int = 0
    private val branchList: List<String>
    private val runtime = Runtime.getRuntime()
    private val projectPath = Paths.get(project.basePath!!)
    private lateinit var paths: List<Path>

    init {
        worktreeList = getWorktreePath()
        branchList = executeCommand("git branch -a", 20)
        thisLogger().warn(projectPath.toString())
    }

    fun switchToWorktree() {
        ProjectUtil.openOrImport(paths[selected], null, true)
    }

    fun getBranchList(): List<String> {
        return branchList
    }

    fun addToWorktree(branchName: String) {
        executeCommand("git worktree add ../$branchName $branchName")
        worktreeList = getWorktreePath()
    }

    fun removeWorktree() {
        val branchName = worktreeList[selected]
        executeCommand("git worktree remove $branchName --force")
        worktreeList = getWorktreePath()
    }

    private fun getWorktreePath(): List<String> {
        val list = executeCommand("git worktree list")
        paths = list.map { Path(it.split(" ")[0]) }
        return list.map { it.substringAfter("[").substringBefore("]") }
    }

    private fun executeCommand(command: String, wait: Long = 5): List<String> {
        var result: List<String> = listOf()
        try {
            val process: Process = runtime.exec(command, null, projectPath.toFile())
            process.waitFor(wait, TimeUnit.SECONDS)

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var inline: String?
            while (null != reader.readLine().also { inline = it }) {
                output.append(inline).append("\n")
            }
            process.destroy()
            reader.close()
            result = output.split("\n").dropLast(1)
        } catch (_: IOException) {}
        return result
    }
}
