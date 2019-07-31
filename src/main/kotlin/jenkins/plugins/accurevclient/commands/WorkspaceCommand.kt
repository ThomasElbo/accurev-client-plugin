package jenkins.plugins.accurevclient.commands

interface WorkspaceCommand : AccurevCommand {

    fun create(name: String, backingStream: String) : WorkspaceCommand

}