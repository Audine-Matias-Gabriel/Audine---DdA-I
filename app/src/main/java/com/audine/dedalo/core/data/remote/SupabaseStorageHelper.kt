package com.audine.dedalo.core.data.remote

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

private const val BUCKET = "images"

@Singleton
class SupabaseStorageHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) {
    suspend fun uploadImage(uri: Uri, path: String): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw Exception("No se pudo leer la imagen")
        supabaseClient.storage.from(BUCKET).upload(path, bytes)
        return supabaseClient.storage.from(BUCKET).publicUrl(path)
    }
}
