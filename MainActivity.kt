package com.example.calculadoraproyecto

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp

// ---------- Atajos para leer colores y medidas desde los recursos ----------
@Composable
private fun c(@ColorRes id: Int): Color = colorResource(id)

@Composable
private fun d(@DimenRes id: Int): Dp = dimensionResource(id)

@Composable
private fun Mensaje.texto(): String =
    if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())

// ---------- Estilo de cada cálculo (todo viene de colors.xml y drawable) ----------
private data class Estilo(
    @ColorRes val acento: Int,
    @ColorRes val borde: Int,
    @ColorRes val suave: Int,
    @DrawableRes val icono: Int
)

private fun Calculo.estilo(): Estilo = when (this) {
    Calculo.CUADRATICA -> Estilo(
        R.color.acento_cuadratica, R.color.acento_cuadratica_borde,
        R.color.acento_cuadratica_suave, R.drawable.ic_menu_cuadratica
    )
    Calculo.IMC -> Estilo(
        R.color.acento_imc, R.color.acento_imc_borde,
        R.color.acento_imc_suave, R.drawable.ic_menu_imc
    )
    Calculo.INTERES -> Estilo(
        R.color.acento_interes, R.color.acento_interes_borde,
        R.color.acento_interes_suave, R.drawable.ic_menu_interes
    )
    Calculo.ENERGIA -> Estilo(
        R.color.acento_energia, R.color.acento_energia_borde,
        R.color.acento_energia_suave, R.drawable.ic_menu_energia
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = colorResource(R.color.menta),
                    onPrimary = colorResource(R.color.texto_oscuro)
                )
            ) {
                Surface(color = Color.Transparent, contentColor = colorResource(R.color.blanco)) {
                    FondoLaboratorio {
                        CalculadoraApp()
                    }
                }
            }
        }
    }
}

// Fondo con degradado, dos brillos de color y una cuadrícula de puntos
@Composable
private fun FondoLaboratorio(contenido: @Composable BoxScope.() -> Unit) {
    val arriba = c(R.color.fondo_arriba)
    val abajo = c(R.color.fondo_abajo)
    val punto = c(R.color.blanco_07)
    val brilloVerde = c(R.color.brillo_verde)
    val brilloAzul = c(R.color.brillo_azul)
    val paso = d(R.dimen.paso_cuadricula)
    val radioPunto = d(R.dimen.radio_punto)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(arriba, abajo)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radioBrillo = size.width
            val esquinaSuperior = Offset(size.width, 0f)
            val esquinaInferior = Offset(0f, size.height)
            drawCircle(
                brush = Brush.radialGradient(listOf(brilloAzul, Color.Transparent), center = esquinaSuperior, radius = radioBrillo),
                radius = radioBrillo,
                center = esquinaSuperior
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(brilloVerde, Color.Transparent), center = esquinaInferior, radius = radioBrillo),
                radius = radioBrillo,
                center = esquinaInferior
            )
            val pasoPx = paso.toPx()
            val radioPx = radioPunto.toPx()
            var y = pasoPx / 2
            while (y < size.height) {
                var x = pasoPx / 2
                while (x < size.width) {
                    drawCircle(punto, radius = radioPx, center = Offset(x, y))
                    x += pasoPx
                }
                y += pasoPx
            }
        }
        contenido()
    }
}

@Composable
fun CalculadoraApp() {
    var seleccionado by rememberSaveable { mutableStateOf<Calculo?>(null) }
    BackHandler(enabled = seleccionado != null) { seleccionado = null }

    AnimatedContent(
        targetState = seleccionado,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "pantalla"
    ) { actual ->
        if (actual == null) {
            MenuScreen(onSeleccion = { seleccionado = it })
        } else {
            FormularioScreen(calculo = actual, onVolver = { seleccionado = null })
        }
    }
}

