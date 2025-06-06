package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.Relations.RELATION_FORMAT_OBJECT_TYPES
import com.anytypeio.anytype.core_models.ext.typeOf
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus

/**
 * Wrapper for easily parsing object's relations when object is represented as an untyped structure.
 */
sealed class ObjectWrapper {

    abstract val map: Struct

    /**
     * @property map [map] map with raw data containing relations.
     */
    data class Basic(override val map: Struct) : ObjectWrapper() {

        private val default = map.withDefault { null }

        val lastModifiedDate: Any? by default
        val lastOpenedDate: Any? by default

        val name: String? by default
        val pluralName: String? by default

        val iconEmoji: String? by default
        val iconImage: String? = getSingleValue(Relations.ICON_IMAGE)
        val iconOption: Double? by default
        val iconName: String? by default

        val coverId: String? = getSingleValue(Relations.COVER_ID)

        val coverType: CoverType
            get() = when (val value = map[Relations.COVER_TYPE]) {
                is Double -> CoverType.entries.find { type ->
                    type.code == value.toInt()
                } ?: CoverType.NONE
                else -> CoverType.NONE
            }

        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default

        val type: List<Id> get() = getValues(Relations.TYPE)
        val setOf: List<Id> get() = getValues(Relations.SET_OF)
        val links: List<Id> get() = getValues(Relations.LINKS)

        val layout: ObjectType.Layout?
            get() {
                // Try legacy layout first, then fallback to resolved layout.
                val layoutValue = when {
                    map[Relations.LEGACY_LAYOUT] is Double -> map[Relations.LEGACY_LAYOUT] as Double
                    map[Relations.LAYOUT] is Double -> map[Relations.LAYOUT] as Double
                    else -> null
                }
                return layoutValue?.let { value ->
                    ObjectType.Layout.entries.singleOrNull { it.code == value.toInt() }
                }
            }

        val id: Id by default

        val uniqueKey: String? by default

        val done: Boolean? by default

        val snippet: String? by default

        val fileExt: String? by default

        val fileMimeType: String? by default

        val description: String? = getSingleValue(Relations.DESCRIPTION)

        val url: String? by default

        val featuredRelations: List<Key> get() = getValues(Relations.FEATURED_RELATIONS)

        fun isEmpty(): Boolean = map.isEmpty()

        val relationKey: String by default
        val isFavorite: Boolean? by default
        val isHidden: Boolean? by default

        val relationFormat: RelationFormat?
            get() = when (val value = map[Relations.RELATION_FORMAT]) {
                is Double -> RelationFormat.values().singleOrNull { format ->
                    format.code == value.toInt()
                }
                else -> null
            }

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationOptionColor: String? by default
        val relationReadonlyValue: Boolean? by default

        val sizeInBytes: Double? by default

        val internalFlags: List<InternalFlags>
            get() = when (val value = map[Relations.INTERNAL_FLAGS]) {
                is Double -> buildList {
                    when (value.toInt()) {
                        InternalFlags.ShouldSelectType.code -> InternalFlags.ShouldSelectType
                        InternalFlags.ShouldSelectTemplate.code -> InternalFlags.ShouldSelectTemplate
                        InternalFlags.ShouldEmptyDelete.code -> InternalFlags.ShouldEmptyDelete
                    }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    when (code.toInt()) {
                        InternalFlags.ShouldSelectType.code -> InternalFlags.ShouldSelectType
                        InternalFlags.ShouldSelectTemplate.code -> InternalFlags.ShouldSelectTemplate
                        InternalFlags.ShouldEmptyDelete.code -> InternalFlags.ShouldEmptyDelete
                        else -> null
                    }
                }
                else -> emptyList()
            }

        val targetObjectType: Id?
            get() = getValues<Id>(Relations.TARGET_OBJECT_TYPE).firstOrNull()

        val isValid get() = map.containsKey(Relations.ID)

        val notDeletedNorArchived get() = (isDeleted != true && isArchived != true)

        val spaceId: Id? by default

        // N.B. Only used for space view objects
        val targetSpaceId: Id? by default

        val backlinks get() = getValues<Id>(Relations.BACKLINKS)
    }

    /**
     * Wrapper for bookmark objects
     */
    data class Bookmark(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val name: String? by default
        val description: String? = getSingleValue(Relations.DESCRIPTION)
        val source: String? by default
        val iconEmoji: String? by default
        val iconImage: String? = getSingleValue(Relations.ICON_IMAGE)
        val picture: String? by default
        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default
    }

