package jenkins.plugins.accurevclient.commands

import hudson.FilePath

interface PopulateCommand : AccurevCommand {
    fun stream(stream: String): PopulateCommand

    fun overwrite(overwrite: Boolean): PopulateCommand

    fun timespec(timespec: String): PopulateCommand

    fun elements(set: Set<String>): PopulateCommand

    fun listFile(listFile: FilePath): PopulateCommand
    fun shallow(shallow: Boolean): PopulateCommand
    fun shallow(): PopulateCommand
}
