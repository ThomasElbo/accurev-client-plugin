package jenkins.plugins.accurevclient.commands



interface AddCommand : AccurevCommand {
    fun Add(files: List<String>) : AddCommand

    fun Comment(comment: String) : AddCommand

}