package me.alllexey123.itmoqueue.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "queue_entries")
@EntityListeners(AuditingEntityListener::class)
class QueueEntry (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    val user: User,

    @ManyToOne(fetch = FetchType.EAGER)
    val lab: Lab,

    var done: Boolean = false,

    val attemptNumber: Int,

    @Temporal(TemporalType.TIMESTAMP)
    var markedDoneAt: Instant? = null,
) {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}