package jenkins.plugins.accurevclient

import jenkins.plugins.accurevclient.commands.*
import jenkins.plugins.accurevclient.model.*

interface AccurevClient {
    fun login(): LoginCommand

    fun hist(): HistCommand

    fun update(): UpdateCommand

    fun populate(): PopulateCommand

    fun changelog() : ChangelogCommand

    fun syncTime()

    fun getVersion(): String

    fun getWorkspaces(): AccurevWorkspaces

    fun getReferenceTrees(): AccurevReferenceTrees

    fun getDepots(): AccurevDepots

    fun getStreams(depot: String = ""): AccurevStreams

    fun fetchDepot(depot: String): AccurevDepot?

    fun fetchStream(depot: String, stream: String): AccurevStream?

    fun fetchTransaction(stream: String): AccurevTransaction

    fun fetchTransaction(stream: AccurevStream): AccurevTransaction

    fun getChildStreams(depot: String, stream: String): AccurevStreams

    fun fetchStreamTransactionHistory(stream: String, timeSpec: String) : AccurevTransactions

    fun getUpdatesFromParents( depot : String, stream : String, timeSpec : Long ) : AccurevStream?

    fun getInfo(): AccurevInfo

    fun getUpdatedElements(
        stream: String,
        latestTransaction: Long,
        previousTransaction: Long,
        referenceTree: Boolean = false
    ): AccurevUpdate

    companion object {
        val verbose = java.lang.Boolean.getBoolean("${AccurevClient::class.java.name}.verbose")
    }
}
