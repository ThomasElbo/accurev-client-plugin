package jenkins.plugins.accurevclient

import hudson.EnvVars
import hudson.model.TaskListener
import hudson.util.Secret
import jenkins.plugins.accurevclient.model.AccurevDepot
import jenkins.plugins.accurevclient.model.AccurevDepots
import junit.framework.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.Assert
import org.junit.Assume.assumeNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before

class AccurevClientTest {
	@Rule
	@JvmField
	val rule = JenkinsRule()
	
	
	@Test
	fun loginCommand() {
		val url = System.getenv("_ACCUREV_URL") ?: "TMEL_SERVER2016.wassts.org:5050"
		val username = System.getenv("_ACCUREV_USERNAME") ?: "TMEL"
		val password = System.getenv("_ACCUREV_PASSWORD") ?: "Widex123"
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
		with(client.login()) {
			username(username)
			password(Secret.fromString(password))
			execute()
		}
	}
	
}