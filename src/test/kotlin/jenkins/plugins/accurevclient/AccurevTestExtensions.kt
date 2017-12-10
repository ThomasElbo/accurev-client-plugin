@file:JvmName("AccurevTestUtils")
package jenkins.plugins.accurevclient

fun String.checkCommandExist(): Boolean {
    val which = if (System.getProperty("os.name").toLowerCase().contains("windows")) "where" else "which"
    val cmd = "$which $this"
    return Runtime.getRuntime().exec(cmd).waitFor() == 0
}
