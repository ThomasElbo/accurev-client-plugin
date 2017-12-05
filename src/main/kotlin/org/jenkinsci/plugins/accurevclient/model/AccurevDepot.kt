package org.jenkinsci.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcResponse")
data class AccurevDepots(
    @field:XmlAttribute(name = "Command")
    val command: String = "",
    @field:XmlAttribute(name = "TaskId")
    val taskId: String = "",
    @field:XmlElement(name = "Element")
    val elements: MutableList<AccurevDepot> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevDepot(
    @field:XmlAttribute(name = "Number")
    val number: String = "",
    @field:XmlAttribute(name = "Name")
    val name: String = ""
)
