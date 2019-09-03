package jenkins.plugins.accurevclient

class MkFunctions(client: AccurevClient) {

    private val client: AccurevClient = client

    @Throws(Exception::class)
    fun mkDepot(): String {
        val depot = generateString(10)
        client.depot().create(depot).execute()
        return depot
    }

    @Throws(Exception::class)
    fun mkStream(depot: String): String {
        val stream = generateString(10)
        client.stream().create(stream, depot).execute()
        return stream
    }

    @Throws(Exception::class)
    fun mkWorkspace(stream: String): String {
        val workspace = generateString(10)
        client.workspace().create(workspace, stream).execute()
        return workspace
    }

    private fun generateString(count: Int): String {
        var count = count
        val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val builder = StringBuilder()
        while (count-- != 0) {
            val character = (Math.random() * ALPHA_NUMERIC_STRING.length).toInt()
            builder.append(ALPHA_NUMERIC_STRING[character])
        }
        return builder.toString()
    }
}