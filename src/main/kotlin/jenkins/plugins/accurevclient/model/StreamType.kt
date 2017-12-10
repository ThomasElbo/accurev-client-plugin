package jenkins.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue
import javax.xml.bind.annotation.XmlType

@XmlType
@XmlEnum
enum class StreamType(val type: String) {
    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("snapshot")
    SNAPSHOT("snapshot"),
    @XmlEnumValue("workspace")
    WORKSPACE("workspace"),
    @XmlEnumValue("passthrough")
    PASSTHROUGH("passthrough"),
    @XmlEnumValue("gated")
    GATED("gated"),
    @XmlEnumValue("staging")
    STAGING("staging")
}
