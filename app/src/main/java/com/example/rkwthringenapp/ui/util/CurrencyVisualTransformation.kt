package com.example.rkwthringenapp.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols // <-- Fehlender Import
import java.util.Locale

/**
 * Formatiert einen reinen Zahlen-String (z.B. "12345") in einen lokalisierten Währungs-String
 * (z.B. "12.345 €") für die Anzeige in einem Textfeld.
 */
class CurrencyVisualTransformation : VisualTransformation {

    private val numberFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.GERMANY))

    override fun filter(text: AnnotatedString): TransformedText {
        // `text.text` ist der reine Zahlen-String aus dem ViewModel, z.B. "12345"
        val originalText = text.text

        // Wenn der String leer ist, zeige nichts an, statt "0 €"
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val number = originalText.toLongOrNull() ?: 0L
        val formattedText = numberFormat.format(number) + " €"

        // Diese komplexe Logik sorgt dafür, dass der Cursor beim Tippen und Löschen
        // immer an der korrekten Position bleibt.
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // `offset` ist die Cursor-Position im originalen, unformatierten Text.
                // Wir berechnen die Anzahl der Tausenderpunkte, die vor dem Cursor eingefügt wurden.
                val separators = (offset - 1).coerceAtLeast(0) / 3
                return offset + separators
            }

            override fun transformedToOriginal(offset: Int): Int {
                // `offset` ist die Cursor-Position im formatierten Text.
                // Wir zählen die Anzahl der Ziffern bis zur Cursor-Position, um die
                // ursprüngliche Position zu finden.
                return formattedText.take(offset).count { it.isDigit() }
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}