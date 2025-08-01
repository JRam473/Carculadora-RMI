import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;

public class ClienteCalculadora extends JFrame {
    private CalculadoraNormal calculadoraNormal;
    private CalculadoraCientifica calculadoraCientifica;
    private JTextField display;
    private JPanel simplePanel;
    private JPanel scientificPanel;
    private boolean modoSimple = true;
    private List<String> historial = new ArrayList<>();
    private JTextArea historialArea;
    private JPanel mainPanel; // DECLARAR mainPanel AQUi (a nivel de clase)

    
    public ClienteCalculadora() {
        setTitle("Calculadora Cliente");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Establecer tema oscuro
        getContentPane().setBackground(Color.BLACK);

        display = new JTextField();
        display.setEditable(false);
        display.setBackground(Color.DARK_GRAY);
        display.setForeground(Color.ORANGE);
        add(display, BorderLayout.NORTH);

        simplePanel = crearPanelSimple();
        scientificPanel = crearPanelCientifico();

        // En lugar de crear un nuevo JPanel, usa el de la clase
        mainPanel = new JPanel(new CardLayout()); // Ahora usa la variable de clase
        mainPanel.add(simplePanel, "simple");
        mainPanel.add(scientificPanel, "scientific");
        add(mainPanel, BorderLayout.CENTER);

        historialArea = new JTextArea(5, 30);
        historialArea.setEditable(false);
        historialArea.setBackground(Color.DARK_GRAY);
        historialArea.setForeground(Color.ORANGE);
        JScrollPane scrollPane = new JScrollPane(historialArea);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        conectarServidorCalculadoraNormal();
        conectarServidorCalculadoraCientifica();
    }

    private JPanel crearPanelSimple() {
        JPanel panel = new JPanel(new GridLayout(5, 4));
        String[] botones = {
            "AC", "⌫", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "↔", "0", ".", "="
        };
        agregarBotones(panel, botones);
        return panel;
    }

    private JPanel crearPanelCientifico() {
        JPanel panel = new JPanel(new GridLayout(8, 5));
        String[] botones = {
            "rad","sin^-1", "cos^-1", "tan^-1", "", 
            "deg", "sin", "cos", "tan", " ",
            "x^y", "lg", "ln", "(", ")",
            "√x", "AC", "⌫", "%", "÷",
            "x!", "7", "8", "9", "×",
            "1/x", "4", "5", "6", "-",
            "π", "1", "2", "3", "+",
            "↔", "e", "0", ".", "="
        };
        agregarBotones(panel, botones);
        return panel;
    }

    private Map<String, JButton> botonesMap = new HashMap<>();

    private void agregarBotones(JPanel panel, String[] botones) {
        for (String boton : botones) {
            JButton btn = new JButton(boton);
            btn.setBackground(Color.ORANGE);
            btn.setForeground(Color.BLACK);
            btn.setFocusPainted(false); // Evitar el color azul al hacer clic
            btn.setBorderPainted(false);
            btn.addActionListener(e -> manejarBoton(boton));
            panel.add(btn);

            // Guarda el botón en el mapa con su texto como clave
            botonesMap.put(boton, btn);
        }
    }


    
    
    
    private boolean isDegMode = true; // Modo actual: true para grados, false para radianes

