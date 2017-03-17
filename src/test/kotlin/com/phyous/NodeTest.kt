package com.phyous

import com.phyous.simcity.model.Node
import com.phyous.simcity.model.angle
import org.junit.Test
import kotlin.test.assertTrue

class NodeTest {

    @Test fun testAngle() {
        val n1: Node = Node(1, 100.0, 100.0)
        val n2: Node = Node(1, 100.0, 100.001)

        assertTrue(angle(n1, n1) == 0.0)
        val ninety = angle(n1, n2)
        assertTrue(90.1 > ninety && ninety > 89.9)
    }
}