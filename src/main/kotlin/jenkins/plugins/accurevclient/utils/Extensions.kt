package jenkins.plugins.accurevclient.utils

import hudson.FilePath
import hudson.model.Node
import hudson.util.Secret
import jenkins.model.Jenkins
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Reader
import java.nio.charset.Charset
import javax.xml.bind.JAXB

/**
 * Returns Root path from [Node] as [String]
 *
 * @return [String]
 */
fun FilePath.rootPath(): String = node().rootPath?.remote ?: parent.remote

/**
 * Returns [Node] from [FilePath] however if node is offline returns [Jenkins] node
 *
 * @return [Node]
 */
fun FilePath.node(): Node = toComputer()?.node ?: Jenkins.getInstance()

/**
 * Converts [Secret] to [String] and checks that it is not empty
 */
fun Secret.isNotEmpty(): Boolean = Secret.toString(this).isNotEmpty()

/**
 * Converts [ByteArrayOutputStream] to [String] with [Charset.defaultCharset]
 */
val ByteArrayOutputStream.defaultCharset: String
    get() = toString(Charset.defaultCharset().name())

/**
 * Returns sanitized Accurev Path as [String]
 * Replaces Windows backslash and removes Accurev's prefixed "/./"
 */
fun String.toAccurevPath(): String = replace("\\", "/").removePrefix("/./")

/**
 * Extend [InputStream] with [JAXB.unmarshal]
 */
inline fun <reified T> InputStream.unmarshal(): T = JAXB.unmarshal(this, T::class.java)

/**
 * Extend [Reader] with [JAXB.unmarshal]
 */
inline fun <reified T> Reader.unmarshal(): T = JAXB.unmarshal(this, T::class.java)

/**
 * Extend [String] with [JAXB.unmarshal], casting [String] to [Reader]
 */
inline fun <reified T> String.unmarshal(): T = JAXB.unmarshal(this.reader(), T::class.java)
