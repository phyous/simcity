package com.phyous.simcity.util

import com.github.kittinunf.result.Result
import com.phyous.simcity.model.Direction
import com.phyous.simcity.model.Edge
import com.phyous.simcity.model.GeoMap
import com.phyous.simcity.model.Node
import java.util.PriorityQueue

/**
 * Data structure used to model the cost of traveling to a Node.
 */
data class NodeCost(val node: Node, val cost: Double) {
    override fun equals(other: Any?): Boolean {
        if (other is Node) {
            return this.node == other
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return this.node.hashCode()
    }
}

/**
 * Implementation of a min heap for node distances.
 * Used in implementation of A*.
 */
class NodeDistanceHeap : PriorityQueue<NodeCost>(
        Comparator { (_, c1), (_, c2) ->
            val cost = c1 - c2
            when {
                cost < 0f -> -1
                cost > 0f -> 1
                else -> 0
            }
        }) {
    fun contains(element: Node): Boolean {
        return super.contains(NodeCost(element, 1.0))
    }
}

/**
 * Implementation of A* using pseudocode described here:
 * https://en.wikipedia.org/wiki/A*_search_algorithm
 */
fun GeoMap.path(start: Node, goal: Node): Result<List<Edge>, Exception> {
    val closedSet = HashSet<Node>()
    val openSet = NodeDistanceHeap()
    openSet.add(NodeCost(start, distanceMeters(start, goal)))

    val cameFrom = HashMap<Node, Edge>()
    // For each node, the cost of getting from the start node to that node.
    val gScore = HashMap<Node, Double>().withDefault { x -> Double.MAX_VALUE }
    gScore.put(start, 0.0)

    while (openSet.isNotEmpty()) {
        val current = openSet.poll().node

        if (current == goal) {
            return Result.of { reconstructPath(cameFrom, current) }
        }
        closedSet.add(current)
        this.getChildren(current).forEach { nodeRoute: GeoMap.NodeRoute ->
            val neighbor = nodeRoute.node
            if (closedSet.contains(neighbor)) return@forEach

            val curGscore = gScore[current] as Double
            val tentativeScore = curGscore + distanceMeters(current, neighbor)

            if(!openSet.contains(neighbor)) {
                openSet.add(NodeCost(neighbor, tentativeScore + distanceMeters(neighbor, goal)))
            } else if (tentativeScore > curGscore) {
                return@forEach
            }

            cameFrom.put(neighbor, Edge(current, neighbor, nodeRoute.way))
            gScore.put(neighbor, tentativeScore)
        }
    }

    return Result.error(Exception("Path not found"))
}

/**
 * Given a node, walk the visited nodes in cameFrom and reconstruct the path taken to the origin.
 * Node in cameFrom map is the destination.
 */
fun reconstructPath(cameFrom: Map<Node, Edge>, current: Node): List<Edge> {
    // Ordered list of edges that make up the path
    val totalPath = ArrayList<Edge>()
    totalPath.add(Edge(current, current, null))
    // the fromNode we use to construct an Edge
    var cur = current

    while (cur in cameFrom.keys) {
        val nextEdge = cameFrom[cur] as Edge
        totalPath.add(nextEdge)
        cur = nextEdge.fromNode
    }
    return totalPath.reversed()
}

/**
 * Get the angle of the line going from node1 to node2.
 */
fun angle(n1: Node, n2: Node): Double {
    val dLon: Double = (n2.lng - n1.lng)

    val y: Double = Math.sin(dLon) * Math.cos(n2.lat)
    val x: Double = Math.cos(n1.lat) * Math.sin(n2.lat) - Math.sin(n1.lat) * Math.cos(n2.lat) * Math.cos(dLon)

    var brng: Double = Math.atan2(y, x)

    brng = Math.toDegrees(brng)
    brng = (brng + 360) % 360

    return brng
}

/**
 * Get the angle formed form node1 to node2 as a cardinal direction.
 */
fun angleCardinal(n1: Node, n2: Node): Direction {
    return Direction.parse(angle(n1, n2))
}

/**
 * Distance between two nodes using haversine formula:
 * http://www.movable-type.co.uk/scripts/latlong.html
 *
 * This will be our heuristic function for A*
 */
fun distanceMeters(from: Node, to: Node): Double {
    val R = 6371e3
    val φ1 = from.lat.toRadians()
    val φ2 = to.lat.toRadians()
    val Δφ = (to.lat - from.lat).toRadians()
    val Δλ = (to.lng - from.lng).toRadians()
    val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
                    Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val d = R * c
    return d
}

fun distanceMiles(from: Node, to: Node): Double {
    return distanceMeters(from, to) * 0.000621371
}

fun distanceFeet(from: Node, to: Node): Double {
    return distanceMeters(from, to) * 3.28084
}

fun Double.toRadians(): Double {
    return (this * Math.PI) / 180.0
}