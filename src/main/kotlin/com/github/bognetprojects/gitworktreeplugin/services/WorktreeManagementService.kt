package com.github.bognetprojects.gitworktreeplugin.services

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class WorktreeManagementService(project: Project) {
    var worktreeList: List<String>
    var selected: Int = 0
    private val runtime = Runtime.getRuntime()
    private val projectPath = Paths.get(project.basePath!!)
    private val os = System.getProperty("os.name")
    private lateinit var paths: List<Path>

    init {
        worktreeList = getWorktreePath()
    }

    fun switchToWorktree() {
        ProjectUtil.openOrImport(paths[selected], null, true)
    }

    private fun getWorktreePath(): List<String> {
        val list = executeCommand("git worktree list")
        paths = list.map { Path(it.split(" ")[0]) }
        return list.map { it.substringAfter("[").substringBefore("]") }
    }

    private fun executeCommand(command: String, wait: Long = 5): List<String> {
        var commandString = command
        var result: List<String> = listOf()
        if (!os.lowercase(Locale.getDefault()).startsWith("win")) commandString = arrayOf("/bin/sh", "-c", command).toString()
        try {
            val process: Process = runtime.exec(commandString, null, projectPath.toFile())
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
