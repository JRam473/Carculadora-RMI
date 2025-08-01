import java.math.BigDecimal;
import java.math.MathContext;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class CalculadoraNormalImpl extends UnicastRemoteObject implements CalculadoraNormal {
    private boolean hasError; // Estado de error
    private BigDecimal resultado; // Almacena el resultado de las operaciones

    // Constructor
    public CalculadoraNormalImpl() throws RemoteException {
        super();
        this.hasError = false;
        this.resultado = BigDecimal.ZERO; // Inicializa el resultado
    }

    // Suma
    @Override
    public BigDecimal suma(BigDecimal a, BigDecimal b) throws RemoteException {
        resultado = a.add(b); // Guarda el resultado
        return resultado;
    }

    // Resta
    @Override
    public BigDecimal resta(BigDecimal a, BigDecimal b) throws RemoteException {
        resultado = a.subtract(b); // Guarda el resultado
        return resultado;
    }

    // Multiplicación
    @Override
    public BigDecimal multiplicacion(BigDecimal a, BigDecimal b) throws RemoteException {
        resultado = a.multiply(b); // Guarda el resultado
        return resultado;
    }

    // División
    @Override
    public BigDecimal division(BigDecimal a, BigDecimal b) throws RemoteException {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            manejarError(); // Marca un error
            throw new ArithmeticException("División por cero");
        }
        resultado = a.divide(b, MathContext.DECIMAL128); // Guarda el resultado
        return resultado;
    }

   // Método para calcular el porcentaje
   @Override
   public BigDecimal porcentaje(BigDecimal parte, BigDecimal total) throws RemoteException {
       if (total.compareTo(BigDecimal.ZERO) == 0) {
           throw new ArithmeticException("No se puede calcular porcentaje con un total de cero.");
       }
       return parte.multiply(BigDecimal.ONE).divide(total, 2, BigDecimal.ROUND_HALF_UP);
   }
   



