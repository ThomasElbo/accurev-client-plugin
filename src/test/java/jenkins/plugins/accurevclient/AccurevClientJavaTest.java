package jenkins.plugins.accurevclient;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assume.assumeTrue;

public class AccurevClientJavaTest {
    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test public void loginCommand() throws Exception {
        String url = System.getenv("_ACCUREV_URL");
        String username = System.getenv("_ACCUREV_USERNAME");
        String password = System.getenv("_ACCUREV_PASSWORD");
        assumeTrue("Can only run test with proper test setup",
        AccurevTestUtils.checkCommandExist("accurev") &&
                StringUtils.isNotBlank(url) &&
                StringUtils.isNotBlank(username) &&
                StringUtils.isNotEmpty(password)
        );
        FreeStyleProject project = rule.createFreeStyleProject();
        Accurev accurev = Accurev.with(TaskListener.NULL, new EnvVars())
            .at(project.getBuildDir()).on(url);
        AccurevClient client = accurev.getClient();
        client.login().username(username).password(Secret.fromString(password)).execute();
    }
}
