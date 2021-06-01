package com.anytypeio.anytype.data.auth.repo.block

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.data.auth.exception.BackwardCompatilityNotSupportedException
import com.anytypeio.anytype.data.auth.exception.UndoRedoExhaustedException
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.page.Redo
import com.anytypeio.anytype.domain.page.Undo

class BlockDataRepository(
    private val factory: BlockDataStoreFactory
) : BlockRepository {

    override suspend fun getConfig() = factory.remote.getConfig()

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ) = factory.remote.openDashboard(id = id, contextId = contextId)

    override suspend fun openPage(id: String): Result<Payload> = try {
        Result.Success(factory.remote.openPage(id))
    } catch (e: BackwardCompatilityNotSupportedException) {
        Result.Failure(Error.BackwardCompatibility)
    }

    override suspend fun openProfile(id: String): Payload =
        factory.remote.openProfile(id)

    override suspend fun openObjectSet(id: Id): Payload {
        return factory.remote.openObjectSet(id)
    }

    override suspend fun closeDashboard(id: String) {
        factory.remote.closeDashboard(id)
    }

    override suspend fun updateAlignment(
        command: Command.UpdateAlignment
    ): Payload = factory.remote.updateAlignment(command)

    override suspend fun createPage(parentId: String, emoji: String?) =
        factory.remote.createPage(parentId, emoji)

    override suspend fun closePage(id: String) {
        factory.remote.closePage(id)
    }

    override suspend fun updateDocumentTitle(
        command: Command.UpdateTitle
    ) = factory.remote.updateDocumentTitle(command)

    override suspend fun updateText(command: Command.UpdateText) {
        factory.remote.updateText(command)
    }

    override suspend fun updateTextStyle(
        command: Command.UpdateStyle
    ): Payload = factory.remote.updateTextStyle(command)

    override suspend fun updateTextColor(
        command: Command.UpdateTextColor
    ): Payload = factory.remote.updateTextColor(command)

    override suspend fun updateBackgroundColor(
        command: Command.UpdateBackgroundColor
    ): Payload = factory.remote.updateBackroundColor(command)

    override suspend fun updateCheckbox(
        command: Command.UpdateCheckbox
    ): Payload = factory.remote.updateCheckbox(command)

    override suspend fun create(command: Command.Create): Pair<Id, Payload> {
        return factory.remote.create(command).let { (id, payload) ->
            Pair(id, payload)
        }
    }

    override suspend fun replace(
        command: Command.Replace
    ): Pair<Id, Payload> = factory.remote.replace(command).let { (id, payload) ->
        Pair(id, payload)
    }

    override suspend fun duplicate(
        command: Command.Duplicate
    ): Pair<Id, Payload> = factory.remote.duplicate(command).let { (id, payload) ->
        Pair(id, payload)
    }

    override suspend fun createDocument(
        command: Command.CreateDocument
    ): Triple<String, String, Payload> {
        return factory.remote.createDocument(
            command
        ).let { (id, target, payload) ->
            Triple(id, target, payload)
        }
    }

    override suspend fun createNewDocument(
        command: Command.CreateNewDocument
    ): Id {
        return factory.remote.createNewDocument(command)
    }

    override suspend fun move(command: Command.Move): Payload {
        return factory.remote.move(command)
    }

    override suspend fun unlink(
        command: Command.Unlink
    ): Payload = factory.remote.unlink(command)

    override suspend fun merge(
        command: Command.Merge
    ): Payload = factory.remote.merge(command)

    override suspend fun split(
        command: Command.Split
    ): Pair<Id, Payload> = factory.remote.split(command).let { (id, payload) ->
        Pair(id, payload)
    }

    override suspend fun setDocumentEmojiIcon(
        command: Command.SetDocumentEmojiIcon
    ): Payload = factory.remote.setDocumentEmojiIcon(command)

    override suspend fun setDocumentImageIcon(
        command: Command.SetDocumentImageIcon
    ): Payload = factory.remote.setDocumentImageIcon(command)

    override suspend fun setDocumentCoverColor(
        ctx: String,
        color: String
    ): Payload = factory.remote.setDocumentCoverColor(ctx = ctx, color = color)

    override suspend fun setDocumentCoverGradient(
        ctx: String,
        gradient: String
    ): Payload = factory.remote.setDocumentCoverGradient(ctx = ctx, gradient = gradient)

    override suspend fun setDocumentCoverImage(
        ctx: String,
        hash: String
    ): Payload = factory.remote.setDocumentCoverImage(ctx = ctx, hash = hash)

    override suspend fun removeDocumentCover(
        ctx: String
    ): Payload = factory.remote.removeDocumentCover(ctx)

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ): Payload = factory.remote.setupBookmark(command)

    override suspend fun uploadBlock(command: Command.UploadBlock): Payload =
        factory.remote.uploadBlock(command)

    override suspend fun undo(
        command: Command.Undo
    ): Undo.Result = try {
        Undo.Result.Success(factory.remote.undo(command))
    } catch (e: UndoRedoExhaustedException) {
        Undo.Result.Exhausted
    }

    override suspend fun redo(
        command: Command.Redo
    ): Redo.Result = try {
        Redo.Result.Success(factory.remote.redo(command))
    } catch (e: UndoRedoExhaustedException) {
        Redo.Result.Exhausted
    }

    override suspend fun archiveDocument(
        command: Command.ArchiveDocument
    ) = factory.remote.archiveDocument(command)

    override suspend fun turnIntoDocument(
        command: Command.TurnIntoDocument
    ): List<Id> = factory.remote.turnIntoDocument(command)

    override suspend fun paste(
        command: Command.Paste
    ): Response.Clipboard.Paste = factory.remote.paste(command)

    override suspend fun copy(
        command: Command.Copy
    ): Response.Clipboard.Copy = factory.remote.copy(command)

    override suspend fun uploadFile(
        command: Command.UploadFile
    ): Hash = factory.remote.uploadFile(command)

    override suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinks =
        factory.remote.getPageInfoWithLinks(pageId)

    override suspend fun getListPages(): List<DocumentInfo> = factory.remote.getListPages()

    override suspend fun linkToObject(
        context: Id,
        target: Id,
        block: Id,
        replace: Boolean,
        position: Position
    ): Payload = factory.remote.linkToObject(
        context = context,
        target = target,
        block = block,
        replace = replace,
        position = position
    )

    override suspend fun setRelationKey(command: Command.SetRelationKey): Payload =
        factory.remote.setRelationKey(command)

    override suspend fun updateDivider(
        command: Command.UpdateDivider
    ): Payload = factory.remote.updateDivider(command = command)

    override suspend fun setFields(
        command: Command.SetFields
    ): Payload = factory.remote.setFields(
        command = Command.SetFields(
            context = command.context,
            fields = command.fields.map { (id, fields) ->
                id to Block.Fields(fields.map)
            }
        )
    )

    override suspend fun turnInto(
        context: Id,
        targets: List<Id>,
        style: Block.Content.Text.Style
    ): Payload = factory.remote.turnInto(
        context = context,
        targets = targets,
        style = style
    )

    override suspend fun getTemplates(): List<Template> {
        return factory.remote.getTemplates()
    }

    override suspend fun createTemplate(
        prototype: ObjectType.Prototype
    ): Template = factory.remote.createTemplate(
        ObjectType.Prototype(
            name = prototype.name,
            emoji = prototype.emoji,
            layout = prototype.layout
        )
    )

    override suspend fun createSet(
        context: Id,
        target: Id?,
        position: Position,
        objectType: String?
    ): CreateObjectSet.Response {
        val result = factory.remote.createSet(
            contextId = context,
            targetId = target,
            objectType = objectType,
            position = position
        )
        return CreateObjectSet.Response(
            target = result.targetId,
            block = result.blockId,
            payload = result.payload
        )
    }

    override suspend fun setActiveDataViewViewer(
        context: Id,
        block: Id,
        view: Id,
        offset: Int,
        limit: Int
    ): Payload = factory.remote.setActiveDataViewViewer(
        context = context,
        block = block,
        view = view,
        offset = offset,
        limit = limit
    )

    override suspend fun addDataViewRelation(
        context: Id,
        target: Id,
        name: String,
        format: Relation.Format
    ): Pair<Id, Payload> = factory.remote.addDataViewRelation(
        context = context,
        target = target,
        name = name,
        format = format
    )

    override suspend fun updateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload = factory.remote.updateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun duplicateDataViewViewer(
        context: Id,
        target: Id,
        viewer: DVViewer
    ): Payload = factory.remote.duplicateDataViewViewer(
        context = context,
        target = target,
        viewer = viewer
    )

    override suspend fun addDataViewViewer(
        ctx: String,
        target: String,
        name: String,
        type: DVViewerType
    ): Payload = factory.remote.addDataViewViewer(
        ctx = ctx,
        target = target,
        name = name,
        type = type
    )

    override suspend fun removeDataViewViewer(
        ctx: Id,
        dataview: Id,
        viewer: Id
    ): Payload = factory.remote.removeDataViewViewer(
        ctx = ctx,
        dataview = dataview,
        viewer = viewer
    )

    override suspend fun updateDataViewRecord(
        context: Id,
        target: Id,
        record: Id,
        values: Map<String, Any?>
    ) = factory.remote.updateDataViewRecord(
        context = context,
        target = target,
        record = record,
        values = values
    )

    override suspend fun createDataViewRecord(
        context: Id,
        target: Id
    ): Map<String, Any?> = factory.remote.createDataViewRecord(context = context, target = target)

    override suspend fun addDataViewRelationOption(
        ctx: Id,
        dataview: Id,
        relation: Id,
        record: Id,
        name: Id,
        color: String
    ): Pair<Payload, Id?> = factory.remote.addDataViewRelationOption(
        ctx = ctx,
        dataview = dataview,
        relation = relation,
        record = record,
        name = name,
        color = color
    )

    override suspend fun addObjectRelationOption(
        ctx: Id,
        relation: Id,
        name: String,
        color: String
    ): Pair<Payload, Id?> = factory.remote.addObjectRelationOption(
        ctx = ctx,
        relation = relation,
        name = name,
        color = color
    )

    override suspend fun searchObjects(
        sorts: List<DVSort>,
        filters: List<DVFilter>,
        fulltext: String,
        offset: Int,
        limit: Int
    ): List<Map<String, Any?>> = factory.remote.searchObjects(
        sorts = sorts,
        filters = filters,
        fulltext = fulltext,
        offset = offset,
        limit = limit
    )

    override suspend fun relationListAvailable(ctx: Id) = factory.remote.relationListAvailable(ctx)

    override suspend fun addRelationToObject(
        ctx: Id, relation: Id
    ) : Payload = factory.remote.addRelationToObject(ctx, relation)

    override suspend fun debugSync(): String = factory.remote.debugSync()

    override suspend fun updateDetail(
        ctx: Id,
        key: String,
        value: Any?
    ): Payload = factory.remote.updateDetail(
        ctx = ctx,
        key = key,
        value = value
    )

    override suspend fun updateBlocksMark(command: Command.UpdateBlocksMark): Payload =
        factory.remote.updateBlocksMark(command)
}