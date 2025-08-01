import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;

public class ServidorCalculadora extends JFrame {

    private JButton botonIniciarNormal;
    
    private JButton botonDetenerNormal;
    
    private JTextArea logArea;
    private Registry registryNormal;
    

    // Declaración de las variables para las dos calculadoras
    private CalculadoraNormal calculadoraNormal;
   // private CalculadoraCientifica calculadoraCientifica;

    // Puertos diferentes para los servidores de las calculadoras
    private static final int PUERTO_NORMAL = 1099; // Puerto para la calculadora normal
    

    public ServidorCalculadora() {
        // Configuración de la ventana
        setTitle("Servidor de Calculadora Normal");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Disposición vertical

        // Panel para los botones de la calculadora normal
        JPanel normalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonIniciarNormal = new JButton("Iniciar Calculadora Normal");
        botonDetenerNormal = new JButton("Detener Calculadora Normal");

        // Desactivar los botones de detener al principio
        botonDetenerNormal.setEnabled(false);

        // Añadir los botones al panel correspondiente
        normalPanel.add(botonIniciarNormal);
        normalPanel.add(botonDetenerNormal);
        topPanel.add(normalPanel);

   
        add(topPanel, BorderLayout.NORTH); 

        // Panel de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Acciones de los botones
        botonIniciarNormal.addActionListener(e -> iniciarServidorNormal());
        //botonIniciarCientifica.addActionListener(e -> iniciarServidorCientifica());
        botonDetenerNormal.addActionListener(e -> detenerServidorNormal());
        //botonDetenerCientifica.addActionListener(e -> detenerServidorCientifica());
    }

    private void iniciarServidorNormal() {
        try {
            // Primero, desvincula y desexporta el objeto anterior si existe
            if (calculadoraNormal != null) {
                try {
                    registryNormal.unbind("CalculadoraNormal");
                    UnicastRemoteObject.unexportObject(calculadoraNormal, true);
                    logArea.append("Calculadora normal desvinculada y desexportada.\n");
                } catch (Exception ex) {
                    logArea.append("No se pudo desvincular la calculadora normal: " + ex.getMessage() + "\n");
                }
            }

            // Crear el registro en el puerto 1099 si no existe
            if (registryNormal == null) {
                registryNormal = LocateRegistry.createRegistry(PUERTO_NORMAL);
            }

            // Crear una nueva instancia de la calculadora normal
            calculadoraNormal = new CalculadoraNormalImpl();  // Asumiendo que tienes una implementación para la calculadora normal
            registryNormal.rebind("CalculadoraNormal", calculadoraNormal); // Rebind para evitar problemas con ObjID

            logArea.append("Servidor de Calculadora Normal iniciado en el puerto " + PUERTO_NORMAL + "\n");
            botonIniciarNormal.setEnabled(false);
            botonDetenerNormal.setEnabled(true);
        } catch (Exception ex) {
            logArea.append("Error al iniciar el servidor de calculadora normal: " + ex.getMessage() + "\n");
        }
    }

 
    private void detenerServidorNormal() {
        try {
            if (registryNormal != null) {
                // Desvincular el objeto de la calculadora normal del registro
                if (calculadoraNormal != null) {
                    try {
                        registryNormal.unbind("CalculadoraNormal");
                        UnicastRemoteObject.unexportObject(calculadoraNormal, true);
                        logArea.append("Calculadora Normal desvinculada y desexportada correctamente.\n");
                    } catch (Exception ex) {
                        logArea.append("No se pudo desvincular la Calculadora Normal: " + ex.getMessage() + "\n");
                    }
                }

                logArea.append("Servidor de Calculadora Normal detenido.\n");
                botonIniciarNormal.setEnabled(true);
                botonDetenerNormal.setEnabled(false);
            }
        } catch (Exception ex) {
            logArea.append("Error al detener el servidor de Calculadora Normal: " + ex.getMessage() + "\n");
        }
    }

 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServidorCalculadora().setVisible(true);
        });
    }
}
