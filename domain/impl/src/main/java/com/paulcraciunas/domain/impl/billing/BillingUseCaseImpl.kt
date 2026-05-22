package com.paulcraciunas.domain.impl.billing

import com.paulcraciunas.domain.api.billing.BillingUseCase
import com.paulcraciunas.domain.api.billing.BillingUseCase.PurchaseEvent
import com.paulcraciunas.global.qualifiers.IoDispatcher
import com.paulcraciunas.user.api.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class BillingUseCaseImpl @Inject constructor(
    private val engine: BillingUseCase.BillingEngine,
    private val userRepository: UserRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : BillingUseCase {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _events = MutableSharedFlow<PurchaseEvent>()
    override val events: Flow<PurchaseEvent> = _events.asSharedFlow()

    init {
        engine.start()
        scope.launch {
            engine.events.receiveAsFlow().collect { event ->
                if (event is PurchaseEvent.Success) {
                    markUserSupporter()
                }
                _events.emit(event)
            }
        }
    }

    private suspend fun markUserSupporter() {
        val user = userRepository.get()
        userRepository.update(user.copy(profile = user.profile.copy(isSupporter = true)))
    }

    override fun donate(event: BillingUseCase.Donate) {
        engine.accept(event)
    }
}
