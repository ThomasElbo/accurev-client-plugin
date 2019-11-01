package jenkins.plugins.accurevclient

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.model.TaskListener
import hudson.remoting.LocalChannel
import jenkins.model.Jenkins
import java.io.File
import java.io.Serializable

data class Accurev(
        @Transient var workspace: FilePath? = Jenkins.getInstanceOrNull()!!.rootPath,
        var url: String = "",
        var exe: String = "accurev",
        val env: EnvVars = EnvVars(),
        val listener: TaskListener = TaskListener.NULL,
        @Transient val launcher: Launcher? = Launcher.LocalLauncher(TaskListener.NULL)
    ) : Serializable {
    val client: AccurevClient
        get() = AccurevCliAPI(workspace, env, exe, url, launcher, listener)

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
        fun with(listener: TaskListener, env: EnvVars, launcher: Launcher) = Accurev(listener = listener, env = env, workspace = null, launcher = launcher)
    }
}
