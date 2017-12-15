package jenkins.plugins.accurevclient.commands

interface HistCommand : AccurevCommand {
    fun depot(depot: String): HistCommand

    fun stream(stream: String): HistCommand

    fun timeSpec(timeSpec: String): HistCommand
}