// ---------- Componentes reutilizables ----------
@Composable
private fun PildoraIdioma() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(c(R.color.blanco_12))
            .padding(horizontal = d(R.dimen.espacio_m), vertical = d(R.dimen.espacio_xs))
    ) {
        Text(
            text = stringResource(R.string.idioma_codigo),
            color = c(R.color.menta),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IconoCirculo(estilo: Estilo, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(c(R.color.blanco)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(estilo.icono),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(d(R.dimen.icono_relleno))
        )
    }
}

// ---------- Menú: lista de tarjetas con borde de neón ----------
@Composable
fun MenuScreen(onSeleccion: (Calculo) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = d(R.dimen.margen_menu), vertical = d(R.dimen.espacio_xl))
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PildoraIdioma()
        }
        Spacer(Modifier.height(d(R.dimen.espacio_xl)))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = c(R.color.blanco)
        )
        Spacer(Modifier.height(d(R.dimen.espacio_s)))
        Text(
            text = stringResource(R.string.menu_subtitulo),
            style = MaterialTheme.typography.titleMedium,
            color = c(R.color.menta)
        )
        Spacer(Modifier.height(d(R.dimen.espacio_xxl)))

        Calculo.entries.forEach { calculo ->
            TarjetaCalculo(calculo, onSeleccion)
            Spacer(Modifier.height(d(R.dimen.espacio_l)))
        }
    }
}

