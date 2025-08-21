package com.example.foc_test_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.ArrayDeque
import kotlin.math.abs

class CalculatorFragment : Fragment(), View.OnClickListener {

    private lateinit var display: TextView
    private val expression = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_calculator, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        display = view.findViewById(R.id.tvDisplay)

        val ids = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMul, R.id.btnDiv,
            R.id.btnEq, R.id.btnClear
        )
        ids.forEach { view.findViewById<Button>(it).setOnClickListener(this) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnClear -> {
                expression.clear()
                display.text = ""
            }
            R.id.btnEq -> {
                val expr = expression.toString().trim()
                if (expr.isEmpty()) return
                try {
                    // Sanitize pretty symbols first
                    val normalized = normalize(expr)
                    val result = eval(normalized)
                    display.text = toPretty(result)
                    expression.clear()
                    expression.append(result)
                } catch (e: Exception) {
                    display.text = "Error"
                }
            }
            else -> if (v is Button) {
                val token = when (v.id) {
                    R.id.btnMul -> "*"
                    R.id.btnDiv -> "/"
                    R.id.btnMinus -> "-"
                    R.id.btnPlus -> "+"
                    else -> v.text.toString()
                }
                // Avoid duplicate operators like "++", "+*", etc.
                if (isOperator(token)) {
                    if (expression.isEmpty()) {
                        // Only allow leading '-' for negative numbers
                        if (token != "-") return
                    } else {
                        val last = expression.last()
                        if (isOperator(last.toString())) {
                            expression.setCharAt(expression.length - 1, token[0])
                            display.text = expression.toString()
                            return
                        }
                    }
                }
                expression.append(token)
                display.text = expression.toString()
            }
        }
    }

    private fun isOperator(s: String): Boolean = s == "+" || s == "-" || s == "*" || s == "/"

    private fun normalize(expr: String): String =
        expr.replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace('–', '-') // en-dash safety

    private fun toPretty(value: Double): String {
        // Show integer values without .0
        val i = value.toLong()
        return if (abs(value - i) < 1e-9) i.toString() else value.toString()
    }

    /**
     * Simple shunting-yard evaluator supporting + - * / and decimals.
     * No parentheses (not in your UI).
     */
    private fun eval(expr: String): Double {
        val output = ArrayDeque<String>()
        val ops = ArrayDeque<Char>()

        var i = 0
        fun precedence(op: Char) = when (op) { '+','-' -> 1; '*','/' -> 2; else -> 0 }

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++

                // number (supports decimals)
                c.isDigit() || (c == '.' && i + 1 < expr.length && expr[i + 1].isDigit()) ||
                        // unary minus for negative literal
                        (c == '-' && (i == 0 || expr[i - 1] in "+-*/") &&
                                i + 1 < expr.length && (expr[i + 1].isDigit() || expr[i + 1] == '.')) -> {
                    val start = i
                    i++ // consume first char
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                    output.add(expr.substring(start, i))
                }

                c in "+-*/" -> {
                    while (ops.isNotEmpty() && precedence(ops.peek()) >= precedence(c)) {
                        output.add(ops.pop().toString())
                    }
                    ops.push(c)
                    i++
                }

                else -> throw IllegalArgumentException("Bad char: $c")
            }
        }
        while (ops.isNotEmpty()) output.add(ops.pop().toString())

        // Evaluate RPN
        val stack = ArrayDeque<Double>()
        for (tok in output) {
            when (tok) {
                "+","-","*","/" -> {
                    if (stack.size < 2) throw IllegalStateException("Bad expression")
                    val b = stack.pop()
                    val a = stack.pop()
                    val r = when (tok) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> 0.0
                    }
                    stack.push(r)
                }
                else -> stack.push(tok.toDouble())
            }
        }
        if (stack.size != 1) throw IllegalStateException("Bad expression")
        return stack.pop()
    }
}
