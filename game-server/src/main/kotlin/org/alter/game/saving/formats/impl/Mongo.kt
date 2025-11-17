package org.alter.game.saving.formats.impl

import com.mongodb.client.model.Filters.regex
import com.mongodb.client.model.Updates.set
import org.alter.game.model.entity.Client
import org.alter.game.saving.formats.FormatHandler
import org.bson.Document
import org.bson.conversions.Bson

class Mongo(override val collectionName: String) : FormatHandler(collectionName) {

    override fun init() {
        DatabaseManager.connect()
    }

    override fun saveDocument(client: Client, document: Document) {
        if (!playerExists(client)) {
            DatabaseManager.getCollection(collectionName).insertOne(document)
        } else {
            val caseInsensitiveFilter = createCaseInsensitiveFilter(client)
            val attrs = document.get("attributes", Document::class.java)
            DatabaseManager.getCollection(collectionName).updateOne(caseInsensitiveFilter, set("attributes", attrs))
        }
    }

    override fun parseDocument(client : Client): Document {
        val caseInsensitiveFilter = createCaseInsensitiveFilter(client)
        return DatabaseManager.getCollection(collectionName).find(caseInsensitiveFilter).first()!!
    }

    /**
     * Loads all player documents from the MongoDB collection.
     * 
     * TODO: Implement this method to return all player documents from the database.
     * 
     * Expected behavior:
     * - Query the MongoDB collection for all documents
     * - Return a Map where:
     *   - Key: Player username (or unique identifier)
     *   - Value: Document containing player data
     * 
     * This method is likely used for:
     * - Admin commands to list all players
     * - Server statistics/analytics
     * - Bulk operations on player data
     * 
     * Implementation example:
     * ```kotlin
     * return DatabaseManager.getCollection(collectionName)
     *     .find()
     *     .associate { doc -> 
     *         doc.getString("loginUsername") to doc 
     *     }
     * ```
     */
    override fun loadAll(): Map<String, Document> {
        TODO("Not yet implemented")
    }

    override fun playerExists(client: Client): Boolean {
        val caseInsensitiveFilter = createCaseInsensitiveFilter(client)
        return DatabaseManager.getCollection(collectionName)
            .find(caseInsensitiveFilter)
            .toList()
            .isNotEmpty()
    }

    private fun createCaseInsensitiveFilter(client: Client): Bson {
        return regex("loginUsername", "^${client.loginUsername}$", "i")
    }
}