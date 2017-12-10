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
@XmlRootElement(name = "streams")
data class Streams(
    @field:XmlElement(name = "stream")
    val streams: MutableList<Stream> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Stream(
    @field:XmlAttribute(required = true)
    val name: String = "",
    @field:XmlAttribute
    val depotName: String = "",
    @field:XmlAttribute(required = true)
    val streamNumber: Int = 0,
    @field:XmlAttribute
    val basisStreamNumber: Int? = null,
    @field:XmlAttribute(name = "isDynamic")
    val dynamic: Boolean = false,
    @field:XmlAttribute
    val type: StreamType = StreamType.NORMAL,
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute
    val startTime: Date = Date()
)
