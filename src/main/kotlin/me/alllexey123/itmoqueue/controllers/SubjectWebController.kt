package me.alllexey123.itmoqueue.controllers

import me.alllexey123.itmoqueue.services.QueueService
import me.alllexey123.itmoqueue.services.SubjectService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class SubjectWebController(
    private val queueService: QueueService,
    private val subjectService: SubjectService
) {

    @GetMapping("/subject/{shortId}")
    fun showLabQueuePage(@PathVariable shortId: String, model: Model): String {
        val subject = subjectService.findByShortId(shortId)

        if (subject == null) {
            model.addAttribute("shortId", shortId)
            return "subject_not_found"
        }

        val sortedEntries = queueService.sortedEntries(subject)

        model.addAttribute("subject", subject)
        model.addAttribute("entries", sortedEntries)

        return "subject_queue"
    }
}