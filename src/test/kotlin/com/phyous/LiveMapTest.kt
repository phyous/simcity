package com.phyous

import com.phyous.simcity.model.GeoMap
import com.phyous.simcity.util.distanceMiles
import com.phyous.simcity.util.path
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue
import kotlin.test.fail

class LiveMapTest {

    companion object {
        val map = GeoMap(File("./data/san_francisco.map"))
    }

    @Test fun testIntersectionLookup() {
        val lookup = map.lookupNode("25th", "Mississippi")
        assertTrue(lookup != null)
        assertTrue(lookup?.id == 65288943L)
    }

    @Test fun testGetDirections() {
        val from = map.lookupNode("25th", "Mississippi")
        val to = map.lookupNode("Mission", "1st")

        if (from != null && to != null) {
            map.path(from, to).fold({ result ->
                result.forEach(::println)
                println(String.format("Total trip distance: %.4f miles",
                        result.fold(0.0) { acc, edge -> acc + distanceMiles(edge.fromNode, edge.toNode) }))
            }, { error ->
                fail(error.toString())
            })
        } else {
            fail("couldn't find from and to nodes")
        }

    }
}