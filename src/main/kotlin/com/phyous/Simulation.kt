package com.phyous

import com.phyous.simcity.model.GeoMap
import java.io.File

fun main(args: Array<String>) {
    val fileName: String = args[0]
    val file: File = File(fileName)

    val graph = GeoMap(file)
    println("Edges parsed: %d".format(graph.wayMap.size))
    println("Nodes parsed: %d".format(graph.nodeMap.size))
}
