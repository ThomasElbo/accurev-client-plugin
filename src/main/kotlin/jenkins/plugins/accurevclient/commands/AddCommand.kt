package jenkins.plugins.accurevclient.commands

interface AddCommand : AccurevCommand {
    fun add(files: List<String>): AddCommand

    fun comment(comment: String): AddCommand
}