package jenkins.plugins.accurevclient.commands

import hudson.util.Secret

interface LoginCommand : AccurevCommand {
    fun username(username: String): LoginCommand

    fun password(password: Secret): LoginCommand
}
