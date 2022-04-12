package ru.sberdevices.common.binderhelper

import android.os.IInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.sberdevices.common.binderhelper.entities.BinderState

/**
 * @author Николай Пахомов on 03.02.2022
 */

/**
 * Метод, позволяющий повесить повторяющиеся операции,
 * которые будут выполняться при достижении того или иного [BinderState]
 * @param helper хелпер, на чей [BinderHelper.binderStateFlow] осуществится подписка
 * @param binderState стейт, при котором [block] будет выполнен
 * @param block саспенд лямбда, которая будет вызвана при достижении определенного [BinderState].
 */
fun <T : IInterface> CoroutineScope.repeatOnState(
    helper: BinderHelper<T>,
    binderState: BinderState,
    block: suspend () -> Unit
) {
    helper.binderStateFlow
        .filter { it == binderState }
        .onEach { block.invoke() }
        .launchIn(this)
}
