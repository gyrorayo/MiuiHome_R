plugins {
    id("com.android.application") version "7.3.0-alpha08" apply false
    id("com.android.library") version "7.3.0-alpha08" apply false
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}