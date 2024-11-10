package com.nft.generator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

data class TraitConfig(
    val project: Project,
    val traits: Map<String, TraitValues>
)

data class TraitValues(
    val values: List<String>,
    val weights: List<Int>
)

data class Project(
    val imageBaseUrl: String,
    val name: String
)

val mapper = jacksonObjectMapper()

// Load configuration from YAML

val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
val config: TraitConfig = yamlMapper.readValue(File("src/main/resources/config.yaml"))

const val TOTAL_IMAGES = 10
val allImages = mutableListOf<Map<String, String>>()

fun createNewImage(): Map<String, String> {
    val newImage = mapOf(
        "bases" to randomChoice("bases"),
        "hats" to randomChoice("hats"),
        "backgrounds" to randomChoice("backgrounds"),
        "smoke_attributes" to randomChoice("smoke_attributes")
    )
    return if (allImages.contains(newImage)) createNewImage() else newImage
}

fun randomChoice(trait: String): String {
    val values = config.traits[trait]?.values ?: return ""
    val weights = config.traits[trait]?.weights ?: return ""
    val totalWeight = weights.sum()
    val randomVal = Random.nextInt(totalWeight)
    var cumulativeWeight = 0
    for (i in values.indices) {
        cumulativeWeight += weights[i]
        if (randomVal < cumulativeWeight) return values[i]
    }
    return values.last()
}

fun generateImages() {
    for (i in 0 until TOTAL_IMAGES) {
        allImages.add(createNewImage().plus("tokenId" to i.toString()))
    }
}

fun saveMetadata() {
    File("src/main/resources/metadata/all-traits.json").writeText(mapper.writeValueAsString(allImages))
}

fun generateAndSaveImages() {
    for (item in allImages) {
        try {
            val baseImage = loadImage("bases", item["bases"])
            val hatImage = loadImage("hats", item["hats"])
            val backgroundImage = loadImage("backgrounds", item["backgrounds"])
            val smokeImage = loadImage("smoke_attributes", item["smoke_attributes"])

            val finalImage =
                overlayImages(overlayImages(overlayImages(backgroundImage, smokeImage), baseImage), hatImage)
            ImageIO.write(finalImage, "png", File("src/main/resources/images/${item["tokenId"]}.png"))
        } catch (e: Exception) {
            println("Error generating image: ${e.message}")
        }
    }
}

fun loadImage(traitType: String, traitName: String?): BufferedImage {
    return ImageIO.read(File("src/main/resources/trait-layers/$traitType/$traitName.png"));
}

fun overlayImages(base: BufferedImage, overlay: BufferedImage): BufferedImage {
    val combined = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
    val g = combined.createGraphics()
    g.drawImage(base, 0, 0, null)
    g.drawImage(overlay, 0, 0, null)
    g.dispose()
    return combined
}

fun generateIndividualMetadata() {
    allImages.forEach { image ->
        val tokenId = image["tokenId"]
        val metadata = mapOf(
            "image" to "${config.project.imageBaseUrl}$tokenId.png",
            "tokenId" to tokenId,
            "name" to "${config.project.name} $tokenId",
            "attributes" to listOf(
                mapOf("trait_type" to "backgrounds", "value" to image["backgrounds"]),
                mapOf("trait_type" to "hats", "value" to image["hats"]),
                mapOf("trait_type" to "bases", "value" to image["bases"]),
                mapOf("trait_type" to "smoke_attributes", "value" to image["smoke_attributes"])
            )
        )
        File("src/main/resources/metadata/$tokenId.json").writeText(mapper.writeValueAsString(metadata))
    }
}

fun main() {
    generateImages()
    saveMetadata()
    generateAndSaveImages()
    generateIndividualMetadata()
}
