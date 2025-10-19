package dev.alllexey.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "groups")
class Group(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String?,

    @Column(unique = true, nullable = false)
    val chatId: Long,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    val members: MutableSet<Membership> = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    val subjects: MutableList<Subject> = mutableListOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "group", orphanRemoval = true)
    val labs: MutableList<Lab> = mutableListOf(),
) {
    @OneToOne(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    lateinit var settings: GroupSettings
}