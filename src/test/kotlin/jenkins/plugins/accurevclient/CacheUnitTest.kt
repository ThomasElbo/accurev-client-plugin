package jenkins.plugins.accurevclient

import hudson.util.ArgumentListBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class CacheUnitTest {

    @Test
    fun testEmptyCache() {
        val cacheUnit: Cache<String, CacheUnitTest> = Cache(1, TimeUnit.MINUTES)
        assertEquals(cacheUnit.size(), 0)
    }

    @Test
    fun testInitialCache() {
        val cache: Cache<String, TestObject> = Cache(1, TimeUnit.MINUTES)

        assertEquals(cache.size(), 0)

        val items = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(items.name, "CacheEntry1")
        assertEquals(cache.size(), 1)
    }

    @Test
    fun testNoDuplicateEntries() {
        val cache: Cache<String, TestObject> = Cache(1, TimeUnit.MINUTES)

        assertEquals(cache.size(), 0)

        val item1 = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(item1.name, "CacheEntry1")
        assertEquals(cache.size(), 1)

        val item2 = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(item1.name, item2.name)
        assertEquals(cache.size(), 1)
    }

    @Test
    fun testItemNotInCache() {
        val cache: Cache<String, TestObject> = Cache(1, TimeUnit.MINUTES)

        assertEquals(cache.size(), 0)

        val item1 = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(cache.size(), 1)
        assertNotEquals(item1.name, "CacheEntry2")

        val item2 = cache["testKey2", Callable {
            with(argumentBuilder("CacheEntry2")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(item2.name, "CacheEntry2")
        assertEquals(cache.size(), 2)
    }

    @Test
    fun testReplaceKeyWhenFullCache() {
        val cache: Cache<String, TestObject> = Cache(1, TimeUnit.MINUTES, 1)

        assertEquals(0, cache.size())

        val item1 = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(cache.size(), 1)
        assertEquals(item1.name, "CacheEntry1")

        val item2 = cache["testKey1", Callable {
            with(argumentBuilder("CacheEntry2")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(cache.size(), 1)
        assertEquals(item2.name, "CacheEntry2")
    }

    @Test
    fun testClearCache() {
        val cache: Cache<String, TestObject> = Cache(1, TimeUnit.MINUTES, 1)

        assertEquals(0, cache.size())

        val item1 = cache["testKey", Callable {
            with(argumentBuilder("CacheEntry1")) {
                return@Callable launch()
            }
        } ] ?: TestObject()

        assertEquals(1, cache.size())
        assertEquals("CacheEntry1", item1.name)

        cache.evictAll()

        assertEquals(0, cache.size())
    }

    private fun argumentBuilder(cmd: String) = ArgumentListBuilder().apply {
        add(cmd)
    }

    private fun ArgumentListBuilder.launch(): TestObject = this@CacheUnitTest.emulateLaunchCommand(this)

    private fun emulateLaunchCommand(args: ArgumentListBuilder): TestObject {
        val list = args.toList()
        val requestedKeys: MutableList<TestObject> = arrayListOf()
        list.forEach { word -> requestedKeys.add(TestObject(word)) }
        return requestedKeys[0]
    }

    data class TestObject(
            val name: String = ""
    )
}