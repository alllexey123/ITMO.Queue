package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Group
import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface MembershipRepository : JpaRepository<Membership, Long> {

    fun findMembershipByGroupAndUser(group: Group, user: User): Membership?

    fun findMembershipByGroupChatIdAndUserTelegramId(chatId: Long, telegramId: Long): Membership?
}