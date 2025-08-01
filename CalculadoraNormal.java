import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculadoraNormal extends Remote {
    // Métodos de operaciones básicas
    BigDecimal suma(BigDecimal a, BigDecimal b) throws RemoteException;
    BigDecimal resta(BigDecimal a, BigDecimal b) throws RemoteException;
    BigDecimal multiplicacion(BigDecimal a, BigDecimal b) throws RemoteException;
    BigDecimal division(BigDecimal a, BigDecimal b) throws RemoteException;
    BigDecimal porcentaje(BigDecimal parte, BigDecimal total) throws RemoteException;
    /*BigDecimal potencia(BigDecimal base, BigDecimal exponente) throws RemoteException;
    BigDecimal raizCuadrada(BigDecimal a) throws RemoteException;
    BigDecimal logaritmoNatural(BigDecimal numero) throws RemoteException;
    
    // Método para calcular el logaritmo en base 10
    BigDecimal logaritmoBase10(BigDecimal numero) throws RemoteException;
    BigDecimal factorial(BigDecimal numero) throws RemoteException;
    BigDecimal valorAbsoluto(BigDecimal numero) throws RemoteException;
    BigDecimal exponencial(BigDecimal exponente) throws RemoteException;
    BigDecimal modulo(BigDecimal a, BigDecimal b) throws RemoteException;
    double gradosARadianes(double grados) throws RemoteException;
    double radianesAGrados(double radianes) throws RemoteException;
    double senoHiperbolico(double angulo) throws RemoteException;
    double cosenoHiperbolico(double angulo) throws RemoteException;
    double tangenteHiperbolico(double angulo) throws RemoteException;
    String aBinario(int numero) throws RemoteException;
    String aOctal(int numero) throws RemoteException;
    String aHexadecimal(int numero) throws RemoteException;
    BigDecimal inverso(BigDecimal numero) throws RemoteException;  // Método para calcular 1/x

    // Métodos adicionales para funciones trigonométricas
    double seno(double angulo) throws RemoteException;
    double coseno(double angulo) throws RemoteException;
    double tangente(double angulo) throws RemoteException;*/

    // Método ping para verificar conexión
    void ping() throws RemoteException;

    // Métodos para manejo de errores
    boolean hasError() throws RemoteException;
    void limpiarEstado() throws RemoteException;
    BigDecimal getResultado() throws RemoteException;

    // Métodos para constantes matemáticas
   /* double obtenerPi() throws RemoteException;
    double obtenerE() throws RemoteException;
    double calcularSeno(double valor, boolean isDegMode)throws RemoteException;
    // Método para calcular el coseno
    double calcularCoseno(double valor, boolean isDegMode)throws RemoteException;
    // Método para calcular la tangente
    double calcularTangente(double valor, boolean isDegMode)throws RemoteException;
    // Método para calcular el arcoseno (sin^-1)
    double calcularArcoseno(double valor, boolean isDegMode)throws RemoteException;
    // Método para calcular el arccoseno (cos^-1)
    double calcularArccoseno(double valor, boolean isDegMode)throws RemoteException;
    // Método para calcular el arctangente (tan^-1)
    double calcularArctangente(double valor, boolean isDegMode)throws RemoteException;*/
}
