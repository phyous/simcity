package com.phyous.simcity.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.kittinunf.result.Result
import java.io.File
import java.io.InputStream
import java.util.HashMap

class GeoMap {
    var nodeMap: MutableMap<Long, Node> = HashMap()
    var wayMap: MutableMap<Long, Way> = HashMap()
    // Maps the position of a node along a way
    var nodeWays: MutableMap<Long, MutableList<WayPosition>> = HashMap()

    // The position of a node in an way
    data class WayPosition(val way: Way, val position: Int)

    // A path to a node, through an way
    data class NodeRoute(val node: Node, val way: Way)

    constructor(file: File) {
        val parsedData = parse(file.inputStream())
        processData(parsedData.nodeMap.values.toList(), parsedData.wayMap.values.toList())
    }

    constructor(nodes: List<Node>, ways: List<Way>) {
        processData(nodes, ways)
    }

    fun getChildren(node: Node): List<NodeRoute> {
        return nodeWays.getOrDefault(node.id, mutableListOf()).map { (way, position) ->
            val oneWay = way.tags.getOrDefault("oneway", "no") == "yes"
            val next = if (position < way.nodes.size - 1) way.nodes[position + 1] else null
            val previous = if (!oneWay && position > 0) way.nodes[position - 1] else null
            listOfNotNull(
                    if (next != null) NodeRoute(nodeMap.get(next) as Node, way) else null,
                    if (previous != null) NodeRoute(nodeMap.get(previous) as Node, way) else null)
        }.flatMap { x -> x }
    }

    fun getChildren(nodeId: Long): List<NodeRoute> {
        val node = nodeMap.get(nodeId)
        return if (node != null) getChildren(node) else listOf()
    }

    fun node(nodeId: Long): Node? {
        return nodeMap.get(nodeId)
    }

    private fun processData(nodes: List<Node>, ways: List<Way>) {
        nodes.forEach { e -> nodeMap.put(e.id, e) }
        ways.forEach {
            e ->
            wayMap.put(e.id, e)
            e.nodes.forEachIndexed { i, n ->
                val ep = WayPosition(e, i)
                if (n in nodeWays.keys) {
                    nodeWays[n]?.add(ep)
                } else {
                    nodeWays.put(n, mutableListOf(ep))
                }
            }
        }
    }

    data class ParsedGraphData(val nodeMap: HashMap<Long, Node>, val wayMap: HashMap<Long, Way>)

    private fun parse(inputStream: InputStream): ParsedGraphData {
        val mapper: ObjectMapper = ObjectMapper()
        val json: JsonNode = mapper.readTree(inputStream)
        val elements: ArrayNode = json["elements"] as ArrayNode

        val nodes = HashMap<Long, Node>()
        val edges = HashMap<Long, Way>()
        elements.forEach { e: JsonNode ->
            val id: Long = e["id"].longValue()
            val type: String = e["type"].asText()
            when {
                type == "node" -> nodes[id] = Node(id, e["lat"].doubleValue(), e["lon"].doubleValue())
                type == "way" -> {
                    val childNodes: List<Long> = e["nodes"].map(JsonNode::asLong)
                    val tags: Map<String, String> = mapper.convertValue(e["tags"], object : TypeReference<HashMap<String, String>>() {})
                    edges[id] = Way(id, childNodes, tags)
                }
            }
        }
        return ParsedGraphData(nodes, edges)
    }
}