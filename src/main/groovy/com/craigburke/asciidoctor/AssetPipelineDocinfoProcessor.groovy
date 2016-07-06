package com.craigburke.asciidoctor

import groovy.transform.CompileStatic
import org.jruby.RubyBasicObject
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor

/**
 * Docinfo processor for the Asset Pipeline
 * @author Craig Burke
 */
@CompileStatic
class AssetPipelineDocinfoProcessor extends DocinfoProcessor {

    AssetPipelineDocinfoProcessor() {
        super()
    }

    AssetPipelineDocinfoProcessor(Map config) {
        super(config as Map<String, Object>)
    }

    @Override
    String process(Document document) {
        String fileName = processorConfig?.location == 'footer' ? 'docinfo-footer.html' : 'docinfo.html'

        List<String> filesToCheck = []

        if (config.header_footer || config.docinfo || config.docinfo2) {
            String documentFileName = AsciidoctorUtil.currentDocumentPath.tokenize('/').last()
            String filePrefix = documentFileName - ".${documentFileName.tokenize('.').last()}"

            filesToCheck << ("/${filePrefix}-${fileName}" as String)
        }

        if (config.header_footer || config.docinfo1 || config.docinfo2) {
            filesToCheck << ("/${fileName}" as String)
        }

        filesToCheck.findResult { String file -> AsciidoctorUtil.loadAndCacheFile(file) }
    }

    Map<String, String> getProcessorConfig() {
        config.collectEntries { Object key, Object value ->
            String valueAsString = ((RubyBasicObject)value).asJavaString()
            [(key.toString()), valueAsString]
        } as Map<String, String>
    }

}
