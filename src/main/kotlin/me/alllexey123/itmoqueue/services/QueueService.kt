package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.enums.QueueType
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.repositories.QueueEntryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ln
import kotlin.math.pow

@Service
class QueueService(
    private val queueEntryRepository: QueueEntryRepository
) {

    fun save(queueEntry: QueueEntry): QueueEntry {
        return queueEntryRepository.save(queueEntry)
    }
    
    fun findEntryById(id: Long): QueueEntry? {
        return queueEntryRepository.findByIdOrNull(id)
    }

    fun findEntryByLabAndUserAndDone(lab: Lab, user: User, done: Boolean): QueueEntry? {
        return queueEntryRepository.findByLabAndUserAndDone(lab, user, done)
    }

    fun hasActiveEntry(user: User, lab: Lab): Boolean {
        return queueEntryRepository.findByLabAndUserAndDone(lab, user, false) != null
    }

    fun addToQueue(user: User, lab: Lab, attempt: Int): QueueEntry {
        return queueEntryRepository.save(QueueEntry(
            user = user,
            lab = lab,
            attemptNumber = attempt,
        ))
    }

    fun sortedEntries(lab: Lab): List<QueueEntry> {
        return when (lab.queueType) {
            QueueType.SIMPLE -> lab.queueEntries.sortedBy { it.createdAt }
            QueueType.FIRST_PRIORITY -> lab.queueEntries.sortedWith { l1, l2 ->
                when {
                    l1.attemptNumber == 1 && l2.attemptNumber != 1 -> -1
                    l1.attemptNumber != 1 && l2.attemptNumber == 1 -> 1
                    else -> l1.createdAt.compareTo(l2.createdAt)
                }
            }

            QueueType.PRIORITY -> {
                val weightAttempt = 600.0
                val attemptPow= 0.7
                val weightTime = 1.0 / 40.0

                lab.queueEntries.sortedBy { entry ->
                    val attemptPenalty = weightAttempt * ln(entry.attemptNumber.toDouble()).pow(attemptPow)

                    val minutesDiff = (entry.createdAt.toEpochMilli() - lab.createdAt.toEpochMilli()) / 60.0 / 1000.0
                    val timePenalty = minutesDiff * weightTime

                    attemptPenalty + timePenalty
                }
            }
        }
    }

    @Transactional
    fun removeUserFromQueue(user: User, lab: Lab): Boolean {
        val removed = lab.queueEntries.removeIf { !it.done && it.user == user }
        return removed
    }

}