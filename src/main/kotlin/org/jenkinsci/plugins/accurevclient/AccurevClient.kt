package org.jenkinsci.plugins.accurevclient

import org.jenkinsci.plugins.accurevclient.commands.LoginCommand

interface AccurevClient {
    fun login(): LoginCommand

    companion object {
        val verbose = java.lang.Boolean.getBoolean("${AccurevClient::class.java.name}.verbose")
    }
}
