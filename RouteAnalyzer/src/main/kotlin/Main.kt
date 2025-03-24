import config.CustomParameters
import kotlin.math.*

data class Waypoint(var timestamp: Long?, var latitude: Double, var longitude: Double)

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

fun computeMostFrequented(waypoints: List<Waypoint>,radius : Double) : Pair<Waypoint,Int>{
    val minLat = waypoints.minOf{it.latitude}
    val maxLat = waypoints.maxOf{it.latitude}
    val minLon = waypoints.minOf{it.longitude}
    val maxLon = waypoints.maxOf{it.longitude}

    val step = haversine(waypoints[0].latitude,waypoints[0].longitude,waypoints[1].latitude,waypoints[1].longitude)*0.1

    var maxCount = 0;
    var best = Waypoint(null,0.0,0.0)

    var lat = minLat

    while (lat <= maxLat){
        var lon = minLon
        while(lon <= maxLon){
            val candidate = Waypoint(timestamp = null, latitude = lat, longitude = lon)

            val count = waypoints.count{haversine(it.latitude,it.longitude,candidate.latitude,candidate.longitude) <= radius}

            if (count >= maxCount){
                maxCount = count;
                best = candidate
            }

            lon += step;
        }
        lat += step;
    }


    return Pair(best,maxCount)
}

fun main() {

    val inputStream = object {}.javaClass.getResourceAsStream("/waypoints.csv")
    val lines = inputStream?.bufferedReader()?.readLines()
    if (lines != null) {
        println("Lines from CSV:")
        lines.forEach { println(it) }  // Stampa ogni riga del CSV

        val waypoints = parseWaypoints(lines)
        //for ((i,waypoint) in waypoints.withIndex()) {
            //println("$i : ${waypoint.toString()}")
        //}

        // Calcola la distanza massima dal punto di partenza

        val (farthestWaypoint, maxDistance,index) = maxDistanceFromStart(waypoints)
        println( "The farthest waypoint is waypoint #:${index} with a distance of ${String.format("%.2f",maxDistance)} Kms")

        //Most frequented area computation
        var radius = 0.0;
        if(CustomParameters.mostFrequentedAreaRadiusKm == null){
            radius = maxDistance * 0.1
        }
        else {
            radius = CustomParameters.mostFrequentedAreaRadiusKm
        }
        println("Radius :  $radius")
        println(computeMostFrequented(waypoints,radius))


    } else {
        println("Error opening file!")
    }


}