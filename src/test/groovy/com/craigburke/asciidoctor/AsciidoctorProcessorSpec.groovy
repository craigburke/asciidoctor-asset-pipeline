package com.craigburke.asciidoctor

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Unroll

class AsciidoctorProcessorSpec extends AsciidoctorBaseSpec {

    def "Document doesn't render body element when embeddable is true"() {
        setup:
        assetPipelineConfig = [embeddable: true]

        when:
        String result = processAsciidoc(input)

        then:
        result.startsWith('<div class="paragraph">')

        where:
        input = '''\
        |= Heading
        |
        |Lorem ipsum.'''.stripMargin()
    }

    def "Document renders body element when embeddable is false"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        then:
        result.startsWith('<!DOCTYPE html>')
        document.select('#header')
        document.select('#content')
        document.select('#footer')
        document.select('h1').text() == 'Heading'

        where:
        input = '''\
        |= Heading
        |:!stylesheet:
        |
        |Lorem ipsum.'''.stripMargin()
    }

    def "Sets safe mode specified by name"() {
        setup:
        assetPipelineConfig = [safe: 'server']

        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        then:
        document.select('p').text() == 'server'

        where:
        input = '{safe-mode-name}'
    }

    @Unroll
    def "Document renders subheadings correctly"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        Element header = document.select(selector).first()

        then:
        header.text() == 'SubHeading'

        where:
        input              | selector
        '== SubHeading'    | 'h2'
        '=== SubHeading'   | 'h3'
        '==== SubHeading'  | 'h4'
        '===== SubHeading' | 'h5'
    }

    @Unroll
    def "Document renders link #label correctly"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        Element link = document.select('a').first()

        then:
        link.text() == text ?: url

        and:
        link.attr('href') == url

        where:
        url                         | text
        'http://www.google.com'     | 'Let Me Google that for you'
        'http://www.google.com'     | ''
        'http://www.craigburke.com' | 'Check out my website'
        'http://www.craigburke.com' | ''

        input = "${url}${text ? '[' + text + ']' : ''}"
        label = "${url}${text ? ' labeled ' + text : ''}"
    }

    def "Renders unordered lists correctly"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        Element list = document.select('body div.ulist > ul').first()
        Element item1 = list.child(0)
        Element item2 = list.child(1)

        Element item1Sublist = item1.select('ul').first()
        Element item2Sublist = item2.select('ul').first()

        Element item21Sublist = item2Sublist.select('ul').last()

        then:
        item1.tagName() == 'li'
        item2.tagName() == 'li'

        and:
        list.children().size() == 2

        and:
        item1.text().startsWith 'Item 1'

        and:
        item1Sublist.text() == 'Item 1.1'

        and:
        item2Sublist.text().startsWith 'Item 2'

        and:
        item21Sublist.text().startsWith 'Item 2.1'

        and:
        item21Sublist.child(0).text() == 'Item 2.1.1'

        where:
        input = '''\
        * Item 1
        ** Item 1.1
        * Item 2
        ** Item 2.1
        *** Item 2.1.1'''.stripIndent()
    }

    def "Renders ordered lists correctly"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        Element list = document.select('body div.olist > ol').first()
        Element item1 = list.child(0)
        Element item2 = list.child(1)

        Element item1Sublist = item1.select('ol').first()
        Element item2Sublist = item2.select('ol').first()
        Element item21Sublist = item2Sublist.select('ol').last()

        then:
        item1.tagName() == 'li'
        item2.tagName() == 'li'

        and:
        list.children().size() == 2

        and:
        item1.text().startsWith 'Item 1'

        and:
        item1Sublist.text() == 'Item 1.1'

        and:
        item2Sublist.text().startsWith 'Item 2'

        and:
        item21Sublist.text().startsWith 'Item 2.1'

        and:
        item21Sublist.child(0).text() == 'Item 2.1.1'

        where:
        input = '''\
        . Item 1
        .. Item 1.1
        . Item 2
        .. Item 2.1
        ... Item 2.1.1'''.stripIndent()
     }

    def "Renders tables correctly"() {
        when:
        String result = processAsciidoc(input)
        Document document = Jsoup.parseBodyFragment(result)

        Element table = document.select('body table').first()
        Element row1 = table.select('tr')[0]
        Element row2 = table.select('tr')[1]
        Element row3 = table.select('tr')[2]

        then:
        row1.child(0).text() == 'COL1-1'
        row1.child(1).text() == 'COL2-1'

        and:
        row2.child(0).text() == 'COL1-2'
        row2.child(1).text() == 'COL2-2'

        and:
        row3.child(0).text() == 'COL1-3'
        row3.child(1).text() == 'COL2-3'

        where:
        input = '''\
        |===
        | COL1-1 | COL2-1
        | COL1-2 | COL2-2
        | COL1-3 | COL2-3
        |==='''.stripIndent()
    }

}
