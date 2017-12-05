package org.jenkinsci.plugins.accurevclient

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.Launcher.LocalLauncher
import hudson.model.TaskListener
import hudson.util.ArgumentListBuilder
import hudson.util.Secret
import org.jenkinsci.plugins.accurevclient.commands.LoginCommand
import org.jenkinsci.plugins.accurevclient.utils.defaultCharset
import org.jenkinsci.plugins.accurevclient.utils.isNotEmpty
import org.jenkinsci.plugins.accurevclient.utils.rootPath
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

            override fun execute() {
                launchCommand(args)
            }
        }
    }

    @JvmOverloads
    @Throws(AccurevException::class)
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
            throw AccurevException("Oh no, accurev was interrupted trying to execute $command", e)
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
