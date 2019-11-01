package jenkins.plugins.accurevclient;

import com.palantir.docker.compose.DockerComposeRule;
import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultipleRepositoriesTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Rule
    public DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/docker/docker-compose-multiple.yml")
            .build();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static String url;
    private static String username;
    private static String password;

    @BeforeClass
    public static void init() {
        url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
        assumeTrue("Can only run test with proper test setup",
                AccurevTestUtils.checkCommandExist("accurev") &&
                        StringUtils.isNotBlank(url) &&
                        StringUtils.isNotBlank(username) &&
                        StringUtils.isNotEmpty(password)
        );
    }

    private AccurevClient client;

    @Before
    public void setup() throws IOException, InterruptedException {
        Accurev accurev = Accurev.with(TaskListener.NULL, new EnvVars())
                .at(temp.getRoot()).on(url);
        client = accurev.getClient();
        client.login().username(username).password(Secret.fromString(password)).execute();
        assertTrue(client.getInfo().getLoggedIn());
    }


    @Test
    public void switchRepository() throws InterruptedException {
        assertTrue(client.getInfo().getLoggedIn());
        assertEquals(url, client.getInfo().getServerName() + ":" + client.getInfo().getServerPort());
        url = "localhost:5051";
        Accurev accurev = Accurev.with(TaskListener.NULL, new EnvVars())
                .at(temp.getRoot()).on(url);
        AccurevClient client1 = accurev.getClient();
        client1.logout();
        client1.login().username(username).password(Secret.fromString(password)).execute();

        assertEquals(url, client1.getInfo().getServerName() + ":" + client1.getInfo().getServerPort());
    }
}
