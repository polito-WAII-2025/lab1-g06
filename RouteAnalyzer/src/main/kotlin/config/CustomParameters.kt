package config
import org.yaml.snakeyaml.Yaml
import java.io.File

class CustomParameters {
    companion object {
        private val yaml = Yaml()

        // Percorsi in cui cercare il file (in ordine di priorità)
        private val possibleConfigPaths = listOf(
            "evaluation/custom-parameters.yml",  // Percorso relativo da IntelliJ
            "/app/custom-parameters.yml",        // Percorso assoluto in Docker
            "custom-parameters.yml"             // Fallback
        )

        private val configFile = findConfigFile()
        private val data: Map<String, Any> = yaml.load(configFile.inputStream())

        val earthRadiusKm = data["earthRadiusKm"].toString().toDouble()
        val geofenceCenterLatitude = data["geofenceCenterLatitude"].toString().toDouble()
        val geofenceCenterLongitude = data["geofenceCenterLongitude"].toString().toDouble()
        val geofenceRadiusKm = data["geofenceRadiusKm"].toString().toDouble()
        val mostFrequentedAreaRadiusKm = data["mostFrequentedAreaRadiusKm"]?.toString()?.toDouble()

        private fun findConfigFile(): File {
            return possibleConfigPaths
                .map { File(it) }
                .firstOrNull { it.exists() }
                ?: throw IllegalStateException("""
                    Config file not found! Tried:
                    ${possibleConfigPaths.joinToString("\n")}
                    Current working dir: ${File("").absolutePath}
                    """.trimIndent())
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