    // Método principal para manejar los botones
    private void manejarBoton(String boton) {
        // Verificar si se trata de un operador
        boolean esOperador = boton.equals("+") || boton.equals("×") || boton.equals("÷") || boton.equals("-");

        if (esOperador) {
            String textoActual = display.getText().trim();

            // Si el display está vacío, evitar que se ingrese un operador inicial (excepto "-")
            if (textoActual.isEmpty()) {
                if (boton.equals("-")) {
                    display.setText(boton); // Permitir "-" para números negativos
                }
                return; // Salir si es otro operador
            }

            // Verificar si el último carácter es un operador
            char ultimoCaracter = textoActual.charAt(textoActual.length() - 1);

            // Si el último operador es "/" y se ingresa "-", permitirlo para números negativos
            if (ultimoCaracter == '/' && boton.equals("-")) {
                display.setText(textoActual + boton); // Permitir "- después de /"
                return;
            }

            // Si el último carácter ya es un operador (excepto el caso anterior), reemplazarlo con el nuevo
            if (esOperador(ultimoCaracter)) {
                display.setText(textoActual.substring(0, textoActual.length() - 1) + boton);
            } else if (textoActual.matches(".*[+\\-×÷]\\(1/[πe]*$")) {
                // Completar la operación si termina en "(1/"
                display.setText(textoActual + ")");
            } else {
                // Agregar el operador normalmente
                display.setText(textoActual + boton);
            }
            return;
        }

        // Lógica para el punto (.) en los números
        if (boton.equals(".")) {
            String textoActual = display.getText();
            if (textoActual.isEmpty()) {
                // Si no hay número en el display, agregar "0."
                display.setText("0.");
            } else if (esOperador(textoActual.charAt(textoActual.length() - 1))) {
                // Si el último carácter es un operador, agregar "0."
                display.setText(textoActual + "0.");
            } else if (!textoActual.contains(".")) {
                // Si ya hay un número y no contiene un punto, agregar el punto
                display.setText(textoActual + ".");
            }
            return;
        }

        // Lógica para la acción de "="
        switch (boton) {
            case "=":
                calcular();
                break;
            case "AC":
                if (display.getText().isEmpty()) {
                    // Si el display está vacío, borrar el historial
                    historialArea.setText(""); // Borra el historial
                } else {
                    // Si hay algo en el display, limpiar solo el display
                    display.setText("");
                }
                break;
            case "rad":
                isDegMode = false;
                actualizarModo();
                break;
            case "deg":
                isDegMode = true;
                actualizarModo();
                break;
            case "CE":
            case "⌫":
                if (!display.getText().isEmpty()) {
                    display.setText(display.getText().substring(0, display.getText().length() - 1));
                }
                break;
            case "Hist":
                mostrarHistorial();
                break;
            case "x!":
                String textoActualFactorial = display.getText().trim();
            
                try {
                    if (textoActualFactorial.isEmpty()) {
                        display.setText("1");
                        agregarAlHistorial("0! = 1");
                    } else {
                        char ultimoCaracter = textoActualFactorial.charAt(textoActualFactorial.length() - 1);
            
                        if (esOperador(ultimoCaracter)) {
                            textoActualFactorial = textoActualFactorial.substring(0, textoActualFactorial.length() - 1).trim();
                        }
            
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualFactorial.lastIndexOf('+'),
                            Math.max(
                                textoActualFactorial.lastIndexOf('-'),
                                Math.max(
                                    textoActualFactorial.lastIndexOf('×'),
                                    Math.max(
                                        textoActualFactorial.lastIndexOf('÷'),
                                        textoActualFactorial.lastIndexOf('/')
                                    )
                                )
                            )
                        );
            
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualFactorial;
                        } else {
                            numeroStr = textoActualFactorial.substring(ultimaPosicionOperador + 1).trim();
                        }
            
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
            
                        if (numeroStr.isEmpty()) {
                            display.setText("1");
                            agregarAlHistorial("0! = 1");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
            
                            if (numero.compareTo(BigDecimal.ZERO) < 0) {
                                display.setText("Error");
                                agregarAlHistorial("Error: Factorial de números negativos no definido");
                            } else {
                                double valor = numero.doubleValue();
                                BigDecimal resultado;
            
                                // Verificar si el número es entero
                                if (numero.stripTrailingZeros().scale() <= 0) {
                                    // Calcular el factorial exacto para enteros
                                    resultado = calcularFactorialEntero(numero.intValue());
                                } else {
                                    // Calcular el factorial aproximado para decimales
                                    double resultadoAprox = aproximarFactorial(valor);
                                    resultado = BigDecimal.valueOf(resultadoAprox).setScale(8, RoundingMode.HALF_UP);
                                }
            
                                String nuevoTexto;
                                if (ultimaPosicionOperador == -1) {
                                    nuevoTexto = resultado.toString();
                                } else {
                                    nuevoTexto = textoActualFactorial.substring(0, ultimaPosicionOperador + 1) + resultado;
                                }
            
                                display.setText(nuevoTexto);
                                agregarAlHistorial(numero + "! = " + resultado);
                            }
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida");
                }
                break;            
            case "%":
                // Obtener el texto actual del display
                String textoActuall = display.getText().trim();
            
                // Verificar si el texto está vacío
                if (textoActuall.isEmpty()) {
                    // No hacer nada si el display está vacío
                    return;  // Salir de la función sin hacer nada
                }
            
                // Si el texto es 0, asumimos que el resultado es 0
                if (textoActuall.equals("0")) {
                    display.setText("0");
                    agregarAlHistorial("0% = 0");
                } else {
                    try {
                        // Convertir el texto a un número
                        BigDecimal numero = new BigDecimal(textoActuall);
            
                        // Llamar al método porcentaje en el servidor
                        BigDecimal resultado = calculadoraNormal.porcentaje(numero, BigDecimal.valueOf(100));
            
                        // Mostrar el resultado en el display
                        display.setText(resultado.stripTrailingZeros().toPlainString());
            
                        // Agregar al historial
                        agregarAlHistorial(numero + "% = " + resultado);
            
                    } catch (RemoteException e) {
                        // Si hay error de conexión con el servidor
                        display.setText("Error de conexión");
                        agregarAlHistorial("Error: No se pudo conectar al servidor");
                    } catch (ArithmeticException e) {
                        // Si hay error al dividir por cero (aunque no debería pasar aquí)
                        display.setText("Error");
                        agregarAlHistorial("Error: No se pudo calcular el porcentaje");
                    } catch (Exception e) {
                        // Manejo de otros errores
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada no válida");
                    }
                }
                break;                                        
            case "π":
            case "e":
                    try {
                        // Obtener el valor de π o e
                        double valor = boton.equals("π") ? calculadoraCientifica.obtenerPi() : calculadoraCientifica.obtenerE();
                
                        // Obtener el contenido actual del display
                        String textoActualpieu = display.getText().trim();
                
                        if (textoActualpieu.isEmpty()) {
                            // Si el display está vacío, mostrar π o e
                            display.setText(String.valueOf(valor));
                        } else {
                            // Verificar si el último carácter es un operador
                            char ultimoCaracter = textoActualpieu.charAt(textoActualpieu.length() - 1);
                            if (esOperador(ultimoCaracter)) {
                                display.setText(textoActualpieu + valor);
                            } else if (textoActualpieu.matches(".*[+\\-×÷]\\(1/$")) {
                                display.setText(textoActualpieu + valor);
                            } else {
                                display.setText(String.valueOf(valor)); // Reemplazar el contenido con π o e
                            }
                        }
                
                        // Agregar al historial
                        agregarAlHistorial(boton + " = " + valor);
                    } catch (RemoteException ex) {
                        display.setText("Error de conexión");
                        agregarAlHistorial("Error: No se pudo obtener el valor de " + boton);
                    }
                    break;                                                   
            case "1/x":
                try {
                    // Obtener el texto actual del display
                    String textoActual1x = display.getText().trim();
            
                    // Verificar si el texto está vacío
                    if (textoActual1x.isEmpty()) {
                        return; // Salir si el display está vacío
                    }
            
                    // Verificar si el último carácter es un operador
                    char ultimoCaracter = textoActual1x.charAt(textoActual1x.length() - 1);
                    if (esOperador(ultimoCaracter)) {
                        // Si es un operador, agrega "(1/" al display
                        display.setText(textoActual1x + "(1/");
                    } else {
                        // Si no es un operador, encapsula el número actual en "1/()"
                        display.setText("1/(" + textoActual1x + ")");
                    }
                } catch (Exception e) {
                    // Manejo de errores genéricos
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida");
                }
                break;                
            case "√x":
                // Obtener el texto actual del display
                String textoRaiz = display.getText().trim();
            
                // Verificar si el texto está vacío
                if (textoRaiz.isEmpty()) {
                    return;  // Salir de la función sin hacer nada
                }
            
                try {
                    // Identificar el último operador en el texto
                    int ultimaPosicionOperador = Math.max(
                        textoRaiz.lastIndexOf('+'),
                        Math.max(
                            textoRaiz.lastIndexOf('-'),
                            Math.max(
                                textoRaiz.lastIndexOf('×'),
                                textoRaiz.lastIndexOf('÷')
                            )
                        )
                    );
            
                    String numeroStr;
                    if (ultimaPosicionOperador == -1) {
                        // Si no hay operadores, usar el texto completo
                        numeroStr = textoRaiz;
                    } else {
                        // Extraer el número después del último operador
                        numeroStr = textoRaiz.substring(ultimaPosicionOperador + 1).trim();
                    }
            
                    // Reemplazar constantes como π o e si están presentes
                    numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                         .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
            
                    // Convertir el texto del número a BigDecimal
                    BigDecimal numero = new BigDecimal(numeroStr);
            
                    // Calcular la raíz cuadrada
                    BigDecimal resultado = calculadoraCientifica.raizCuadrada(numero);
            
                    // Formatear el resultado para mostrarlo en el display
                    String nuevoTexto;
                    if (ultimaPosicionOperador == -1) {
                        nuevoTexto = resultado.stripTrailingZeros().toPlainString();
                    } else {
                        // Reemplazar el número después del operador con el resultado
                        nuevoTexto = textoRaiz.substring(0, ultimaPosicionOperador + 1) + resultado.stripTrailingZeros().toPlainString();
                    }
            
                    // Mostrar el resultado en el display
                    display.setText(nuevoTexto);
            
                    // Agregar al historial
                    agregarAlHistorial("√" + numeroStr + " = " + resultado);
            
                } catch (RemoteException e) {
                    // Si hay error de conexión con el servidor
                    display.setText("Error de conexión");
                    agregarAlHistorial("Error: No se pudo conectar al servidor");
                } catch (ArithmeticException e) {
                    // Si hay un error de cálculo (por ejemplo, raíz de un número negativo)
                    display.setText("Error");
                    agregarAlHistorial("Error: " + e.getMessage());
                } catch (Exception e) {
                    // Manejo de otros errores
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida");
                }
                break; 
            case "x^y":
            case "^":
                String textoActual = display.getText().trim();

                // Verificar si el display está vacío o si el último carácter es un operador
                if (textoActual.isEmpty() || esOperador(textoActual.charAt(textoActual.length() - 1))) {
                    // Si está vacío o el último carácter es un operador, solo agregar el operador "^"
                    display.setText(textoActual + "^");
                } else {
                    // Si el último carácter no es un operador, agregar el operador "^"
                    display.setText(textoActual + "^");
                }
                break;  
            case "lg":
                String textoActualLg = display.getText().trim();
            
                try {
                    if (textoActualLg.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para logaritmo.");
                    } else {
                        // Detectar y manejar si hay operadores antes del número
                        char ultimoCaracter = textoActualLg.charAt(textoActualLg.length() - 1);
                        if (esOperador(ultimoCaracter)) {
                            textoActualLg = textoActualLg.substring(0, textoActualLg.length() - 1).trim();
                        }
            
                        // Buscar la última posición de un operador
                        int ultimaPosicionOperador = Math.max(
                            textoActualLg.lastIndexOf('+'),
                            Math.max(
                                textoActualLg.lastIndexOf('-'),
                                Math.max(
                                    textoActualLg.lastIndexOf('×'),
                                    Math.max(
                                        textoActualLg.lastIndexOf('÷'),
                                        textoActualLg.lastIndexOf('/')
                                    )
                                )
                            )
                        );
            
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualLg;
                        } else {
                            numeroStr = textoActualLg.substring(ultimaPosicionOperador + 1).trim();
                        }
            
                        // Reemplazar constantes como π y e
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
            
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para logaritmo.");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
                            BigDecimal resultado = calculadoraCientifica.logaritmoBase10(numero);
            
                            String nuevoTexto;
                            if (ultimaPosicionOperador == -1) {
                                nuevoTexto = resultado.toString();
                            } else {
                                nuevoTexto = textoActualLg.substring(0, ultimaPosicionOperador + 1) + resultado;
                            }
            
                            display.setText(nuevoTexto);
                            agregarAlHistorial("lg(" + numero + ") = " + resultado);
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: " + ex.getMessage());
                }
                break;            
            case "ln":
                String textoActualLn = display.getText().trim();
            
                try {
                    if (textoActualLn.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada no válida");
                        break;
                    }
            
                    // Verificar si hay algún operador antes del número
                    char ultimoCaracter = textoActualLn.charAt(textoActualLn.length() - 1);
            
                    if (esOperador(ultimoCaracter)) {
                        textoActualLn = textoActualLn.substring(0, textoActualLn.length() - 1).trim();
                    }
            
                    // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                    int ultimaPosicionOperador = Math.max(
                        textoActualLn.lastIndexOf('+'),
                        Math.max(
                            textoActualLn.lastIndexOf('-'),
                            Math.max(
                                textoActualLn.lastIndexOf('×'),
                                Math.max(
                                    textoActualLn.lastIndexOf('÷'),
                                    textoActualLn.lastIndexOf('/')
                                )
                            )
                        )
                    );
            
                    String numeroStr;
                    if (ultimaPosicionOperador == -1) {
                        numeroStr = textoActualLn;
                    } else {
                        numeroStr = textoActualLn.substring(ultimaPosicionOperador + 1).trim();
                    }
            
                    numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                         .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
            
                    if (numeroStr.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada no válida");
                    } else {
                        BigDecimal numero = new BigDecimal(numeroStr);
            
                        // Verificar si el número es válido (mayor que 0)
                        if (numero.compareTo(BigDecimal.ZERO) <= 0) {
                            display.setText("Error");
                            agregarAlHistorial("Error: El logaritmo natural no está definido para números menores o iguales a 0.");
                        } else {
                            // Calcular el logaritmo natural
                            BigDecimal resultado = calculadoraCientifica.logaritmoNatural(numero);
                            String nuevoTexto;
                            if (ultimaPosicionOperador == -1) {
                                nuevoTexto = resultado.toString();
                            } else {
                                nuevoTexto = textoActualLn.substring(0, ultimaPosicionOperador + 1) + resultado;
                            }
            
                            display.setText(nuevoTexto);
                            agregarAlHistorial("ln(" + numero + ") = " + resultado);
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida");
                }
                break;                                                                     
            // Evento para el botón "sin"
            case "sin":
                // Obtener el texto actual del display
                String textoActualSeno = display.getText().trim();

                try {
                    if (textoActualSeno.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para sin");
                        return;
                    }

                    // Verificar si el último carácter es un operador
                    char ultimoCaracter = textoActualSeno.charAt(textoActualSeno.length() - 1);
                    if (esOperador(ultimoCaracter)) {
                        // Eliminar el operador al final si existe
                        textoActualSeno = textoActualSeno.substring(0, textoActualSeno.length() - 1).trim();
                    }

                    // Reemplazar constantes matemáticas (π y e) con sus valores
                    textoActualSeno = textoActualSeno.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                                    .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));

                    // Buscar el último operador para obtener el número correcto
                    int ultimaPosicionOperador = Math.max(
                        textoActualSeno.lastIndexOf('+'),
                        Math.max(
                            textoActualSeno.lastIndexOf('-'),
                            Math.max(
                                textoActualSeno.lastIndexOf('×'),
                                Math.max(
                                    textoActualSeno.lastIndexOf('÷'),
                                    textoActualSeno.lastIndexOf('/')
                                )
                            )
                        )
                    );

                    String numeroStr;
                    if (ultimaPosicionOperador == -1) {
                        // No hay operador, tomar todo el texto como número
                        numeroStr = textoActualSeno;
                    } else {
                        // Obtener el número después del último operador
                        numeroStr = textoActualSeno.substring(ultimaPosicionOperador + 1).trim();
                    }

                    if (numeroStr.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Número vacío después del operador para sin");
                        return;
                    }

                    // Convertir el texto del número a BigDecimal
                    BigDecimal numero = new BigDecimal(numeroStr);
                    double valor = numero.doubleValue();

                    // Calcular el seno
                    double resultado = calculadoraCientifica.calcularSeno(valor, isDegMode);

                    // Mostrar el resultado en el display
                    display.setText(String.valueOf(resultado));

                    // Agregar al historial con el modo adecuado
                    if (isDegMode) {
                        agregarAlHistorial("sin(" + valor + "°) = " + resultado);
                    } else {
                        agregarAlHistorial("sin(" + valor + " rad) = " + resultado);
                    }
                } catch (RemoteException e) {
                    display.setText("Error de conexión");
                    agregarAlHistorial("Error: No se pudo conectar al servidor para calcular sin");
                } catch (NumberFormatException e) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para sin");
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para sin");
                }
                break;
            case "cos":
                String textoActualCoseno = display.getText().trim();
                
