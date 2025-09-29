package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "queues")
class Queue (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToMany(mappedBy = "queue", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    val entries: MutableList<QueueEntry> = mutableListOf(),

    @ManyToOne(fetch = FetchType.EAGER)
    val labWork: LabWork,

    @Enumerated(EnumType.STRING)
    val type: QueueType,

    @ManyToOne
    val teacher: Teacher?,
)