package com.sapiens.localize.translate.parser

import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

object XmlParser {
    fun parse(filePath: String): List<Pair<String, String>> {
        // 存储解析后的键值对结果
        val items = mutableListOf<Pair<String, String>>()
        val inputStream: InputStream = File(filePath).inputStream()

        // 初始化 XML 解析器
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(inputStream)
        document.documentElement.normalize()

        // 获取所有 string 标签节点
        val nodeList = document.getElementsByTagName("string")
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                val element = node as org.w3c.dom.Element
                // 跳过标记为不可翻译的字符串
                if (element.getAttribute("translatable") == "false") {
                    continue
                }
                // 提取 name 属性和文本内容
                val name = element.getAttribute("name")
                val value = element.textContent
                items.add(Pair(name, value))
            }
        }

        return items
    }
}