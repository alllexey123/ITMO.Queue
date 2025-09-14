package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Group
import org.springframework.data.jpa.repository.JpaRepository

interface GroupRepository : JpaRepository<Group, Long> {

}