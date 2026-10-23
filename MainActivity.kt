package com.fcc.imc
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import net.objecthunter.exp4j.operator.Operator
class MainActivity : AppCompatActivity() {

    // Variables para la calculadora científica (Horizontal)
    private var currentExpression = ""
    private var justCalculated = false
    private var isInvActive = false

    // Vistas de IMC (Vertical) - se inicializan de forma segura
    private var etPeso: EditText? = null
    private var etEstatura: EditText? = null
    private var tvResultadoImc: TextView? = null
    private var rbMetrico: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Intentar enlazar las vistas de la calculadora científica
        val tvDisplay = findViewById<TextView>(R.id.tvDisplay)

        if (tvDisplay != null) {
            // ESTAMOS EN MODO HORIZONTAL (Calculadora Científica)
            if (savedInstanceState != null) {
                currentExpression = savedInstanceState.getString("EXPRESSION", "")
                justCalculated = savedInstanceState.getBoolean("JUST_CALCULATED", false)
                isInvActive = savedInstanceState.getBoolean("IS_INV_ACTIVE", false)
                tvDisplay.text = if (currentExpression.isEmpty()) "0" else currentExpression
            }
            setupCalculatorLogic()
            updateInvUI()
        } else {
            // ESTAMOS EN MODO VERTICAL (Calculadora de IMC)
            etPeso = findViewById(R.id.etPeso)
            etEstatura = findViewById(R.id.etEstatura)
            tvResultadoImc = findViewById(R.id.tvResultadoImc)
            rbMetrico = findViewById(R.id.rbMetrico)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Guardar estado solo si estamos usando la calculadora científica
        if (findViewById<TextView>(R.id.tvDisplay) != null) {
            outState.putString("EXPRESSION", currentExpression)
            outState.putBoolean("JUST_CALCULATED", justCalculated)
            outState.putBoolean("IS_INV_ACTIVE", isInvActive)
        }
        super.onSaveInstanceState(outState)
    }

    // --- LÓGICA DE IMC (VERTICAL) ---
    // --- LÓGICA DE IMC (VERTICAL) ---
    fun calcularImc(view: View) {
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etEstatura = findViewById<EditText>(R.id.etEstatura)
        val tvResultadoImc = findViewById<TextView>(R.id.tvResultadoImc)
        val tvClasificacionImc = findViewById<TextView>(R.id.tvClasificacionImc)
        val rbMetrico = findViewById<RadioButton>(R.id.rbMetrico)

        val pesoStr = etPeso?.text.toString()
        val estaturaStr = etEstatura?.text.toString()

        if (pesoStr.isEmpty() || estaturaStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa peso y estatura", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val peso = pesoStr.toDouble()
            var estatura = estaturaStr.toDouble()

            if (estatura <= 0 || peso <= 0) {
                tvResultadoImc?.text = "Error"
                tvClasificacionImc?.text = ""
                return
            }

            // Si está en centímetros (sistema métrico común), convertir a metros si es mayor a 3
            if (rbMetrico?.isChecked == true && estatura > 3.0) {
                estatura = estatura / 100.0
            }

            val imc = if (rbMetrico?.isChecked == true) {
                peso / (estatura * estatura)
            } else {
                // Sistema inglés (libras y pulgadas)
                (peso / (estatura * estatura)) * 703
            }

            // Mostrar resultado numérico con 2 decimales
            tvResultadoImc?.text = String.format("%.2f", imc)

            // Determinar la clasificación según la tabla proporcionada
            val clasificacion = when {
                imc < 16.00 -> "Delgadez severa"
                imc in 16.00..16.99 -> "Delgadez moderada"
                imc in 17.00..18.49 -> "Delgadez leve"
                imc in 18.50..24.99 -> "Peso Normal"
                imc in 25.00..29.99 -> "Preobeso"
                imc in 30.00..34.99 -> "Obesidad leve"
                imc in 35.00..39.99 -> "Obesidad media"
                else -> "Obesidad mórbida"
            }

            tvClasificacionImc?.text = clasificacion

        } catch (e: Exception) {
            tvResultadoImc?.text = "Error"
            tvClasificacionImc?.text = ""
        }
    }

    // --- LÓGICA DE LA CALCULADORA CIENTÍFICA (HORIZONTAL) ---
    private fun setupCalculatorLogic() {
        val tvDisplay = findViewById<TextView>(R.id.tvDisplay)

        val numberAndBracketIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnOpenBracket, R.id.btnCloseBracket
        )

