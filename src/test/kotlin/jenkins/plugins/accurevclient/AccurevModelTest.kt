package jenkins.plugins.accurevclient

import ch.tutteli.atrium.api.cc.en_UK.*
import ch.tutteli.atrium.verbs.expect.expect
import jenkins.plugins.accurevclient.model.*
import jenkins.plugins.accurevclient.utils.TimestampAdapter
import jenkins.plugins.accurevclient.utils.unmarshal
import org.junit.Test
import java.util.Date

class AccurevModelTest {
    private val timestampAdapter = TimestampAdapter()

    @Test fun depotModel() {
        val input = this.javaClass.getResourceAsStream("depots.xml")

        input.use { xml ->
            val depots = xml.unmarshal() as AccurevDepots
            println(depots)
            expect(depots) {
                property(it::list).hasSize(2)
            }
            expect(depots.list[0]) {
                property(it::number).toBe(1)
                property(it::name).toBe("accurev")
            }
        }
    }

    @Test fun streamModel() {
        val input = this.javaClass.getResourceAsStream("streams.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevStreams
            println(output)
            expect(output) {
                property(it::list).hasSize(10)
            }
            expect(output.list[0]) {
                property(it::name).toBe("accurev")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(1)
                property(it::basisStreamNumber).isNull()
                property(it::dynamic).isTrue()
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169249))
                property(it::type).toBe(AccurevStreamType.Normal)
                property(it::children).hasSize(6)
            }
            expect(output.list[1]) {
                property(it::name).toBe("accurev_josp")
                property(it::depotName).toBe("accurev")
                property(it::streamNumber).toBe(2)
                property(it::basisStreamNumber).isNotNull { toBe(1) }
                property(it::dynamic).isFalse()
                property(it::parent).isNotNull { toBe(output.list[0]) }
                property(it::startTime).toBe(timestampAdapter.unmarshal(1512169250))
                property(it::type).toBe(AccurevStreamType.Workspace)
            }
        }
    }

    @Test fun emptyStreamsModel() {
        val input = "<streams></streams>"
        val output = input.unmarshal() as AccurevStreams
        println(output)
        expect(output) {
            property(it::list).hasSize(0)
        }
    }

    @Test fun transactionModel() {
        val input = this.javaClass.getResourceAsStream("hist.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevTransactions
            println(output)
            expect(output.transactions[0]) {
                property(it::comment).toBe("c")
                property(it::id).toBe(13)
                property(it::type).toBe(TransactionType.Promote)
                property(it::user).toBe("josp")
                property(it::time).toBe(timestampAdapter.unmarshal(1512907647))
                property(it::version).isNotNull { toBe(AccurevTransactionVersion("bud", 3)) }
                property(it::stream).isNull()
            }
            expect(output.transactions[3]) {
                property(it::comment).toBe("")
                property(it::id).toBe(6)
                property(it::type).toBe(TransactionType.MakeStream)
                property(it::user).toBe("josp")
                property(it::time).toBe(timestampAdapter.unmarshal(1512907076))
                property(it::version).isNull()
                property(it::stream).isNotNull { toBe(AccurevStream(
                    "other_stream",
                    "accurev",
                    3,
                    "accurev",
                    1,
                    false,
                    AccurevStreamType.Normal,
                    Date(0),
                    Date(0)
                )) }
            }
        }
    }

    @Test fun updateModel() {
        val input = this.javaClass.getResourceAsStream("update.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevUpdate
            println(output)
            expect(output.elements[0]) {
                property(it::path).toBe("doubleDAMN")
            }
        }
    }

    @Test fun workspaceModel() {
        val input = this.javaClass.getResourceAsStream("workspaces.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevWorkspaces
            println(output)
            expect(output.list[0]) {
                property(it::name).toBe("accurev_josp")
            }
        }
    }

    @Test fun infoModelWithWorkspace() {
        val input = this.javaClass.getResourceAsStream("inworkspace-info.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevInfo
            println(output)
            expect(output) {
                property(it::host).toBe("joseph-laptop")
                property(it::loggedIn).toBe(true)
                property(it::loggedOut).toBe(false)
            }
        }
    }

    @Test fun infoModelLoggedOut() {
        val input = this.javaClass.getResourceAsStream("logged-out-info.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevInfo
            println(output)
            expect(output) {
                property(it::host).toBe("joseph-laptop")
                property(it::loggedIn).toBe(false)
                property(it::loggedOut).toBe(true)
            }
        }
    }

    @Test fun filesModel() {
        val input = this.javaClass.getResourceAsStream("files.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevFiles
            println(output)
            expect(output.files[0]) {
                property(it::status).toBe("(overlap)(member)")
                property(it::location).contains("Jenkinsfile")
                property(it::dir).toBe(false)
                property(it::executable).toBe(false)
                property(it::id).toBe(3)
                property(it::elemType).toBe(AccurevFileType.Text)
                property(it::size).toBe(248)
                property(it::modTime).toBe("1559907875")
                property(it::hierType).toBe("parallel")
                property(it::virtual).toBe("5\\2")
                property(it::namedVersion).toBe("TestStream\\2")
                property(it::real).toBe("6\\2")

            }
        }
    }
}