    /**
     * Wrapper for object types
     */
    data class Type(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val uniqueKey: String by default
        val name: String? by default
        val pluralName: String? by default
        val sourceObject: Id? get() = getSingleValue(Relations.SOURCE_OBJECT)
        val description: String? = getSingleValue(Relations.DESCRIPTION)
        val isArchived: Boolean? by default
        val iconEmoji: String? by default
        val isDeleted: Boolean? by default
        val recommendedRelations: List<Id> get() = getValues(Relations.RECOMMENDED_RELATIONS)
        val recommendedFeaturedRelations: List<Id> get() = getValues(Relations.RECOMMENDED_FEATURED_RELATIONS)
        val recommendedHiddenRelations: List<Id> get() = getValues(Relations.RECOMMENDED_HIDDEN_RELATIONS)
        val recommendedFileRelations: List<Id> get() = getValues(Relations.RECOMMENDED_FILE_RELATIONS)
        val recommendedLayout: ObjectType.Layout?
            get() = when (val value = map[Relations.RECOMMENDED_LAYOUT]) {
                is Double -> ObjectType.Layout.entries.singleOrNull { layout ->
                    layout.code == value.toInt()
                }
                else -> ObjectType.Layout.BASIC
            }
        val layout: ObjectType.Layout?
            get() {
                // Try legacy layout first, then fallback to resolved layout.
                val layoutValue = when {
                    map[Relations.LEGACY_LAYOUT] is Double -> map[Relations.LEGACY_LAYOUT] as Double
                    map[Relations.LAYOUT] is Double -> map[Relations.LAYOUT] as Double
                    else -> null
                }
                return layoutValue?.let { value ->
                    ObjectType.Layout.entries.singleOrNull { it.code == value.toInt() }
                }
            }

        val defaultTemplateId: Id? by default

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }

                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }

                else -> emptyList()
            }

        val iconName: String? by default
        val iconOption: Double? by default

        val allRecommendedRelations: List<Id>
            get() = recommendedFeaturedRelations + recommendedRelations + recommendedFileRelations + recommendedHiddenRelations

        val isValid get() =
            map.containsKey(Relations.UNIQUE_KEY) && map.containsKey(Relations.ID)
    }

    data class Relation(override val map: Struct) : ObjectWrapper() {

        private val default = map.withDefault { null }

        private val relationKey : Key by default

        val relationFormat: RelationFormat
            get() {
                val value = map[Relations.RELATION_FORMAT]
                return if (value is Double) {
                    RelationFormat.entries.firstOrNull { f ->
                        f.code == value.toInt()
                    } ?: RelationFormat.UNDEFINED
                } else {
                    RelationFormat.UNDEFINED
                }
            }

        private val relationReadonlyValue: Boolean? by default

        val id: Id by default
        val uniqueKey: String? by default
        val key: Key get() = relationKey
        val spaceId: Id? by default
        val sourceObject: Id? by default
        val format: RelationFormat get() = relationFormat
        val name: String? by default
        val isHidden: Boolean? by default
        val isReadOnly: Boolean? by default
        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default
        val isReadonlyValue: Boolean = relationReadonlyValue ?: false

        val restrictions: List<ObjectRestriction>
            get() = when (val value = map[Relations.RESTRICTIONS]) {
                is Double -> buildList {
                    ObjectRestriction.entries.firstOrNull { it.code == value.toInt() }
                }
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    ObjectRestriction.entries.firstOrNull { it.code == code.toInt() }
                }
                else -> emptyList()
            }

        val relationFormatObjectTypes get() = getValues<Id>(RELATION_FORMAT_OBJECT_TYPES)

        val type: List<Id> get() = getValues(Relations.TYPE)

        val isValid get() =
            map.containsKey(Relations.RELATION_KEY) && map.containsKey(Relations.ID)

        val isValidToUse get() = isValid && isDeleted != true && isArchived != true && isHidden != true

    }

    data class Option(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        private val relationOptionColor : String? by default

        val id: Id by default
        val name: String? by default
        val color: String = relationOptionColor.orEmpty()
        val isDeleted: Boolean? by default
    }

    data class SpaceView(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }

        val id: Id by default
        val name: String? by default
        val description: String? = getSingleValue(Relations.DESCRIPTION)
        val iconImage: String? get() = getSingleValue(Relations.ICON_IMAGE)
        val iconOption: Double? by default

        // N.B. Only used for space view objects
        val targetSpaceId: String? by default

        val chatId: Id? by default

        val creator: Id? by default

        val spaceAccountStatus: SpaceStatus
            get() {
                val code = getValue<Double?>(Relations.SPACE_ACCOUNT_STATUS)
                return SpaceStatus
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
                    ?: SpaceStatus.UNKNOWN
            }

        val spaceLocalStatus: SpaceStatus
            get() {
                val code = getValue<Double?>(Relations.SPACE_LOCAL_STATUS)
                return SpaceStatus
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
                    ?: SpaceStatus.UNKNOWN
            }

        val spaceAccessType: SpaceAccessType?
            get() {
                val code = getValue<Double?>(Relations.SPACE_ACCESS_TYPE)
                return SpaceAccessType
                    .entries
                    .firstOrNull { it.code == code?.toInt() }
            }

        val writersLimit: Double? by default
        val readersLimit: Double? by default

        val sharedSpaceLimit: Int
            get() {
                val value = getValue<Double?>(Relations.SHARED_SPACES_LIMIT)
                return value?.toInt() ?: 0
            }

        val isLoading: Boolean
            get() {
                return spaceLocalStatus == SpaceStatus.LOADING
                        && spaceAccountStatus != SpaceStatus.SPACE_REMOVING
                        && spaceAccountStatus != SpaceStatus.SPACE_DELETED
                        && spaceAccountStatus != SpaceStatus.SPACE_JOINING
            }

        val isActive: Boolean
            get() {
                return spaceLocalStatus == SpaceStatus.OK
                        && spaceAccountStatus != SpaceStatus.SPACE_REMOVING
                        && spaceAccountStatus != SpaceStatus.SPACE_DELETED
            }
    }

    inline fun <reified T> getValue(relation: Key): T? {
        val value = map.getOrDefault(relation, null)
        return if (value is T)
            value
        else
            null
    }

    inline fun <reified T> getSingleValue(relation: Key): T? = map.getSingleValue(relation)

    inline fun <reified T> getValues(relation: Key): List<T> {
        return when (val value = map.getOrDefault(relation, emptyList<T>())) {
            is T -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
    }

    data class ObjectInternalFlags(override val map: Struct) : ObjectWrapper() {
        val internalFlags: List<InternalFlags>
            get() = when (val value = map[Relations.INTERNAL_FLAGS]) {
                is List<*> -> value.typeOf<Double>().mapNotNull { code ->
                    when (code.toInt()) {
                        InternalFlags.ShouldSelectType.code -> InternalFlags.ShouldSelectType
                        InternalFlags.ShouldSelectTemplate.code -> InternalFlags.ShouldSelectTemplate
                        InternalFlags.ShouldEmptyDelete.code -> InternalFlags.ShouldEmptyDelete
                        else -> null
                    }
                }
                else -> emptyList()
            }
    }

    data class File(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? by default
        val description: String? = getSingleValue(Relations.DESCRIPTION)
        val fileExt: String? by default
        val fileMimeType: String? by default
        val sizeInBytes: Double? by default
        val url: String? by default
        val isArchived: Boolean? by default
        val isDeleted: Boolean? by default
    }

    data class SpaceMember(override val map: Struct): ObjectWrapper() {
        private val default = map.withDefault { null }

        val id: Id by default
        val spaceId: Id? by default
        val identity: Id by default

        val name: String? by default
        val iconImage: String? by default

        val status
            get() = getSingleValue<Double>(Relations.PARTICIPANT_STATUS)
                .let { code ->
                    ParticipantStatus.entries.firstOrNull { it.code == code?.toInt() }
                }

        val permissions
            get() = getSingleValue<Double>(Relations.PARTICIPANT_PERMISSIONS)
                .let { code ->
                    SpaceMemberPermissions.values().firstOrNull { it.code == code?.toInt() }
                }

        val globalName: String? by default
    }

    data class Date(override val map: Struct) : ObjectWrapper() {
        private val default = map.withDefault { null }
        val id: Id by default
        val name: String? by default
        val timestamp: Double?
            get() = when (val value = map[Relations.TIMESTAMP]) {
                is Double -> value
                is Int -> value.toDouble()
                else -> null
            }
    }
}

inline fun <reified T> Struct.getSingleValue(relation: Key): T? =
    when (val value = getOrDefault(relation, null)) {
        is T -> value
        is List<*> -> value.typeOf<T>().firstOrNull()
        else -> null
    }