package org.example.types

import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object JsonConfig {
    val jsonTC: Json = Json {
        serializersModule = SerializersModule {
            contextual(TextContent::class, TextContent.Companion.serializer())
        }
    }
    val json: Json = Json {
        prettyPrint = true
    }
}