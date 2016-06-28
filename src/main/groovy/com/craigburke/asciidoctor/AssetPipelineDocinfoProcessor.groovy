package com.craigburke.asciidoctor

import static com.craigburke.asciidoctor.AsciidoctorUtil.*
import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor

class AssetPipelineDocinfoProcessor extends DocinfoProcessor {

    AssetPipelineDocinfoProcessor() {
        super()
    }

    AssetPipelineDocinfoProcessor(Map<String, Object> config) {
        super(config)
    }

    @Override
    String process(Document document) {
        String fileName = processorConfig.location == 'footer' ? 'docinfo-footer.html' : 'docinfo.html'

        List<String> filesToCheck = []

        if (convertOptions.docinfo || convertOptions.docinfo2) {
            String documentFileName = currentDocumentPath.tokenize('/').last()
            String filePrefix = documentFileName - ".${documentFileName.tokenize('.').last()}"

            filesToCheck << "/${filePrefix}-${fileName}"
        }
        else if (convertOptions.docinfo1 || convertOptions.docinfo2) {
            filesToCheck << "/${fileName}"
        }

        filesToCheck.findResult { String file -> loadAndCacheFile(file) }
    }

    Map<String, String> getProcessorConfig() {
        config.collectEntries {
            [it.key.toString(), it.value.asJavaString()]
        } as Map<String, String>
    }

}
