package jenkins.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "workspaceInfo")
data class AccurevInfo(
    @field:XmlAttribute
    val principal: String = "",
    @field:XmlAttribute
    val host: String = "",
    @field:XmlAttribute
    val clientProtocolVersion: Int = 0,
    @field:XmlAttribute
    val serverProtocolVersion: Int = 0,
    @field:XmlAttribute
    val inWorkspace: Boolean = false,
    @field:XmlAttribute
    val workspace: String? = null,
    @field:XmlAttribute
    val serverName: String? = null,
    @field:XmlAttribute
    val serverPort: Int? = null,
    @field:XmlAttribute
    val depot: String? = null,
    @field:XmlAttribute
    val backingStream: String? = null,
    @field:XmlAttribute
    val workspaceTopDirectory: String? = null,
    @field:XmlAttribute
    val updateTrans: Long? = null,
    @field:XmlAttribute
    val targetTrans: Long? = null
) {
    val loggedOut: Boolean
        get() = principal == "(not logged in)"
    val loggedIn: Boolean
        get() = !loggedOut
}
