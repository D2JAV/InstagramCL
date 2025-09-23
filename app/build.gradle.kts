plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)

  id("com.google.gms.google-services")
  id("kotlin-kapt")
}

android {
  namespace = "com.example.instagramcl"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.instagramcl"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.github.bumptech.glide:glide:4.16.0")
  kapt("com.github.bumptech.glide:compiler:4.16.0")

}