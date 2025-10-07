package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long> {

    fun findByShortId(shortId: String): Subject?
}