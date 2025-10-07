package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Lab
import me.alllexey123.itmoqueue.model.QueueEntry
import me.alllexey123.itmoqueue.model.Subject
import me.alllexey123.itmoqueue.model.enums.QueueType
import me.alllexey123.itmoqueue.model.User
import me.alllexey123.itmoqueue.model.enums.MergedQueueType
import me.alllexey123.itmoqueue.repositories.QueueEntryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.PriorityQueue
import kotlin.math.ln
import kotlin.math.max
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
        return queueEntryRepository.save(
            QueueEntry(
                user = user,
                lab = lab,
                attemptNumber = attempt,
            )
        )
    }

    fun sortedEntries(lab: Lab): List<QueueEntry> {
        return when (lab.queueType) {
            QueueType.SIMPLE -> lab.queueEntries.sortedBy { it.createdAt }
            QueueType.FIRST_PRIORITY -> lab.queueEntries.sortedWith(compareBy<QueueEntry> {
                if (it.attemptNumber == 1) 0 else 1
            }.thenBy { it.createdAt })

            QueueType.BALANCED_FORMULA -> {
                val weightAttempt = 600.0
                val attemptPow = 0.7
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

    fun sortedEntries(subject: Subject): List<QueueEntry> {
        val labs = subject.labs.filter { it.queueEntries.isNotEmpty() }
        if (labs.isEmpty()) return emptyList()

        return when (subject.mergedQueueType) {
            MergedQueueType.SIMPLE -> {
                labs.flatMap { it.queueEntries }.sortedBy { it.createdAt }
            }

            MergedQueueType.FIRST_PRIORITY -> {
                labs.flatMap { it.queueEntries }
                    .sortedWith(
                        compareBy<QueueEntry> {
                            if (it.attemptNumber == 1) 0 else 1
                        }.thenBy { it.createdAt }
                    )
            }

            MergedQueueType.BASIC_MERGE -> {
                val perLab = labs.map { sortedEntries(it).toMutableList() }.toMutableList()
                val result = mutableListOf<QueueEntry>()
                var idx = 0
                while (perLab.any { it.isNotEmpty() }) {
                    if (perLab[idx].isNotEmpty()) {
                        result.add(perLab[idx].removeAt(0))
                    }
                    idx = (idx + 1) % perLab.size
                }
                result
            }

            MergedQueueType.BALANCED_MERGE -> {
                data class LabNode(val labIndex: Int, var next: Double, val weight: Double)

                val perLab = labs.map { sortedEntries(it).toMutableList() }
                if (perLab.all { it.isEmpty() }) return emptyList()

                val weights = perLab.map { max(1.0, it.size.toDouble()) }

                val pq = PriorityQueue(compareBy<LabNode> { it.next }.thenBy { it.labIndex })
                for ((i, w) in weights.withIndex()) {
                    if (perLab[i].isNotEmpty()) pq.add(LabNode(i, 0.0, w))
                }

                val result = mutableListOf<QueueEntry>()
                while (pq.isNotEmpty()) {
                    val node = pq.poll()
                    val q = perLab[node.labIndex]
                    if (q.isEmpty()) {
                        continue
                    }

                    result.add(q.removeAt(0))
                    node.next += 1.0 / node.weight

                    if (q.isNotEmpty()) pq.add(node)
                }
                result
            }

            else -> emptyList()
        }
    }

    @Transactional
    fun removeUserFromQueue(user: User, lab: Lab): Boolean {
        val removed = lab.queueEntries.removeIf { !it.done && it.user == user }
        return removed
    }

}