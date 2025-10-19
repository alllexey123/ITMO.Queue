package dev.alllexey.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "memberships")
class Membership(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    val group: Group,
    
    @Enumerated(EnumType.STRING)
    var type: Type,
) {

    enum class Type {
        ADMIN,
        MEMBER
    }

}