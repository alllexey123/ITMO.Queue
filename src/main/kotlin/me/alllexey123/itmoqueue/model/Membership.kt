package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "memberships")
class Membership(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    val group: Group,
    
    @Enumerated(EnumType.STRING)
    val type: Type,
) {

    enum class Type {
        ADMIN,
        MEMBER
    }

}