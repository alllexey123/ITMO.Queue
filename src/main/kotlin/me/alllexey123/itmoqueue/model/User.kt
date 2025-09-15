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
    var telegramId: Long,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "user", orphanRemoval = true)
    var memberships: MutableSet<Membership> = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "user", orphanRemoval = true)
    var queueEntries: MutableSet<QueueEntry> = mutableSetOf(),

    )