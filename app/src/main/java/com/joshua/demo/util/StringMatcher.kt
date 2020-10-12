package com.joshua.demo.util

object StringMatcher {
    @JvmStatic
    fun match(value: String?, keyword: String?): Boolean {
        if (value == null || keyword == null
          || keyword.length > value.length) return false
        var index = 0
        var keywordPosition = 0
        do {
            if (keyword[keywordPosition] == value[index]) {
                index++
                keywordPosition++
            } else if (keywordPosition > 0) break else index++
        } while (index < value.length && keywordPosition < keyword.length)

        return keywordPosition == keyword.length
    }
}