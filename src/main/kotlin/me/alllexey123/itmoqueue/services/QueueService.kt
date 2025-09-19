package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Queue
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.QueueType
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.repositories.QueueEntryRepository
import me.alllexey123.itmoqueue.repositories.QueueRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QueueService(
    private val queueRepository: QueueRepository,
    private val queueEntryRepository: QueueEntryRepository
) {

    fun save(queue: Queue): Queue {
        return queueRepository.save(queue)
    }

    fun save(queueEntry: QueueEntry): QueueEntry {
        return queueEntryRepository.save(queueEntry)
    }

    fun findQueueById(id: Long): Queue? {
        return queueRepository.findByIdOrNull(id)
    }

    fun findEntryById(id: Long): QueueEntry? {
        return queueEntryRepository.findByIdOrNull(id)
    }

    fun sortedEntries(queue: Queue): List<QueueEntry> {
        when (queue.type) {
            QueueType.SIMPLE -> {
                queue.entries.sortBy { lab ->
                    lab.addedAt
                }
            }

            QueueType.FIRST_PRIORITY -> {
                queue.entries.sortWith { l1, l2 ->
                    if (l1.attemptNumber == 1 && l2.attemptNumber == 1) {
                        l1.addedAt.compareTo(l2.addedAt)
                    } else {
                        if (l1.attemptNumber == 1) -1
                        if (l2.attemptNumber == 1) 1
                        else l1.addedAt.compareTo(l2.addedAt)
                    }
                }
            }

            QueueType.PRIORITY -> {

            }
        }
        return queue.entries
    }


    @Transactional
    fun removeUserFromQueue(user: User, queue: Queue): Boolean {
        val entryExists = queue.entries.any { !it.done && it.user == user }

        if (!entryExists) {
            return false
        }

        queueEntryRepository.deleteByUserAndQueue(user, queue)
        return true
    }
}