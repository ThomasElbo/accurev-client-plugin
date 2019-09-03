package jenkins.plugins.accurevclient;

import com.palantir.docker.compose.DockerComposeRule;
import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.plugins.accurevclient.model.AccurevFiles;
import jenkins.plugins.accurevclient.model.AccurevStream;
import jenkins.plugins.accurevclient.model.AccurevStreams;
import jenkins.plugins.accurevclient.model.AccurevTransaction;
import jenkins.plugins.accurevclient.model.AccurevWorkspace;
import jenkins.plugins.accurevclient.model.AccurevWorkspaces;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccurevClientJavaTest {
    @Rule public JenkinsRule rule = new JenkinsRule();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/docker/docker-compose.yml")
            .build();


    @Test public void loginCommand() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());

        mkWorkspace(streamName, client);

        File f = new File(project.getBuildDir() + "/file");
        f.createNewFile();
        List<String> files = new ArrayList<>();
        files.add(f.getName());
        client.add().Add(files).Comment("Initial").execute();
        client.keep().comment("Initial").files(files).execute();
        client.promote().files(files).comment("Initial promote").execute();

        AccurevTransaction transaction = client.fetchTransaction(streamName);
        assertEquals("Initial promote" , transaction.getComment());
        assertEquals(6, transaction.getId());
    }

    @Test public void fetchStream() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());
    }

    @Test public void getUpdatedParents() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());


        mkWorkspace(depotName, client);

        File f = new File(project.getBuildDir() + "/file");
        f.createNewFile();
        List<String> files = new ArrayList<>();
        files.add(f.getName());
        client.add().Add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        Collection<AccurevTransaction> as = client.getUpdatesFromAncestors(depotName, streamName, client.fetchTransaction(streamName).getId());

        assertEquals(1, as.size());
        assertEquals("file to depotStream", as.iterator().next().getComment());
    }

    @Test public void testGetFiles() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());


        mkWorkspace(streamName, client);
        File f = new File(project.getBuildDir() + "/file");
        f.createNewFile();
        List<String> files = new ArrayList<>();
        files.add(f.getName());
        client.add().Add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        AccurevFiles accurevfiles = client.getFiles(streamName);
        assertEquals(1, accurevfiles.getFiles().size());
        assertTrue(accurevfiles.getFiles().iterator().next().getLocation().contains("file"));
    }

    @Test public void testFindFile() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());


        mkWorkspace(streamName, client);

        File f = new File(project.getBuildDir() + "/file");
        f.createNewFile();
        List<String> files = new ArrayList<>();
        files.add(f.getName());
        client.add().Add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        assertTrue(client.fileExists("file", streamName));
    }
    
    @Test public void testChangeWorkspace() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
        assumeTrue("Can only run test with proper test setup",
                AccurevTestUtils.checkCommandExist("accurev") &&
                        StringUtils.isNotBlank(url) &&
                        StringUtils.isNotBlank(username) &&
                        StringUtils.isNotEmpty(password)
        );
        FreeStyleProject project = rule.createFreeStyleProject();
        Accurev accurev = Accurev.with(TaskListener.NULL, new EnvVars())
                .at(temp.getRoot()).on(url);
        AccurevClient client = accurev.getClient();

        client.login().username(username).password(Secret.fromString(password)).execute();
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);

        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());
        String workspace = mkWorkspace(streamName, client);

        assertTrue(client.getInfo().getInWorkspace());
        //temp.newFile("newWS");

        temp.newFolder("newWS");
        File f = new File(temp.getRoot() + "/newWS");
        assertTrue(f.exists());

        client.changeWS().name(workspace).location(f.getAbsolutePath()).execute();
        assertFalse(client.getInfo().getInWorkspace());

        AccurevWorkspaces accurevWorkspace = client.getWorkspaces();
        String storage = accurevWorkspace.getList().get(0).getStorage();
        String replace = f.getAbsolutePath().replace('\\', '/');
        assertEquals(storage, replace);
        Files.deleteIfExists(f.toPath());
    }


    @Test public void getNDepthChildStreams() throws Exception {
        String url = System.getenv("_ACCUREV_URL") == "" ? System.getenv("_ACCUREV_URL") : "localhost:5050";
        String username = System.getenv("_ACCUREV_USERNAME") != null ? System.getenv("_ACCUREV_URL") : "accurev_user";
        String password = System.getenv("_ACCUREV_PASSWORD") != null ? System.getenv("_ACCUREV_URL") : "docker";
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
        assertTrue(client.getInfo().getLoggedIn());

        String depotName = mkDepot(client);
        String streamD1 = mkStream(depotName, client);
        String streamD2 = mkStream(streamD1, client);

        /**
         *  depotName
         *      - streamD1
         *          - streamD2
         *              - streamD3
         */
        Collection<AccurevStream> streams = client.getNDepthChildStreams(depotName, depotName, 1L);


        assertEquals(1, streams.size());
        assertThat(streams, contains(
                Matchers.hasProperty("name", is(streamD1))
        ));

        streams = client.getNDepthChildStreams(depotName, depotName, 2L);

        assertEquals(2, streams.size());

        assertThat(streams, containsInAnyOrder(
                Matchers.hasProperty("name", is(streamD1)),
                Matchers.hasProperty("name", is(streamD2))
        ));

        String streamD3 = mkStream(streamD2, client);

        streams = client.getNDepthChildStreams(depotName, depotName, 3L);
        assertEquals(3, streams.size());
        assertThat(streams, containsInAnyOrder(
                Matchers.hasProperty("name", is(streamD1)),
                Matchers.hasProperty("name", is(streamD2)),
                Matchers.hasProperty("name", is(streamD3))
        ));

        /**
         *  depotName
         *      - streamD1
         *          - streamD2
         *              - streamD3
         *      - streamD11
         */
        String streamD11 = mkStream(depotName, client);

        streams = client.getNDepthChildStreams(depotName, depotName, 1L);
        assertEquals(2, streams.size());
        assertThat(streams, containsInAnyOrder(
                Matchers.hasProperty("name", is(streamD1)),
                Matchers.hasProperty("name", is(streamD11))
        ));
        /**
         *  depotName
         *      - streamD1
         *          - streamD2
         *              - streamD3
         *      - streamD11
         *          - streamD22
         */
        String streamD22 = mkStream(streamD11, client);
        streams = client.getNDepthChildStreams(depotName, depotName, 3L);
        assertEquals(5, streams.size());
        assertThat(streams, containsInAnyOrder(
                Matchers.hasProperty("name", is(streamD1)),
                Matchers.hasProperty("name", is(streamD2)),
                Matchers.hasProperty("name", is(streamD3)),
                Matchers.hasProperty("name", is(streamD11)),
                Matchers.hasProperty("name", is(streamD22))
        ));



    }


    private String mkDepot(AccurevClient client) throws Exception {
        String depot = generateString(10);
        client.depot().create(depot).execute();
        return depot;
    }

    private String mkStream(String depot, AccurevClient client) throws Exception {
        String stream = generateString(10);
        client.stream().create(stream, depot).execute();
        return stream;
    }

    private String mkWorkspace(String stream, AccurevClient client) throws Exception {
        String workspace = generateString(10);
        client.workspace().create(workspace, stream).execute();
        return workspace;
    }

    private String generateString(int count){
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
