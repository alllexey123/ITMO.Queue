package dev.alllexey.itmoqueue.controllers

import dev.alllexey.itmoqueue.services.LabService
import dev.alllexey.itmoqueue.services.QueueService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class LabWebController(private val labService: LabService, private val queueService: QueueService) {

    @GetMapping("/lab/{shortId}")
    fun showLabQueuePage(@PathVariable shortId: String, model: Model): String {
        val lab = labService.findByShortId(shortId)

        if (lab == null) {
            model.addAttribute("shortId", shortId)
            return "lab_not_found"
        }

        val sortedEntries = queueService.sortedEntries(lab)

        model.addAttribute("lab", lab)
        model.addAttribute("entries", sortedEntries)

        return "lab_queue"
    }
}