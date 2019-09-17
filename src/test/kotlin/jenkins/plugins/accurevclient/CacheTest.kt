package jenkins.plugins.accurevclient

import ch.tutteli.atrium.verbs.assertthat.assertThat
import com.palantir.docker.compose.DockerComposeRule
import hudson.EnvVars
import hudson.model.TaskListener
import hudson.util.Secret
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.Rule
import org.junit.Test

import org.jvnet.hudson.test.JenkinsRule

class CacheTest {

    @Rule @JvmField
    val docker = DockerComposeRule.builder()
            .file("src/docker/docker-compose.yml")
            .build()!!
    @Rule
    @JvmField
    val rule = JenkinsRule()

    @Test
    fun testCacheServerValidation() {
        System.err.println("Test cache server validation")
        val url = System.getenv("_ACCUREV_URL") ?: "localhost:5050"
        val username = System.getenv("_ACCUREV_USERNAME") ?: "accurev_user"
        val password = System.getenv("_ACCUREV_PASSWORD") ?: "docker"
        Assume.assumeTrue("Can only run test with proper test setup",
                "accurev".checkCommandExist() &&
                        url.isNotBlank() &&
                        username.isNotBlank() &&
                        password.isNotEmpty()
        )
        System.err.println("Creating project")
        val project = rule.createFreeStyleProject()
        val accurev = Accurev.with(TaskListener.NULL, EnvVars())
                .at(project.buildDir).on(url)
        val client = accurev.client
        client.login().username(username).password(Secret.fromString(password)).execute()
        System.err.println("Assert logged in")
        assertTrue(client.getInfo().loggedIn)

        val create = MkFunctions(client)

        val depot = create.mkDepot()

        var streams = client.getStreams(depot)
        System.err.println("Assert stream added")
        assertEquals(1, streams.list.size)
        assertThat(streams.list, contains(
                hasProperty("name", equalTo(depot))
        ))

        val stream = create.mkStream(depot)
        val stream2 = create.mkStream(stream)

        streams = client.getStreams(depot)
        System.err.println("Assert updated cache list of 3 items")
        assertEquals(3, streams.list.size)
        assertThat(streams.list, contains(
                hasProperty("name", equalTo(depot)),
                hasProperty("name", equalTo(stream)),
                hasProperty("name", equalTo(stream2))
        ))
        System.err.println("Test ended")
    }
}