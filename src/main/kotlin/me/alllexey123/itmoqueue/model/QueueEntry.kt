package me.alllexey123.itmoqueue.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "queue_entries")
class QueueEntry (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    val user: User,

    @ManyToOne(fetch = FetchType.EAGER)
    val queue: Queue,

    var done: Boolean = false,

    val attemptNumber: Int,

    val addedAt: OffsetDateTime
)