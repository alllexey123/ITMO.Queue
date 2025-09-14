package me.alllexey123.itmoqueue.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
class Membership(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group,

    // if type == Pending, then this is the time of the request
    val since: OffsetDateTime,

    @Enumerated(EnumType.STRING)
    val type: Type,
) {

    enum class Type {
        CREATOR,
        MEMBER,
        PENDING
    }

}