package config
import org.yaml.snakeyaml.Yaml
import java.io.File

class CustomParameters {
    companion object {
        private val yaml = Yaml()

        private val configFile = File("evaluation/custom-parameters.yml")

        private val data: Map<String, Any> = yaml.load(configFile.inputStream())

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