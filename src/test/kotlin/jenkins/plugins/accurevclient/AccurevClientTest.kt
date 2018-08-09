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
import org.junit.Assume.*
import org.junit.Before

class AccurevClientTest {
    @Rule @JvmField val rule = JenkinsRule()


    @Test fun loginCommand() {
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


class AccurevClientTestWithLogin {

	@Rule @JvmField val rule = JenkinsRule()

	@Test fun `Get all depots`(){
		val depots = client.getDepots()
		assertNotNull(depots)
	}

	@Test fun `Get all streams`(){
		val streams = client.getStreams()
		assertNotNull(streams)
	}

	@Test fun `Fetch single depot`(){
		val depots  = client.getDepots()
		assumeNotNull(depots)
		val depot = client.fetchDepot(depots.list[0].name)
		assertNotNull(depot)

	}

	@Test fun `Fetch single stream`(){
		val streams = client.getStreams()
		assumeNotNull(streams)
		val stream = client.fetchStream(streams.list[0].depotName, streams.list[0].name)
		assertNotNull(stream)
	}

	@Test fun `Fetch transaction with String`(){
		val streams = client.getStreams()
		assumeNotNull(streams)
		val transaction = client.fetchTransaction(streams.list[0].name)
		assertNotNull(transaction)
	}

	@Test fun `Fetch transaction with AccurevStream`(){
		val streams = client.getStreams()
		assumeNotNull(streams)
		val transaction = client.fetchTransaction(streams.list[0])
		assertNotNull(transaction)
	}

	@Test fun `Get child streams of AccurevStream`(){
		val streams = client.getStreams()
		assumeNotNull(streams)
		val childStreams = client.getChildStreams(streams.list[0].depotName, streams.list[0].name)
		assertNotNull(childStreams)
	}

	@Test fun `Get Accurev reference trees`(){
		val referenceTrees = client.getReferenceTrees()
		assertNotNull(referenceTrees)
	}

	lateinit var client : AccurevClient

	@Before
	fun setupClient(){
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
		client = accurev.client
		with(client.login()) {
			username(username)
			password(hudson.util.Secret.fromString(password))
			execute()
		}
	}

}