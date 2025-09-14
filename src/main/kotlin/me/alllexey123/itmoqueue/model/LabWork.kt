package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
class LabWork(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group,

    @ManyToMany(fetch = FetchType.LAZY)
    var teachers: MutableList<Teacher>

)