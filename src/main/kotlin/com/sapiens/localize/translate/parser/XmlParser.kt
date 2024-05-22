package com.sapiens.localize.translate.parser

import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

object XmlParser {
    fun parse(filePath: String): List<Pair<String, String>> {
        val items = mutableListOf<Pair<String, String>>()
        val inputStream: InputStream = File(filePath).inputStream()

        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(inputStream)
        document.documentElement.normalize()

        val nodeList = document.getElementsByTagName("string")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                val element = node as org.w3c.dom.Element
                // 检查是否包含 translatable="false"
                if (element.getAttribute("translatable") == "false") {
                    continue
                }
                val name = element.getAttribute("name")
                val value = element.textContent
                items.add(Pair(name, value))
            }
        }

        return items
    }
}