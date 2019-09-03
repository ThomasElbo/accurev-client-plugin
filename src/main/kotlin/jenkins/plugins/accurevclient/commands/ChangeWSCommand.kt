package jenkins.plugins.accurevclient.commands

interface ChangeWSCommand : AccurevCommand {

    fun name(name: String): ChangeWSCommand

    fun location(location: String): ChangeWSCommand
}