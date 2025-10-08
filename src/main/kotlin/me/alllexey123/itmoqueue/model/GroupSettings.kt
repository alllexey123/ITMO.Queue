package me.alllexey123.itmoqueue.model

import jakarta.persistence.*

@Entity
@Table(name = "group_settings")
class GroupSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    var group: Group,

    var attemptsEnabled: Boolean = false,
    var sendAttemptAskDirectly: Boolean = false,
    var mainThreadId: Long? = null,
) {

    fun forceSpecificThread(): Boolean {
        return mainThreadId != null
    }
}