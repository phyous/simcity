package com.phyous.simcity.model

enum class Direction {
    N,
    NW,
    W,
    SW,
    S,
    SE,
    E,
    NE;

    companion object {
        val directionCount = Direction.values().size
        val directionAngle = 360.0 / directionCount

        fun parse(angle: Double): Direction {
            val modAngle = angle % 360
            Direction.values().forEachIndexed { index, direction ->
                val delta = Math.abs(modAngle - index * directionAngle)
                if (delta <= directionAngle / 2)  {
                    return direction
                }
            }
            return NE
        }
    }
}