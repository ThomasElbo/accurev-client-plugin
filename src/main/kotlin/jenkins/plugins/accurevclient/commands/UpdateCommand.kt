package jenkins.plugins.accurevclient.commands

/**
 * Useful command for checking changes that includes parent stream
 */
interface UpdateCommand : AccurevCommand {
    fun referenceTree(referenceTree: String): UpdateCommand

    fun stream(stream: String): UpdateCommand

    fun range(latestTransaction: Long, previousTransaction: Long): UpdateCommand

    fun preview(output: MutableList<String>): UpdateCommand
}
