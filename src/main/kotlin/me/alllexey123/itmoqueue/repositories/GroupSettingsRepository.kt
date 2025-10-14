package me.alllexey123.itmoqueue.repositories

import me.alllexey123.itmoqueue.model.GroupSettings
import org.springframework.data.jpa.repository.JpaRepository

interface GroupSettingsRepository : JpaRepository<GroupSettings, Long> {

}