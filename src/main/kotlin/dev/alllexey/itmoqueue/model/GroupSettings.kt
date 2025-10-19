package dev.alllexey.itmoqueue.model

import jakarta.persistence.*
import dev.alllexey.itmoqueue.model.enums.MergedQueueType
import dev.alllexey.itmoqueue.model.enums.QueueType

@Entity
@Table(name = "group_settings")
class GroupSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "group_id", unique = true)
    var group: Group?,

    var attemptsEnabled: Boolean = false,
    var askAttemptsDirectly: Boolean = false,
    var mainThreadId: Int? = null,
    var defaultMergedQueueType: MergedQueueType = MergedQueueType.SIMPLE,
    var defaultQueueType: QueueType = QueueType.SIMPLE,
) {

    fun forceSpecificThread(): Boolean {
        return mainThreadId != null
    }
}