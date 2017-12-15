package jenkins.plugins.accurevclient.utils

import java.time.Instant
import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

class TimestampAdapter : XmlAdapter<Long, Date>() {
    override fun marshal(date: Date?): Long = date?.toInstant()?.epochSecond ?: Instant.now().epochSecond

    override fun unmarshal(timestamp: Long?): Date = timestamp?.let { Date.from(Instant.ofEpochSecond(it)) } ?: Date()
}

class AccurevPathAdapter : XmlAdapter<String, String>() {
    override fun marshal(path: String): String = "/./$path"

    override fun unmarshal(path: String): String = path.toAccurevPath()
}
