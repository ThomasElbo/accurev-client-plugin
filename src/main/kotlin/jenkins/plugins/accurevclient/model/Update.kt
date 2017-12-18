package jenkins.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "acResponse")
data class Update(
    @field:XmlElement(name = "element")
    val elements: MutableList<Location> = mutableListOf()
)

data class Location(
    @field:XmlAttribute(name = "location")
    val path: String = ""
)