                try {
                    if (textoActualCoseno.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para cos");
                    } else {
                        char ultimoCaracter = textoActualCoseno.charAt(textoActualCoseno.length() - 1);
                
                        if (esOperador(ultimoCaracter)) {
                            textoActualCoseno = textoActualCoseno.substring(0, textoActualCoseno.length() - 1).trim();
                        }
                
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualCoseno.lastIndexOf('+'),
                            Math.max(
                                textoActualCoseno.lastIndexOf('-'),
                                Math.max(
                                    textoActualCoseno.lastIndexOf('×'),
                                    Math.max(
                                        textoActualCoseno.lastIndexOf('÷'),
                                        textoActualCoseno.lastIndexOf('/')
                                    )
                                )
                            )
                        );
                
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualCoseno;
                        } else {
                            numeroStr = textoActualCoseno.substring(ultimaPosicionOperador + 1).trim();
                        }
                
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para cos");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
                
                            if (numero.compareTo(BigDecimal.ZERO) < 0) {
                                display.setText("Error");
                                agregarAlHistorial("Error: Entrada negativa no válida para cos");
                            } else {
                                double valor = numero.doubleValue();
                                double resultado;
            
                                // Llamar al método calcularCoseno de CalculadoraImpl
                                resultado = calculadoraCientifica.calcularCoseno(valor, isDegMode);
                
                                // Agregar al historial con el modo adecuado (grados o radianes)
                                if (isDegMode) {
                                    agregarAlHistorial("cos(" + valor + "°) = " + resultado);
                                } else {
                                    agregarAlHistorial("cos(" + valor + " rad) = " + resultado);
                                }
                
                                // Mostrar el resultado en el display
                                display.setText(String.valueOf(resultado));
                            }
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para cos");
                }
                break;
            case "tan":
                String textoActualTangente = display.getText().trim();
                
                try {
                    if (textoActualTangente.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para tan");
                    } else {
                        char ultimoCaracter = textoActualTangente.charAt(textoActualTangente.length() - 1);
                
                        if (esOperador(ultimoCaracter)) {
                            textoActualTangente = textoActualTangente.substring(0, textoActualTangente.length() - 1).trim();
                        }
                
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualTangente.lastIndexOf('+'),
                            Math.max(
                                textoActualTangente.lastIndexOf('-'),
                                Math.max(
                                    textoActualTangente.lastIndexOf('×'),
                                    Math.max(
                                        textoActualTangente.lastIndexOf('÷'),
                                        textoActualTangente.lastIndexOf('/')
                                    )
                                )
                            )
                        );
                
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualTangente;
                        } else {
                            numeroStr = textoActualTangente.substring(ultimaPosicionOperador + 1).trim();
                        }
                
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para tan");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
                
                            if (numero.compareTo(BigDecimal.ZERO) < 0) {
                                display.setText("Error");
                                agregarAlHistorial("Error: Entrada negativa no válida para tan");
                            } else {
                                double valor = numero.doubleValue();
                                double resultado;
            
                                // Llamar al método calcularTangente de CalculadoraImpl
                                resultado = calculadoraCientifica.calcularTangente(valor, isDegMode);
                
                                // Agregar al historial con el modo adecuado (grados o radianes)
                                if (isDegMode) {
                                    agregarAlHistorial("tan(" + valor + "°) = " + resultado);
                                } else {
                                    agregarAlHistorial("tan(" + valor + " rad) = " + resultado);
                                }
                
                                // Mostrar el resultado en el display
                                display.setText(String.valueOf(resultado));
                            }
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para tan");
                }
                break;  
            case "sin^-1":
                String textoActualArcoseno = display.getText().trim();
                
                try {
                    if (textoActualArcoseno.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para sin^-1");
                    } else {
                        char ultimoCaracter = textoActualArcoseno.charAt(textoActualArcoseno.length() - 1);
                
                        if (esOperador(ultimoCaracter)) {
                            textoActualArcoseno = textoActualArcoseno.substring(0, textoActualArcoseno.length() - 1).trim();
                        }
                
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualArcoseno.lastIndexOf('+'),
                            Math.max(
                                textoActualArcoseno.lastIndexOf('-'),
                                Math.max(
                                    textoActualArcoseno.lastIndexOf('×'),
                                    Math.max(
                                        textoActualArcoseno.lastIndexOf('÷'),
                                        textoActualArcoseno.lastIndexOf('/')
                                    )
                                )
                            )
                        );
                
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualArcoseno;
                        } else {
                            numeroStr = textoActualArcoseno.substring(ultimaPosicionOperador + 1).trim();
                        }
                
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para sin^-1");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
                
                            // Verificar si el número está en el rango válido para el arcoseno (-1 <= x <= 1)
                            if (numero.compareTo(BigDecimal.valueOf(-1)) < 0 || numero.compareTo(BigDecimal.valueOf(1)) > 0) {
                                display.setText("Error");
                                agregarAlHistorial("Error: Valor fuera de rango para sin^-1");
                            } else {
                                double valor = numero.doubleValue();
                                double resultado;
            
                                // Llamar al método calcularArcoseno de CalculadoraImpl
                                resultado = calculadoraCientifica.calcularArcoseno(valor, isDegMode);
                
                                // Agregar al historial con el modo adecuado (grados o radianes)
                                if (isDegMode) {
                                    agregarAlHistorial("sin^-1(" + valor + ") = " + resultado + "°");
                                } else {
                                    agregarAlHistorial("sin^-1(" + valor + ") = " + resultado + " rad");
                                }
                
                                // Mostrar el resultado en el display
                                display.setText(String.valueOf(resultado));
                            }
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para sin^-1");
                }
                break;   
            case "cos^-1":
                String textoActualArccoseno = display.getText().trim();
                
                try {
                    if (textoActualArccoseno.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para cos^-1");
                    } else {
                        char ultimoCaracter = textoActualArccoseno.charAt(textoActualArccoseno.length() - 1);
                
                        if (esOperador(ultimoCaracter)) {
                            textoActualArccoseno = textoActualArccoseno.substring(0, textoActualArccoseno.length() - 1).trim();
                        }
                
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualArccoseno.lastIndexOf('+'),
                            Math.max(
                                textoActualArccoseno.lastIndexOf('-'),
                                Math.max(
                                    textoActualArccoseno.lastIndexOf('×'),
                                    Math.max(
                                        textoActualArccoseno.lastIndexOf('÷'),
                                        textoActualArccoseno.lastIndexOf('/')
                                    )
                                )
                            )
                        );
                
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualArccoseno;
                        } else {
                            numeroStr = textoActualArccoseno.substring(ultimaPosicionOperador + 1).trim();
                        }
                
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para cos^-1");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
                
                            // Verificar si el número está en el rango válido para el arccoseno (-1 <= x <= 1)
                            if (numero.compareTo(BigDecimal.valueOf(-1)) < 0 || numero.compareTo(BigDecimal.valueOf(1)) > 0) {
                                display.setText("Error");
                                agregarAlHistorial("Error: Valor fuera de rango para cos^-1");
                            } else {
                                double valor = numero.doubleValue();
                                double resultado;
            
                                // Llamar al método calcularArccoseno de CalculadoraImpl
                                resultado = calculadoraCientifica.calcularArccoseno(valor, isDegMode);
                
                                // Agregar al historial con el modo adecuado (grados o radianes)
                                if (isDegMode) {
                                    agregarAlHistorial("cos^-1(" + valor + ") = " + resultado + "°");
                                } else {
                                    agregarAlHistorial("cos^-1(" + valor + ") = " + resultado + " rad");
                                }
                
                                // Mostrar el resultado en el display
                                display.setText(String.valueOf(resultado));
                            }
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para cos^-1");
                }
                break;      
            case "tan^-1":
                String textoActualArctangente = display.getText().trim();
                
                try {
                    if (textoActualArctangente.isEmpty()) {
                        display.setText("Error");
                        agregarAlHistorial("Error: Entrada vacía para tan^-1");
                    } else {
                        char ultimoCaracter = textoActualArctangente.charAt(textoActualArctangente.length() - 1);
                
                        if (esOperador(ultimoCaracter)) {
                            textoActualArctangente = textoActualArctangente.substring(0, textoActualArctangente.length() - 1).trim();
                        }
                
                        // Buscar la última posición de cualquier operador (+, -, ×, ÷, /)
                        int ultimaPosicionOperador = Math.max(
                            textoActualArctangente.lastIndexOf('+'),
                            Math.max(
                                textoActualArctangente.lastIndexOf('-'),
                                Math.max(
                                    textoActualArctangente.lastIndexOf('×'),
                                    Math.max(
                                        textoActualArctangente.lastIndexOf('÷'),
                                        textoActualArctangente.lastIndexOf('/')
                                    )
                                )
                            )
                        );
                
                        String numeroStr;
                        if (ultimaPosicionOperador == -1) {
                            numeroStr = textoActualArctangente;
                        } else {
                            numeroStr = textoActualArctangente.substring(ultimaPosicionOperador + 1).trim();
                        }
                
                        numeroStr = numeroStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                             .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                
                        if (numeroStr.isEmpty()) {
                            display.setText("Error");
                            agregarAlHistorial("Error: Entrada vacía para tan^-1");
                        } else {
                            BigDecimal numero = new BigDecimal(numeroStr);
            
                            double valor = numero.doubleValue();
                            double resultado;
            
                            // Llamar al método calcularArctangente de CalculadoraImpl
                            resultado = calculadoraCientifica.calcularArctangente(valor, isDegMode);
                
                            // Agregar al historial con el modo adecuado (grados o radianes)
                            if (isDegMode) {
                                agregarAlHistorial("tan^-1(" + valor + ") = " + resultado + "°");
                            } else {
                                agregarAlHistorial("tan^-1(" + valor + ") = " + resultado + " rad");
                            }
                
                            // Mostrar el resultado en el display
                            display.setText(String.valueOf(resultado));
                        }
                    }
                } catch (Exception ex) {
                    display.setText("Error");
                    agregarAlHistorial("Error: Entrada no válida para tan^-1");
                }
                break;                                                 
            case "↔": // Este es el nuevo caso para el botón ↔
                modoSimple = !modoSimple;
                CardLayout cl = (CardLayout) mainPanel.getLayout(); // Cambiado a mainPanel
                cl.show(mainPanel, modoSimple ? "simple" : "scientific"); // Cambiar panel
                break;
            default:
                display.setText(display.getText() + (boton.equals("^") ? "" : boton)); // Si el botón no es "^", agregarlo
        }
    
        // Cambiar el texto del botón "AC" a "C" si hay texto en el display
        JButton btnACSimple = (JButton) ((JPanel) mainPanel.getComponent(0)).getComponent(0); // Obtener el botón "AC" del panel simple
        JButton btnACScientific = (JButton) ((JPanel) mainPanel.getComponent(1)).getComponent(16); // Obtener el botón "AC" del panel científico en la posición correcta
        if (display.getText().isEmpty()) {
            btnACSimple.setText("AC");
            btnACScientific.setText("AC");
        } else {
            btnACSimple.setText("C");
            btnACScientific.setText("C");
        }
    }
    
    // Método auxiliar para verificar si un carácter es un operador
    private boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }
    
 
    private void actualizarModo() {
        if (isDegMode) {
            // Habilitar botones "sin^-1", "cos^-1", "tan^-1"
            botonesMap.get("sin^-1").setEnabled(true);
            botonesMap.get("cos^-1").setEnabled(true);
            botonesMap.get("tan^-1").setEnabled(true);
        } else {
            // Deshabilitar botones "sin^-1", "cos^-1", "tan^-1"
            botonesMap.get("sin^-1").setEnabled(false);
            botonesMap.get("cos^-1").setEnabled(false);
            botonesMap.get("tan^-1").setEnabled(false);
        }
    }
    
    
    
    
    // Método para calcular el factorial exacto de números enteros
    private BigDecimal calcularFactorialEntero(int n) {
        BigDecimal factorial = BigDecimal.ONE;
        for (int i = 2; i <= n; i++) {
            factorial = factorial.multiply(BigDecimal.valueOf(i));
        }
        return factorial;
    }

    // Método auxiliar para aproximar el factorial de números decimales
    private double aproximarFactorial(double x) {
        return Math.sqrt(2 * Math.PI * x) * Math.pow(x / Math.E, x);
    }
    

 




    

    private boolean servidorActivoNormal() {
        try {
            calculadoraNormal.ping(); // Llamada al método ping() en la calculadora normal
            return true; // Si no lanza una excepción, el servidor está activo
        } catch (RemoteException e) {
            return false; // Si ocurre una excepción, el servidor no está activo
        }
    }
    
    private boolean servidorActivoCientifica() {
        try {
            calculadoraCientifica.ping(); // Llamada al método ping() en la calculadora científica
            return true; // Si no lanza una excepción, el servidor está activo
        } catch (RemoteException e) {
            return false; // Si ocurre una excepción, el servidor no está activo
        }
    }
    
    

    private void calcular() {
        try {
            // Obtener la expresión original del display
            String expresionOriginal = display.getText().trim();
            if (expresionOriginal.isEmpty()) {
                // Si el display está vacío, no hacer nada
                return;
            }
    
            // Verificar si el servidor correcto está activo
            if (expresionOriginal.contains("^")) {
                // Si es una operación científica (potencia), verificar el servidor científico
                if (!servidorActivoCientifica()) {
                    throw new IllegalStateException("El servidor de la calculadora científica está desconectado.");
                }
            } else {
                // Si es una operación normal, verificar el servidor normal
                if (!servidorActivoNormal()) {
                    throw new IllegalStateException("El servidor de la calculadora normal está desconectado.");
                }
            }
    
            // Validar división por cero
            if (expresionOriginal.contains("÷0") || expresionOriginal.contains("/0")) {
                display.setText("Error");
                agregarAlHistorial("Error: No se puede dividir entre 0");
                return;
            }
    
            // Verificar si es una operación de potencia
            if (expresionOriginal.contains("^")) {
                // Detectar si el primer número es negativo y manejar operadores antes de la base
                char ultimoCaracter = expresionOriginal.charAt(expresionOriginal.length() - 1);
    
                if (esOperador(ultimoCaracter)) {
                    expresionOriginal = expresionOriginal.substring(0, expresionOriginal.length() - 1).trim();
                }
    
                // Buscar la última posición de cualquier operador antes del símbolo de potencia
                int ultimaPosicionOperador = Math.max(
                    expresionOriginal.lastIndexOf('+'),
                    Math.max(
                        expresionOriginal.lastIndexOf('-'),
                        Math.max(
                            expresionOriginal.lastIndexOf('×'),
                            Math.max(
                                expresionOriginal.lastIndexOf('÷'),
                                expresionOriginal.lastIndexOf('/')
                            )
                        )
                    )
                );
    
                String baseStr, exponenteStr;
                if (ultimaPosicionOperador == -1) {
                    // No hay operador previo, la expresión está completa
                    String[] partes = expresionOriginal.split("\\^");
                    baseStr = partes[0].trim();
                    exponenteStr = partes[1].trim();
                } else {
                    // Dividir tomando en cuenta el operador antes de la base
                    String operacionConPotencia = expresionOriginal.substring(ultimaPosicionOperador + 1).trim();
                    String[] partes = operacionConPotencia.split("\\^");
                    baseStr = partes[0].trim();
                    exponenteStr = partes[1].trim();
                }
    
                // Manejar constantes como π y e
                baseStr = baseStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                 .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
                exponenteStr = exponenteStr.replace("π", String.valueOf(calculadoraCientifica.obtenerPi()))
                                           .replace("e", String.valueOf(calculadoraCientifica.obtenerE()));
    
                BigDecimal base = new BigDecimal(baseStr);
                BigDecimal exponente = new BigDecimal(exponenteStr);
    
                // Llamar al método de potencia del servidor
                BigDecimal resultado = calculadoraCientifica.potencia(base, exponente);
    
                // Reconstruir la expresión con el resultado
                String nuevoTexto;
                if (ultimaPosicionOperador == -1) {
                    nuevoTexto = resultado.toString();
                } else {
                    nuevoTexto = expresionOriginal.substring(0, ultimaPosicionOperador + 1) + resultado;
                }
    
                // Mostrar el resultado en el display
                display.setText(nuevoTexto);
    
                // Agregar al historial
                agregarAlHistorial(expresionOriginal + " = " + resultado);
                return;
            }
    
            // Si no es una potencia, evaluar normalmente usando ScriptEngine
            String expresionEvaluable = expresionOriginal
                .replace("×", "*")
                .replace("÷", "/")
                .replace("^", "**");
    
            // Crear un ScriptEngine para evaluar la expresión
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    
            // Evaluar la expresión
            Object resultadoObj = engine.eval(expresionEvaluable);
    
            // Convertir el resultado a un número y mostrarlo en el display
            double resultado = Double.parseDouble(resultadoObj.toString());
            display.setText(String.valueOf(resultado));
    
            // Agregar al historial
            agregarAlHistorial(expresionOriginal + " = " + resultado);
    
        } catch (Exception e) {
            // Manejar errores y agregar al historial
            display.setText("Error");
            agregarAlHistorial("Error: " + e.getMessage());
        }
    }
    
    
    
    

    private void agregarAlHistorial(String operacion) {
        historial.add(operacion);
        actualizarHistorialArea();
    }

    private void actualizarHistorialArea() {
        historialArea.setText("");
        for (int i = Math.max(0, historial.size() - 5); i < historial.size(); i++) {
            historialArea.append(historial.get(i) + "\n");
        }
    }

    private void mostrarHistorial() {
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(String.join("\n", historial))), "Historial", JOptionPane.INFORMATION_MESSAGE);
    }

    private void conectarServidorCalculadoraNormal() {
        String ipNormal = "127.0.0.1"; // IP del servidor de CalculadoraNormal
        boolean conexionExitosa = false;
        int intentos = 0;
        final int maxIntentos = 5;
    

        while (!conexionExitosa && intentos < maxIntentos) {
            try {
                // Conexión al servidor de CalculadoraNormal en el puerto 1099
                String serverAddressNormal = "rmi://" + ipNormal + ":1099/CalculadoraNormal";
                calculadoraNormal = (CalculadoraNormal) Naming.lookup(serverAddressNormal);
                JOptionPane.showMessageDialog(this, "Conexión exitosa a CalculadoraNormal");
                conexionExitosa = true; // Marcar la conexión como exitosa
            } catch (Exception e) {
                intentos++;
                String errorMsg = "Error al intentar conectar con CalculadoraNormal. Intento #" + intentos + ": " + e.getMessage();
                JOptionPane.showMessageDialog(this, errorMsg);
    
                // Esperar antes de reintentar
                if (intentos < maxIntentos) {
                    try {
                        Thread.sleep(3000); // Esperar 3 segundos antes de intentar nuevamente
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo conectar con CalculadoraNormal después de " + maxIntentos + " intentos.");
                }
            }
        }
    }
    
    private void conectarServidorCalculadoraCientifica() {
        String ipCientifica = "127.0.0.1"; // IP del servidor de CalculadoraCientifica
        boolean conexionExitosa = false;
        int intentos = 0;
        final int maxIntentos = 5;
    
        while (!conexionExitosa && intentos < maxIntentos) {
            try {
                // Conexión al servidor de CalculadoraCientifica en el puerto 1100
                String serverAddressCientifica = "rmi://" + ipCientifica + ":1100/CalculadoraCientifica";
                calculadoraCientifica = (CalculadoraCientifica) Naming.lookup(serverAddressCientifica);
                JOptionPane.showMessageDialog(this, "Conexión exitosa a CalculadoraCientifica");
                conexionExitosa = true; // Marcar la conexión como exitosa
            } catch (Exception e) {
                intentos++;
                String errorMsg = "Error al intentar conectar con CalculadoraCientifica. Intento #" + intentos + ": " + e.getMessage();
                JOptionPane.showMessageDialog(this, errorMsg);
    
                // Esperar antes de reintentar
                if (intentos < maxIntentos) {
                    try {
                        Thread.sleep(3000); // Esperar 3 segundos antes de intentar nuevamente
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo conectar con CalculadoraCientifica después de " + maxIntentos + " intentos.");
                }
            }
        }
    }
    
    
    
    

    private boolean reconectarServidorCalculadoraNormal() {
        String ipNormal = "127.0.0.1"; // IP del servidor de CalculadoraNormal
        try {
            // Intentar reconectar al servidor de CalculadoraNormal (Puerto 1099)
            String serverAddressNormal = "rmi://" + ipNormal + ":1099/CalculadoraNormal";
            calculadoraNormal = (CalculadoraNormal) Naming.lookup(serverAddressNormal);
            JOptionPane.showMessageDialog(this, "Reconectado al servidor de CalculadoraNormal en " + ipNormal);
            return true; // Reconexión exitosa
        } catch (Exception e) {
            System.out.println("Error al reconectar con CalculadoraNormal: " + e.getMessage());
            return false; // Fallo en la reconexión
        }
    }
    
    private boolean reconectarServidorCalculadoraCientifica() {
        String ipCientifica = "127.0.0.1"; // IP del servidor de CalculadoraCientifica
        try {
            // Intentar reconectar al servidor de CalculadoraCientifica (Puerto 1100)
            String serverAddressCientifica = "rmi://" + ipCientifica + ":1100/CalculadoraCientifica";
            calculadoraCientifica = (CalculadoraCientifica) Naming.lookup(serverAddressCientifica);
            JOptionPane.showMessageDialog(this, "Reconectado al servidor de CalculadoraCientifica en " + ipCientifica);
            return true; // Reconexión exitosa
        } catch (Exception e) {
            System.out.println("Error al reconectar con CalculadoraCientifica: " + e.getMessage());
            return false; // Fallo en la reconexión
        }
    }
    
    
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteCalculadora cliente = new ClienteCalculadora();
            cliente.setVisible(true);
    
            // Crear un hilo para monitorear y reconectar servidores
            new Thread(() -> {
                while (true) {
                    // Comprobar si los servidores no están activos
                    if (!cliente.servidorActivoNormal()) {
                        System.out.println("El servidor CalculadoraNormal no está activo. Intentando reconectar...");
                        if (cliente.reconectarServidorCalculadoraNormal()) {
                            System.out.println("Reconexión exitosa a CalculadoraNormal.");
                        } else {
                            System.out.println("Falló la reconexión a CalculadoraNormal.");
                        }
                    }
    
                    if (!cliente.servidorActivoCientifica()) {
                        System.out.println("El servidor CalculadoraCientifica no está activo. Intentando reconectar...");
                        if (cliente.reconectarServidorCalculadoraCientifica()) {
                            System.out.println("Reconexión exitosa a CalculadoraCientifica.");
                        } else {
                            System.out.println("Falló la reconexión a CalculadoraCientifica.");
                        }
                    }
    
                    try {
                        // Esperar 5 segundos antes de volver a comprobar
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("Hilo interrumpido: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restaurar el estado interrumpido
                        break; // Salir del bucle si se interrumpe el hilo
                    }
                }
            }).start();
        });
    }
    
    
}
