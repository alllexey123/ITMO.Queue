package dev.alllexey.itmoqueue.bot.callback

import dev.alllexey.itmoqueue.bot.extensions.getIntOrNull
import dev.alllexey.itmoqueue.bot.extensions.getLongOrNull
import dev.alllexey.itmoqueue.model.*
import dev.alllexey.itmoqueue.services.*
import org.springframework.stereotype.Component

@Component
class CallbackUtils(
    private val labService: LabService,
    private val subjectService: SubjectService,
    private val groupService: GroupService,
    private val managedMessageService: ManagedMessageService,
    private val userService: UserService,
    private val membershipService: MembershipService
) : ICallbackUtils {

    override fun requireMetadataAdmin(context: CallbackContext): Boolean {
        val group = getGroupOrDelete(context) ?: return false
        val membership = membershipService.findByGroupAndUser(group, context.user) ?: return false
        return context.requireAdmin(membership)
    }

    override fun mergeMetadata(vararg maps: Map<String, Any>?): MutableMap<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (map in maps) {
            if (map != null) result.putAll(map)
        }
        return result
    }

    override fun groupMetadata(group: Group): MutableMap<String, Any> {
        return mutableMapOf(GROUP_ID_KEY to group.id!!)
    }

    override fun subjectMetadata(subject: Subject): MutableMap<String, Any> {
        return mutableMapOf(SUBJECT_ID_KEY to subject.id!!, GROUP_ID_KEY to subject.group.id!!)
    }

    override fun labMetadata(lab: Lab): MutableMap<String, Any> {
        return mutableMapOf(LAB_ID_KEY to lab.id!!, SUBJECT_ID_KEY to lab.subject.id!!, GROUP_ID_KEY to lab.group.id!!)
    }

    override fun userMetadata(user: User): MutableMap<String, Any> {
        return mutableMapOf(USER_ID_KEY to user.id!!)
    }

    override fun parentMessageMetadata(parentMessage: ManagedMessage): MutableMap<String, Any> {
        return mutableMapOf(
            PARENT_MESSAGE_CHAT_ID_KEY to parentMessage.id.chatId,
            PARENT_MESSAGE_ID_KEY to parentMessage.id.messageId
        )
    }

    override fun getGroupOrDelete(context: CallbackContext, groupId: Long?): Group? {
        val group = getGroup(context, groupId)
        if (group == null) {
            context.deleteMessage()
            return null
        }
        return group
    }

    override fun getGroup(context: CallbackContext, groupId: Long?): Group? {
        return context.group ?: getGroup(context.managedMessage.metadata, groupId)
    }

    override fun getGroup(metadata: Map<String, Any>, groupId: Long?): Group? {
        return groupService.findById(groupId ?: metadata.getLongOrNull(GROUP_ID_KEY))
    }

    override fun getSubjectOrDelete(context: CallbackContext, subjectId: Long?): Subject? {
        val subject = getSubject(context.managedMessage.metadata, subjectId)
        if (subject == null) {
            context.deleteMessage()
            return null
        }
        return subject
    }

    override fun getSubject(metadata: Map<String, Any>, subjectId: Long?): Subject? {
        return subjectService.findById(subjectId ?: metadata.getLongOrNull(SUBJECT_ID_KEY))
    }

    override fun getLabOrDelete(context: CallbackContext, labId: Long?): Lab? {
        val lab = getLab(context.managedMessage.metadata, labId)
        if (lab == null) {
            context.deleteMessage()
            return null
        }
        return lab
    }

    override fun getLab(metadata: Map<String, Any>, labId: Long?): Lab? {
        return labService.findById(labId ?: metadata.getLongOrNull(LAB_ID_KEY))
    }

    override fun getUserOrDelete(context: CallbackContext, userId: Long?): User? {
        val user = getUser(context.managedMessage.metadata, userId)
        if (user == null) {
            context.deleteMessage()
            return null
        }
        return user
    }

    override fun getUser(metadata: Map<String, Any>, userId: Long?): User? {
        return userService.findById(userId ?: metadata.getLongOrNull(USER_ID_KEY))
    }

    override fun getParentMessageOrDelete(context: CallbackContext, parentMessageId: ManagedMessageId?): ManagedMessage? {
        val parentMessage = getParentMessage(context.managedMessage.metadata, parentMessageId)
        if (parentMessage == null) {
            context.deleteMessage()
            return null
        }
        return parentMessage
    }

    override fun getParentMessage(metadata: Map<String, Any>, parentMessageId: ManagedMessageId?): ManagedMessage? {
        return managedMessageService.findById(
            parentMessageId ?: metadata.getLongOrNull(PARENT_MESSAGE_CHAT_ID_KEY)?.let { chatId ->
                metadata.getIntOrNull(PARENT_MESSAGE_ID_KEY)?.let { id -> ManagedMessageId(chatId, id) }
            }
        )
    }

    companion object {
        const val LAB_ID_KEY = "lab_id"
        const val SUBJECT_ID_KEY = "subject_id"
        const val GROUP_ID_KEY = "group_id"
        const val PARENT_MESSAGE_CHAT_ID_KEY = "parent_message_chat_id"
        const val PARENT_MESSAGE_ID_KEY = "parent_message_id"
        const val USER_ID_KEY = "user_id"
        const val LAB_PINNED_KEY = "pinned"
        const val LAB_LIST_PAGE_KEY = "lab_list_page"
        const val SUBJECT_LIST_PAGE_KEY = "subject_list_page"
        const val USER_LABS_KEY = "user_labs"
    }
}
