package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "queues")
class Queue (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToMany(mappedBy = "queue", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var entries: MutableList<QueueEntry> = mutableListOf(),

    @ManyToOne
    var labWork: LabWork,

    @ManyToOne
    var teacher: Teacher?,
)