package jenkins.plugins.accurevclient

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.Launcher.LocalLauncher
import hudson.model.TaskListener
import hudson.util.ArgumentListBuilder
import hudson.util.Secret
import jenkins.plugins.accurevclient.commands.HistCommand
import jenkins.plugins.accurevclient.commands.LoginCommand
import jenkins.plugins.accurevclient.commands.PopulateCommand
import jenkins.plugins.accurevclient.commands.UpdateCommand
import jenkins.plugins.accurevclient.model.AccurevDepots
import jenkins.plugins.accurevclient.model.AccurevInfo
import jenkins.plugins.accurevclient.model.AccurevReferenceTrees
import jenkins.plugins.accurevclient.model.AccurevStream
import jenkins.plugins.accurevclient.model.AccurevStreams
import jenkins.plugins.accurevclient.model.AccurevUpdate
import jenkins.plugins.accurevclient.model.AccurevWorkspaces
import jenkins.plugins.accurevclient.utils.defaultCharset
import jenkins.plugins.accurevclient.utils.isNotEmpty
import jenkins.plugins.accurevclient.utils.rootPath
import jenkins.plugins.accurevclient.utils.unmarshal
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit

class AccurevCliAPI(
    private val workspace: FilePath,
    private val environment: EnvVars,
    val accurevExe: String,
    val server: String,
    private val listener: TaskListener
) : AccurevClient {
    @Transient private val launcher: Launcher

    init {
        environment.putIfAbsent("ACCUREV_HOME", workspace.rootPath())
        launcher = LocalLauncher(if (AccurevClient.verbose) listener else TaskListener.NULL)
    }

    fun accurev(cmd: String, xml: Boolean = false) = ArgumentListBuilder().apply {
        add(accurevExe, cmd)
        if (server.isNotBlank()) add("-H", server)
        if (xml) add("-fx")
    }

    override fun hist(): HistCommand {
        return object : HistCommand {
            private val args = accurev("hist", true)

            override fun depot(depot: String): HistCommand {
                args.add("-p", depot)
                return this
            }

            override fun stream(stream: String): HistCommand {
                args.add("-s", stream)
                return this
            }

            override fun timeSpec(timeSpec: String): HistCommand {
                args.add("-t", timeSpec)
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute() {
                launchCommand(args)
            }
        }
    }

    override fun populate(): PopulateCommand {
        return object : PopulateCommand {
            val args = accurev("pop").add("-L", workspace.remote)

            override fun stream(stream: String): PopulateCommand {
                args.add("-v", stream)
                return this
            }

            override fun overwrite(overwrite: Boolean): PopulateCommand {
                args.add("-O")
                return this
            }

            override fun timespec(timespec: String): PopulateCommand {
                args.add("-t", timespec)
                return this
            }

            override fun elements(set: Set<String>): PopulateCommand {
                args.add("-R", if (set.isEmpty()) "." else set.joinToString(","))
                return this
            }

            override fun listFile(listFile: FilePath): PopulateCommand {
                args.add("-l", listFile.remote)
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute() {
                launchCommand(args)
            }
        }
    }

    override fun update(): UpdateCommand {
        return object : UpdateCommand {
            val args = accurev("update", true)

            override fun referenceTree(referenceTree: String): UpdateCommand {
                args.add("-r", referenceTree)
                return this
            }

            override fun stream(stream: String): UpdateCommand {
                args.add("-s", stream)
                return this
            }

            override fun range(latestTransaction: Long, previousTransaction: Long): UpdateCommand {
                args.add("-t", "$latestTransaction-$previousTransaction")
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute() {
                launchCommand(args)
            }
        }
    }

    override fun getWorkspaces(): AccurevWorkspaces {
        with(accurev("show", true)) {
            add("wspaces")
            return launch().unmarshal() as AccurevWorkspaces
        }
    }

    override fun getReferenceTrees(): AccurevReferenceTrees {
        with(accurev("show", true)) {
            add("refs")
            return launch().unmarshal() as AccurevReferenceTrees
        }
    }

    override fun getDepots(): AccurevDepots {
        with(accurev("show", true)) {
            add("depots")
            return launch().unmarshal() as AccurevDepots
        }
    }

    @Throws(AccurevException::class)
    override fun getStream(stream: String): AccurevStream {
        with(accurev("show", true)) {
            add("-s", stream, "streams")
            val accurevStreams = launch().unmarshal() as AccurevStreams
            if (accurevStreams.list.size != 1) throw AccurevException("Stream not found")
            return accurevStreams.list[0]
        }
    }

    override fun getStreams(depot: String): AccurevStreams {
        with(accurev("show", true)) {
            if (depot.isNotBlank()) add("-p", depot)
            add("streams")
            return launch().unmarshal() as AccurevStreams
        }
    }

    override fun getChildStreams(stream: String): AccurevStreams {
        with(accurev("show", true)) {
            add("-R", "-s", stream, "streams")
            return launch().unmarshal() as AccurevStreams
        }
    }

    override fun getUpdatedElements(
        stream: String,
        latestTransaction: Long,
        previousTransaction: Long,
        referenceTree: Boolean
    ): AccurevUpdate {
        with(accurev("update", true)) {
            add(if (referenceTree) "-r" else "-s", stream)
            add("-t", "$latestTransaction-$previousTransaction", "-i")
            return launch().unmarshal() as AccurevUpdate
        }
    }

    override fun syncTime() {
        accurev("synctime").launch()
    }

    override fun getInfo(): AccurevInfo {
        with(accurev("info", true)) {
            add("-v")
            return launch().unmarshal() as AccurevInfo
        }
    }

    override fun getVersion(): String {
        val result = ArgumentListBuilder(accurevExe).launch()
        return result.split(' ')[1]
    }

    override fun login(): LoginCommand {
        return object : LoginCommand {
            private val args = accurev("login")

            override infix fun username(username: String): LoginCommand {
                args.add(username)
                return this
            }

            override infix fun password(password: Secret): LoginCommand {
                when {
                    password.isNotEmpty() -> args.addMasked(password)
                // Workaround for https://issues.jenkins-ci.org/browse/JENKINS-39066
                    launcher.isUnix -> args.add("", true)
                    else -> args.addQuoted("", true)
                }
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute() {
                launchCommand(args)
            }
        }
    }

    @JvmOverloads
    @Throws(AccurevException::class, InterruptedException::class)
    fun launchCommand(
        args: ArgumentListBuilder,
        ws: FilePath = workspace,
        env: EnvVars = environment,
        timeout: Int = TIMEOUT
    ): String {
        val fos = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val environment = EnvVars(env)
        val command = args.toString()

        val p = launcher.launch().apply {
            cmds(args)
            envs(environment)
            stdout(fos)
            stderr(err)
            pwd(ws)
        }
        try {
            val status = p.start().joinWithTimeout(timeout.toLong(), TimeUnit.MINUTES, listener)
            val result = fos.defaultCharset
            if (status != 0) {
                throw AccurevException(
                    """
                    Command: '$command'
                    Exit code: $status
                    stdout: $result
                    stderr: ${err.defaultCharset}
                    """.trimIndent()
                )
            }
            return result
        } catch (e: InterruptedException) {
            throw e
        } catch (e: IOException) {
            throw AccurevException("IO failed while trying to execute $command", e)
        } catch (e: UnsupportedEncodingException) {
            throw AccurevException("The encoding is pure gibberish while trying to execute $command", e)
        }
    }

    companion object {
        val TIMEOUT: Int = Integer.getInteger("${AccurevClient::class.java.name}.timeOut", 10)
    }

    private fun ArgumentListBuilder.launch(): String = this@AccurevCliAPI.launchCommand(this)
}
