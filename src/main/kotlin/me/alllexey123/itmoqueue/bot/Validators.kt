package me.alllexey123.itmoqueue.bot

import me.alllexey123.itmoqueue.model.Group
import org.springframework.stereotype.Component

@Component
class Validators {

    private val markdownCharsRegex = Regex("[_*`\\[\\]()~>#+\\-=|{}.!]")

    fun checkSubjectName(
        subjectName: String,
        group: Group
    ): ValidationResult {

        if (subjectName.length > 20) {
            return ValidationResult.Failure("Название предмета длиннее 20 символов, попробуйте снова")
        }

        if (group.subjects.any { subject -> subject.name == subjectName }) {
            ValidationResult.Failure("Предмет с таким названием уже добавлен, попробуйте снова")
        }

        val mdCheck = checkForMarkdownCharacters(subjectName)
        if (mdCheck is ValidationResult.Failure) {
            return mdCheck
        }

        return ValidationResult.Success
    }

    fun checkLabName(
        labName: String,
        group: Group
    ): ValidationResult {

        if (labName.length > 20) {
            return ValidationResult.Failure("Название лабы длиннее 20 символов, попробуйте снова")
        }

        if (group.labs.any { work -> work.name == labName }) {
            return ValidationResult.Failure("Лаба с таким названием уже добавлена, попробуйте снова")
        }

        val mdCheck = checkForMarkdownCharacters(labName)
        if (mdCheck is ValidationResult.Failure) {
            return mdCheck
        }

        return ValidationResult.Success
    }

    private fun checkForMarkdownCharacters(input: String): ValidationResult {
        if (markdownCharsRegex.containsMatchIn(input)) {
            return ValidationResult.Failure(
                "Имя содержит служебные символы Telegram, которые могут нарушить форматирование, попробуйте снова"
            )
        }
        return ValidationResult.Success
    }

    fun checkUserName(
        userName: String,
    ): ValidationResult {

        if (userName.length > 15) {
            return ValidationResult.Failure("Слишком длинное имя (макс. 15 символов), попробуйте снова")
        }

        val mdCheck = checkForMarkdownCharacters(userName)
        if (mdCheck is ValidationResult.Failure) {
            return mdCheck
        }

        return ValidationResult.Success
    }

}