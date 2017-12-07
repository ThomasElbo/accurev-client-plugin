package org.jenkinsci.plugins.accurevclient.utils

import java.time.Instant
import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

class TimestampAdapter : XmlAdapter<Long, Date>() {
    override fun marshal(date: Date?): Long {
        if (date == null) return Instant.now().epochSecond
        val instant = date.toInstant()
        if (instant == null) return Instant.now().epochSecond
        return instant.epochSecond
    }

    override fun unmarshal(timestamp: Long?): Date {
        if (timestamp == null) return Date()
        return Date.from(Instant.ofEpochSecond(timestamp))
    }
}
