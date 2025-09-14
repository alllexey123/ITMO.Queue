package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Membership
import org.springframework.data.jpa.repository.JpaRepository

interface MembershipRepository : JpaRepository<Membership, Long> {
}