

import config.CustomParameters
import kotlin.math.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

@Serializable
data class Waypoint(val timestamp: Long, val latitude: Double, val longitude: Double)

data class Segment(val start : Waypoint, val end : Waypoint)

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

@Serializable
data class Intersections(val count : Int,val intersections : List<Waypoint> )

@Serializable
data class ExtraResults(val intersections : Intersections)

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

fun findIntersection(s1 : Segment, s2 : Segment) : Waypoint?{

    val x1 = s1.start.latitude
    val y1 = s1.start.longitude
    val x2 = s1.end.latitude
    val y2 = s1.end.longitude
    val x3 = s2.start.latitude
    val y3 = s2.start.longitude
    val x4 = s2.end.latitude
    val y4 = s2.end.longitude

    val denominator = ((x4-x3)*(y2-y1)) - ((y4-y3)*(x2-x1))
    val a = ((x4-x3)*(y3-y1)-((y4-y3)*(x3-x1)))
    val b = ((x2-x1)*(y3-y1)-((y2-y1)*(x3-x1)))

    val alfa = a/denominator
    val beta = b/denominator

    if(denominator == 0.0){
        //Segments are parallel
        return null
    }
    else if(a == 0.0 && b == 0.0){
        //Segments are collinear, we do not consider this an intersection
        return null
    }
    else if((alfa > 0 && alfa <1) && (beta > 0 && beta <1)){
        val x0 = x1 + alfa*(x2-x1)
        val y0 = y3 + beta*(y4-y3)
        return Waypoint(0,x0,y0)
    }
    else{
        return null
    }
}

fun findIntersections(waypoints : List<Waypoint>) : List<Waypoint>{
    val intersections : MutableList<Waypoint> = mutableListOf()
    for (i in 0..<waypoints.size-2){
        for(j in i+2..<waypoints.size-1){

            val intersection = findIntersection(Segment(waypoints[i],waypoints[i+1]),Segment(waypoints[j],waypoints[j+1]))
            if(intersection != null){
                intersections.add(intersection)
            }

        }
    }

    return intersections
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


        //println(json.encodeToString(r))

        val intersections = findIntersections(waypoints)

        val er = ExtraResults(Intersections(intersections.size,intersections))

        val file = File("evaluation/output.json")
        file.writeText(json.encodeToString(r))

        val file2 = File("evaluation/output_advanced.json")
        file2.writeText((json.encodeToString(er)))

    } else {
        println("Error opening file!")
    }


}

