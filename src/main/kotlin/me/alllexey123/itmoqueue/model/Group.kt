package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "groups")
class Group (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String?,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    var members: MutableSet<Membership> = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    var labs: MutableList<LabWork> = mutableListOf(),

    @Column(unique = true, nullable = false)
    var chatId: Long,

    @OneToOne(fetch = FetchType.LAZY)
    var addedBy: User? = null,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    var subjects: MutableList<Subject> = mutableListOf(),
)