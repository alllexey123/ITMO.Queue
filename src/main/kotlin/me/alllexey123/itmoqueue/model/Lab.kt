package me.alllexey123.itmoqueue.model

import jakarta.persistence.*
import me.alllexey123.itmoqueue.model.enums.QueueType

@Entity
@Table(name = "labs")
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
    val queueType: QueueType,

    @ManyToOne(fetch = FetchType.EAGER)
    val subject: Subject,
)