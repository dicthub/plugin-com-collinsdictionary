package org.dicthub.com_collinsdictionary

import JQuery
import jQuery


class ZhParser : DictHtmlParser {

    override fun parse(html: String): DictItem? {
        val jq = jQuery(html = html)
        val mainPart = jq.find(".he").has(".hom").first()

        val cbh = mainPart.find(".cB-h").first()
        val query = cbh.children(".h2_entry").first().text().takeIf { it.isNotBlank() } ?: return null
        val pronunciation = parsePronunciation(cbh.children(".pron").first()).takeIf { it.isNotBlank() }

        val parts = mainPart.find(".hom").mapToList { parseDictItemCategory(it) }

        return DictItem(query = query, pronunciation = pronunciation, categories = parts.toTypedArray())
    }

    private fun parsePronunciation(element: JQuery): String {
        return formatParenthesis(element.text())
    }

    private fun parseDictItemCategory(element: JQuery): DictItemCategory {
        val pos = element.find(".gramGrp .pos").text()

        val senses = element.children(".sense_list").children(".sense_list_item").mapToList { parseSenseList(it) }.toTypedArray()

        return DictItemCategory(
                pos = pos,
                senses = senses
        )
    }

    private fun parseSenseList(element: JQuery): DictItemSenseItem {

        val subLineElements = element.find("li.sense_list_item")
        val lines = if (subLineElements.length.toInt() > 0) {
            subLineElements.mapToList { parseSenseLine(it) }
        } else {
            if (element.has(".phrase").length.toInt() > 0) {
                element.add(element.children(".phrase")).has(".cit-type-translation").mapToList { parseSenseLine(it) }
            } else {
                parseSenseLine(element)?.let { listOf(it) } ?: emptyList()
            }
        }
        val exampleList = element.find(".cit-type-example").mapToList { parseSenseItemExample(it) }
        return DictItemSenseItem(
                lines = lines.toTypedArray(),
                exmaples = exampleList.toTypedArray()
        )
    }

    private fun parseSenseLine(element: JQuery): DictItemSenseItemLine? {
        val labels = arrayOf("lbl", "phr", "gramGrp").mapNotNull { cssClass ->
            element.children(".$cssClass").text().takeIf { it.isNotEmpty() }
        }

        val translation = element.children(".cit-type-translation").text().takeIf { it.isNotBlank() } ?: return null
        val translationPron = element.children(".orth").text().takeIf { it.isNotBlank() }

        return DictItemSenseItemLine(
                translation = translation,
                prefix = labels.toTypedArray(),
                suffix = translationPron?.let { arrayOf(it) } ?: emptyArray()
        )
    }

    private fun parseSenseItemExample(element: JQuery): String {
        val orth = element.children(".orth").text().takeIf { it.isNotBlank() } ?: return element.text()
        val translation = element.children(".cit-type-translation").text().takeIf { it.isNotBlank() }
                ?: return element.text()
        val pron = element.find(".orth .pron").text()
        return arrayOf(orth, translation, pron).joinToString("\r\n")
    }

    private val removeParenthesisReg = Regex("\\((.+)\\)")
    private fun formatParenthesis(textValue: String): String {
        return removeParenthesisReg.matchEntire(textValue.trim())?.groups?.get(1)?.value ?: textValue
    }

}