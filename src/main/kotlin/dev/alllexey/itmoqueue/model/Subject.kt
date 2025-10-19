package dev.alllexey.itmoqueue.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import dev.alllexey.itmoqueue.model.enums.MergedQueueType

@Entity
class Subject (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    val group: Group,

    @OneToMany(mappedBy = "subject", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    val labs: MutableList<Lab> = mutableListOf(),

    @Enumerated(value = EnumType.STRING, )
    @Column(nullable = true, length = 50)
    var mergedQueueType: MergedQueueType = MergedQueueType.SIMPLE, // if null then merged queue is disabled

    @Column(unique = true, nullable = false, updatable = false)
    var shortId: String? = null
) {
}