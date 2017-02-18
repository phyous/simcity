package com.phyous.simcity.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import java.io.File
import java.io.InputStream
import java.util.HashMap

class Graph {
    var nodeMap: Map<Long, Node> = HashMap()
    var edgeMap: Map<Long, Edge> = HashMap()

    constructor(file: File) {
        parse(file.inputStream()).let { nodeMap = it.nodeMap; edgeMap = it.edgeMap }
    }

    data class ParsedGraphData(val nodeMap:HashMap<Long, Node>, val edgeMap: HashMap<Long, Edge>)
    fun parse(inputStream: InputStream): ParsedGraphData {
        val mapper: ObjectMapper = ObjectMapper()
        val json: JsonNode = mapper.readTree(inputStream)
        val elements: ArrayNode = json["elements"] as ArrayNode

        val nodes = HashMap<Long, Node>()
        val edges = HashMap<Long, Edge>()
        elements.forEach { e: JsonNode ->
            val id: Long = e["id"].longValue()
            val type: String = e["type"].asText()
            when {
                type == "node" -> nodes[id] = Node(id, e["lat"].doubleValue(), e["lon"].doubleValue())
                type == "way" -> {
                    val childNodes: List<Long> = e["nodes"].map(JsonNode::asLong)
                    val tags: Map<String, String> = mapper.convertValue(e["tags"], object : TypeReference<HashMap<String, String>>() {})
                    edges[id] = Edge(id, childNodes, tags)
                }
            }
        }
        return ParsedGraphData(nodes, edges)
    }
}