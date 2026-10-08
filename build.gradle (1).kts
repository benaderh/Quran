plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "quran.hb.com.quran"
    compileSdk = 35

    defaultConfig {
        applicationId = "quran.hb.com.quran"   // نفس المعرّف القديم: التحديث يحافظ على قاعدة البيانات و العلامات
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    androidResources {
        noCompress += listOf("sqlite", "otf")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("io.coil-kt:coil-compose:2.7.0")
    // قراءة ملفات Excel (.xls) الخاصة بتوقيت الآيات في التلاوة - نفس مكتبة التطبيق القديم
    implementation("net.sourceforge.jexcelapi:jxl:2.6.12")
}
