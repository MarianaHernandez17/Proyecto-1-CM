package com.example.calculadoraproyecto

import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

// Todos los límites y constantes del ejercicio viven aquí (y no repartidos por el código)
object Limites {
    const val PESO_MIN = 20.0
    const val PESO_MAX = 300.0
    const val ESTATURA_MIN = 0.5
    const val ESTATURA_MAX = 2.5
    const val TASA_MIN = 0.0
    const val TASA_MAX = 100.0
    const val TIEMPO_MAX = 100.0
    const val VELOCIDAD_LUZ = 299_792_458.0

    const val IMC_BAJO = 18.5
    const val IMC_NORMAL = 25.0
    const val IMC_SOBREPESO = 30.0

    const val PORCENTAJE = 100.0
}

private val formato = DecimalFormat("#,##0.####")

// Da formato a un número para mostrarlo (el +0.0 evita "-0")
fun formatoNumero(x: Double): String = formato.format(x + 0.0)

/**
 * Valida y calcula. Devuelve (errores por campo, resultado).
 * La clave "general" sirve para errores que involucran varios campos.
 */
fun calcular(calculo: Calculo, v: Map<String, String>): Pair<Map<String, Mensaje>, Resultado?> {
    val errores = mutableMapOf<String, Mensaje>()

    // Campo obligatorio + número válido
    fun num(id: String): Double? {
        val texto = v[id].orEmpty().trim()
        if (texto.isEmpty()) {
            errores[id] = Mensaje(R.string.error_obligatorio)
            return null
        }
        val n = texto.replace(',', '.').toDoubleOrNull()
        if (n == null || n.isNaN() || n.isInfinite()) {
            errores[id] = Mensaje(R.string.error_numero_invalido)
            return null
        }
        return n
    }

    var resultado: Resultado? = null

    when (calculo) {
        Calculo.CUADRATICA -> {
            val a = num("a"); val b = num("b"); val c = num("c")
            if (a != null && a == 0.0) errores["a"] = Mensaje(R.string.error_a_cero)
            if (errores.isEmpty() && a != null && b != null && c != null) {
                val d = b * b - 4 * a * c
                if (d < 0) {
                    errores["general"] = Mensaje(R.string.error_discriminante)
                } else {
                    val raiz = sqrt(d)
                    val x1 = (-b + raiz) / (2 * a)
                    val x2 = (-b - raiz) / (2 * a)
                    resultado = if (d == 0.0)
                        Resultado(R.string.res_cuadratica_doble, listOf(formatoNumero(x1), formatoNumero(d)))
                    else
                        Resultado(R.string.res_cuadratica, listOf(formatoNumero(x1), formatoNumero(x2), formatoNumero(d)))
                }
            }
        }

        Calculo.IMC -> {
            val p = num("peso"); val h = num("estatura")
            if (p != null && (p < Limites.PESO_MIN || p > Limites.PESO_MAX)) {
                errores["peso"] = Mensaje(
                    R.string.error_peso_rango,
                    listOf(formatoNumero(Limites.PESO_MIN), formatoNumero(Limites.PESO_MAX))
                )
            }
            if (h != null && (h < Limites.ESTATURA_MIN || h > Limites.ESTATURA_MAX)) {
                errores["estatura"] = Mensaje(
                    R.string.error_estatura_rango,
                    listOf(formatoNumero(Limites.ESTATURA_MIN), formatoNumero(Limites.ESTATURA_MAX))
                )
            }
            if (errores.isEmpty() && p != null && h != null) {
                val imc = p / (h * h)
                val nota = when {
                    imc < Limites.IMC_BAJO -> R.string.imc_bajo
                    imc < Limites.IMC_NORMAL -> R.string.imc_normal
                    imc < Limites.IMC_SOBREPESO -> R.string.imc_sobrepeso
                    else -> R.string.imc_obesidad
                }
                resultado = Resultado(R.string.res_imc, listOf(formatoNumero(imc)), nota)
            }
        }

        Calculo.INTERES -> {
            val cap = num("capital"); val tasa = num("tasa"); val t = num("tiempo")
            if (cap != null && cap <= 0) errores["capital"] = Mensaje(R.string.error_positivo)
            if (tasa != null && (tasa < Limites.TASA_MIN || tasa > Limites.TASA_MAX)) {
                errores["tasa"] = Mensaje(
                    R.string.error_tasa_rango,
                    listOf(formatoNumero(Limites.TASA_MIN), formatoNumero(Limites.TASA_MAX))
                )
            }
            if (t != null && (t <= 0 || t > Limites.TIEMPO_MAX)) {
                errores["tiempo"] = Mensaje(R.string.error_tiempo_rango, listOf(formatoNumero(Limites.TIEMPO_MAX)))
            }
            if (errores.isEmpty() && cap != null && tasa != null && t != null) {
                val monto = cap * (1 + tasa / Limites.PORCENTAJE).pow(t)
                resultado = Resultado(
                    R.string.res_interes,
                    listOf(formatoNumero(monto), formatoNumero(monto - cap))
                )
            }
        }

        Calculo.ENERGIA -> {
            val m = num("masa"); val vel = num("velocidad")
            if (m != null && m <= 0) errores["masa"] = Mensaje(R.string.error_positivo)
            if (vel != null) {
                if (vel < 0) {
                    errores["velocidad"] = Mensaje(R.string.error_no_negativo)
                } else if (vel >= Limites.VELOCIDAD_LUZ) {
                    errores["velocidad"] = Mensaje(
                        R.string.error_velocidad_luz,
                        listOf(formatoNumero(Limites.VELOCIDAD_LUZ))
                    )
                }
            }
            if (errores.isEmpty() && m != null && vel != null) {
                resultado = Resultado(R.string.res_energia, listOf(formatoNumero(0.5 * m * vel * vel)))
            }
        }
    }
    return Pair(errores, if (errores.isEmpty()) resultado else null)
}
