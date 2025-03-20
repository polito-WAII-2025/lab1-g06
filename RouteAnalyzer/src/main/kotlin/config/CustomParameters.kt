package config

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream

class CustomParameters(
    val earthRadiusKm: Double,
    val geofenceCenterLatitude: Double,
    val geofenceCenterLongitude: Double,
    val geofenceRadiusKm: Double,
    val mostFrequentedAreaRadiusKm: Double?
) {
    companion object {

            private val yaml = Yaml()
            private val inputStream = object {}.javaClass.getResourceAsStream("/custom-parameters.yml")
            private val data: Map<String, Any> = yaml.load(inputStream)



                val earthRadiusKm = data["earthRadiusKm"].toString().toDouble()
                val geofenceCenterLatitude = data["geofenceCenterLatitude"].toString().toDouble()
                val geofenceCenterLongitude = data["geofenceCenterLongitude"].toString().toDouble()
                val geofenceRadiusKm = data["geofenceRadiusKm"].toString().toDouble()
                val mostFrequentedAreaRadiusKm = data["mostFrequentedAreaRadiusKm"]?.toString()?.toDouble()




    }

    override fun toString(): String {
        return """
            Earth Radius (km): $earthRadiusKm
            Geofence Center Latitude: $geofenceCenterLatitude
            Geofence Center Longitude: $geofenceCenterLongitude
            Geofence Radius (km): $geofenceRadiusKm
            Most Frequented Area Radius (km): $mostFrequentedAreaRadiusKm
        """.trimIndent()
    }
}