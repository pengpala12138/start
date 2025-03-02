
plugins {
    alias(libs.plugins.androidApplication)
}

android {
    //设置项目的命名空间，用于唯一标识项目
    namespace = "com.example.lbsdemo_d"
    //设置项目的编译SDK版本
    compileSdk = 34

    defaultConfig {
        //设置应用程序的包名，用于在设备上唯一表示应用程序
        applicationId = "com.example.lbsdemo_d"
        //设置应用程序的最小支持SDK版本
        minSdk = 24
        //设置应用程序的目标SDK版本
        targetSdk = 34
        //设置应用程序的版本号和版本代码
        versionCode = 1
        versionName = "1.0"
        //设置应用程序的测试运行器
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    /**
     * 配置项目的源集，指定主源集下 JNI 库的源目录。
     * 此配置确保项目在构建时能够正确找到并使用位于 'libs' 目录下的 JNI 库文件。
     */
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    // 添加百度SDK需要的配置
    ndkVersion = "25.1.8937393"
    defaultConfig {
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
    }
}

dependencies {
    // 引入app/libs目录下的所有jar和aar文件
    implementation(fileTree("libs") { include("*.jar", "*.aar") })

    // 使用正确的Glide版本
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    //引入AndroidX库 AppCompat、CardView、RecyclerView、ConstraintLayout、Lifecycle、CoreKtx等
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")
    //动画库
    implementation("com.airbnb.android:lottie:3.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.protobuf:protobuf-java:3.21.9")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}