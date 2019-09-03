package jenkins.plugins.accurevclient.commands

interface DepotCommand : AccurevCommand {
    fun create(name: String): DepotCommand
}