@Composable
private fun TarjetaCalculo(calculo: Calculo, onSeleccion: (Calculo) -> Unit) {
    val estilo = calculo.estilo()
    Card(
        onClick = { onSeleccion(calculo) },
        shape = RoundedCornerShape(d(R.dimen.radio_tarjeta)),
        colors = CardDefaults.cardColors(containerColor = c(R.color.blanco_07)),
        border = BorderStroke(d(R.dimen.borde_tarjeta), c(estilo.borde)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(d(R.dimen.espacio_l)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoCirculo(estilo, d(R.dimen.icono_menu))
            Spacer(Modifier.width(d(R.dimen.espacio_l)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(calculo.titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c(R.color.blanco)
                )
                Spacer(Modifier.height(d(R.dimen.espacio_xs)))
                Text(
                    text = stringResource(calculo.descripcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c(R.color.blanco_70)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = c(estilo.acento),
                modifier = Modifier.size(d(R.dimen.icono_flecha))
            )
        }
    }
}

// ---------- Formulario ----------
@Composable
private fun FormularioScreen(calculo: Calculo, onVolver: () -> Unit) {
    val context = LocalContext.current
    val estilo = calculo.estilo()
    val acento = c(estilo.acento)
    val oscuro = c(R.color.texto_oscuro)

    // Se reinician cuando cambia el cálculo seleccionado
    val valores = remember(calculo) { mutableStateMapOf<String, String>() }
    var errores by remember(calculo) { mutableStateOf<Map<String, Mensaje>>(emptyMap()) }
    var resultado by remember(calculo) { mutableStateOf<Resultado?>(null) }
    val animacionResultado = remember(resultado) {
        MutableTransitionState(false).apply { targetState = true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = d(R.dimen.espacio_l), vertical = d(R.dimen.espacio_m)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(c(R.color.blanco_12))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_volver),
                    tint = c(R.color.blanco)
                )
            }
            Spacer(Modifier.width(d(R.dimen.espacio_m)))
            Text(
                text = stringResource(calculo.titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c(R.color.blanco),
                modifier = Modifier.weight(1f)
            )
            PildoraIdioma()
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = d(R.dimen.espacio_xl), vertical = d(R.dimen.espacio_s)),
            verticalArrangement = Arrangement.spacedBy(d(R.dimen.espacio_l))
        ) {
            // Descripción y fórmula
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCirculo(estilo, d(R.dimen.icono_formulario))
                Spacer(Modifier.width(d(R.dimen.espacio_m)))
                Text(
                    text = stringResource(calculo.descripcion),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c(R.color.blanco_85)
                )
            }
            Card(
                shape = RoundedCornerShape(d(R.dimen.radio_tarjeta)),
                colors = CardDefaults.cardColors(containerColor = c(R.color.blanco)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(calculo.formula),
                    contentDescription = stringResource(calculo.titulo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(d(R.dimen.alto_formula))
                        .padding(d(R.dimen.espacio_m))
                )
            }

            // Campos dinámicos según el cálculo
            calculo.campos.forEach { campo ->
                val error = errores[campo.id]
                OutlinedTextField(
                    value = valores[campo.id] ?: "",
                    onValueChange = { valores[campo.id] = it },
                    label = { Text(stringResource(campo.etiqueta)) },
                    supportingText = { Text((error ?: campo.ayuda).texto()) },
                    isError = error != null,
                    singleLine = true,
                    shape = RoundedCornerShape(d(R.dimen.radio_campo)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = c(R.color.blanco),
                        unfocusedTextColor = c(R.color.blanco),
                        errorTextColor = c(R.color.blanco),
                        focusedBorderColor = acento,
                        unfocusedBorderColor = c(R.color.blanco_35),
                        errorBorderColor = c(R.color.error_fuerte),
                        focusedLabelColor = acento,
                        unfocusedLabelColor = c(R.color.blanco_70),
                        errorLabelColor = c(R.color.error_fuerte),
                        cursorColor = acento,
                        focusedContainerColor = c(R.color.blanco_06),
                        unfocusedContainerColor = c(R.color.blanco_06),
                        errorContainerColor = c(R.color.blanco_06),
                        focusedSupportingTextColor = c(R.color.blanco_60),
                        unfocusedSupportingTextColor = c(R.color.blanco_60),
                        errorSupportingTextColor = c(R.color.error_claro)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (campo.permiteNegativos) KeyboardType.Text else KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error que involucra varios campos
            errores["general"]?.let { mensaje ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = c(R.color.error_claro))
                    Spacer(Modifier.width(d(R.dimen.espacio_s)))
                    Text(
                        text = mensaje.texto(),
                        color = c(R.color.error_claro),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = {
                    val (errs, res) = calcular(calculo, valores)
                    errores = errs
                    resultado = res
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = acento, contentColor = oscuro),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(d(R.dimen.alto_boton))
            ) {
                Text(
                    text = stringResource(R.string.btn_calcular),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Resultado (aparece con animación)
            resultado?.let { r ->
                AnimatedVisibility(
                    visibleState = animacionResultado,
                    enter = fadeIn() + expandVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(d(R.dimen.radio_tarjeta)),
                        colors = CardDefaults.cardColors(containerColor = c(R.color.blanco)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(d(R.dimen.espacio_l)),
                            verticalArrangement = Arrangement.spacedBy(d(R.dimen.espacio_m))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c(R.color.verde_ok))
                                Spacer(Modifier.width(d(R.dimen.espacio_s)))
                                Text(
                                    text = stringResource(R.string.resultado_titulo),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = oscuro
                                )
                            }
                            Text(
                                text = stringResource(r.texto, *r.args.toTypedArray()),
                                style = MaterialTheme.typography.titleLarge,
                                color = oscuro
                            )
                            r.nota?.let { nota ->
                                Surface(color = acento, shape = CircleShape) {
                                    Text(
                                        text = stringResource(nota),
                                        modifier = Modifier.padding(
                                            horizontal = d(R.dimen.espacio_l),
                                            vertical = d(R.dimen.espacio_xs)
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = oscuro
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Limpiar y Compartir
            Row(
                horizontalArrangement = Arrangement.spacedBy(d(R.dimen.espacio_m)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        valores.clear()
                        errores = emptyMap()
                        resultado = null
                    },
                    shape = CircleShape,
                    border = BorderStroke(d(R.dimen.borde_fino), c(R.color.blanco_50)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = c(R.color.blanco)),
                    modifier = Modifier
                        .weight(1f)
                        .height(d(R.dimen.alto_boton_pequeno))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(d(R.dimen.espacio_s)))
                    Text(stringResource(R.string.btn_limpiar))
                }
                Button(
                    onClick = {
                        resultado?.let { r ->
                            val titulo = context.getString(calculo.titulo)
                            val cuerpo = context.getString(r.texto, *r.args.toTypedArray())
                            val nota = r.nota
                            val texto = if (nota != null) {
                                context.getString(R.string.compartir_con_nota, titulo, cuerpo, context.getString(nota))
                            } else {
                                context.getString(R.string.compartir_sin_nota, titulo, cuerpo)
                            }
                            val intento = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            context.startActivity(
                                Intent.createChooser(intento, context.getString(R.string.compartir_titulo))
                            )
                        }
                    },
                    enabled = resultado != null,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c(estilo.suave),
                        contentColor = acento,
                        disabledContainerColor = c(R.color.blanco_06),
                        disabledContentColor = c(R.color.blanco_35)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(d(R.dimen.alto_boton_pequeno))
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(d(R.dimen.espacio_s)))
                    Text(stringResource(R.string.btn_compartir))
                }
            }
            Spacer(Modifier.height(d(R.dimen.espacio_s)))
        }
    }
}