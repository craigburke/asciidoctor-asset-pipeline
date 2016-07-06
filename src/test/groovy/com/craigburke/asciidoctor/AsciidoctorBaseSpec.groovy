package com.craigburke.asciidoctor

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AsciidoctorBaseSpec extends Specification {

    @Rule TemporaryFolder assetFolder

    def setup() {
        assetPipelineConfig = [:]
        FileSystemAssetResolver appResolver = new FileSystemAssetResolver('application', assetRoot)
        AssetPipelineConfigHolder.resolvers = [appResolver]
    }

    protected static setAssetPipelineConfig(Map config) {
        AssetPipelineConfigHolder.config.asciidoctor = config
    }

    protected String getAssetRoot() {
        assetFolder.root.absolutePath
    }

    protected String getAsciidocRoot() {
        "${assetRoot}/asciidoc" as String
    }
}
