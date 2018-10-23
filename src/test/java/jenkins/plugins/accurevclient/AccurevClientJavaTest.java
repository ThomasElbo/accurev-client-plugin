package jenkins.plugins.accurevclient;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.plugins.accurevclient.model.AccurevStream;
import jenkins.plugins.accurevclient.model.AccurevStreams;
import jenkins.plugins.accurevclient.model.AccurevTransaction;
import jenkins.plugins.accurevclient.model.AccurevTransactions;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

import static org.junit.Assume.assumeTrue;

public class AccurevClientJavaTest {
    @Rule public JenkinsRule rule = new JenkinsRule();

    @Test public void loginCommand() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "TMEL_SERVER2016.wassts.org:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "TMEL";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "Widex123";
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

    @Test public void fetchLatestTransaction() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "TMEL_SERVER2016.wassts.org:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "TMEL";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "Widex123";
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

        AccurevTransaction dummyProj = client.fetchTransaction("Dummyproj_test");
        System.out.println(dummyProj);
        dummyProj.getId();

    }

    @Test public void fetchStream() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "TMEL_SERVER2016.wassts.org:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "TMEL";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "Widex123";
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

        //AccurevTransactions ats = client.fetchStreamTransactionHistory("now.10", "dummyproj_test");

        //for(AccurevTransaction at : ats.getTransactions())
        AccurevStreams ass = client.getChildStreams("DummyProj", "Dummyproj_test");
        ass.getList().removeAll(Collections.singleton(null));
        for(AccurevStream as : ass.getList()){
            AccurevStream ap = as.getParent();
            System.out.print("Stream: " + as.getName());
            if (ap != null){
                if((ap.getStreamNumber() < as.getStreamNumber()))
                System.out.println(" has " + ap.getName() + " as parent.");

            }

        }
    }

    @Test public void getUpdatedParents() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "TMEL_SERVER2016.wassts.org:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "TMEL";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "Widex123";
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

        AccurevStream as = client.getUpdatesFromParents("DummyProj", "LowestStream", client.fetchTransaction("LowestStream").getId());

        System.out.println("LowestStream has id " + client.fetchTransaction("LowestStream").getId());
        System.out.println( "Highest stream: " + as.getName() + ", with id: " + client.fetchTransaction(as.getName()).getId());

        for(AccurevStream a : client.getChildStreams("DummyProj", as.getName()).getList()){
            System.out.println("Streams that needs to be updated: " + a.getName());
        }
    }

}
