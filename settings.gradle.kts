pluginManagement {
	repositories {
		gradlePluginPortal()
		google()
		mavenCentral()
		mavenLocal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven ("https://jitpack.io")
		mavenLocal()
	}
}

rootProject.name = "AnitubeApp"
include (":app")
include (":treeview")
include (":bottomsheetMenu")
include (":doubletapplayerview")