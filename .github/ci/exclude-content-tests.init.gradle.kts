// 公開ミラーにはコンテンツ TSV が含まれないため、TSV からバンドル用スナップショット DB を
// 生成するテスト(PrepopulatedDbGenerator)だけを CI の対象から外す。
gradle.lifecycle.beforeProject {
    tasks.withType(Test::class.java).configureEach {
        filter {
            excludeTestsMatching("*.PrepopulatedDbGenerator")
        }
    }
}
