package com.phyous

import com.phyous.simcity.data.OverpassClient
import com.phyous.simcity.model.GeoMap
import com.phyous.simcity.model.Node
import com.phyous.simcity.util.distanceMiles
import com.phyous.simcity.util.path
import kotlinx.coroutines.experimental.*
import java.io.File
import java.lang.Exception
import java.lang.Thread.sleep
import java.util.*

fun main(args: Array<String>) = runBlocking {
    val selectedMap = Main.selectMap()
    Main.lookupDirections(selectedMap)
}

class Main {
    companion object Main {
        val dataDir = "./data/"

        /**
         * Retreive all maps stored on the filesystem.
         */
        fun getStoredMaps(): List<String> {
            return File(dataDir)
                    .listFiles()
                    .filter { x -> x.isFile && x.path.endsWith(".map") }
                    .map { x -> x.canonicalPath.split("/").last() }
        }

        /**
         * UI flow for selecting an existing map, or downloading a new one.
         */
        suspend fun selectMap(): GeoMap {
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
         * Read a value from an input source using a provided scanner function (e.g: nextInt, nextLine... etc)
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
         * Query stdin for a lat/lng bounding box, and use the Overpass client to query & store all relevant map data.
         */
        suspend fun downloadMap(): GeoMap {
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

            fun getRoadsAsync() = async(CommonPool) {
                OverpassClient.getRoads(OverpassClient.Bounds(latMin, lngMin, latMax, lngMax))
            }
            val defferdRetrun = getRoadsAsync()
            print("Downloading map: ")
            while(!defferdRetrun.isCompleted) {
                print('.')
                sleep(1000)
            }

            defferdRetrun.await().fold({ value ->
                val fullFileOutputPath = dataDir + fileName
                println("\nQuery successful; storing results in $fileName")
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

        /**
         * UI flow for looking up directions between two points given a GeoMap.
         */
        fun lookupDirections(map: GeoMap) {
            println("Provide two intersections in the selected map to do a direction lookup.")
            println("Ex: from> 25th & Mississippi")
            println("    to>   Mission & 1st")

            fun strToNode(map: GeoMap, str: String): Node {
                val splits = str.split("&").map(String::trim).take(2)
                assert(splits.size == 2, {"You must provide an intersection (2 street names) separated by '&'"})
                val node = map.lookupNode(splits[0], splits[1]) ?: error("Intersection '$str' not found on map!")
                return node
            }

            print("from")
            val from = strToNode(map, readValue(readerFunc = Scanner::nextLine))
            print("to")
            val to = strToNode(map, readValue(readerFunc = Scanner::nextLine))

            map.path(from, to).fold({ result ->
                result.forEach(::println)
                println(String.format("Total trip distance: %.4f miles",
                        result.fold(0.0) { acc, edge -> acc + distanceMiles(edge.fromNode, edge.toNode) }))
            }, { error -> throw Exception(error.toString()) })
        }
    }
}