        val operatorIds = listOf(
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        val scientificIds = listOf(
            R.id.btnSin, R.id.btnCos, R.id.btnTan,
            R.id.btnSqrt, R.id.btnPower, R.id.btnPi, R.id.btnLog
        )

        findViewById<Button>(R.id.btnINV)?.setOnClickListener {
            isInvActive = !isInvActive
            updateInvUI()
        }

        for (id in numberAndBracketIds) {
            findViewById<Button>(id)?.setOnClickListener { view ->
                val buttonText = (view as Button).text.toString()
                if (justCalculated) {
                    currentExpression = ""
                    justCalculated = false
                }
                if (currentExpression == "0" && buttonText != ".") {
                    currentExpression = buttonText
                } else {
                    currentExpression += buttonText
                }
                tvDisplay.text = currentExpression
            }
        }

        for (id in operatorIds) {
            findViewById<Button>(id)?.setOnClickListener { view ->
                val buttonText = (view as Button).text.toString()
                if (justCalculated) {
                    justCalculated = false
                }
                if (currentExpression.isEmpty()) {
                    if (buttonText == "-") {
                        currentExpression = "-"
                        tvDisplay.text = currentExpression
                    }
                    return@setOnClickListener
                }
                val lastChar = currentExpression.last()
                val isLastOperator = lastChar in listOf('+', '-', '×', '÷', '*', '/')
                if (isLastOperator) {
                    if (buttonText == "-" && (isLastOperator || lastChar == '(')) {
                        currentExpression += buttonText
                    } else if (buttonText != "-") {
                        currentExpression = currentExpression.dropLast(1) + buttonText
                    }
                } else {
                    currentExpression += buttonText
                }
                tvDisplay.text = currentExpression
            }
        }

        for (id in scientificIds) {
            findViewById<Button>(id)?.setOnClickListener { view ->
                val buttonText = (view as Button).text.toString()
                if (justCalculated) {
                    justCalculated = false
                }
                if (currentExpression == "0") {
                    currentExpression = ""
                }

                when (buttonText) {
                    "sin" -> currentExpression += "sin("
                    "cos" -> currentExpression += "cos("
                    "tan" -> currentExpression += "tan("
                    "asin" -> currentExpression += "asin("
                    "acos" -> currentExpression += "acos("
                    "atan" -> currentExpression += "atan("
                    "√" -> currentExpression += "√("
                    "log" -> currentExpression += "log10("
                    "ln" -> currentExpression += "log("
                    "^" -> currentExpression += "^"
                    "!" -> currentExpression += "!"
                    "π" -> currentExpression += "3.1415926535"
                    "e" -> currentExpression += "2.7182818284"
                }
                tvDisplay.text = currentExpression
            }
        }

        findViewById<Button>(R.id.btnC)?.setOnClickListener {
            currentExpression = ""
            tvDisplay.text = "0"
            justCalculated = false
        }

        findViewById<Button>(R.id.btnDel)?.setOnClickListener {
            if (justCalculated) {
                currentExpression = ""
                tvDisplay.text = "0"
                justCalculated = false
            } else if (currentExpression.isNotEmpty()) {
                currentExpression = currentExpression.dropLast(1)
                tvDisplay.text = if (currentExpression.isEmpty()) "0" else currentExpression
            }
        }

        findViewById<Button>(R.id.btnEquals)?.setOnClickListener {
            if (currentExpression.isNotEmpty()) {
                try {
                    var formattedExpression = currentExpression
                        .replace("×", "*")
                        .replace("÷", "/")

                    val openBrackets = formattedExpression.count { it == '(' }
                    val closeBrackets = formattedExpression.count { it == ')' }
                    if (openBrackets > closeBrackets) {
                        formattedExpression += ")".repeat(openBrackets - closeBrackets)
                    }

                    // Funciones trigonométricas en Grados con restricciones matemáticas exactas
                    // Función Seno con corrección de ceros exactos (para múltiplos de 180°)
                    val sinDeg = object : Function("sin", 1) {
                        override fun apply(vararg args: Double): Double {
                            val rad = Math.toRadians(args[0])
                            val result = Math.sin(rad)
                            // Si está extremadamente cerca de 0, lo devolvemos como 0 exacto
                            return if (Math.abs(result) < 1e-10) 0.0 else result
                        }
                    }

                    // Función Coseno con corrección de ceros exactos (para 90°, 270°)
                    val cosDeg = object : Function("cos", 1) {
                        override fun apply(vararg args: Double): Double {
                            val rad = Math.toRadians(args[0])
                            val result = Math.cos(rad)
                            // Si está extremadamente cerca de 0, se devuelve como 0 exacto
                            return if (Math.abs(result) < 1e-10) 0.0 else result
                        }
                    }

                    val tanDeg = object : Function("tan", 1) {
                        override fun apply(vararg args: Double): Double {
                            val angle = args[0]
                            // Normalizar el ángulo para verificar si cae en 90° o 270° (tangente indefinida
                            val normalized = ((angle % 180) + 180) % 180
                            if (Math.abs(normalized - 90.0) < 1e-9) {
                                return Double.NaN // Esto genera "Error" en pantalla
                            }
                            return Math.tan(Math.toRadians(angle))
                        }
                    }
                    val asinDeg = object : Function("asin", 1) {
                        override fun apply(vararg args: Double): Double = Math.toDegrees(Math.asin(args[0]))
                    }
                    val acosDeg = object : Function("acos", 1) {
                        override fun apply(vararg args: Double): Double = Math.toDegrees(Math.acos(args[0]))
                    }
                    val atanDeg = object : Function("atan", 1) {
                        override fun apply(vararg args: Double): Double = Math.toDegrees(Math.atan(args[0]))
                    }

                    //Factorial
                    // 1. Definir la función factorial normal
                    val factorialFunc = object : Function("fact", 1) {
                        override fun apply(vararg args: Double): Double {
                            val n = args[0].toInt()
                            if (n < 0 || args[0] != n.toDouble()) return Double.NaN
                            var fact = 1.0
                            for (i in 1..n) {
                                fact *= i.toDouble()
                            }
                            return fact
                        }
                    }

                    // 2. Extender la clase abstracta Operator para el símbolo postfijo '!'
                    val factorialOperator = object : Operator("!", 1, true, Operator.PRECEDENCE_POWER + 1) {
                        override fun apply(vararg args: Double): Double {
                            val n = args[0].toInt()
                            if (n < 0 || args[0] != n.toDouble()) return Double.NaN
                            var fact = 1.0
                            for (i in 1..n) {
                                fact *= i.toDouble()
                            }
                            return fact
                        }
                    }

                    // 3. Construir la expresión incluyendo el operador y las funciones
                    val expression = ExpressionBuilder(formattedExpression)
                        .operator(factorialOperator)
                        .function(factorialFunc)
                        .function(sinDeg)
                        .function(cosDeg)
                        .function(tanDeg)
                        .function(asinDeg)
                        .function(acosDeg)
                        .function(atanDeg)
                        .build()

                    val result = expression.evaluate()


                    val resultString = if (result.isNaN()) {
                        "Error"
                    } else if (result == result.toLong().toDouble()) {
                        result.toLong().toString()
                    } else {
                        result.toString()
                    }

                    tvDisplay.text = resultString
                    currentExpression = resultString
                    justCalculated = true
                } catch (e: Exception) {
                    tvDisplay.text = "Error"
                    currentExpression = ""
                    justCalculated = true
                }
            }
        }
    }

    private fun updateInvUI() {
        val btnInv = findViewById<Button>(R.id.btnINV)
        val btnSin = findViewById<Button>(R.id.btnSin)
        val btnCos = findViewById<Button>(R.id.btnCos)
        val btnTan = findViewById<Button>(R.id.btnTan)
        val btnPi = findViewById<Button>(R.id.btnPi)
        val btnPower = findViewById<Button>(R.id.btnPower)
        val btnLog = findViewById<Button>(R.id.btnLog)

        if (isInvActive) {
            btnInv?.setBackgroundColor(android.graphics.Color.parseColor("#43A047"))
            btnSin?.text = "asin"
            btnCos?.text = "acos"
            btnTan?.text = "atan"
            btnPi?.text = "e"
            btnPower?.text = "!"
            btnLog?.text = "ln"
        } else {
            btnInv?.setBackgroundColor(android.graphics.Color.parseColor("#1E1E1E"))
            btnSin?.text = "sin"
            btnCos?.text = "cos"
            btnTan?.text = "tan"
            btnPi?.text = "π"
            btnPower?.text = "^"
            btnLog?.text = "log"
        }
    }
}