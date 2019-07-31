package jenkins.plugins.accurevclient

import hudson.EnvVars
import hudson.FilePath
import hudson.model.TaskListener
import jenkins.model.Jenkins
import java.io.File
import java.io.Serializable

data class Accurev(
    var workspace: FilePath? = Jenkins.getInstanceOrNull()?.rootPath,
    var url: String = "",
    var exe: String = "accurev",
    val env: EnvVars = EnvVars(),
    val listener: TaskListener = TaskListener.NULL
) : Serializable {
    val client: AccurevClient
        get() = AccurevCliAPI(workspace, env, exe, url, listener)

    fun at(workspace: File): Accurev = at(FilePath(workspace))

    fun at(workspace: FilePath): Accurev {
        this.workspace = workspace
        return this
    }

    fun using(exe: String): Accurev {
        this.exe = exe
        return this
    }

    fun on(url: String): Accurev {
        this.url = url
        return this
    }

    companion object {
        @JvmStatic
        fun with(listener: TaskListener, env: EnvVars) = Accurev(listener = listener, env = env)
    }
}
