package com.phyous

import com.phyous.simcity.model.Direction
import org.junit.Test
import kotlin.test.assertEquals

class DirectionTest {

    @Test fun testParseDirectionBoundary() {
        // Test boundary conditions
        assertEquals(Direction.N, Direction.parse(-1.0))
        assertEquals(Direction.N, Direction.parse(0.0))
        assertEquals(Direction.N, Direction.parse(0.0))
        assertEquals(Direction.N, Direction.parse(0.1))
        assertEquals(Direction.N, Direction.parse(22.5))
        assertEquals(Direction.NW, Direction.parse(22.51))
    }

    @Test fun testParseDirectionAll() {
        // Make sure all cardinal directions work as expected
        assertEquals(Direction.N, Direction.parse(0.0))
        assertEquals(Direction.NW, Direction.parse(45.0))
        assertEquals(Direction.W, Direction.parse(90.0))
        assertEquals(Direction.SW, Direction.parse(135.0))
        assertEquals(Direction.S, Direction.parse(180.0))
        assertEquals(Direction.SE, Direction.parse(225.0))
        assertEquals(Direction.E, Direction.parse(270.0))
        assertEquals(Direction.NE, Direction.parse(315.0))
        assertEquals(Direction.NE, Direction.parse(337.5))
    }
}
