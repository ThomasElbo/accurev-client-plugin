package org.jenkinsci.plugins.accurevclient.utils

import java.time.Instant
import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

class TimestampAdapter : XmlAdapter<Long, Date>() {
    override fun marshal(date: Date?): Long = date?.toInstant()?.epochSecond ?: Instant.now().epochSecond

    override fun unmarshal(timestamp: Long?): Date = timestamp?.let { Date.from(Instant.ofEpochSecond(it)) } ?: Date()
}
