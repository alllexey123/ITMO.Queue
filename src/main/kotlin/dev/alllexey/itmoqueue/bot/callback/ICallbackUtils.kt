package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.model.Group
import dev.alllexey.itmoqueue.model.Lab
import dev.alllexey.itmoqueue.model.ManagedMessage
import dev.alllexey.itmoqueue.model.ManagedMessageId
import dev.alllexey.itmoqueue.model.Subject
import dev.alllexey.itmoqueue.model.User

interface ICallbackUtils {

    fun requireMetadataAdmin(context: CallbackContext): Boolean

    fun mergeMetadata(vararg maps: Map<String, Any>?): MutableMap<String, Any>

    fun groupMetadata(group: Group): MutableMap<String, Any>

    fun subjectMetadata(subject: Subject): MutableMap<String, Any>

    fun labMetadata(lab: Lab): MutableMap<String, Any>

    fun userMetadata(user: User): MutableMap<String, Any>

    fun parentMessageMetadata(parentMessage: ManagedMessage): MutableMap<String, Any>

    fun getGroupOrDelete(context: CallbackContext, groupId: Long? = null): Group?

    fun getGroup(context: CallbackContext, groupId: Long? = null): Group?

    fun getGroup(metadata: Map<String, Any>, groupId: Long? = null): Group?

    fun getSubjectOrDelete(context: CallbackContext, subjectId: Long? = null): Subject?

    fun getSubject(metadata: Map<String, Any>, subjectId: Long? = null): Subject?

    fun getLabOrDelete(context: CallbackContext, labId: Long? = null): Lab?

    fun getLab(metadata: Map<String, Any>, labId: Long? = null): Lab?

    fun getUserOrDelete(context: CallbackContext, userId: Long? = null): User?

    fun getUser(metadata: Map<String, Any>, userId: Long? = null): User?

    fun getParentMessageOrDelete(context: CallbackContext, parentMessageId: ManagedMessageId? = null): ManagedMessage?

    fun getParentMessage(metadata: Map<String, Any>, parentMessageId: ManagedMessageId? = null): ManagedMessage?
}
