package jenkins.plugins.accurevclient;

import com.palantir.docker.compose.DockerComposeRule;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.plugins.accurevclient.model.AccurevFiles;
import jenkins.plugins.accurevclient.model.AccurevStream;
import jenkins.plugins.accurevclient.model.AccurevStreams;
import jenkins.plugins.accurevclient.model.AccurevTransaction;
import jenkins.plugins.accurevclient.model.AccurevWorkspace;
import jenkins.plugins.accurevclient.model.AccurevWorkspaces;
import kotlin.jvm.JvmField;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccurevClientJavaTest {
    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/docker/docker-compose.yml")
            .build();

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
    private FreeStyleProject project;
    private AccurevClient client;

    @Before
    public void setup() throws IOException, InterruptedException {
        project = rule.createFreeStyleProject();
        Accurev accurev = Accurev.with(TaskListener.NULL, new EnvVars(), new Launcher.LocalLauncher(TaskListener.NULL))
                .at(project.getBuildDir()).on(url);
        client = accurev.getClient();
        client.login().username(username).password(Secret.fromString(password)).execute();
        assertTrue(client.getInfo().getLoggedIn());
    }

    @After
    public void tearDown() {
        client.logout();
        client.resetCaches();

    }



    @Test public void fetchLatestTransaction() throws Exception {
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
        client.add().add(files).comment("Initial").execute();
        client.keep().comment("Initial").files(files).execute();
        client.promote().files(files).comment("Initial promote").execute();

        AccurevTransaction transaction = client.fetchTransaction(streamName);
        assertEquals("Initial promote" , transaction.getComment());
        assertEquals(6, transaction.getId());
    }

    @Test public void fetchStream() throws Exception {
        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);
        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());

        AccurevStream generatedStream = streams.getList().get(0);
        assertEquals(depotName, generatedStream.getDepotName());
        assertEquals(streamName, generatedStream.getName());
    }

    @Test public void getUpdatedParents() throws Exception {
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
        client.add().add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        Collection<AccurevTransaction> as = client.getUpdatesFromAncestors(depotName, streamName, client.fetchTransaction(streamName).getId());

        assertEquals(1, as.size());
        assertEquals("file to depotStream", as.iterator().next().getComment());
    }

    @Test public void testGetFiles() throws Exception {
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
        client.add().add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        AccurevFiles accurevfiles = client.getFiles(streamName);
        assertEquals(1, accurevfiles.getFiles().size());
        assertTrue(accurevfiles.getFiles().iterator().next().getLocation().contains("file"));
    }

    @Test public void testFindFile() throws Exception {
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
        client.add().add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();

        assertTrue(client.fileExists("file", streamName));
    }
    
    @Test public void testChangeWorkspace() throws Exception {
        String depotName = mkDepot(client);
        String streamName = mkStream(depotName, client);

        AccurevStreams streams = client.getChildStreams(depotName, streamName);

        assertEquals(1, streams.getList().size());
        String workspace = mkWorkspace(streamName, client);

        assertTrue(client.getInfo().getInWorkspace());

        temp.newFolder("newWS");
        File f = new File(temp.getRoot() + "/newWS");
        assertTrue(f.exists());

        client.changeWS().name(workspace).location(f.getAbsolutePath()).execute();
        assertFalse(client.getInfo().getInWorkspace());

        AccurevWorkspaces accurevWorkspace = client.getWorkspaces();
        List<AccurevWorkspace> wsList = accurevWorkspace.getList();
        Optional<AccurevWorkspace> ws = wsList.stream().filter(wspace -> wspace.getName().contains(workspace)).findAny();

        assertEquals(f.getAbsolutePath(), ws.get().getStorage().replace("/", "\\"));
    }


    @Test public void getNDepthChildStreams() throws Exception {
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

    @Test public void getActiveElements() throws Exception {
        String depotName = mkDepot(client);
        String streamD1 = mkStream(depotName, client);
        /**
         *  depotName
         *      - streamD1
         *          - streamD2
         */
        mkWorkspace(streamD1, client);
        File f = new File(project.getBuildDir() + "/file");
        f.createNewFile();
        List<String> files = new ArrayList<>();
        files.add(f.getName());
        client.add().add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();
        assertEquals(client.getActiveElements(depotName).getFiles().size(),0);
        assertEquals(client.getActiveElements(streamD1).getFiles().size(),1);

        f = new File(project.getBuildDir() + "/file2");
        f.createNewFile();
        files = new ArrayList<>();
        files.add(f.getName());
        client.add().add(files).execute();
        client.keep().files(files).comment("").execute();
        client.promote().files(files).comment("file to depotStream").execute();
        assertEquals(client.getActiveElements(streamD1).getFiles().size(),2);
    }


    private String mkDepot(AccurevClient client) throws Exception {
        String depot = generateString(10);
        client.depot().create(depot).execute();
        return depot;
    }

    private String mkStream(String depot, AccurevClient client) throws Exception {
        String stream = generateString(10);
        String execute = client.stream().create(stream, depot).execute();
        assertTrue(execute.contains(stream));
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
