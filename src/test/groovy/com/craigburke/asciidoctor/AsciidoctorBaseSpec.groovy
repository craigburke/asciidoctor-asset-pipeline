package com.craigburke.asciidoctor

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.GenericAssetFile
import asset.pipeline.fs.FileSystemAssetResolver
import spock.lang.Specification

class AsciidoctorBaseSpec extends Specification {

    private String assetRoot

    def setup() {
        assetRoot = AsciidoctorBaseSpec.classLoader.getResource('assets').path
        assetPipelineConfig = [:]
        FileSystemAssetResolver appResolver = new FileSystemAssetResolver('application', assetRoot)
        AssetPipelineConfigHolder.resolvers = [appResolver]
    }

    protected static processAsciidoc(String input) {
        AsciidoctorProcessor processor = new AsciidoctorProcessor(null)
        processor.process(input, new GenericAssetFile())
    }

    protected static setAssetPipelineConfig(Map config) {
        AssetPipelineConfigHolder.config.asciidoctor = config
    }

    protected String getAssetRoot() {
        assetRoot
    }

    protected String getAsciidocRoot() {
        "${assetRoot}/asciidoc" as String
    }
}
