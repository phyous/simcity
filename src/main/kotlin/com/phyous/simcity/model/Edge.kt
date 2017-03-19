package com.phyous.simcity.model

/**
 * An edge represents two nodes connected by a specific way.
 * Multiple Edges are useful for representing a path through a GeoMap.
 */
data class Edge(val fromNode:Node, val toNode:Node, val way:Way?) {

    override fun toString(): String {
        val distMiles = distanceMiles(fromNode, toNode)
        if (way != null) {
            val name: String = way.tags.getOrElse("name") { way.tags.get("highway") as String}
            val direction = angleCardinal(fromNode, toNode)
            return String.format("[%d] %.2f miles %s on %s", way.id, distMiles, direction, name)
        } else {
            return String.format("Destination @ lat:[%.4f] lng:[%.4f]", fromNode.lat, fromNode.lng)
        }
    }
}