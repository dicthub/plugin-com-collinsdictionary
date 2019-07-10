package org.dicthub.com_collinsdictionary

import JQuery
import jQuery


class DeParser : DictHtmlParser {

    override fun parse(html: String): DictItem? {

        val jq = jQuery(html = html)

        val mainPart = jq.find(".cb-def").has(".definitions").first()

        val cbh = mainPart.children(".cB-h")
        val query = cbh.children(".h2_entry").first().text().takeIf { it.isNotBlank() } ?: return null
        val pronunciation = cbh.children(".mini_h2").find(".pron").text().takeIf { it.isNotBlank() }
        val audioUrl = cbh.children(".mini_h2").find("a.sound").attr("data-src-mp3").takeIf { it.isNotBlank() }

        val parts = mainPart.children(".definitions").children(".hom").has(".sense").has(".type-translation").mapToList { parseSenseItemPart(it) }

        return DictItem(
                query = query,
                pronunciation = pronunciation,
                audioUrl = audioUrl,
                categories = parts.toTypedArray()
        )
    }

    private fun parseSenseItemPart(element: JQuery): DictItemCategory {
        val pos = element.children(".gramGrp").find(".pos").first().text()
        val senses = element.children(".sense").mapToList { parseSenseListItem(it) }

        return DictItemCategory(pos = pos, senses = senses.toTypedArray())
    }

    private fun parseSenseListItem(element: JQuery): DictItemSenseItem? {

        val subLineElements = element.children(".sense")
        val lines = subLineElements.add(element).mapToList { parseSenseLine(it) }

        val examples = element.find(".type-example").mapToList { parseSenseItemExample(it) }
        val phrases = element.find(".type-lexstring").mapToList { parseSenseItemLexString(it) }
        val usages = mutableListOf<String>().apply {
            addAll(examples)
            addAll(phrases)
        }.filter { it.isNotBlank() }

        return DictItemSenseItem(
                lines = lines.toTypedArray(),
                exmaples = usages.toTypedArray()
        )
    }

    private fun parseSenseLine(element: JQuery): DictItemSenseItemLine? {

        val translation = element.children(".type-translation").text()
        val label = element.children(".lbl").text()
        val colloc = element.children(".gramGrp").text()

        val isBlank = arrayOf(translation, label, colloc).all { it.isBlank() }
        if (isBlank) {
            if (element.children(".re").has(".type-min_phr").length.toInt() > 0) {
                return parseSenseMinimalUnit(element.children(".re").has(".type-min_phr"))
            }
        }

        return DictItemSenseItemLine(
                translation = translation,
                prefix = listOf(label, colloc).filter { it.isNotBlank() }.toTypedArray()
        )
    }

    private fun parseSenseMinimalUnit(element: JQuery): DictItemSenseItemLine? {
        val translation = element.children(".type-translation").text()
        val orth = element.children(".type-min_phr").text()
        return DictItemSenseItemLine(
                translation = translation,
                prefix = arrayOf(orth)
        )
    }

    private fun parseSenseItemExample(element: JQuery): String? {
        val quote = element.children(".quote").text()
        val translation = element.children(".type-translation").text()
        if (quote.isBlank() && translation.isBlank()) {
            return null
        }
        return arrayOf(quote, translation).filter { it.isNotBlank() }.joinToString("\r\n")
    }

    private fun parseSenseItemLexString(element: JQuery): String? {
        val phr = element.children(".type-min_phr").text()
        val translation = element.children(".type-translation").text()
        if (phr.isBlank() && translation.isBlank()) {
            return null
        }
        return arrayOf(phr, translation).filter { it.isNotBlank() }.joinToString("\r\n")
    }
}