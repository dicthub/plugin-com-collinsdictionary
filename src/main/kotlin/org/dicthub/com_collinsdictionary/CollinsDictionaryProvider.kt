package org.dicthub.com_collinsdictionary

import org.dicthub.plugin.shared.util.*
import kotlin.js.Promise

const val ID = "plugin-com-collinsdictionary"


interface DictHtmlParser {
    fun parse(html: String): DictItem?
}

class CollinsDictionaryProvider constructor(
        private val httpClient: HttpAsyncClient
) : TranslationProvider {
    override fun id(): String {
        return ID
    }

    override fun meta() = createMeta(
            name = "Collins Dictionary",
            description = "Collins Dictionary for English, Chinese, French, German, Italian, Portuguese and Spanish.",
            source = "Collins Dictionary",
            sourceUrl = "https://www.collinsdictionary.com",
            author = "DictHub",
            authorUrl = "https://github.com/willings/DictHub"
    )

    override fun canTranslate(query: Query): Boolean {
        return getParser(query) != null
    }

    override fun translate(query: Query): Promise<String> {

        val sourceUrl = sourceUrl(query)
        val parser = getParser(query)
                ?: return Promise.reject(IllegalArgumentException("No parser found for ${query.getFrom()} to ${query.getTo()}"))

        return Promise { resolve, _ ->
            httpClient.get(sourceUrl).then { html ->
                val htmlWithoutImgTag = removeTagForJquery(html, "img")
                parser.parse(htmlWithoutImgTag)?.let {
                    resolve(renderItem(query, it))
                } ?: run {
                    renderFailure(query, TranslationNotFoundException())
                }
            }.catch {
                resolve(renderFailure(query, it))
            }
        }
    }

    private fun getParser(query: Query): DictHtmlParser? {
        return when (query.getFrom() to query.getTo()) {
            "en" to "zh-CN", "en" to "zh-TW", "zh-CN" to "en", "zh-TW" to "en" -> ZhParser()
            "en" to "fr", "fr" to "en" -> FrParser()
            "en" to "de", "de" to "en" -> DeParser()
            "en" to "es", "es" to "en" -> EsParser()
            "en" to "pt", "pt" to "en" -> PtParser()
            "en" to "it", "it" to "en" -> ItParser()
            else -> null
        }
    }

}