package me.alllexey123.itmoqueue.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Subject (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,

    @OneToMany(mappedBy = "subject", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    var labWorks: MutableList<LabWork> = mutableListOf(),
)