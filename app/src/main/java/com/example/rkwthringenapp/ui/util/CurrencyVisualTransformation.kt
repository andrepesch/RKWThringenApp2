package com.example.rkwthringenapp.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.filter { it.isDigit() }
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formattedText = originalText.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed() + " €"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val dotsBefore = (offset - 1).coerceAtLeast(0) / 3
                return offset + dotsBefore
            }

            override fun transformedToOriginal(offset: Int): Int {
                val dotsInSubstring = text.text.substring(0, offset).count { it == '.' }
                return offset - dotsInSubstring
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}