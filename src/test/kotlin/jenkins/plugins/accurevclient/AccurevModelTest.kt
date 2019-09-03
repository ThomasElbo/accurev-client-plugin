package jenkins.plugins.accurevclient

import ch.tutteli.atrium.api.cc.en_UK.contains
import ch.tutteli.atrium.api.cc.en_UK.hasSize
import ch.tutteli.atrium.api.cc.en_UK.it
import ch.tutteli.atrium.api.cc.en_UK.property
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.expect.expect
import ch.tutteli.atrium.api.cc.en_UK.isFalse
import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.isTrue
import jenkins.plugins.accurevclient.model.AccurevInfo
import jenkins.plugins.accurevclient.model.AccurevTransactions
import jenkins.plugins.accurevclient.model.AccurevUpdate
import jenkins.plugins.accurevclient.model.AccurevWorkspaces
import jenkins.plugins.accurevclient.model.AccurevFileType
import jenkins.plugins.accurevclient.model.AccurevFiles
import jenkins.plugins.accurevclient.model.AccurevDepots
import jenkins.plugins.accurevclient.model.AccurevStreamType
import jenkins.plugins.accurevclient.model.AccurevStreams
import jenkins.plugins.accurevclient.model.TransactionType

import jenkins.plugins.accurevclient.utils.TimestampAdapter
import jenkins.plugins.accurevclient.utils.unmarshal
import org.junit.Test

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
        val input = this.javaClass.getResourceAsStream("transaction.xml")

        input.use { xml ->
            val output = xml.unmarshal() as AccurevTransactions
            println(output)
            expect(output.transactions[1]) {
                property(it::comment).toBe("Test double keep")
                property(it::id).toBe(264)
                property(it::type).toBe(TransactionType.Keep)
                property(it::user).toBe("TMEL_CLIENT")
                property(it::time).toBe(timestampAdapter.unmarshal(1560839707))
                property(it::version).isNotNull { this.hasSize(2) }
                property(it::stream).isNull()
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
