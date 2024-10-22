
import io.newm.shared.NewmAppLogger
import io.newm.shared.internal.api.models.MobileClientConfig
import io.newm.shared.internal.api.models.MobileConfig
import io.newm.shared.internal.implementations.ForceAppUpdateUseCaseImpl
import io.newm.shared.internal.repositories.RemoteConfigRepository
import io.newm.shared.public.usecases.ForceAppUpdateUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ForceAppUpdateUseCaseTest {

    private lateinit var useCase: ForceAppUpdateUseCase

    private lateinit var logger: NewmAppLogger

    private fun setUp() {
        // Setting up the test environment with fake implementations
        val fakeConfig = MobileConfig(
            version = 1,
            android = MobileClientConfig("1.0.2"), // You might want to adjust these based on the test
            ios = MobileClientConfig("1.0.0")
        )
        val remoteConfigRepository = FakeRemoteConfigRepository(fakeConfig)
        logger = NewmAppLogger()
        useCase = ForceAppUpdateUseCaseImpl(remoteConfigRepository)
    }

    @Test
    fun `test Android update required when current version is less`() = runBlocking {
        setUp()
        assertTrue(
            useCase.isAndroidUpdateRequired("1.0.0", "anyString"),
            "Android update should be required when the current version is less than the minimum required version"
        )
    }

    @Test
    fun `test iOS update not required when current version is greater`() = runBlocking {
        setUp()
        assertFalse(
            useCase.isiOSUpdateRequired("2.0.0", "anyString"),
            "iOS update should not be required when the current version is greater than the minimum required version"
        )
    }


    // Inner classes for fakes
    class FakeRemoteConfigRepository(private val config: MobileConfig?) : RemoteConfigRepository {
        override suspend fun getMobileConfig(humanVerificationCode: String): MobileConfig? = config
    }
}