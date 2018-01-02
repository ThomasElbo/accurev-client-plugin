package jenkins.plugins.accurevclient.model

import jenkins.plugins.accurevclient.utils.TimestampAdapter
import java.util.Date
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevReferenceTrees(
    @field:XmlElement(name = "Element")
    val elements: MutableList<AccurevReferenceTree> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevReferenceTree(
    @field:XmlAttribute(name = "Name")
    val name: String = "",
    @field:XmlAttribute(name = "Storage")
    val storage: String = "",
    @field:XmlAttribute(name = "Host")
    val host: String = "",
    @field:XmlAttribute(name = "Stream")
    val stream: Long = 0,
    @field:XmlAttribute(name = "Trans")
    val transaction: Long = 0,
    @field:XmlAttribute
    val depot: String = "",
    @field:XmlAttribute(name = "user_name")
    val username: String = "",
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute(name = "fileModTime")
    val lastModified: Date = Date(0)
)
