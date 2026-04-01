import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.formdev.flatlaf.FlatDarkLaf;

/**
 * MenuPrincipal
 * Ventana principal del Sistema de Gestión de Flota.
 * Punto de entrada de la aplicación con acceso a todos los módulos.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class MenuPrincipal extends JFrame {

    /**
     * Constructor: configura la ventana principal y sus componentes.
     */
    public MenuPrincipal() {
        setTitle("Hirata - Sistema de Gestión de Flota");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        inicializarComponentes();
    }

    /**
     * Crea y organiza todos los componentes visuales del menú principal.
     */
    private void inicializarComponentes() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 40, 8, 40);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // --- Título del sistema ---
        JLabel titulo = new JLabel("Sistema de Gestión de Flota", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 40, 2, 40);
        panel.add(titulo, gbc);

        // --- Subtítulo ---
        JLabel subtitulo = new JLabel("Empresa de Transporte Hirata", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 40, 20, 40);
        panel.add(subtitulo, gbc);

        // --- Botón: Registrar Kilometraje ---
        gbc.insets = new Insets(8, 40, 8, 40);
        JButton btnKilometraje = new JButton("Registrar Kilometraje");
        gbc.gridy = 2;
        panel.add(btnKilometraje, gbc);

        // --- Botón: Gestión de Conductores ---
        JButton btnConductores = new JButton("Gestión de Conductores");
        gbc.gridy = 3;
        panel.add(btnConductores, gbc);

        // --- Botón: Gestión de Camiones ---
        JButton btnCamiones = new JButton("Gestión de Camiones");
        gbc.gridy = 4;
        panel.add(btnCamiones, gbc);

        // --- Botón: Gestión de Mantenimiento ---
        JButton btnMantenimiento = new JButton("Gestión de Mantenimiento");
        gbc.gridy = 5;
        panel.add(btnMantenimiento, gbc);

        // --- Acciones de los botones ---
        btnKilometraje.addActionListener(new ActionListener() {
            /**
             * Abre el formulario de registro de kilometraje.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                new FormularioKilometrajeFlatLaf().setVisible(true);
            }
        });

        btnConductores.addActionListener(new ActionListener() {
            /**
             * Abre el formulario de gestión de conductores.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                new FormularioConductores().setVisible(true);
            }
        });

        btnCamiones.addActionListener(new ActionListener() {
            /**
             * Abre el formulario de gestión de camiones.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                new FormularioCamiones().setVisible(true);
            }
        });

        btnMantenimiento.addActionListener(new ActionListener() {
            /**
             * Abre el formulario de gestión de mantenimiento.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                new FormularioMantenimiento().setVisible(true);
            }
        });

        add(panel);
    }

    /**
     * Punto de entrada del programa.
     * Configura el tema FlatDarkLaf y lanza el menú principal
     * en el hilo de despacho de eventos de Swing.
     *
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FlatDarkLaf.setup();
                new MenuPrincipal().setVisible(true);
            }
        });
    }
}
