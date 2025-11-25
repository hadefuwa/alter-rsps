package org.alter.game.saving.impl

import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.entity.Client
import org.alter.game.saving.DocumentHandler
import org.bson.Document
import java.util.concurrent.ConcurrentHashMap

class AttributeSerialisation(override val name: String = "attribute") : DocumentHandler {

    override fun fromDocument(client: Client, doc: Document) {
        doc.forEach { key, value ->
            val attributeKey = AttributeKey<Any>(key)
            val processedValue = when {
                value is Double -> value.toInt()
                value is Document -> {
                    // Convert Document to Map<String, Int> for boss killcounts
                    val map = ConcurrentHashMap<String, Int>()
                    value.forEach { mapKey, mapValue ->
                        val intValue = when (mapValue) {
                            is Double -> mapValue.toInt()
                            is Number -> mapValue.toInt()
                            else -> mapValue as? Int ?: 0
                        }
                        map[mapKey] = intValue
                    }
                    map
                }
                else -> value
            }
            client.attr[attributeKey] = processedValue
        }
    }

    override fun asDocument(client: Client): Document {
        return Document().apply {
            client.attr.toPersistentMap().forEach { (key, value) ->
                append(key, value)
            }
        }
    }
}