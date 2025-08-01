import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;

public class ServidorCalculadoraCientifica extends JFrame {

   
    private JButton botonIniciarCientifica;
   
    private JButton botonDetenerCientifica;
    private JTextArea logArea;
  
    private Registry registryCientifica;

  
    private CalculadoraCientifica calculadoraCientifica;


    private static final int PUERTO_CIENTIFICA = 1100; // Puerto para la calculadora científica

    public ServidorCalculadoraCientifica() {
        // Configuración de la ventana
        setTitle("Servidor de Calculadora Cientifica");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Disposición vertical

  

        // Panel para los botones de la calculadora científica
        JPanel cientificaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonIniciarCientifica = new JButton("Iniciar Calculadora Científica");
        botonDetenerCientifica = new JButton("Detener Calculadora Científica");

        // Desactivar los botones de detener al principio
        botonDetenerCientifica.setEnabled(false);

        // Añadir los botones al panel correspondiente
        cientificaPanel.add(botonIniciarCientifica);
        cientificaPanel.add(botonDetenerCientifica);
        topPanel.add(cientificaPanel);

        add(topPanel, BorderLayout.NORTH);

        // Panel de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Acciones de los botones
       
        botonIniciarCientifica.addActionListener(e -> iniciarServidorCientifica());
       
        botonDetenerCientifica.addActionListener(e -> detenerServidorCientifica());
    }


    private void iniciarServidorCientifica() {
        try {
            // Primero, desvincula y desexporta el objeto anterior si existe
            if (calculadoraCientifica != null) {
                try {
                    registryCientifica.unbind("CalculadoraCientifica");
                    UnicastRemoteObject.unexportObject(calculadoraCientifica, true);
                    logArea.append("Calculadora científica desvinculada y desexportada.\n");
                } catch (Exception ex) {
                    logArea.append("No se pudo desvincular la calculadora científica: " + ex.getMessage() + "\n");
                }
            }

            // Crear el registro en el puerto 1100 si no existe
            if (registryCientifica == null) {
                registryCientifica = LocateRegistry.createRegistry(PUERTO_CIENTIFICA);
            }

            // Crear una nueva instancia de la calculadora científica
            calculadoraCientifica = new CalculadoraCientificaImpl();  // Asumiendo que tienes una implementación para la calculadora científica
            registryCientifica.rebind("CalculadoraCientifica", calculadoraCientifica); // Rebind para evitar problemas con ObjID

            logArea.append("Servidor de Calculadora Científica iniciado en el puerto " + PUERTO_CIENTIFICA + "\n");
            botonIniciarCientifica.setEnabled(false);
            botonDetenerCientifica.setEnabled(true);
        } catch (Exception ex) {
            logArea.append("Error al iniciar el servidor de calculadora científica: " + ex.getMessage() + "\n");
        }
    }


    private void detenerServidorCientifica() {
        try {
            if (registryCientifica != null) {
                // Desvincular el objeto de la calculadora científica del registro
                if (calculadoraCientifica != null) {
                    try {
                        registryCientifica.unbind("CalculadoraCientifica");
                        UnicastRemoteObject.unexportObject(calculadoraCientifica, true);
                        logArea.append("Calculadora Científica desvinculada y desexportada correctamente.\n");
                    } catch (Exception ex) {
                        logArea.append("No se pudo desvincular la Calculadora Científica: " + ex.getMessage() + "\n");
                    }
                }

                logArea.append("Servidor de Calculadora Científica detenido.\n");
                botonIniciarCientifica.setEnabled(true);
                botonDetenerCientifica.setEnabled(false);
            }
        } catch (Exception ex) {
            logArea.append("Error al detener el servidor de Calculadora Científica: " + ex.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServidorCalculadoraCientifica().setVisible(true);
        });
    }
}
