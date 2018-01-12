package jenkins.plugins.accurevclient

import jenkins.plugins.accurevclient.commands.HistCommand
import jenkins.plugins.accurevclient.commands.LoginCommand
import jenkins.plugins.accurevclient.commands.PopulateCommand
import jenkins.plugins.accurevclient.commands.UpdateCommand
import jenkins.plugins.accurevclient.model.AccurevDepots
import jenkins.plugins.accurevclient.model.AccurevInfo
import jenkins.plugins.accurevclient.model.AccurevReferenceTrees
import jenkins.plugins.accurevclient.model.AccurevStreams
import jenkins.plugins.accurevclient.model.AccurevUpdate
import jenkins.plugins.accurevclient.model.AccurevWorkspaces

interface AccurevClient {
    fun login(): LoginCommand

    fun hist(): HistCommand

    fun update(): UpdateCommand

    fun populate(): PopulateCommand

    fun syncTime()

    fun getVersion(): String

    fun getWorkspaces(): AccurevWorkspaces

    fun getReferenceTrees(): AccurevReferenceTrees

    fun getDepots(): AccurevDepots

    fun getStreams(depot: String = ""): AccurevStreams

    fun getChildStreams(depot: String, stream: String): AccurevStreams

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
