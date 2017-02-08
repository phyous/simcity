package com.phyous.simcity.data

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import javax.json.Json
import javax.json.JsonObject

class OverpassClient {

    class Bounds(val latMin: Float, val lngMin: Float, val latMax: Float, val lngMax: Float) {
        fun toList(): List<Float> {
            return listOf(latMin, lngMin, latMax, lngMax)
        }
    }

    companion object Methods {

        val API_URL = "http://overpass-api.de/api/interpreter"
        val HIGHWAY_TYPES = arrayOf(
                "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                "residential", "motorway_link", "trunk_link", "primary_link", "secondary_link",
                "tertiary_link", "living_street")

        fun getRoads(boundingBox: Bounds): Result<JsonObject, Exception> {
            val (request, response, result) = API_URL.httpPost()
                    .body(constructQuery(boundingBox))
                    .header(Pair("Accept-Charset", "utf-8;q=0.7,*;q=0.7"))
                    .responseString()


            if (result.component2() != null) return Result.of { throw Exception(result.component2().toString()) }
            else {
                return Result.of(
                        Json.createReader(
                                result.get().byteInputStream()).readObject()
                )
            }
        }

        fun constructQuery(boundingBox: Bounds): String {
            var q = "[out:json];("
            val bounds = boundingBox.toList().joinToString(",", "(", ")")

            q += HIGHWAY_TYPES.fold(StringBuilder()) {
                    builder,
                    highway -> builder.append("way[\"highway\"=\"$highway\"]$bounds;")
                }.toString()

            q += "); (._;>;); out;"
            return q
        }
    }
}