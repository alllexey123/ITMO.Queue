package dev.alllexey.itmoqueue.model

import jakarta.persistence.*
import dev.alllexey.itmoqueue.model.enums.QueueType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "labs", indexes = [Index(name = "idx_lab_shortid", columnList = "shortId", unique = true)])
@EntityListeners(AuditingEntityListener::class)
class Lab(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    val group: Group,

    @OneToMany(mappedBy = "lab", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    val queueEntries: MutableList<QueueEntry> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var queueType: QueueType = QueueType.SIMPLE,

    @ManyToOne(fetch = FetchType.EAGER)
    val subject: Subject,

    @Column(unique = true, nullable = false, updatable = false)
    var shortId: String? = null
) {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}