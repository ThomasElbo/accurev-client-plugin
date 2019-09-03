package jenkins.plugins.accurevclient.commands

interface PromoteCommand : AccurevCommand {

    fun comment(comment: String): PromoteCommand
    fun files(files: List<String>): PromoteCommand
}