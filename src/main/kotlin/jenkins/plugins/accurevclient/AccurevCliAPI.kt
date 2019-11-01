package jenkins.plugins.accurevclient

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials

import hudson.EnvVars
import hudson.FilePath
import hudson.Launcher
import hudson.Launcher.LocalLauncher
import hudson.model.TaskListener
import hudson.util.ArgumentListBuilder
import hudson.util.Secret
import jenkins.plugins.accurevclient.commands.HistCommand
import jenkins.plugins.accurevclient.commands.LoginCommand
import jenkins.plugins.accurevclient.commands.LogoutCommand
import jenkins.plugins.accurevclient.commands.StreamCommand
import jenkins.plugins.accurevclient.commands.UpdateCommand
import jenkins.plugins.accurevclient.commands.PopulateCommand
import jenkins.plugins.accurevclient.commands.KeepCommand
import jenkins.plugins.accurevclient.commands.DepotCommand
import jenkins.plugins.accurevclient.commands.WorkspaceCommand
import jenkins.plugins.accurevclient.commands.PromoteCommand
import jenkins.plugins.accurevclient.commands.FilesCommand
import jenkins.plugins.accurevclient.commands.AddCommand
import jenkins.plugins.accurevclient.commands.ChangeWSCommand
import jenkins.plugins.accurevclient.model.AccurevWorkspaces
import jenkins.plugins.accurevclient.model.AccurevReferenceTrees
import jenkins.plugins.accurevclient.model.AccurevDepots
import jenkins.plugins.accurevclient.model.AccurevStreams
import jenkins.plugins.accurevclient.model.AccurevDepot
import jenkins.plugins.accurevclient.model.AccurevStream
import jenkins.plugins.accurevclient.model.AccurevTransaction
import jenkins.plugins.accurevclient.model.AccurevTransactions
import jenkins.plugins.accurevclient.model.AccurevInfo
import jenkins.plugins.accurevclient.model.AccurevUpdate
import jenkins.plugins.accurevclient.model.AccurevFiles
import jenkins.plugins.accurevclient.model.AccurevFile
import jenkins.plugins.accurevclient.model.AccurevStreamType
import jenkins.plugins.accurevclient.utils.defaultCharset
import jenkins.plugins.accurevclient.utils.isNotEmpty
import jenkins.plugins.accurevclient.utils.unmarshal
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.Date

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class AccurevCliAPI(
        @Transient private val workspace: FilePath?,
        private val environment: EnvVars = EnvVars(),
        val accurevExe: String,
        val server: String,
        @Transient val launcher: Launcher? = LocalLauncher(TaskListener.NULL),
        @Transient private val listener: TaskListener
) : AccurevClient {

    override var credentials: StandardUsernamePasswordCredentials? = null

    init {
        environment.putIfAbsent("ACCUREV_HOME", workspace!!.remote)
    }

    fun accurev(cmd: String, xml: Boolean = false, appendServer: Boolean = true) = ArgumentListBuilder().apply {
        add(accurevExe, cmd)
        if (server.isNotBlank() && appendServer) add("-H", server)
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
            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun stream(): StreamCommand {
        return object : StreamCommand {
            private val args = accurev("mkstream", xml = false)

            override fun create(name: String, backingStream: String): StreamCommand {
                args.add("-s", name)
                args.add("-b", backingStream)
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun depot(): DepotCommand {
        return object : DepotCommand {
            private val args = accurev("mkdepot", xml = false)

            override fun create(name: String): DepotCommand {
                args.add("-p", name)
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun add(): AddCommand {
        return object : AddCommand {
            private val args = accurev("add", false)

            override fun add(files: List<String>): AddCommand {
                files.forEach { args.add(it) }
                return this
            }

            override fun comment(comment: String): AddCommand {
                args.add("-c", comment)
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun keep(): KeepCommand {
        return object : KeepCommand {
            private val args = accurev("keep", false)

            override fun comment(comment: String): KeepCommand {
                args.add("-c", "\"$comment\"")
                return this
            }

            override fun files(files: List<String>): KeepCommand {
                files.forEach { args.add(it) }
                return this
            }

            override fun recurse(): KeepCommand {
                args.add("-m")
                return this
            }

            override fun modified(): KeepCommand {
                args.add("-m")
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun promote(): PromoteCommand {
        return object : PromoteCommand {
            private val args = accurev("promote", false)

            override fun files(files: List<String>): PromoteCommand {
                files.forEach { args.add(it) }
                return this
            }

            override fun comment(comment: String): PromoteCommand {
                args.add("-c", "\"$comment\"")
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun workspace(): WorkspaceCommand {
        return object : WorkspaceCommand {
            private val args = accurev("mkws", false)
            override fun create(name: String, backingStream: String): WorkspaceCommand {
                args.add("-w", name)
                args.add("-b", backingStream)
                args.add("-l", workspace.toString())
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun files(): FilesCommand {
        return object : FilesCommand {
            private val args = accurev("files", true)

            override fun stream(stream: String): FilesCommand {
                args.add("-s", stream)
                return this
            }

            override fun overwrite(overwrite: Boolean): FilesCommand {
                if (overwrite) args.add("-O")
                return this
            }

            override fun ignore(files: Set<String>): FilesCommand {
                // for String in set, add param --ignore=""
                return this
            }

            override fun addExcluded(addExcluded: Boolean): FilesCommand {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun populate(): PopulateCommand {
        return object : PopulateCommand {
            val args = accurev("pop").add("-L", workspace!!.remote)
            var shallow = false

            override fun stream(stream: String): PopulateCommand {
                args.add("-v", stream)
                return this
            }

            override fun shallow(): PopulateCommand {
                return this.shallow(true)
            }

            override fun shallow(shallow: Boolean): PopulateCommand {
                this.shallow = shallow
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

            override fun overrideFile(): PopulateCommand {
                args.add("-O")
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute(): String {
                return launchCommand(args)
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
            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun changeWS(): ChangeWSCommand {
        return object : ChangeWSCommand {
            val args = accurev("chws", false)

            override fun name(name: String): ChangeWSCommand {
                args.add("-w", name)
                return this
            }

            override fun location(location: String): ChangeWSCommand {
                args.add("-l", location)
                return this
            }

            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun getWorkspaces(): AccurevWorkspaces {
        val accurevWorkspaces = cachedAccurevWorkspaces[server, Callable {
            with(accurev("show", true)) {
                add("-a", "wspaces")
                return@Callable launch().unmarshal() as AccurevWorkspaces
            }
        }, this ] ?: AccurevWorkspaces()
        val accurevStreams = getStreams().map
        accurevWorkspaces.list.forEach {
            accurevStreams[it.name]?.let { stream ->
                if (it.stream != stream) it.stream = stream
            }
        }
        return accurevWorkspaces
    }

    override fun getReferenceTrees(): AccurevReferenceTrees {
        val accurevReferenceTrees = cachedAccurevReferenceTrees[server, Callable {
            with(accurev("show", true)) {
                add("refs")
                return@Callable launch().unmarshal() as AccurevReferenceTrees
            }
        }, this ] ?: AccurevReferenceTrees()
        val accurevStreams = getStreams().map
        accurevReferenceTrees.list.forEach {
            accurevStreams[it.name]?.let { stream ->
                if (it.stream != stream) it.stream = stream
            }
        }
        return accurevReferenceTrees
    }

    override fun getDepots(): AccurevDepots {
        return cachedAccurevDepots[server, Callable {
            with(accurev("show", true)) {
                add("depots")
                return@Callable launch().unmarshal() as AccurevDepots
            }
        }, this ] ?: AccurevDepots()
    }

    override fun getStreams(depot: String): AccurevStreams {
        val key = if (depot.isNotBlank()) "$server:$:$depot" else server
        return cachedAccurevStreams[key, Callable {
            with(accurev("show", true)) {
                if (depot.isNotBlank()) add("-p", depot)
                add("streams")
                return@Callable launch().unmarshal() as AccurevStreams
            }
        }, this, listOf("chstream", "defcomp", "mkstream")] ?: AccurevStreams()
    }

    override fun getFile(stream: String, path: String, transaction: String): String {
        try {
            populate().timespec(transaction).elements(hashSetOf(path)).stream(stream).overrideFile().execute()
        } catch (e: AccurevException) {
            return ""
        }
        val f = File("$workspace/$path")
        val content = f.readText()
        return content
    }

    override fun getFiles(stream: String): AccurevFiles {
        val key = if (stream.isNotBlank()) "$server:$:$stream" else server
        return cachedAccurevFiles[key, Callable {
            with(accurev("files", true)) {
                if (stream.isNotBlank()) add ("-s", stream)
                return@Callable launch().unmarshal() as AccurevFiles
            }
        }, this] ?: AccurevFiles()
    }

    override fun fileExists(name: String, stream: String): Boolean {
        val accurevFiles = getFiles(stream)
        if (accurevFiles.files.isEmpty()) return false
        return accurevFiles.files.contains(AccurevFile(name))
    }

    override fun fetchDepot(depot: String): AccurevDepot? {
        val depots = getDepots()
        return depots.map[depot]
    }

    override fun fetchStream(depot: String, stream: String): AccurevStream? {
        val streams = getStreams(depot)
        return streams.map[stream]
    }

    override fun fetchTransaction(stream: String): AccurevTransaction {
        with(accurev("hist", true)) {
            var accurevTransaction = add("-t", "now.1", "-s", stream).launch().unmarshal() as AccurevTransactions
            accurevTransaction.transactions[0].stream = stream
            return accurevTransaction.transactions[0]
        }
    }

    override fun fetchTransaction(stream: AccurevStream): AccurevTransaction {
        return fetchTransaction(stream.name)
    }

    override fun getChildStreams(depot: String, stream: String): AccurevStreams {
        val key = "$server:$:$depot:$:$stream"
        return cachedAccurevStreams[key, Callable {
            with(accurev("show", true)) {
                add("-R", "-s", stream, "streams")
                return@Callable launch().unmarshal() as AccurevStreams
            }
        }, this, listOf("chstream", "defcomp", "mkstream")] ?: AccurevStreams()
    }

    override fun getNDepthChildStreams(depot: String, stream: String, depth: Long): Collection<AccurevStream> {
        val streams = getChildStreams(depot, stream)
        val childStreams = mutableListOf<AccurevStream>()
        if (depth == 1L) {
            streams.list.forEach {
                if (it.parent != null) {
                    if (it.parent!!.name == stream) {
                        childStreams.add(it)
                    }
                }
            }
            return childStreams
        }
        var queue: MutableList<String> = mutableListOf(stream)

        for (i in 1..depth ) {
            val iterator = queue.iterator()
            val temp = mutableListOf<AccurevStream>()
            for (item in iterator) {
                val stream = streams.list.firstOrNull { x -> x.name == item }
                if (stream != null) {
                    if (stream.children.isNotEmpty()) {
                        temp.addAll(stream!!.children)
                        childStreams.addAll(stream!!.children)
                    }
                }
            }

            queue = mutableListOf()
            for (item in temp) { queue.add(item.name) }
        }
        return childStreams.distinct()
    }

    override fun fetchStreamTransactionHistory(stream: String, timeSpecLower: String, timeSpecUpper: String): AccurevTransactions {
        with(accurev("hist", true)) {
            val accurevTransactions = add("-t", "$timeSpecLower-$timeSpecUpper", "-s", stream).launch().unmarshal() as AccurevTransactions // Range
            accurevTransactions.transactions.forEach { transaction -> transaction.stream = stream }
            return accurevTransactions
        }
    }

    override fun fetchDepotTransactionHistory(depot: String, timeSpecLower: String, timeSpecUpper: String, types: Collection<String>): AccurevTransactions {
        with(accurev("hist", true)) {
            add("-t", "$timeSpecLower-$timeSpecUpper", "-p", depot)
            types.forEach {
                add("-k", it)
            }
            return launch().unmarshal() as AccurevTransactions // Range
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

    override fun getUpdatesFromAncestors( depot: String, stream: String, timeSpec: Long ): MutableList<AccurevTransaction> {
        var s = this.fetchStream(depot, stream)
        var ts = timeSpec
        var updates: MutableCollection<AccurevTransaction> = mutableListOf()

        while ( s != null ) {
            var listOfTransactions = fetchStreamTransactionHistory(s.name, (timeSpec + 1).toString())

            if (!listOfTransactions.transactions.isEmpty()) {
                listOfTransactions.transactions.forEach { at -> if (at.id > ts) updates.add(at) }
            }

            if (s != null) {
                if (s.type == (AccurevStreamType.Snapshot) || ((s.time != null) && (s.time!!.before( Date(System.currentTimeMillis()))))) {
                    break
                }
            }

            s = this.fetchStream(depot, s.parent?.name ?: "")
        }

        return (updates.sortedByDescending { x -> x.id }).toMutableList()
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

    override fun resetCaches(): Boolean {
        cachedAccurevDepots.evictAll()
        cachedAccurevFiles.evictAll()
        cachedAccurevReferenceTrees.evictAll()
        cachedAccurevWorkspaces.evictAll()
        cachedAccurevStreams.evictAll()
        return true
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
                    launcher!!.isUnix -> args.add("", true)
                    else -> args.addQuoted("", true)
                }
                return this
            }

            @Throws(AccurevException::class, InterruptedException::class)
            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    override fun logout(): LogoutCommand {
        return object : LogoutCommand {
            private val args = accurev("logout")
            override fun execute(): String {
                return launchCommand(args)
            }
        }
    }

    @JvmOverloads
    @Throws(AccurevException::class, InterruptedException::class)
    fun launchCommand(
            args: ArgumentListBuilder,
            ws: FilePath? = workspace,
            env: EnvVars = environment,
            timeout: Int = TIMEOUT
    ): String {
        val fos = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val environment = EnvVars(env)
        val command = args.toString()

        val p = launcher!!.launch().apply {
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
        @Transient private val cachedAccurevStreams: Cache<String, AccurevStreams> = Cache(3, TimeUnit.HOURS)
        @Transient private val cachedAccurevDepots: Cache<String, AccurevDepots> = Cache(3, TimeUnit.HOURS)
        @Transient private val cachedAccurevWorkspaces: Cache<String, AccurevWorkspaces> = Cache(3, TimeUnit.HOURS)
        @Transient private val cachedAccurevReferenceTrees: Cache<String, AccurevReferenceTrees> = Cache(3, TimeUnit.HOURS)
        @Transient private val cachedAccurevFiles: Cache<String, AccurevFiles> = Cache(3, TimeUnit.HOURS)
    }

    private fun ArgumentListBuilder.launch(): String = this@AccurevCliAPI.launchCommand(this)
}
