package org.jenkinsci.plugins.accurevclient.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "streams")
data class AccurevStreams(
    @field:XmlElement(name = "stream")
    val streams: MutableList<AccurevStream> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
data class AccurevStream(
    @field:XmlAttribute(required = true)
    val name: String = "",
    @field:XmlAttribute
    val depotName: String = "",
    @field:XmlAttribute(required = true)
    val streamNumber: Int = 0,
    @field:XmlAttribute
    val basisStreamNumber: Int? = null,
    @field:XmlAttribute(name = "isDynamic")
    val dynamic: Boolean = false,
    @field:XmlAttribute
    val type: String = "",
    @field:XmlAttribute
    val startTime: String = ""
)
