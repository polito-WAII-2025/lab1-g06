package config

import org.yaml.snakeyaml.Yaml
import java.io.File

class CustomParameters(
) {
    companion object {

            private val yaml = Yaml()
            private val inputStream = File("files/custom-parameters.yml").inputStream()
            //private val inputStream = object {}.javaClass.getResourceAsStream("/custom-parameters.yml")
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