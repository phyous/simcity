package com.phyous

import com.phyous.simcity.model.GeoMap
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class LiveMapTest {

    companion object {
        val map = GeoMap(File("/Users/phil/workspace/personal/simcity/data/san_francisco_pretty_printed.map"))
    }

    @Test fun testStreetLookup() {
        val lookup = map.lookupNode("25th", "Mississippi")
        assertTrue(lookup != null)
        assertTrue(lookup?.id == 65288943L)
    }
}