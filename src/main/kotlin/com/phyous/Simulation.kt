package com.phyous

import com.phyous.simcity.model.Graph
import java.io.File

fun main(args: Array<String>) {
    val fileName: String = args[0]
    val file: File = File(fileName)

    val graph = Graph(file)
    println("Edges parsed: %d".format(graph.edgeMap.size))
    println("Nodes parsed: %d".format(graph.nodeMap.size))
}
