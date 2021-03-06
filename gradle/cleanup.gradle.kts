// Copied from: https://github.com/cortinico/kotlin-android-template/blob/master/buildSrc/src/main/kotlin/cleanup.gradle.kts

check(rootProject.name == name) {
    "The cleanup plugin should be applied to the root project and not $name"
}

tasks.register("cleanup") {
    val repository = System.getenv("GITHUB_REPOSITORY")
        ?: error("No GITHUB_REPOSITORY environment variable. Are you running from Github Actions?")

    val (owner, name) = repository.split("/").let {
        it[0].sanitized() to it[1].sanitized()
    }

    file("settings.gradle").replace(
        "rootProject.name = \"AndroidDevChallenge\"",
        "rootProject.name = \"$name\""
    )

    file("app/build.gradle").replace(
        "applicationId \"com.example.androiddevchallenge\"",
        "applicationId \"com.example.$name\""
    )

    file("results").listFiles()?.forEach { it.delete() }

    changePackageName(name)

    //Cleanup the cleanup :)
    file("build.gradle").replace(
        "apply(from = \"gradle/cleanup.gradle.kts\")",
        " "
    )
    file("gradle/cleanup.gradle.kts").delete() //self-delete
}

fun String.sanitized() = replace(Regex("[^A-Za-z0-9]"), "").toLowerCase()

fun File.replace(oldValue: String, newValue: String) {
    writeText(readText().replace(oldValue, newValue))
}


fun srcDirectories() = projectDir.listFiles()!!
    .filter { it.isDirectory && (it.name == "app") }
    .flatMap { it.listFiles()!!.filter { it.isDirectory && it.name == "src" } }

fun changePackageName(name: String) {
    srcDirectories().forEach {
        it.walk().filter {
            it.isFile && (it.extension == "kt" || it.extension == "kts"  || it.extension == "xml")
        }.forEach {
            it.replace("com.example.androiddevchallange", "com.example.$name")
            it.replace("AndroidDevChallenge", name)
        }
    }
    srcDirectories().forEach {
        it.listFiles()!!.filter { it.isDirectory } // down to src/main, test & androidTest
            .flatMap { it.listFiles()!!.filter { it.isDirectory } }
            .forEach {
                val newDir = File(it, "com/example/$name")
                newDir.parentFile.mkdirs()
                File(it, "com/example/androiddevchallange").renameTo(newDir)
                File(it, "com/example/androiddevchallange").deleteRecursively()
            }
    }
}
