package org.example.types

import dev.inmo.tgbotapi.types.message.abstracts.PrivateContentMessage
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import java.util.concurrent.ConcurrentHashMap

class MessageService {
    val messages = ConcurrentHashMap<String, Pair<PrivateContentMessage<PhotoContent>, String>>()
    
    fun addMessage(uuid: String, message: PrivateContentMessage<PhotoContent>) {
        messages.put(uuid, message to "added")
    }
    
    fun messageStatusToWaited(uuid: String) {
        messages.put(uuid, messages.getValue(uuid).first to "waited")
    }
    
    fun getMessage(uuid: String): PrivateContentMessage<PhotoContent> {
        return messages.getValue(uuid).first
    }
    
    fun getWaitingMessages(): List<PrivateContentMessage<PhotoContent>> {
        return messages.values.filter { it.second == "waited" }.map { it.first }
    }
    fun getWaitingMessagesWithUUIDs(): List<Pair<String, PrivateContentMessage<PhotoContent>>> {
        return messages.filter { it.value.second == "waited" }
            .map { it.key to it.value.first }
    }

    fun removeMessage(uuid: String) {
        messages.remove(uuid)
    }
}

fun getMemWord(amount: Int): String {
    return when {
        amount % 10 == 1 && amount % 100 != 11 -> "мем"
        amount % 10 in 2..4 && (amount % 100 < 10 || amount % 100 >= 20) -> "мема"
        else -> "мемов"
    }
}