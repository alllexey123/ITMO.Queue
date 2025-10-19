package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long> {

    fun findByShortId(shortId: String): Subject?
}