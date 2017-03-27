package com.phyous

import com.phyous.simcity.data.OverpassClient
import com.phyous.simcity.model.GeoMap
import java.io.File
import java.lang.Exception
import java.util.*

fun main(args: Array<String>) {
    val selection = Main.selectMap()
}

class Main {

    companion object Main {
        val dataDir = "./data/"

        fun getStoredMaps(): List<String> {
            return File(dataDir)
                    .listFiles()
                    .filter { x -> x.isFile && x.path.endsWith(".map") }
                    .map { x -> x.canonicalPath.split("/").last() }
        }

        fun selectMap(): GeoMap {
            val storedMaps = getStoredMaps()
            println("Welcome to simcity! Let's grab a map and get some directions ...\n" +
                    "Select a map to start:\n" +
                    "0) Download new map")
            storedMaps.forEachIndexed { i, s -> println(String.format("%d) %s", i + 1, s)) }
            val selection = readValue(
                    readerFunc = Scanner::nextInt,
                    validationFunc = { t -> t >= 0 && t <= storedMaps.size + 1 })

            if (selection == 0) {
                return downloadMap()
            } else {
                return GeoMap(File(dataDir + storedMaps[selection - 1]))
            }
        }

        /**
         * Read a value from an input source using a provided scanner and
         * a reader function (e.g: nextInt, nextLine... etc)
         * A function to validate input can optionally be provided as well.
         */
        fun <T> readValue(readerFunc: (Scanner) -> T,
                          validationFunc: (T) -> Boolean = { _ -> true }): T {
            val scanner = Scanner(System.`in`)
            while (true) {
                print("> ")
                try {
                    val read = readerFunc(scanner)
                    if (!validationFunc(read)) throw InputMismatchException()
                    return read
                } catch (e: InputMismatchException) {
                    println("Please enter a valid selection")
                    if (scanner.hasNext()) scanner.next()
                }
            }
        }

        /**
         * Query stdin for a lat/lng bounding box, and use the Overpass client to query & store all relevant map data
         */
        fun downloadMap(): GeoMap {
            println("To download a new map, we'll need to form a bounding box with min/max latitude & longitude.\n" +
                    "For example a map of manhattan would be: \n" +
                    "lat_min: 40.6932, lng_min:-74.0176\n" +
                    "lat_max: 40.8796, lng_max:-73.8789\n")

            println("Enter minimum latitude:")
            val latMin: Float = readValue(Scanner::nextFloat)
            println("Enter minimum longitude:")
            val lngMin: Float = readValue(Scanner::nextFloat)
            println("Enter maximum latitude:")
            val latMax: Float = readValue(Scanner::nextFloat)
            println("Enter maximum longitude:")
            val lngMax: Float = readValue(Scanner::nextFloat)
            println("Enter a filename to save as (ending with extension *.map):")
            val fileName: String = readValue(Scanner::nextLine, {x -> !x.isNullOrEmpty() && x.endsWith(".map")})

            
            val ret = OverpassClient.getRoads(OverpassClient.Bounds(latMin, lngMin, latMax, lngMax))
            ret.fold({ value ->
                val fullFileOutputPath = dataDir + fileName
                println("Query successful; storing results in $fileName")
                File(fullFileOutputPath).printWriter().use { out ->
                    out.println(value)
                }
                return GeoMap(File(fullFileOutputPath))
            }, { error ->
                println("Fatal error downloading map:")
                println(error.toString())
                throw Exception()
            })
        }
    }
}