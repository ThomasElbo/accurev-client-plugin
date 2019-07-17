package jenkins.plugins.accurevclient;

import hudson.EnvVars;
import hudson.model.TaskListener;
import hudson.util.Secret;
import hudson.util.StreamTaskListener;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class PopulateCommandTest {

    AccurevClient client;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public void createPopulateTestRepo() throws IOException, InterruptedException {
        EnvVars env = new hudson.EnvVars();
        TaskListener listener = StreamTaskListener.fromStdout();
        File workspace = temporaryFolder.newFolder();
        client = Accurev.with(listener, env).at(workspace).getClient();
        Secret s = Secret.fromString("password");
        client.login().username("user").password(s).execute();
    }

}
