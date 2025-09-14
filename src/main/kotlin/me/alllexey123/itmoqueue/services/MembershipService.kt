package me.alllexey123.itmoqueue.services

import me.alllexey123.itmoqueue.model.Membership
import me.alllexey123.itmoqueue.repositories.MembershipRepository
import org.springframework.stereotype.Service

@Service
class MembershipService (private val membershipRepository: MembershipRepository) {

    fun save(membership: Membership): Membership {
        return membershipRepository.save(membership)
    }
}