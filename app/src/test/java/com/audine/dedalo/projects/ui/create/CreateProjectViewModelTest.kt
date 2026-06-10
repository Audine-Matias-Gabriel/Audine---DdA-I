package com.audine.dedalo.projects.ui.create

import android.net.Uri
import com.audine.dedalo.core.data.remote.LocationiqResponse
import com.audine.dedalo.core.data.remote.LocationiqService
import com.audine.dedalo.projects.data.ImageData
import com.audine.dedalo.projects.data.ImageType
import com.audine.dedalo.projects.data.ProjectRepository
import com.google.firebase.storage.FirebaseStorage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateProjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: ProjectRepository = mockk()
    private val locationiqService: LocationiqService = mockk()
    private val storage: FirebaseStorage = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `input fields update state correctly`() = runTest(testDispatcher) {
        val viewModel = CreateProjectViewModel(repository, locationiqService, storage)

        viewModel.onNombreChange("Test Obra")
        assert(viewModel.nombre.value == "Test Obra")

        viewModel.onDireccionChange("Test Address")
        assert(viewModel.direccion.value == "Test Address")

        viewModel.onFosChange("2025-01-01")
        assert(viewModel.fos.value == "2025-01-01")

        viewModel.onFotChange("2025-12-31")
        assert(viewModel.fot.value == "2025-12-31")
    }

    @Test
    fun `suggestion selection updates lat lng and clears suggestions`() = runTest(testDispatcher) {
        val viewModel = CreateProjectViewModel(repository, locationiqService, storage)
        val suggestion = LocationiqResponse(lat = "-34.5789", lon = "-58.4102", displayName = "Av. Del Libertador 4950, Palermo")

        viewModel.onDireccionChange("Av. Del")
        viewModel.onSuggestionSelected(suggestion)

        assert(viewModel.direccion.value == "Av. Del Libertador 4950, Palermo")
        assert(viewModel.suggestions.value.isEmpty())
    }

    @Test
    fun `image management adds removes and sets types`() = runTest(testDispatcher) {
        val viewModel = CreateProjectViewModel(repository, locationiqService, storage)
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()

        viewModel.addImageUri(uri1)
        assert(viewModel.selectedImages.value.size == 1)

        viewModel.addImageUri(uri2)
        assert(viewModel.selectedImages.value.size == 2)

        viewModel.setImageType(uri1, ImageType.BLUEPRINT)
        assert(viewModel.selectedImages.value[0].type == ImageType.BLUEPRINT)

        viewModel.removeImageUri(uri1)
        assert(viewModel.selectedImages.value.size == 1)
        assert(viewModel.selectedImages.value[0].uri == uri2)
    }

    @Test
    fun `createProject without images calls repository and emits success`() = runTest(testDispatcher) {
        val viewModel = CreateProjectViewModel(repository, locationiqService, storage)
        coEvery { repository.createProject(any()) } returns "new-id"

        viewModel.onNombreChange("Test")
        viewModel.createProject()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.saveSuccess.value)
        assert(viewModel.isSaving.value == false)
    }

    @Test
    fun `createProject on error emits saveError`() = runTest(testDispatcher) {
        val viewModel = CreateProjectViewModel(repository, locationiqService, storage)
        coEvery { repository.createProject(any()) } throws Exception("Firebase error")

        viewModel.onNombreChange("Test")
        viewModel.createProject()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.saveError.value == "Firebase error")
        assert(viewModel.isSaving.value == false)
        assert(viewModel.saveSuccess.value == false)
    }
}
