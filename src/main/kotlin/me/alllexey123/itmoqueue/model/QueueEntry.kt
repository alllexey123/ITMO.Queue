package me.alllexey123.itmoqueue.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "queue_entries")
class QueueEntry (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    val queue: Queue,

    val done: Boolean = false,

    val attemptNumber: Int,

    val addedAt: OffsetDateTime
)