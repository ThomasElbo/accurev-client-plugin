package jenkins.plugins.accurevclient.model

import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevDepots(
    @field:XmlElement(name = "Element")
    val list: MutableList<AccurevDepot> = mutableListOf()
) {
    @Transient lateinit var map: Map<String, AccurevDepot>

    @Suppress("unused", "UNUSED_PARAMETER")
    fun afterUnmarshal(unmarshaller: Unmarshaller, any: Any) {
        map = list.associateBy { it.name }
    }
}

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevDepot(
    @field:XmlAttribute(name = "Number")
    val number: Long = 0,
    @field:XmlAttribute(name = "Name")
    val name: String = ""
)
