package me.alllexey123.itmoqueue.bot

import me.alllexey123.itmoqueue.model.Group
import org.springframework.stereotype.Component

@Component
class Validators {

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

        return ValidationResult.Success
    }

}