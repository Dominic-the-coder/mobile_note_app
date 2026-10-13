package com.example.notesapp.data.model

data class Note(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val color: Long = 0xFF2D3033,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    fun toHashMap(): Map<String, Any> {
        val data = mutableMapOf<String, Any>(
            "title" to title,
            "content" to content,
            "color" to color,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isDeleted" to isDeleted
        )

        deletedAt?.let { data["deletedAt"] = it }

        return data
    }

    companion object {
        fun fromHashMap(
            id: String,
            map: Map<String, Any>
        ): Note = Note(
            id = id,
            title = map["title"]?.toString() ?: "",
            content = map["content"]?.toString() ?: "",
            color = (map["color"] as? Number)?.toLong()
                ?: 0xFF2D3033,
            createdAt = (map["createdAt"] as? Number)?.toLong()
                ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong()
                ?: System.currentTimeMillis(),
            isDeleted = map["isDeleted"] as? Boolean ?: false,
            deletedAt = (map["deletedAt"] as? Number)?.toLong()
        )
    }
}