/*

    @Override
    public BigDecimal factorial(BigDecimal numero) throws IllegalArgumentException {
        if (numero.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El factorial no está definido para números negativos.");
        }
        if (numero.equals(BigDecimal.ZERO)) {
            return BigDecimal.ONE;
        }
        return numero.multiply(factorial(numero.subtract(BigDecimal.ONE)));
    }



    @Override
    public double obtenerPi() throws RemoteException {
        return Math.PI;
    }

    @Override
    public double obtenerE() throws RemoteException {
        return Math.E;
    }

    @Override
    public BigDecimal inverso(BigDecimal numero) throws RemoteException {
        if (numero.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("No se puede dividir entre cero");
        }
        return BigDecimal.ONE.divide(numero, 10, RoundingMode.HALF_UP);  // Retorna el recíproco de 'numero'
    }
    


    // Potencia
    @Override
    public BigDecimal potencia(BigDecimal base, BigDecimal exponente) {
        // Calcular la potencia utilizando Math.pow para obtener un valor de tipo double
        double resultado = Math.pow(base.doubleValue(), exponente.doubleValue());
        return BigDecimal.valueOf(resultado).setScale(8, RoundingMode.HALF_UP);
    }

    // Raíz cuadrada
    @Override
    public BigDecimal raizCuadrada(BigDecimal a) throws RemoteException {
        if (a.compareTo(BigDecimal.ZERO) < 0) {
            manejarError(); // Marca un error
            throw new ArithmeticException("No se puede calcular la raíz cuadrada de un número negativo");
        }
        resultado = new BigDecimal(Math.sqrt(a.doubleValue())); // Guarda el resultado
        return resultado;
    }

    // Logaritmo natural
    @Override
    public BigDecimal logaritmoNatural(BigDecimal numero) throws IllegalArgumentException {
        if (numero.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El logaritmo natural no está definido para números menores o iguales a 0.");
        }
        // Usar Math.log para calcular el logaritmo natural
        double resultado = Math.log(numero.doubleValue());
        return BigDecimal.valueOf(resultado).setScale(8, RoundingMode.HALF_UP);
    }




    // Logaritmo base 10
    @Override
    public BigDecimal logaritmoBase10(BigDecimal numero) throws IllegalArgumentException {
        if (numero.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El logaritmo base 10 solo está definido para números positivos.");
        }

        // Calcular el logaritmo base 10
        double valor = Math.log10(numero.doubleValue());
        return new BigDecimal(valor, MathContext.DECIMAL64);
    }


   // Implementación para calcular el seno
   @Override
   public double calcularSeno(double valor, boolean isDegMode) throws IllegalArgumentException {
       if (isDegMode) {
           // Si está en modo grados, convertir el valor a radianes
           return Math.sin(Math.toRadians(valor)); // Seno en grados
       } else {
           // Si está en modo radianes
           return Math.sin(valor); // Seno en radianes
       }
   }

   // Implementación para calcular el coseno
   @Override
   public double calcularCoseno(double valor, boolean isDegMode)throws IllegalArgumentException {
       if (isDegMode) {
           // Si está en modo grados, convertir el valor a radianes
           return Math.cos(Math.toRadians(valor)); // Coseno en grados
       } else {
           // Si está en modo radianes
           return Math.cos(valor); // Coseno en radianes
       }
   }

   // Implementación para calcular la tangente
   @Override
   public double calcularTangente(double valor, boolean isDegMode)throws IllegalArgumentException {
       if (isDegMode) {
           // Si está en grados, convertir a radianes
           return Math.tan(Math.toRadians(valor)); // Tangente en grados
       } else {
           // Si está en radianes
           return Math.tan(valor); // Tangente en radianes
       }
   }

   // Implementación para calcular el arcoseno (sin^-1)
   @Override
   public double calcularArcoseno(double valor, boolean isDegMode)throws IllegalArgumentException {
       if (valor < -1 || valor > 1) {
           throw new IllegalArgumentException("El valor para el arcoseno debe estar entre -1 y 1.");
       }

       double resultado = Math.asin(valor); // Calcular arcoseno en radianes

       if (isDegMode) {
           return Math.toDegrees(resultado); // Convertir el resultado a grados si es necesario
       } else {
           return resultado; // Retornar en radianes si no es necesario convertir
       }
   }

   // Implementación para calcular el arccoseno (cos^-1)
   @Override
   public double calcularArccoseno(double valor, boolean isDegMode)throws IllegalArgumentException {
       if (valor < -1 || valor > 1) {
           throw new IllegalArgumentException("El valor para el arccoseno debe estar entre -1 y 1.");
       }

       double resultado = Math.acos(valor); // Calcular arccoseno en radianes

       if (isDegMode) {
           return Math.toDegrees(resultado); // Convertir el resultado a grados si es necesario
       } else {
           return resultado; // Retornar en radianes si no es necesario convertir
       }
   }

   // Implementación para calcular el arctangente (tan^-1)
   @Override
   public double calcularArctangente(double valor, boolean isDegMode)throws IllegalArgumentException {
       double resultado = Math.atan(valor); // Calcular arctangente en radianes

       if (isDegMode) {
           return Math.toDegrees(resultado); // Convertir el resultado a grados si es necesario
       } else {
           return resultado; // Retornar en radianes si no es necesario convertir
       }
   }

    

    // Valor absoluto
    @Override
    public BigDecimal valorAbsoluto(BigDecimal numero) throws RemoteException {
        resultado = numero.abs(); // Guarda el resultado
        return resultado;
    }

    // Exponencial (e^x)
    @Override
    public BigDecimal exponencial(BigDecimal exponente) throws RemoteException {
        resultado = BigDecimal.valueOf(Math.exp(exponente.doubleValue())); // Guarda el resultado
        return resultado;
    }

    // Módulo (resto)
    @Override
    public BigDecimal modulo(BigDecimal a, BigDecimal b) throws RemoteException {
        resultado = a.remainder(b); // Guarda el resultado
        return resultado;
    }

    // Conversión de grados a radianes
    @Override
    public double gradosARadianes(double grados) throws RemoteException {
        return Math.toRadians(grados);
    }

    // Conversión de radianes a grados
    @Override
    public double radianesAGrados(double radianes) throws RemoteException {
        return Math.toDegrees(radianes);
    }

    // Funciones trigonométricas hiperbólicas
    @Override
    public double senoHiperbolico(double angulo) throws RemoteException {
        return Math.sinh(Math.toRadians(angulo));
    }

    @Override
    public double cosenoHiperbolico(double angulo) throws RemoteException {
        return Math.cosh(Math.toRadians(angulo));
    }

    @Override
    public double tangenteHiperbolico(double angulo) throws RemoteException {
        return Math.tanh(Math.toRadians(angulo));
    }

    // Conversión a binario
    @Override
    public String aBinario(int numero) throws RemoteException {
        return Integer.toBinaryString(numero);
    }

    // Conversión a octal
    @Override
    public String aOctal(int numero) throws RemoteException {
        return Integer.toOctalString(numero);
    }

    // Conversión a hexadecimal
    @Override
    public String aHexadecimal(int numero) throws RemoteException {
        return Integer.toHexString(numero);
    }
*/
    // Método ping
    @Override
    public void ping() throws RemoteException {
        // Solo para verificación de conexión
    }
/*
    @Override
    public double seno(double angulo) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'seno'");
    }

    @Override
    public double coseno(double angulo) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'coseno'");
    }

    @Override
    public double tangente(double angulo) throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'tangente'");
    }
*/
    // Método para manejar errores
    public void manejarError() {
        this.hasError = true; // Cambia el estado de error
    }

    // Método para verificar el estado de error
    public boolean hasError() {
        return hasError;
    }

    // Método para limpiar el estado
    public void limpiarEstado() {
        hasError = false; // Resetea el estado de error
        resultado = BigDecimal.ZERO; // Resetea el resultado
    }

    // Método para obtener el resultado actual
    public BigDecimal getResultado() {
        return resultado;
    }
}
