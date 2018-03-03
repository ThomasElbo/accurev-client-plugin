package jenkins.plugins.accurevclient.model

import jenkins.plugins.accurevclient.utils.AccurevPathAdapter
import jenkins.plugins.accurevclient.utils.TimestampAdapter
import java.util.Date
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevTransactions(
    @field:XmlElement(name = "transaction")
    val transactions: MutableList<AccurevTransaction> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevTransaction(
    @field:XmlAttribute(required = true)
    val id: Long = 0,
    @field:XmlAttribute
    val user: String = "",
    @field:XmlAttribute
    val type: TransactionType = TransactionType.Promote,
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute
    val time: Date = Date(0),
    @field:XmlElement
    val comment: String = "",
    @field:XmlElement
    val version: AccurevTransactionVersion? = null,
    @field:XmlElement
    val stream: AccurevStream? = null
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevTransactionVersion(
    @field:XmlAttribute(required = true)
    @field:XmlJavaTypeAdapter(AccurevPathAdapter::class)
    val path: String = "",
    @field:XmlAttribute
    val eid: Long = 0,
    @field:XmlElement
    val issueNum: Long? = null
)

@XmlType
@XmlEnum
enum class TransactionType(val type: String) {
    @XmlEnumValue("add")
    Add("add"),
    @XmlEnumValue("archive")
    Archive("archive"),
    @XmlEnumValue("chstream")
    ChangeStream("chstream"),
    @XmlEnumValue("co")
    CheckOut("co"),
    @XmlEnumValue("defcomp")
    RuleChange("defcomp"),
    @XmlEnumValue("defunct")
    Deleted("defunct"),
    @XmlEnumValue("demote_from")
    DemoteFrom("demote_from"),
    @XmlEnumValue("demote_to")
    DemoteTo("demote_to"),
    @XmlEnumValue("dispatch")
    Dispatch("dispatch"),
    @XmlEnumValue("eacl")
    ElementACL("eacl"),
    @XmlEnumValue("keep")
    Keep("keep"),
    @XmlEnumValue("move")
    Move("move"),
    @XmlEnumValue("mkstream")
    MakeStream("mkstream"),
    @XmlEnumValue("promote")
    Promote("promote"),
    @XmlEnumValue("purge")
    Purge("purge"),
    @XmlEnumValue("unarchive")
    Unarchive("unarchive"),
    @XmlEnumValue("undefunct")
    Restore("undefunct")
}
