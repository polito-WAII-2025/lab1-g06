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
        fun fromYaml(filePath: String): CustomParameters {
            val yaml = Yaml()
            val inputStream = FileInputStream(filePath)
            val data: Map<String, Any> = yaml.load(inputStream)

            return CustomParameters(
                earthRadiusKm = data["earthRadiusKm"].toString().toDouble(),
                geofenceCenterLatitude = data["geofenceCenterLatitude"].toString().toDouble(),
                geofenceCenterLongitude = data["geofenceCenterLongitude"].toString().toDouble(),
                geofenceRadiusKm = data["geofenceRadiusKm"].toString().toDouble(),
                mostFrequentedAreaRadiusKm = data["mostFrequentedAreaRadiusKm"]?.toString()?.toDouble()
            )
        }
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