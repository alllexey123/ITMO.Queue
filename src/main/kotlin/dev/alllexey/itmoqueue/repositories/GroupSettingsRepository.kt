package dev.alllexey.itmoqueue.repositories

import dev.alllexey.itmoqueue.model.GroupSettings
import org.springframework.data.jpa.repository.JpaRepository

interface GroupSettingsRepository : JpaRepository<GroupSettings, Long> {

}