package com.sapiens.localize.translate.utils

import com.sapiens.localize.translate.env.StringItem

// 以 source 为基准排序，并将source 不存在的行删除，然后添加 本地需要增加的行
fun List<StringItem>.alignWithSource(otherStrings: List<StringItem>): List<StringItem> {
    val localStrings = this.toMutableList()
    val sourceStrings = otherStrings.toMutableList()

    val remoteKeys = sourceStrings.map { it.name }
    val localKeys = localStrings.map { it.name }
    // check delete
    localStrings.removeAll { !remoteKeys.contains(it.name) }

    // check add
    localStrings.addAll(sourceStrings.filter { !localKeys.contains(it.name) })

    return sourceStrings.map { remote -> localStrings.first { remote.name == it.name } }
}


fun List<StringItem>.diffWithSource(sourceStrings: List<StringItem>): List<StringItem> {
    return sourceStrings.filter { source -> this.firstOrNull { it.name == source.name } == null }
}