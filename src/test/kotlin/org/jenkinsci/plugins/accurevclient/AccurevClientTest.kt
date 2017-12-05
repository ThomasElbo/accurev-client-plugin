package org.jenkinsci.plugins.accurevclient

import hudson.EnvVars
import hudson.model.TaskListener
import hudson.util.Secret
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class AccurevClientTest {
    @Rule @JvmField val rule = JenkinsRule()

    @Test fun loginCommand() {
        val url = System.getenv("_ACCUREV_URL") ?: ""
        val username = System.getenv("_ACCUREV_USERNAME") ?: ""
        val password = System.getenv("_ACCUREV_PASSWORD") ?: ""
        assumeTrue("Can only run test with proper test setup",
        "accurev".checkCommandExist() &&
            url.isNotBlank() &&
            username.isNotBlank() &&
            password.isNotEmpty()
        )
        val project = rule.createFreeStyleProject()
        val accurev = Accurev.with(TaskListener.NULL, EnvVars())
            .at(project.buildDir).on(url)
        val client = accurev.client
        with (client.login()) {
            username(username)
            password(Secret.fromString(password))
            execute()
        }
    }
}
