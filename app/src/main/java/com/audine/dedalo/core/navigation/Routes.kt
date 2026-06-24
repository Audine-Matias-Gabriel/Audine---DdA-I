package com.audine.dedalo.core.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val PROJECTS = "projects"
    const val CREATE_PROJECT = "create_project"
    const val CHAT = "chat"
    const val PROFILE = "profile"
    const val PROJECT_DETAIL = "project/{obraId}"
    const val EDIT_PROJECT = "edit_project/{obraId}"
    const val IMAGE_VIEWER = "image/{imageUrl}"

    fun projectDetail(obraId: String) = "project/$obraId"
    fun editProject(obraId: String) = "edit_project/$obraId"
    fun imageViewer(imageUrl: String) = "image/$imageUrl"
}
