package jenkins.plugins.accurevclient.model

import jenkins.plugins.accurevclient.utils.AccurevPathAdapter
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
data class Transactions(
    @field:XmlElement(name = "transaction")
    val transactions: MutableList<Transaction> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Transaction(
    @field:XmlAttribute(required = true)
    val id: Int = 0,
    @field:XmlAttribute
    val user: String = "",
    @field:XmlAttribute
    val type: String = "",
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute
    val startTime: Date = Date(),
    @field:XmlElement
    val comment: String = "",
    @field:XmlElement
    val version: Version? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Version(
    @field:XmlAttribute(required = true)
    @field:XmlJavaTypeAdapter(AccurevPathAdapter::class)
    val path: String = "",
    @field:XmlAttribute
    val eid: Int = 0
)
