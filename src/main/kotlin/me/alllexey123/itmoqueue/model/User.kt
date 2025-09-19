package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var nickname: String?,

    @Column(unique = true, nullable = false)
    val telegramId: Long,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "user", orphanRemoval = true)
    val memberships: MutableSet<Membership> = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "user", orphanRemoval = true)
    val queueEntries: MutableSet<QueueEntry> = mutableSetOf(),

    )