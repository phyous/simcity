package com.phyous

import com.phyous.simcity.data.OverpassClient
import com.phyous.simcity.data.OverpassClient.Bounds
import java.io.File

fun main(args: Array<String>) {
    val latMin: Float = args[0].toFloat()
    val lngMin: Float = args[1].toFloat()
    val latMax: Float = args[2].toFloat()
    val lngMax: Float = args[3].toFloat()
    val fileName: String = args[4]

    val ret = OverpassClient.getRoads(Bounds(latMin, lngMin, latMax, lngMax))
    ret.fold({ value ->
        println("Query successful; storing results in $fileName")
        File(fileName).printWriter().use { out ->
            out.println(value)
        }
    }, { error ->
        println(error.toString())
    })
}

