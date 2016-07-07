package com.craigburke.asciidoctor

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spock.lang.Unroll

class IncludeProcessorSpec extends AsciidoctorBaseSpec {

    def "include include doesn't throw exception"() {
        when:
        processAsciidoc(input)

        then:
        notThrown(Exception)

        where:
        input = 'include::source/bogus.txt[]'
    }

    def "Can include simple text file"() {
        setup:
        assetPipelineConfig = [embeddable: true]

        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        then:
        document.select('p').text() == 'Hello World!'

        where:
        input = 'include::source/hello.txt[]'
    }

    def "Can include entire tagged code file"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        then:
        document.select('pre').text() == getFileText('source/code.groovy')

        where:
        input = '''\
        |----
        |include::source/code.groovy[]
        |----
        |'''.stripMargin()
    }

    @Unroll
    def "Can include code with the followings tags #tags"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        then:
        document.select('pre').toString() == "<pre>${expectedResult}</pre>" as String

        where:
        tags           | expectedResult
        ['foo']        | "    String foo() { 'FOO!' }"
        ['bar']        | "    String bar() { 'BAR!' }"
        ['foo', 'bar'] | "    String foo() { 'FOO!' }\n    String bar() { 'BAR!' }"
        ['bar', 'foo'] | "    String bar() { 'BAR!' }\n    String foo() { 'FOO!' }"

        includeOptions = tags ? "tags=${tags.join(';')}" : ''
        input = """\
        |----
        |include::source/code.groovy[${includeOptions}]
        |----
        |""".stripMargin()
    }

    def "Referring to a nonexistent tag causes an Exception"() {
        when:
        processAsciidoc(input)

        then:
        thrown(RuntimeException)

        where:
        input = '''\
        |----
        |include::source/code.groovy[tags=bogus]
        |----
        |'''.stripMargin()
    }

    def "Including a file with no closing tag causes an Exception"() {
        when:
        processAsciidoc(input)

        then:
        thrown(RuntimeException)

        where:
        input = '''\
        |----
        |include::source/no-closing-tag.groovy[tags=foo]
        |----
        |'''.stripMargin()
    }

    private String getFileText(String file) {
        IncludeProcessorSpec.classLoader.getResource("assets/asciidoc/${file}")?.text ?: ''
    }

}
