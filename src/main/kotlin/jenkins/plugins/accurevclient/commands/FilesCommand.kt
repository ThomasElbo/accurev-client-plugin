package jenkins.plugins.accurevclient.commands
/**
 * Command used to calculate total history however to need to traverse up the backing stream
 */
interface FilesCommand : AccurevCommand {
    fun stream(stream: String) : FilesCommand

    fun overwrite(overwrite: Boolean) : FilesCommand

    fun ignore (files: Set<String>) : FilesCommand

    fun addExcluded (addExcluded : Boolean) : FilesCommand
}
