package com.anytypeio.anytype.domain.dataview

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetDataViewProperties @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetDataViewProperties.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val params = Command.SetDataViewProperties(
            objectId = params.objectId,
            blockId = DEFAULT_DATA_VIEW_BLOCK_ID,
            properties = params.properties
        )
        return repo.setDataViewProperties(command = params)
    }

    companion object {
        const val DEFAULT_DATA_VIEW_BLOCK_ID = "dataview"
    }

    data class Params(
        val objectId: String,
        val properties: List<Key>
    )
}