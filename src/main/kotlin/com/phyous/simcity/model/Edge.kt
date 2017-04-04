package com.phyous.simcity.model

import com.phyous.simcity.util.angleCardinal
import com.phyous.simcity.util.distanceMiles

/**
 * An edge represents two nodes connected by a specific way.
 * Multiple Edges are useful for representing a path through a GeoMap.
 */
class Edge(val fromNode:Node, val toNode:Node, val way:Way?) {

    fun name(): String? {
        return way?.tags?.getOrElse("name") { way.tags.get("highway") as String}
    }

    override fun toString(): String {
        val distMiles = distanceMiles(fromNode, toNode)
        val name = this.name()
        if (name != null && way != null) {
            val direction = angleCardinal(fromNode, toNode)
            return String.format("[%d] %.2f miles %s on %s", way.id, distMiles, direction, name)
        } else {
            return String.format("Destination @ lat:[%.4f] lng:[%.4f]", fromNode.lat, fromNode.lng)
        }
    }
}