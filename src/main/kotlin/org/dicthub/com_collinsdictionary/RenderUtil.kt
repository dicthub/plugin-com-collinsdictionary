package org.dicthub.com_collinsdictionary

import JQuery
import jQuery
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.dicthub.plugin.shared.util.*
import kotlin.random.Random


data class DictItem(
        val query: String,
        val pronunciation: String? = null,
        val audioUrl: String? = null,
        val categories: Array<DictItemCategory>
)

data class DictItemCategory(
        val pos: String,
        val senses: Array<DictItemSenseItem>
)

data class DictItemSenseItem(
        val lines: Array<DictItemSenseItemLine>,
        val labels: Array<String> = emptyArray(),
        val exmaples: Array<String> = emptyArray()
)

data class DictItemSenseItemLine(
        val translation: String,
        val prefix: Array<String> = emptyArray(),
        val suffix: Array<String> = emptyArray()
)


fun renderItem(q: Query, item: DictItem): String {
    val stringBuilder = StringBuilder()

    val container = stringBuilder.appendHTML()

    container.div(classes = "t-result") {

        div(classes = "alert alert-info") {
            em(classes = "translation-lang") {
                +"[${q.getFrom()}]"
            }
            strong {
                +item.query
            }
            +" "
            span(classes = "translation-pronunciation") {
                em {
                    item.pronunciation?.let { +"[$it]" }
                }
            }
            +" "
            item.audioUrl?.let { audioUrl ->
                span(classes = "translation-voice") {
                    audio {
                        src = audioUrl
                    }
                }
            }
        }

        ul(classes = "list-group") {
            li(classes = "list-group-item") {

                val detailId = "collins${Random.nextInt()}"
                em(classes = "translation-lang") {
                    +"[${q.getTo()}]"
                }

                val firstTranslation = item.categories.flatMap { it.senses.toList() }.flatMap { it.lines.toList() }.mapNotNull { it.translation }.first { it.isNotBlank() }
                +firstTranslation

                a(classes = "btn btn-light btn-sm mb-2", href = "#$detailId") {
                    role = "button"
                    attributes["data-toggle"] = "collapse"
                    +"\uD83D\uDCDA"
                }

                div(classes = "collapse") {
                    id = detailId
                    ul(classes = "list-group") {
                        item.categories.forEach { part ->
                            li(classes = "list-group-item") {
                                em(classes = "translation-poc") {
                                    strong {
                                        +part.pos
                                    }
                                }
                            }
                            part.senses.forEach { listItem ->
                                val lineBreak = "\r\n"
                                val titleStr = listItem.exmaples.joinToString(lineBreak + lineBreak)
                                li(classes = "list-group-item small") {
                                    title = titleStr
                                    attributes["data-toggle"] = "tooltip"
                                    listItem.labels.forEach {
                                        em(classes = "translation-secondary") { +it }
                                        +" "
                                    }
                                    if (listItem.lines.size < 2) {
                                        listItem.lines.firstOrNull()?.let { line ->
                                            line.prefix.forEach {
                                                span(classes = "badge badge-light") { +it }
                                                +" "
                                            }
                                            span(classes = "translation-primary") { +line.translation }
                                        }
                                    } else {
                                        ul {
                                            attributes["style"] = "padding-left: 0; list-style: none;"
                                            listItem.lines.forEach { line ->
                                                li {
                                                    line.prefix.forEach {
                                                        em(classes = "badge badge-light") { +it }
                                                        +" "
                                                    }
                                                    span(classes = "translation-primary") { +line.translation }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renderSource(container, q)

    return stringBuilder.toString()
}


fun sourceUrl(query: Query): String {
    val baseUrl = "https://www.collinsdictionary.com/dictionary"
    val queryText = query.getText().trim().replace(" ", "-")

    val dictionaryMap = mapOf(
            ("en" to "zh-CN") to "english-chinese",
            ("en" to "zh-TW") to "english-chinese",
            ("zh-CN" to "en") to "chinese-english",
            ("zh-TW" to "en") to "chinese-english",
            ("en" to "fr") to "english-french",
            ("fr" to "en") to "french-english",
            ("en" to "de") to "english-german",
            ("de" to "en") to "german-english",
            ("en" to "es") to "english-spanish",
            ("es" to "en") to "spanish-english",
            ("en" to "pt") to "english-portuguese",
            ("pt" to "en") to "portuguese-english",
            ("en" to "it") to "english-italian",
            ("it" to "en") to "italian-english"
    )

    return "$baseUrl/${dictionaryMap[query.getFrom() to query.getTo()]}/$queryText"
}

private fun logoBgColor(query: Query): String {
    val colorMap = mapOf(
            ("en" to "zh-CN") to "#af1e21",
            ("en" to "zh-TW") to "#af1e21",
            ("zh-CN" to "en") to "#af1e21",
            ("zh-TW" to "en") to "#af1e21",
            ("en" to "fr") to "#0069b3",
            ("fr" to "en") to "#0069b3",
            ("en" to "de") to "#434448",
            ("de" to "en") to "#434448",
            ("en" to "es") to "#ee6603",
            ("es" to "en") to "#ee6603",
            ("en" to "pt") to "#34ab99",
            ("pt" to "en") to "#34ab99",
            ("en" to "it") to "#3ea434",
            ("it" to "en") to "#3ea434"

    )
    return colorMap[query.getFrom() to query.getTo()] ?: "transparent"
}


fun renderSource(container: TagConsumer<StringBuilder>, query: Query) {
    container.div(classes = "translation-source") {
        small {
            +"Powered by "
            a(href = sourceUrl(query)) {
                target = "_blank"
                img(src = "https://www.collinsdictionary.com/external/images/logo.png?version=3.1.249") {
                    attributes["style"] = "background-color: ${logoBgColor(query)}; padding:1px"
                    height = "16"
                }
            }
        }
    }
}

fun removeTagForJquery(html: String, vararg unsupportedTags: String): String {
    var replacedHtml = html
    for (tag in unsupportedTags) {
        val regex = Regex("<$tag[^>]*?>")
        replacedHtml = replacedHtml.replace(regex, "")
    }
    return replacedHtml
}

fun <T> JQuery.mapToList(mapFunc: (JQuery) -> T?): List<T> {
    val results = kotlin.collections.mutableListOf<T>()
    this.each { _, elem ->
        val jqElem = jQuery(elem)
        mapFunc(jqElem)?.let { results.add(it) }
        true
    }
    return results
}


fun renderFailure(query: Query, failure: Throwable): String {
    console.error("Failure from $ID on $query", failure)

    val stringBuilder = StringBuilder()
    val container = stringBuilder.appendHTML()

    when (failure) {
        is TranslationNotFoundException -> {
            container.p(classes = "translation-failure alert alert-warning") {
                +"No result found for \"${query.getText()}\""
            }
        }

        is TranslationParsingFailureException -> {
            container.p(classes = "translation-failure alert alert-danger") {
                +"Parse query html failed \"${query.getText()}\""
            }
        }

        else -> {
            container.p(classes = "translation-failure alert alert-warning") {
                +(failure.message ?: "Error when calling service")
            }
        }
    }
    renderBugReport(container, ID, query, failure)
    renderSource(container, query)

    return stringBuilder.toString()
}