package jenkins.plugins.accurevclient.commands

import com.sun.corba.se.spi.ior.ObjectId
import java.io.Writer

interface ChangelogCommand : AccurevCommand {
    fun excludes(var1: String): ChangelogCommand

    fun excludes(var1: ObjectId): ChangelogCommand

    fun includes(var1: String): ChangelogCommand

    fun includes(var1: ObjectId): ChangelogCommand

    fun to(var1: Writer): ChangelogCommand

    fun max(var1: Int): ChangelogCommand

    fun abort()
}