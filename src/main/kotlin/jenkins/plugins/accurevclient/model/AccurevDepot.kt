package jenkins.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevDepots(
    @field:XmlElement(name = "Element")
    val elements: MutableList<AccurevDepot> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevDepot(
    @field:XmlAttribute(name = "Number")
    val number: Long = 0,
    @field:XmlAttribute(name = "Name")
    val name: String = ""
)
