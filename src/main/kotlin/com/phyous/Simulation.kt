package com.phyous

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.phyous.simcity.graph.Edge
import com.phyous.simcity.graph.Node
import java.io.File
import java.util.HashMap

fun main(args: Array<String>) {
    val fileName: String = args[0]

    val mapper: ObjectMapper = ObjectMapper()
    val json: JsonNode = mapper.readTree(File(fileName).inputStream())
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
    println("Edges parsed: %d".format(edges.size))
    println("Nodes parsed: %d".format(nodes.size))
}
