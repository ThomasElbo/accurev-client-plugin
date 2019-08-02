package jenkins.plugins.accurevclient

import java.sql.Timestamp
import java.util.LinkedHashMap
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class Cache<K, V> @JvmOverloads constructor(duration: Int, unit: TimeUnit, maxEntries: Int = MAX_ENTRIES_DEFAULT) {

    private val entries: MutableMap<K, Entry<V>>

    private val expireAfterNanos: Long = unit.toNanos(duration.toLong())

    private val lastUpdate: Long = Timestamp(System.currentTimeMillis()).nanos.toLong()


    init {
        this.entries = LimitedMap(maxEntries)
    }

    @Synchronized
    @Throws(ExecutionException::class)
    operator fun get(key: K, callable: Callable<V>, client: AccurevClient? = null, types: Collection<String>? = emptyList()): V? {
        if (isExpired(key)) {
            doRemove(key)
        }

        if (!types!!.isEmpty()){
            val result = client!!.hist().depot(key.toString()).timeSpec("$lastUpdate-now").execute()
            if(result.equals(10)) {

            }
        }

        if (entries.containsKey(key)) {
            return entries[key]?.value
        }

        val result: V
        try {
            result = callable.call()
        } catch (e: Exception) {
            throw ExecutionException("Cannot load value for key: " + key, e)
        }

        return doPut(key, result)
    }

    fun evictAll() {
        entries.clear()
    }

    fun size(): Int {
        return entries.size
    }

    private fun isExpired(key: K): Boolean {
        val entry = entries[key]
        return entry != null && System.nanoTime() - entry.nanos > expireAfterNanos
    }

    private fun doRemove(key: K) {
        entries.remove(key)
    }

    private fun doPut(key: K, value: V): V {
        entries.put(key, Entry(value))
        return value
    }

    private class LimitedMap<K, V>(private val maxEntries: Int) : LinkedHashMap<K, V>() {

        override fun removeEldestEntry(eldest: kotlin.collections.Map.Entry<K, V>?): Boolean {
            return size > maxEntries
        }
    }

    private class Entry<out V>(val value: V) {
        val nanos: Long = System.nanoTime()
    }

    companion object {
        @JvmStatic private val MAX_ENTRIES_DEFAULT = 100
    }
}