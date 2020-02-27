package jenkins.plugins.accurevclient

import com.palantir.docker.compose.DockerComposeRule
import hudson.EnvVars
import hudson.Launcher
import hudson.model.TaskListener
import hudson.util.Secret
import junit.framework.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class AccurevClientTest {

    @Rule @JvmField
    val rule = JenkinsRule()

    @Rule
    @JvmField
    var docker = DockerComposeRule.builder()
            .file("src/docker/docker-compose.yml")
            .build()!!

    @Test
    fun loginCommand() {
        System.err.println("Running login test")
        val url = System.getenv("_ACCUREV_URL") ?: "localhost:5050"
        val username = System.getenv("_ACCUREV_USERNAME") ?: "accurev_user"
        val password = System.getenv("_ACCUREV_PASSWORD") ?: "docker"
        assumeTrue("Can only run test with proper test setup",
                        "accurev".checkCommandExist() &&
                                        url.isNotBlank() &&
                                        username.isNotBlank() &&
                                        password.isNotEmpty()
        )
        val accurev = Accurev.with(TaskListener.NULL, EnvVars(), Launcher.LocalLauncher(TaskListener.NULL)).on(url)
                        //.at(project.buildDir).on(url)
        val client = accurev.client
        with(client.login()) {
            username(username)
            password(Secret.fromString(password))
            execute()
        }
        assertTrue(client.getInfo().loggedIn)
    }
}