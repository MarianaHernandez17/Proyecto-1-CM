package com.example.calculadoraproyecto

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Mensaje(
    @StringRes val id: Int,
    val args: List<Any> = emptyList()
)

data class Resultado(
    @StringRes val texto: Int,
    val args: List<Any> = emptyList(),
    @StringRes val nota: Int? = null
)

data class Campo(
    val id: String,
    @StringRes val etiqueta: Int,
    val ayuda: Mensaje,
    val permiteNegativos: Boolean = false
)

enum class Calculo(
    @StringRes val titulo: Int,
    @StringRes val descripcion: Int,
    @DrawableRes val formula: Int,
    val campos: List<Campo>
) {
    CUADRATICA(
        titulo = R.string.calc_cuadratica_titulo,
        descripcion = R.string.calc_cuadratica_desc,
        formula = R.drawable.formula_cuadratica,
        campos = listOf(
            Campo("a", R.string.campo_a, Mensaje(R.string.ayuda_a), permiteNegativos = true),
            Campo("b", R.string.campo_b, Mensaje(R.string.ayuda_numero), permiteNegativos = true),
            Campo("c", R.string.campo_c, Mensaje(R.string.ayuda_numero), permiteNegativos = true)
        )
    ),
    IMC(
        titulo = R.string.calc_imc_titulo,
        descripcion = R.string.calc_imc_desc,
        formula = R.drawable.formula_imc,
        campos = listOf(
            Campo("peso", R.string.campo_peso, Mensaje(R.string.ayuda_peso, listOf(formatoNumero(Limites.PESO_MIN), formatoNumero(Limites.PESO_MAX)))),
            Campo("estatura", R.string.campo_estatura, Mensaje(R.string.ayuda_estatura, listOf(formatoNumero(Limites.ESTATURA_MIN), formatoNumero(Limites.ESTATURA_MAX))))
        )
    ),
    INTERES(
        titulo = R.string.calc_interes_titulo,
        descripcion = R.string.calc_interes_desc,
        formula = R.drawable.formula_interes,
        campos = listOf(
            Campo("capital", R.string.campo_capital, Mensaje(R.string.ayuda_capital)),
            Campo("tasa", R.string.campo_tasa, Mensaje(R.string.ayuda_tasa, listOf(formatoNumero(Limites.TASA_MIN), formatoNumero(Limites.TASA_MAX)))),
            Campo("tiempo", R.string.campo_tiempo, Mensaje(R.string.ayuda_tiempo, listOf(formatoNumero(Limites.TIEMPO_MAX))))
        )
    ),
    ENERGIA(
        titulo = R.string.calc_energia_titulo,
        descripcion = R.string.calc_energia_desc,
        formula = R.drawable.formula_energia,
        campos = listOf(
            Campo("masa", R.string.campo_masa, Mensaje(R.string.ayuda_masa)),
            Campo("velocidad", R.string.campo_velocidad, Mensaje(R.string.ayuda_velocidad, listOf(formatoNumero(Limites.VELOCIDAD_LUZ))))
        )
    )
}
