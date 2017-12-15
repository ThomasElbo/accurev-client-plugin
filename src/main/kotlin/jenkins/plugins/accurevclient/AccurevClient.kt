package jenkins.plugins.accurevclient

import jenkins.plugins.accurevclient.commands.HistCommand
import jenkins.plugins.accurevclient.commands.LoginCommand

interface AccurevClient {
    fun login(): LoginCommand

    fun hist(): HistCommand

    companion object {
        val verbose = java.lang.Boolean.getBoolean("${AccurevClient::class.java.name}.verbose")
    }
}
