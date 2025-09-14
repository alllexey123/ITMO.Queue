package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var name: String,

    var surname: String,

    var email: String,

    @OneToMany(cascade = [(CascadeType.ALL)], mappedBy = "user", orphanRemoval = true)
    var memberships: MutableSet<Membership>,
)