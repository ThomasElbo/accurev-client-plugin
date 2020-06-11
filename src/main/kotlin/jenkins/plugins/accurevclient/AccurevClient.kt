package jenkins.plugins.accurevclient

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey
import com.cloudbees.plugins.credentials.CredentialsMatcher
import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
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
import jenkins.plugins.accurevclient.model.AccurevStreamType

interface AccurevClient {

    var credentials: StandardUsernamePasswordCredentials?

    fun login(): LoginCommand

    fun logout(): LogoutCommand

    fun hist(): HistCommand

    fun update(): UpdateCommand

    fun populate(): PopulateCommand

    fun files(): FilesCommand

    fun keep(): KeepCommand

    fun promote(): PromoteCommand

    fun stream(): StreamCommand

    fun depot(): DepotCommand

    fun workspace(): WorkspaceCommand

    fun add(): AddCommand

    fun changeWS(): ChangeWSCommand

    fun syncTime()

    fun getVersion(): String

    fun getWorkspaces(): AccurevWorkspaces

    fun getReferenceTrees(): AccurevReferenceTrees

    fun getDepots(): AccurevDepots

    fun getStreams(depot: String = ""): AccurevStreams

    fun getActiveElements(stream: String = ""): AccurevFiles

    fun getActiveTransactions(stream: String = ""): AccurevTransactions

    fun fetchDepot(depot: String): AccurevDepot?

    fun fetchStream(depot: String, stream: String): AccurevStream?

    fun fetchStreams(depot: String, types: Collection<AccurevStreamType>): Collection<AccurevStream>

    fun fetchChildStreams(depot: String, stream: String, types: Collection<AccurevStreamType>): Collection<AccurevStream>

    fun fetchTransaction(stream: String): AccurevTransaction

    fun fetchTransaction(stream: AccurevStream): AccurevTransaction

    fun fetchTransaction(stream: String, transaction: Long): AccurevTransaction

    fun fetchTransaction(stream: AccurevStream, transaction: Long): AccurevTransaction

    fun fetchTransaction(stream: AccurevStream, transaction: AccurevTransaction): AccurevTransaction

    fun getChildStreams(depot: String, stream: String): AccurevStreams

    fun fetchStreamTransactionHistory(stream: String, timeSpecLower: String, timeSpecUpper: String = "now"): AccurevTransactions

    fun getUpdatesFromAncestors( depot: String, stream: String, timeSpec: Long ): MutableCollection<AccurevTransaction>

    fun getInfo(): AccurevInfo

    fun getUpdatedElements(
        stream: String,
        latestTransaction: Long,
        previousTransaction: Long,
        referenceTree: Boolean = false
    ): AccurevUpdate

    companion object {
        val verbose = java.lang.Boolean.getBoolean("${AccurevClient::class.java.name}.verbose")
        val CREDENTIALS_MATCHER: CredentialsMatcher = CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials::class.java), CredentialsMatchers.instanceOf(SSHUserPrivateKey::class.java))
    }

    fun getFile(stream: String, path: String, transaction: String = "highest"): String

    fun fileExists(name: String, stream: String): Boolean

    fun getFiles(stream: String): AccurevFiles

    fun fetchDepotTransactionHistory(depot: String, timeSpecLower: String, timeSpecUpper: String, types: Collection<String>): AccurevTransactions

    fun getNDepthChildStreams(depot: String, stream: String, depth: Long): Collection<AccurevStream>

    fun resetCaches(): Boolean
}
