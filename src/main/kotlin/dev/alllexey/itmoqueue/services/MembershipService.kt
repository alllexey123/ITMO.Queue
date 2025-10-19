package dev.alllexey.itmoqueue.services

import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.Membership
import dev.alllexey.itmoqueue.model.User
import dev.alllexey.itmoqueue.repositories.MembershipRepository
import org.springframework.stereotype.Service

@Service
class MembershipService (private val membershipRepository: MembershipRepository) {

    fun save(membership: Membership): Membership {
        return membershipRepository.save(membership)
    }

    fun findByGroupAndUser(group: Group, user: User): Membership? {
        return membershipRepository.findMembershipByGroupAndUser(group, user)
    }

    fun findByChatIdAndUserId(chatId: Long, userId: Long): Membership? {
        return membershipRepository.findMembershipByGroupChatIdAndUserTelegramId(chatId, userId)
    }

    fun resetMembershipTypes(group: Group) {
        group.members.forEach { membership -> membership.type = Membership.Type.MEMBER }
    }
}