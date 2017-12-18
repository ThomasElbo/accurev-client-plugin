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
import jenkins.plugins.accurevclient.utils.defaultCharset
import jenkins.plugins.accurevclient.utils.isNotEmpty
import jenkins.plugins.accurevclient.utils.rootPath
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit

class AccurevCliAPI (
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
            lateinit var output: MutableList<String>
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

            override fun preview(output: MutableList<String>): UpdateCommand {
                args.add("-i")
                this.output = output
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute() {
                val result = launchCommand(args)
            }
        }
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
}
