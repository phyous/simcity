package com.phyous.simcity.model

import com.github.kittinunf.result.Result
import java.util.PriorityQueue

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

// Implementation of a min heap for node distances
class NodeDistanceHeap : PriorityQueue<NodeCost>(
        kotlin.Comparator { (_, c1), (_, c2) ->
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
 * A* using pseudocode described here:
 * https://en.wikipedia.org/wiki/A*_search_algorithm
 */
fun GeoMap.path(start: Node, goal: Node): Result<List<Node>, Exception> {
    val closedSet = HashSet<Node>()
    val openSet = NodeDistanceHeap()
    openSet.add(NodeCost(start, distanceBetween(start, goal)))

    val cameFrom = HashMap<Node, Node>()
    // For each node, the cost of getting from the start node to that node.
    val gScore = HashMap<Node, Double>().withDefault { x -> Double.MAX_VALUE }
    gScore.put(start, 0.0)

    while (openSet.isNotEmpty()) {
        val current = openSet.poll().node

        if (current == goal) {
            return Result.of { reconstructPath(cameFrom, current) }
        }
        closedSet.add(current)
        this.getChildren(current).forEach { (neighbor, _): GeoMap.NodeRoute ->
            if (closedSet.contains(neighbor)) return@forEach

            val curGscore = gScore[current] as Double
            val tentativeScore = curGscore + distanceBetween(current, neighbor)

            if(!openSet.contains(neighbor)) {
                openSet.add(NodeCost(neighbor, tentativeScore + distanceBetween(neighbor, goal)))
            } else if (tentativeScore > curGscore) {
                return@forEach
            }

            cameFrom.put(neighbor, current)
            gScore.put(neighbor, tentativeScore)
        }
    }

    return Result.error(Exception("Path not found"))
}

/**
 * Given a node, walk the visited nodes in cameFrom and reconstruct the path taken to the origin
 */
fun reconstructPath(cameFrom: Map<Node, Node>, current: Node): List<Node> {
    val totalPath = mutableListOf(current)
    var cur = current
    while (cur in cameFrom.keys) {
        val nextNode = cameFrom[cur] as Node
        totalPath.add(nextNode)
        cur = nextNode
    }
    return totalPath
}


/**
 * Distance between two nodes using haversine formula:
 * http://www.movable-type.co.uk/scripts/latlong.html
 *
 * This will be our heuristic function for A*
 */
fun distanceBetween(from: Node, to: Node): Double {
    val R = 6371e3
    val φ1 = from.lat.toRadians()
    val φ2 = to.lat.toRadians()
    val Δφ = (to.lat - from.lat).toRadians()
    val Δλ = (to.lng - from.lng).toRadians()
    val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
                    Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    val d = R * c
    return d
}

fun Double.toRadians(): Double {
    return (this * Math.PI) / 180
}