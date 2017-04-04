package com.phyous

import com.phyous.simcity.model.*
import com.phyous.simcity.util.path
import com.phyous.simcity.util.reconstructPath
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.test.assertEquals

class GeoMapTest {

    companion object {
        /*
            Build the following map:
            n1-e1-n2
            |      |
            e2    e3
            |      |
            n3-e4-n4-e4-n5-e4-n6
        */
        val simpleMap = GeoMap(
                listOf(
                        Node(1, 0.0, 1.0),
                        Node(2, 1.0, 1.0),
                        Node(3, 0.0, 0.0),
                        Node(4, 1.0, 0.0),
                        Node(5, 2.0, 0.0),
                        Node(6, 3.0, 0.0)),
                listOf(
                        Way(1, listOf(1, 2)),
                        Way(2, listOf(1, 3)),
                        Way(3, listOf(2, 4)),
                        Way(4, listOf(3, 4, 5, 6))))
    }

    @Test fun testBuildMap() {
        assertTrue("There should be 4 edges", simpleMap.wayMap.size == 4)
        assertTrue("There should be 6 nodes", simpleMap.nodeMap.size == 6)
    }

    @Test fun testGetChildrenSimple() {
        // Test that getting children of a node connected
        // through an way that connects only 2 nodes returns correctly
        val children = simpleMap.getChildren(1)
        val nodeIds = children.map { n -> n.node.id }
        assertTrue(nodeIds.contains(2))
        assertTrue(nodeIds.contains(3))
    }

    @Test fun testGetChildrenComposite() {
        val children = simpleMap.getChildren(4)
        val nodeIds = children.map { n -> n.node.id }
        assertTrue(nodeIds.contains(2))
        assertTrue(nodeIds.contains(3))
        assertTrue(nodeIds.contains(5))
    }

    @Test fun testReconstructPath() {
        val n1 = Node(1, 0.0, 1.0)
        val n2 = Node(2, 1.0, 1.0)
        val n3 = Node(3, 0.0, 0.0)
        val w1 = Way(1, listOf(1,2))
        val w2 = Way(2, listOf(2,3))

        val cameFrom = mapOf(Pair(n2, Edge(n1, n2, w1)), Pair(n3, Edge(n2, n3, w2)))
        val ret = reconstructPath(cameFrom, n3)
        assertTrue(ret[0].fromNode == n3)
        assertTrue(ret[0].toNode == n3)
        assertTrue(ret[0].way == null)
        assertTrue(ret[1].fromNode == n2)
        assertTrue(ret[2].fromNode == n1)
    }

    @Test fun testAstar() {
        val from = simpleMap.node(2)
        val to = simpleMap.node(5)
        if (from != null && to != null)
            simpleMap.path(from, to).fold({
              path -> assertEquals(path.size, 3)
            }, {
                error -> fail(error.message)
            })
        else
            fail("expected nodes not found")
    }

}
