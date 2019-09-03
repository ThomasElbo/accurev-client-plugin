package jenkins.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevFiles(
        @field:XmlElement(name = "element")
        val files: MutableList<AccurevFile> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevFile(
        @field:XmlAttribute(required = true)
        val location: String = "",
        @field:XmlAttribute
        val dir: Boolean = false,
        @field:XmlAttribute
        val executable: Boolean = false,
        @field:XmlAttribute
        val id: Long = 0,
        @field:XmlAttribute
        val elemType: AccurevFileType = AccurevFileType.Text,
        @field:XmlAttribute(name = "Virtual")
        val virtual: String = "",
        @field:XmlAttribute
        val namedVersion: String = "",
        @field:XmlAttribute(name = "Real")
        val real: String = "",
        @field:XmlAttribute
        val status: String = "",
        @field:XmlAttribute
        val size: Long = 0,
        @field:XmlAttribute
        val modTime: String = "",
        @field:XmlAttribute
        val hierType: String = ""

) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as AccurevFile

                if (!other.location.contains(location)) return false

                return true
        }

        override fun hashCode(): Int {
                return location.hashCode()
        }
}

@XmlType
@XmlEnum
enum class AccurevFileType(val type: String) {
    @XmlEnumValue("text")
    Text("text"),
}
