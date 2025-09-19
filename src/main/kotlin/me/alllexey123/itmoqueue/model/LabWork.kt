package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "lab_works")
class LabWork(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    val group: Group,

    @OneToMany(mappedBy = "labWork", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    val queues: MutableList<Queue> = mutableListOf(),

    @ManyToOne
    val subject: Subject,
)