package org.dicthub.com_collinsdictionary

import JQuery
import jQuery


class FrParser : DictHtmlParser {

    override fun parse(html: String): DictItem? {

        val jq = jQuery(html = html)

        val mainPart = jq.find(".cb-def").has(".definitions").first()

        val cbh = mainPart.children(".cB-h")
        val query = cbh.children(".h2_entry").first().text().takeIf { it.isNotBlank() } ?: return null
        val pronunciation = cbh.children(".mini_h2").find(".pron").text().takeIf { it.isNotBlank() }
        val audioUrl = cbh.children(".mini_h2").find("a.sound").attr("data-src-mp3")

        val parts = mainPart.children(".definitions").children(".hom").has(".sense").mapToList { parseSenseItemPart(it) }

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

        val translation = element.children(".type-translation").text()
        val label = element.children(".lbl").text()
        val colloc = element.find(".gramGrp .colloc").text()
        val line = DictItemSenseItemLine(
                translation = translation,
                prefix = listOf(label, colloc).filter { it.isNotBlank() }.toTypedArray()
        )

        val examples = element.children(".type-example").mapToList { parseSenseItemExample(it) }
        val phrases = element.children(".type-phr").mapToList { parseSenseItemPhases(it) }
        val usages = mutableListOf<String>().apply {
            addAll(examples)
            addAll(phrases)
        }.filter { it.isNotBlank() }

        return DictItemSenseItem(
                lines = arrayOf(line),
                exmaples = usages.toTypedArray()
        )
    }


    private fun parseSenseItemExample(element: JQuery): String {
        val quote = element.children(".quote").text()
        val translation = element.children(".type-translation").text()
        return quote + "\r\n" + translation
    }

    private fun parseSenseItemPhases(element: JQuery): String {
        val phr = element.children(".type-phr").text()
        val translation = element.children(".type-translation").text()
        return phr + "\r\n" + translation
    }
}