package jenkins.plugins.accurevclient.commands

/**
 * Command used to calculate total history however to need to traverse up the backing stream
 */
interface HistCommand : AccurevCommand {
    fun depot(depot: String): HistCommand

    fun stream(stream: String): HistCommand

    fun timeSpec(timeSpec: String): HistCommand
}
