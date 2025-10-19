package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.Membership
import dev.alllexey.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface MembershipRepository : JpaRepository<Membership, Long> {

    fun findMembershipByGroupAndUser(group: Group, user: User): Membership?

    fun findMembershipByGroupChatIdAndUserTelegramId(chatId: Long, telegramId: Long): Membership?
}