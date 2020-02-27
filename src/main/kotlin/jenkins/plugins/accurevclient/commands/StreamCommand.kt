package jenkins.plugins.accurevclient.commands

interface StreamCommand : AccurevCommand {

    fun create(name: String, backingStream: String): StreamCommand

    fun create(name: String, backingStream: String, isGated: Boolean): StreamCommand
}