package uabc.axel.ornelas.prctica02calculadoralogica

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import uabc.axel.ornelas.prctica02calculadoralogica.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {

    //Sirve para acceder a los elementos de la vista
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Inicializa los botones
        crearAccionesBoton()
    }

    /**
     * Inicializa todas las acciones que tiene cada boton
     */
    private fun crearAccionesBoton() {
        val ecuacion = binding.ecuacion
        //Indica que no se abra el teclado cuando se le pica a las variables
        binding.numeroA.inputType = InputType.TYPE_NULL
        binding.numeroB.inputType = InputType.TYPE_NULL
        binding.numeroC.inputType = InputType.TYPE_NULL
        binding.numeroD.inputType = InputType.TYPE_NULL

        //Agrega las acciones que cada boton tiene
        binding.btnCalc.setOnClickListener { calcular() }
        binding.btnCero.setOnClickListener { ponerNumero("0") }
        binding.btnUno.setOnClickListener { ponerNumero("1") }
        binding.btnA.setOnClickListener { ecuacion.append("A") }
        binding.btnB.setOnClickListener { ecuacion.append("B") }
        binding.btnD.setOnClickListener { ecuacion.append("D") }
        binding.btnC.setOnClickListener { ecuacion.append("C") }
        binding.btnOr.setOnClickListener { ecuacion.append("+") }
        binding.btnAnd.setOnClickListener { ecuacion.append("*") }
        binding.btnClr.setOnClickListener { ecuacion.setText(ecuacion.text.dropLast(1)) }
        binding.btnNot.setOnClickListener { ecuacion.append("!") }
        binding.btnParenInicio.setOnClickListener { ecuacion.append("(") }
        binding.btnParenFinal.setOnClickListener { ecuacion.append(")") }
    }

    /**
     * Sirve para poner el numero en la variable
     *
     * @param numero es el numero que se pondra
     */
    private fun ponerNumero(numero: String) {
        var num: EditText? = null
        //Determina que número fue presionado
        when {
            binding.numeroA.isFocused -> num = binding.numeroA
            binding.numeroB.isFocused -> num = binding.numeroB
            binding.numeroC.isFocused -> num = binding.numeroC
            binding.numeroD.isFocused -> num = binding.numeroD
        }
        //Pone el número en el texto
        num?.setText(numero)
    }

    /**
     * Calcula el resultado de la expresion matematica
     */
    private fun calcular() {
        //Es el mensaje de error
        val error: Snackbar =
            Snackbar.make(binding.root, "Error en la expresión", Snackbar.LENGTH_SHORT)
        //Expresion de la ecuación
        var expresion = binding.ecuacion.text.toString()
        val patron: Boolean =
            expresion.contains(Regex("[A-Z]{2,}")) //Verifica que no haya 2 letras seguidas
                    || expresion.contains(Regex("[A-Z]+\\(.*\\)[A-Z]+")) //Verifica que no haya parentesis en el medio
                    || expresion.contains(Regex("[A-Z]+\\(.*\\)+")) //Verifica si no agrega el * para el AND
                    || expresion.contains(Regex("\\(.*\\)[A-Z]+")) //Verifica si no agrega el * para el AND
                    || expresion.contains(Regex("[A-Z, )]+\\!")) //Verifica si el not no este a la derecha de cualquier letra
                    || expresion.contains(Regex("\\(\\)")) //Verifica parentesis vacios
                    || expresion.contains(Regex("\\(.*\\)\\(.*\\)")) //Verifica parentesis vacios
        when {
            //Verifico si la expresión esta vacia
            expresion.isEmpty() -> {
                error.setText("Expresión Vacia")
                error.show()
                return
            }
            //Validacion con expresiones regulares
            (patron) -> {
                error.setText("Error de sintaxis")
                error.show()
                return
            }
        }
        try {
            expresion = revisarFormato(expresion)
            binding.resultado.text = if (evaluarExpresion(expresion)) "1" else "0"
            //Si da error es porque estaba mal formada la ecuación
        } catch (e: Exception) {
            error.setText("Error en el formato, ingrese los datos correctos")
            error.show()
        }
    }

    /**
     * Revisa la expresion para cambiar los valores de las variables
     * @param expr Es la expresion que tiene una ecuacion
     */
    private fun revisarFormato(expr: String): String {
        var expresion: String = expr
        //Se convierte la expresion de la ecuacion a un formato que entienda más fácil la computadora
        expresion = convertirNotacionPolaca(expresion)
        expresion = expresion.replace("A", if (binding.numeroA.text.toString() == "1") "1" else "0")
        expresion = expresion.replace("B", if (binding.numeroB.text.toString() == "1") "1" else "0")
        expresion = expresion.replace("C", if (binding.numeroC.text.toString() == "1") "1" else "0")
        expresion = expresion.replace("D", if (binding.numeroD.text.toString() == "1") "1" else "0")
        return expresion
    }


    /**
     * Convierte la expresion de la ecuación en algo que se pueda evaluar más fácil
     *
     * Este método utiliza un algoritmo que viene en el libro que se llama "TADs,
     * Estructuras de datos y resolución de problemas con C++"
     *
     * @param expresion Es la expresion que tiene una ecuacion
     */
    private fun convertirNotacionPolaca(expresion: String): String {
        //Se utiliza para tener un orden correcto de los operandos y operadores
        val pila: Stack<Char> = Stack();
        val n: Int = expresion.length
        var nuevaExpresion = ""

        // Recorre toda la expresión verificando los operadadores y los operandos
        for (i in 0 until n) {
            //Se obtiene el caracter de la expresion en la pos i
            var car = expresion[i]
            when (car) {
                '(', '!' -> pila.push(car)
                '*', '+' -> {
                    //Se verifica la prioridad debe de ser un parentesis de inicio si no da lo saca
                    if (pila.isEmpty() || pila.peek() == '(' || (pila.peek() != '!' && pila.peek() != '*' && pila.peek() != '+'))
                        pila.push(car)
                    else {
                        nuevaExpresion += pila.pop()
                        pila.push(car)
                    }
                }
                ')' -> {
                    //Recorre toda la pila hasta encontrar un parentesis de inicio
                    while (car != '(' && pila.isNotEmpty()) {
                        car = pila.pop()
                        if (car != '(')
                            nuevaExpresion += car
                    }
                    //Si la pila esta vacia, o si es diferente del parentesis final significa que no lo cierra correctamente
                    if (pila.isEmpty() && car != '(')
                        throw Exception()
                }
                else -> nuevaExpresion += car

            }
        }
        if (pila.isNotEmpty())
            while (pila.isNotEmpty())
                nuevaExpresion += pila.pop()

        return nuevaExpresion
    }

    /**
     * Evalua la expresión de la ecuación siempre y cuando anteriormente este la expresion
     * En notación postfija
     *
     * @param expresion Es la expresion que tiene una ecuacion
     */
    private fun evaluarExpresion(expresion: String): Boolean {
        //Se utiliza para ordenar el orden de las operaciones
        val pila: Stack<Boolean> = Stack()
        val n: Int = expresion.length

        //Recorre toda la expresión
        for (i in 0 until n) {
            //Obtiene el caracter
            val car = expresion[i]
            //Si es un digito ingresa el valor del número como Booleano a la pila
            if (car.isDigit()) {
                pila.push(car == '1')
                continue
            }
            //Si el operador es NOT, niega el tope de la pila y lo vuelve a introducir
            if (car == '!') {
                pila.push(!pila.pop())
                continue
            }
            //Si es otro operador que no sea el NOT, quita 2 elementos del tope de la pila
            val a: Boolean = pila.pop()
            val b: Boolean = pila.pop()

            //Realiza las operaciones correspondientes a su operador
            when (car) {
                '+' -> pila.push(a || b)
                '*' -> pila.push(a && b)
                else -> throw Exception()
            }
        }
        //Devuelve el resultado final de la ecuación
        return pila.pop()
    }
}