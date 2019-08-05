package jenkins.plugins.accurevclient

import com.palantir.docker.compose.DockerComposeRule
import hudson.EnvVars
import hudson.model.TaskListener
import hudson.util.Secret
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasProperty
import org.junit.Assert.*
import org.junit.Assume
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import org.hamcrest.CoreMatchers.`is` as Is

class CacheTest {

    @Rule
    @JvmField
    val rule = JenkinsRule()

    companion object {
        @ClassRule @JvmField
        var docker = DockerComposeRule.builder()
                .file("src/docker/docker-compose.yml")
                .build()
    }



    @Test
    fun testCacheServerValidation() {

        val url = System.getenv("_ACCUREV_URL") ?: "localhost:5050"
        val username = System.getenv("_ACCUREV_USERNAME") ?: "accurev_user"
        val password = System.getenv("_ACCUREV_PASSWORD") ?: "docker"
        Assume.assumeTrue("Can only run test with proper test setup",
                "accurev".checkCommandExist() &&
                        url.isNotBlank() &&
                        username.isNotBlank() &&
                        password.isNotEmpty()
        )

        val project = rule.createFreeStyleProject()
        val accurev = Accurev.with(TaskListener.NULL, EnvVars())
                .at(project.buildDir).on(url)
        val client = accurev.client

        client.login().username(username).password(Secret.fromString(password)).execute()
        assertTrue(client.getInfo().loggedIn)

        val create = MkFunctions(client)

        val depot = create.mkDepot()
        val stream = create.mkStream(depot)


        var streams = client.getStreams(depot)

        assertEquals(2, streams.list.size)
        assertThat(streams.list, contains(
                hasProperty("name", Is(depot)),
                hasProperty("name", Is(stream))
        ))

        val stream2 = create.mkStream(depot)
        streams = client.getStreams(depot)

        assertEquals(3, streams.list.size)
        assertThat(streams.list, contains(
                hasProperty("name", Is(depot)),
                hasProperty("name", Is(stream)),
                hasProperty("name", Is(stream2))
        ))

    }

}