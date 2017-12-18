package jenkins.plugins.accurevclient

import jenkins.plugins.accurevclient.commands.HistCommand
import jenkins.plugins.accurevclient.commands.LoginCommand
import jenkins.plugins.accurevclient.commands.PopulateCommand
import jenkins.plugins.accurevclient.commands.UpdateCommand

interface AccurevClient {
    fun login(): LoginCommand

    fun hist(): HistCommand

    fun update(): UpdateCommand

    fun populate(): PopulateCommand

    companion object {
        val verbose = java.lang.Boolean.getBoolean("${AccurevClient::class.java.name}.verbose")
    }
}
