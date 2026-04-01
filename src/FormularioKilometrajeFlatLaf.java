import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * FormularioKilometrajeFlatLaf
 * Ventana principal del sistema de registro de kilometraje.
 * RF-01: El conductor ingresa el kilometraje del camión al finalizar un
 * recorrido.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class FormularioKilometrajeFlatLaf extends JFrame {

    // --- Componentes del formulario ---
    private JTextField campoPatente;
    private JTextField campoKilometraje;
    private JButton botonRegistrar;
    private JButton botonLimpiar;

    /**
     * Constructor: configura la ventana y construye los componentes visuales.
     */
    public FormularioKilometrajeFlatLaf() {
        // Configuración general de la ventana
        setTitle("Hirata - Registro de Kilometraje [UI Test]");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null); // Centrar en pantalla
        setResizable(false);

        // Inicializar y organizar los componentes del formulario
        inicializarComponentes();
    }

    /**
     * Crea y organiza todos los componentes visuales del formulario.
     */
    private void inicializarComponentes() {
        // Panel principal con margen interno
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Etiqueta y campo: Patente del camión ---
        JLabel etiquetaPatente = new JLabel("Patente del camión:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panel.add(etiquetaPatente, gbc);

        campoPatente = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        panel.add(campoPatente, gbc);

        // --- Etiqueta y campo: Kilometraje recorrido ---
        JLabel etiquetaKilometraje = new JLabel("Kilometraje recorrido (km):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(etiquetaKilometraje, gbc);

        campoKilometraje = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        panel.add(campoKilometraje, gbc);

        // --- Panel de botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        botonRegistrar = new JButton("Registrar");
        botonLimpiar = new JButton("Limpiar");

        panelBotones.add(botonRegistrar);
        panelBotones.add(botonLimpiar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(panelBotones, gbc);

        // Agregar el panel a la ventana
        add(panel);

        // --- Asignar acciones a los botones ---
        botonRegistrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionRegistrar();
            }
        });

        botonLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Limpiar todos los campos del formulario
                campoPatente.setText("");
                campoKilometraje.setText("");
            }
        });
    }

    /**
     * Ejecuta las validaciones y registra el kilometraje al presionar "Registrar".
     * RF-01: Validaciones en orden antes de guardar en base de datos.
     */
    private void accionRegistrar() {
        // Obtener los valores ingresados por el conductor
        String patente = campoPatente.getText().trim();
        String kilometrajeTexto = campoKilometraje.getText().trim();

        // Validación 1: La patente no debe estar vacía
        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar la patente del camión.",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación 2: El kilometraje no debe estar vacío
        if (kilometrajeTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar el kilometraje.",
                    "Campo requerido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación 3: El kilometraje debe ser un número entero válido
        int km;
        try {
            km = Integer.parseInt(kilometrajeTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "El kilometraje debe ser un número válido.",
                    "Valor no válido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación 4: El kilometraje debe ser mayor a cero
        if (km <= 0) {
            JOptionPane.showMessageDialog(this,
                    "El kilometraje debe ser mayor a cero.",
                    "Valor no válido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Todas las validaciones pasaron: guardar en base de datos
        // Se instancia ConexionDB para llamar el método (no es estático)
        ConexionDB db = new ConexionDB();
        db.registrarKilometraje(patente, km);

        // RF-03: La alerta se basa en kilometraje ACUMULADO, no solo el viaje actual
        int totalAcumulado = db.obtenerKilometrajeTotalPorPatente(patente);
        if (totalAcumulado >= 5000) {
            JOptionPane.showMessageDialog(this,
                    "ALERTA: El camión con patente " + patente +
                            " ha acumulado " + totalAcumulado + " km y requiere mantenimiento preventivo.",
                    "Mantenimiento requerido",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Kilometraje registrado correctamente.\nKilometraje acumulado: " + totalAcumulado + " km.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
