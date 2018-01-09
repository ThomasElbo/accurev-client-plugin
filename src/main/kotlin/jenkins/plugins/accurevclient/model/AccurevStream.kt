package jenkins.plugins.accurevclient.model

import jenkins.plugins.accurevclient.model.AccurevStreamType.Gated
import jenkins.plugins.accurevclient.model.AccurevStreamType.Normal
import jenkins.plugins.accurevclient.model.AccurevStreamType.PassThrough
import jenkins.plugins.accurevclient.model.AccurevStreamType.Snapshot
import jenkins.plugins.accurevclient.model.AccurevStreamType.Staging
import jenkins.plugins.accurevclient.model.AccurevStreamType.Workspace
import jenkins.plugins.accurevclient.utils.TimestampAdapter
import java.util.Date
import javax.xml.bind.Unmarshaller
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
@XmlRootElement(name = "streams")
data class AccurevStreams(
    @field:XmlElement(name = "stream")
    val list: MutableList<AccurevStream> = mutableListOf()
) {

    lateinit var map: Map<String, AccurevStream>

    @Suppress("unused", "UNUSED_PARAMETER")
    fun afterUnmarshal(unmarshaller: Unmarshaller, any: Any) {
        map = list.associateBy { it.name }
        map.values.filter { it.basisName != null }.forEach { it.parent = map[it.basisName] }
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevStream(
    @field:XmlAttribute(required = true)
    val name: String = "",
    @field:XmlAttribute
    val depotName: String = "",
    @field:XmlAttribute(required = true)
    val streamNumber: Long = 0,
    @field:XmlAttribute(name = "basis")
    val basisName: String? = null,
    @field:XmlAttribute
    val basisStreamNumber: Long? = null,
    @field:XmlAttribute(name = "isDynamic")
    val dynamic: Boolean = false,
    @field:XmlAttribute
    val type: AccurevStreamType = Normal,
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute
    val startTime: Date = Date(0),
    @field:XmlJavaTypeAdapter(TimestampAdapter::class)
    @field:XmlAttribute
    val time: Date? = null
) {
    @Transient
    var children = mutableSetOf<AccurevStream>()

    @Transient
    var parent: AccurevStream? = null
        set(value) {
            if (field != value) {
                field?.children?.remove(this)
                field = value
                field?.children?.add(this)
            }
        }

    fun isReceivingChangesFromParent(): Boolean {
        return when(type) {
            Normal -> time == null
            Snapshot -> false
            Workspace, PassThrough, Gated, Staging -> true
        }
    }
}

@XmlType
@XmlEnum
enum class AccurevStreamType(val type: String) {
    @XmlEnumValue("normal")
    Normal("normal"),
    @XmlEnumValue("snapshot")
    Snapshot("snapshot"),
    @XmlEnumValue("workspace")
    Workspace("workspace"),
    @XmlEnumValue("passthrough")
    PassThrough("passthrough"),
    @XmlEnumValue("gated")
    Gated("gated"),
    @XmlEnumValue("staging")
    Staging("staging")
}
