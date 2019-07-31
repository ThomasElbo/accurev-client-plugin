package jenkins.plugins.accurevclient.commands

interface KeepCommand : AccurevCommand {

    fun comment(comment: String) : KeepCommand

    fun recurse() : KeepCommand

    fun modified() : KeepCommand

    fun files(files: List<String>) : KeepCommand
}