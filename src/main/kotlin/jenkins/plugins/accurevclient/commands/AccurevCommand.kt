package jenkins.plugins.accurevclient.commands

import jenkins.plugins.accurevclient.AccurevException

interface AccurevCommand {
    @Throws(AccurevException::class, InterruptedException::class)
    fun execute()
}
