package com.example.autoelite_android.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.autoelite_android.model.FacturaResponse
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generarFacturaPdf(context: Context, factura: FacturaResponse): File {
        val documento = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = documento.startPage(pageInfo)
        val canvas = page.canvas

        val width = pageInfo.pageWidth.toFloat()

        // Colores
        val colorPrimario = Color.parseColor("#1565C0")
        val colorTexto = Color.parseColor("#212121")
        val colorGris = Color.parseColor("#757575")
        val colorGrisClaro = Color.parseColor("#E0E0E0")
        val colorVerde = Color.parseColor("#4CAF50")
        val colorRojo = Color.parseColor("#F44336")

        // Paints
        val paintTitulo = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintSubtitulo = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            isAntiAlias = true
        }
        val paintSeccion = Paint().apply {
            color = colorPrimario
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintLabel = Paint().apply {
            color = colorGris
            textSize = 11f
            isAntiAlias = true
        }
        val paintValor = Paint().apply {
            color = colorTexto
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintNormal = Paint().apply {
            color = colorTexto
            textSize = 12f
            isAntiAlias = true
        }
        val paintTotal = Paint().apply {
            color = colorPrimario
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintLinea = Paint().apply {
            color = colorGrisClaro
            strokeWidth = 1f
        }
        val paintRect = Paint().apply {
            color = colorPrimario
            style = Paint.Style.FILL
        }

        var y = 0f

        // CABECERA (rectángulo azul)
        val headerHeight = 100f
        canvas.drawRect(0f, 0f, width, headerHeight, paintRect)

        canvas.drawText("AutoElite", 40f, 50f, paintTitulo)
        canvas.drawText("Gestión integral de talleres", 40f, 70f, paintSubtitulo)

        // Número de factura en la cabecera (derecha)
        val paintNumFactura = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(factura.numeroFactura, width - 40f, 50f, paintNumFactura)

        val paintFechaHeader = Paint().apply {
            color = Color.parseColor("#B3FFFFFF")
            textSize = 11f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Fecha: ${factura.fecha}", width - 40f, 70f, paintFechaHeader)

        y = headerHeight + 30f

        // DATOS DEL CLIENTE
        canvas.drawText("DATOS DEL CLIENTE", 40f, y, paintSeccion)
        y += 20f
        canvas.drawLine(40f, y, width - 40f, y, paintLinea)
        y += 15f

        canvas.drawText("Cliente:", 40f, y, paintLabel)
        canvas.drawText(factura.clienteNombre, 140f, y, paintValor)
        y += 20f

        canvas.drawText("Nº Factura:", 40f, y, paintLabel)
        canvas.drawText(factura.numeroFactura, 140f, y, paintValor)
        y += 20f

        canvas.drawText("Fecha:", 40f, y, paintLabel)
        canvas.drawText(factura.fecha, 140f, y, paintValor)
        y += 35f

        // DETALLE DE LA FACTURA
        canvas.drawText("DETALLE", 40f, y, paintSeccion)
        y += 20f
        canvas.drawLine(40f, y, width - 40f, y, paintLinea)
        y += 5f

        // Cabecera de tabla
        val paintTableHeader = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, y, width - 40f, y + 25f, paintTableHeader)
        y += 18f

        val paintHeaderText = Paint().apply {
            color = colorGris
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CONCEPTO", 50f, y, paintHeaderText)

        val paintHeaderRight = Paint(paintHeaderText).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("IMPORTE", width - 50f, y, paintHeaderRight)
        y += 20f

        // Fila de datos
        canvas.drawText("Servicios de taller", 50f, y, paintNormal)
        val paintImporteRight = Paint(paintValor).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("${factura.total} €", width - 50f, y, paintImporteRight)
        y += 15f

        canvas.drawLine(40f, y, width - 40f, y, paintLinea)
        y += 30f

        // TOTAL
        // Rectángulo de fondo para el total
        val totalBg = Paint().apply {
            color = Color.parseColor("#E3F2FD")
            style = Paint.Style.FILL
        }
        canvas.drawRect(width - 240f, y - 10f, width - 40f, y + 35f, totalBg)

        val paintTotalLabel = Paint().apply {
            color = colorGris
            textSize = 12f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL:", width - 160f, y + 18f, paintTotalLabel)
        val paintTotalValue = Paint(paintTotal).apply {
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("${factura.total} €", width - 50f, y + 20f, paintTotalValue)
        y += 55f

        // ESTADO DE PAGO
        canvas.drawText("ESTADO DE PAGO", 40f, y, paintSeccion)
        y += 20f
        canvas.drawLine(40f, y, width - 40f, y, paintLinea)
        y += 20f

        val estadoText = if (factura.pagada) "PAGADA" else "PENDIENTE DE PAGO"
        val estadoColor = if (factura.pagada) colorVerde else colorRojo
        val paintEstado = Paint().apply {
            color = estadoColor
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Icono circular de estado
        val circlePaint = Paint().apply {
            color = estadoColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(52f, y - 5f, 8f, circlePaint)
        canvas.drawText(estadoText, 70f, y, paintEstado)
        y += 20f

        if (factura.metodoPago != null) {
            canvas.drawText("Método de pago:", 40f, y, paintLabel)
            canvas.drawText(factura.metodoPago, 160f, y, paintValor)
            y += 20f
        }

        // PIE DE PÁGINA
        val footerY = 800f
        canvas.drawLine(40f, footerY, width - 40f, footerY, paintLinea)

        val paintFooter = Paint().apply {
            color = colorGris
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "AutoElite · Gestión de Talleres · Documento generado automáticamente",
            width / 2f, footerY + 15f, paintFooter
        )
        canvas.drawText(
            "Este documento sirve como justificante de su factura",
            width / 2f, footerY + 28f, paintFooter
        )

        documento.finishPage(page)

        // Guardar en caché
        val dir = File(context.cacheDir, "facturas")
        if (!dir.exists()) dir.mkdirs()

        val archivo = File(dir, "factura_${factura.numeroFactura.replace("/", "-")}.pdf")
        FileOutputStream(archivo).use { out ->
            documento.writeTo(out)
        }
        documento.close()

        return archivo
    }
}