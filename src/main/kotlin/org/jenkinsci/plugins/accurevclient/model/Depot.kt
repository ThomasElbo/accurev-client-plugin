package org.jenkinsci.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class Depots(
    @field:XmlElement(name = "Element")
    val elements: MutableList<Depot> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Depot(
    @field:XmlAttribute(name = "Number")
    val number: String = "",
    @field:XmlAttribute(name = "Name")
    val name: String = ""
)
