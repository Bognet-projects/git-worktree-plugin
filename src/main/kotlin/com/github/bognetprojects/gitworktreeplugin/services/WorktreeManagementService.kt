package com.github.bognetprojects.gitworktreeplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class WorktreeManagementService(project: Project) {
    val worktreeList: List<String>
    var selected: String = ""
    private val runtime = Runtime.getRuntime()
    private val projectPath = Paths.get(project.basePath!!)
    private val os = System.getProperty("os.name")

    init {
        worktreeList = getWorktreePath()
    }

    private fun getWorktreePath(): List<String> {
        var command = "git worktree list"
        if (!os.lowercase(Locale.getDefault()).startsWith("win")) command = arrayOf("/bin/sh", "-c", command).toString()
        var list: List<String> = listOf()
        try {
            val process: Process = runtime.exec(command, null, projectPath.toFile())
            process.waitFor(5, TimeUnit.SECONDS)

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var inline: String?
            while (null != reader.readLine().also { inline = it }) {
                output.append(inline).append("\n")
            }
            process.destroy()
            reader.close()
            list = output.split("\n").map { it.substringAfter("[").substringBefore("]") }
        } catch (_: IOException) {}
        return list
    }
}
