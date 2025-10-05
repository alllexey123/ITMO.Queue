package me.alllexey123.itmoqueue.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import me.alllexey123.itmoqueue.model.enums.MessageType
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant

@Embeddable
data class ManagedMessageId(
    var chatId: Long,
    var messageId: Int
) : Serializable

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "managed_messages")
class ManagedMessage(
    @EmbeddedId
    var id: ManagedMessageId,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var messageType: MessageType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    var metadata: MutableMap<String, Any> = mutableMapOf()
) {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()

}