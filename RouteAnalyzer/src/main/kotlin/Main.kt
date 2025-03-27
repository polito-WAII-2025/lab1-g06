

import config.CustomParameters
import kotlin.math.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class Waypoint(val timestamp: Long, val latitude: Double, val longitude: Double)

@Serializable
data class WaypointsOutsideGeofence(
    val centralWaypoint: Waypoint,
    val areaRadiusKm: Double,
    val count: Int,
    val waypoints: List<Waypoint>
)

@Serializable
data class MostFrequentedArea(val centralWaypoint : Waypoint, val areaRadiusKm : Double, val entriesCount : Int )

@Serializable
data class MaxDistanceFromStart(val waypoint : Waypoint, val distanceKm : Double)

@Serializable
data class Results(val maxDistanceFromStart: MaxDistanceFromStart,val mostFrequentedArea: MostFrequentedArea, val waypointsOutsideGeofence: WaypointsOutsideGeofence)

fun parseWaypoints(lines: List<String>): List<Waypoint> {
    return lines.map { line ->
        val parts = line.split(";")  // Usa ";" come separatore
        Waypoint(
            timestamp = parts[0].toDouble().toLong(),  // Converti prima in Double, poi in Long
            latitude = parts[1].toDouble(),
            longitude = parts[2].toDouble()
        )
    }
}

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    // Raggio della Terra in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return CustomParameters.earthRadiusKm * c
}

fun maxDistanceFromStart(waypoints: List<Waypoint>): Triple<Waypoint, Double, Int> {
    val start = waypoints.first()
    var maxDistance = 0.0
    var farthestWaypoint = start
    var maxIndex = 0

    for ((index,waypoint) in waypoints.withIndex()) {
        val distance = haversine(start.latitude, start.longitude, waypoint.latitude, waypoint.longitude)
        if (distance > maxDistance) {
            maxDistance = distance
            farthestWaypoint = waypoint
            maxIndex = index
        }
    }

    return Triple(farthestWaypoint, maxDistance, maxIndex)
}

fun waypointsOutsideGeofence(waypoints: List<Waypoint>): WaypointsOutsideGeofence {
    val outsideWaypoints: MutableList<Waypoint> = mutableListOf()
    for (waypoint in waypoints) {
        if(haversine(waypoint.latitude, waypoint.longitude, CustomParameters.geofenceCenterLatitude, CustomParameters.geofenceCenterLongitude)>CustomParameters.geofenceRadiusKm){
            outsideWaypoints.add(waypoint)
        }
    }
    return WaypointsOutsideGeofence(Waypoint(0, CustomParameters.geofenceCenterLatitude, CustomParameters.geofenceCenterLongitude), CustomParameters.geofenceRadiusKm, outsideWaypoints.size, outsideWaypoints)
}

val json = Json { prettyPrint = true }

/*
    The area that contains the path is delimited in a rectangle. Inside the rectangle a set of candidate centers are considered with the computed radius and the
    waypoints inside are counted
 */
fun computeMostFrequented(waypoints: List<Waypoint>,radius : Double) : Pair<Waypoint,Int>{
    val minLat = waypoints.minOf{it.latitude}
    val maxLat = waypoints.maxOf{it.latitude}
    val minLon = waypoints.minOf{it.longitude}
    val maxLon = waypoints.maxOf{it.longitude}

    val latStep = (maxLat - minLat)/200
    val lonStep = (maxLon - minLon)/200

    var maxCount = 0
    var best = Waypoint(0,0.0,0.0)


    for (i in 0..200) {
        val lat = minLat + i * latStep
        for (j in 0..200) {
            val lon = minLon + j * lonStep
            val candidate = Waypoint(0, lat, lon)

            val count = waypoints.count { haversine(it.latitude, it.longitude, candidate.latitude, candidate.longitude) <= radius }

            if (count > maxCount) { // Use `>` instead of `>=` to favor earlier points in case of a tie
                maxCount = count
                best = candidate
            }
        }
    }

    return Pair(best,maxCount)
}

fun main() {

    val inputStream = object {}.javaClass.getResourceAsStream("/waypoints.csv")
    val lines = inputStream?.bufferedReader()?.readLines()
    if (lines != null) {

        val waypoints = parseWaypoints(lines)

        // Calcola la distanza massima dal punto di partenza e il punto relativo

        val (farthestWaypoint, maxDistance) = maxDistanceFromStart(waypoints)

        //Most frequented area radius computation
        var radius = maxDistance * 0.1
        if (maxDistance < 1){
            radius = 0.1
        }
        if(CustomParameters.mostFrequentedAreaRadiusKm != null){
            radius =  CustomParameters.mostFrequentedAreaRadiusKm
        }

        //Compute most frequented area
        val (center,count) = computeMostFrequented(waypoints, radius)

        //Compute points outside GeoFence

        val waypointsOutside  = waypointsOutsideGeofence(waypoints)

        //r contiene i risultati per serializzarli in JSON
        val r = Results(MaxDistanceFromStart(farthestWaypoint,maxDistance),MostFrequentedArea(center,radius,count),waypointsOutside)


        println(json.encodeToString(r))

        val file = File("evaluation/output.json")
        file.writeText(json.encodeToString(r))

    } else {
        println("Error opening file!")
    }


}

