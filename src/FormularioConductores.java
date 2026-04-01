import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * FormularioConductores
 * Ventana CRUD para la gestión de conductores de la flota.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class FormularioConductores extends JFrame {

    // --- Componentes del formulario ---
    private JTextField campoNombre;
    private JTextField campoRut;
    private JTextField campoTelefono;
    private JTextField campoEmail;
    private JTable tablaConductores;
    private DefaultTableModel modeloTabla;
    private ConexionDB db;
    private int idSeleccionado = -1;

    /**
     * Constructor: configura la ventana y construye los componentes visuales.
     */
    public FormularioConductores() {
        db = new ConexionDB();
        setTitle("Hirata - Gestión de Conductores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        inicializarComponentes();
        cargarTabla();
    }

    /**
     * Crea y organiza todos los componentes visuales del formulario.
     */
    private void inicializarComponentes() {
        // Panel de campos con GridBagLayout
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Campo: Nombre ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Nombre:"), gbc);
        campoNombre = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoNombre, gbc);

        // --- Campo: RUT ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("RUT:"), gbc);
        campoRut = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoRut, gbc);

        // --- Campo: Teléfono ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Teléfono:"), gbc);
        campoTelefono = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoTelefono, gbc);

        // --- Campo: Email ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Email:"), gbc);
        campoEmail = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoEmail, gbc);

        add(panelCampos, BorderLayout.NORTH);

        // --- Tabla de conductores ---
        modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Teléfono", "Email"}, 0
        ) {
            /**
             * Impide la edición directa de celdas en la tabla.
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaConductores = new JTable(modeloTabla);
        tablaConductores.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            /**
             * Carga los datos de la fila seleccionada en los campos del formulario.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int fila = tablaConductores.getSelectedRow();
                    if (fila >= 0) {
                        idSeleccionado = Integer.parseInt((String) modeloTabla.getValueAt(fila, 0));
                        campoNombre.setText((String) modeloTabla.getValueAt(fila, 1));
                        campoRut.setText((String) modeloTabla.getValueAt(fila, 2));
                        campoTelefono.setText((String) modeloTabla.getValueAt(fila, 3));
                        campoEmail.setText((String) modeloTabla.getValueAt(fila, 4));
                    }
                }
            }
        });
        add(new JScrollPane(tablaConductores), BorderLayout.CENTER);

        // --- Panel de botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(new ActionListener() {
            /** Acción del botón Agregar. */
            @Override
            public void actionPerformed(ActionEvent e) {
                accionAgregar();
            }
        });
        btnActualizar.addActionListener(new ActionListener() {
            /** Acción del botón Actualizar. */
            @Override
            public void actionPerformed(ActionEvent e) {
                accionActualizar();
            }
        });
        btnEliminar.addActionListener(new ActionListener() {
            /** Acción del botón Eliminar. */
            @Override
            public void actionPerformed(ActionEvent e) {
                accionEliminar();
            }
        });
        btnLimpiar.addActionListener(new ActionListener() {
            /** Acción del botón Limpiar. */
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarCampos();
            }
        });

        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Carga todos los conductores desde la base de datos en la tabla.
     */
    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<String[]> conductores = db.listarConductores();
        for (String[] c : conductores) {
            modeloTabla.addRow(c);
        }
    }

    /**
     * Valida los campos y agrega un nuevo conductor a la base de datos.
     */
    private void accionAgregar() {
        String nombre = campoNombre.getText().trim();
        String rut = campoRut.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el nombre del conductor.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (rut.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el RUT del conductor.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        db.agregarConductor(nombre, rut,
            campoTelefono.getText().trim(), campoEmail.getText().trim());
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Conductor agregado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Valida los campos y actualiza el conductor seleccionado en la base de datos.
     */
    private void accionActualizar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un conductor de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombre = campoNombre.getText().trim();
        String rut = campoRut.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el nombre del conductor.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (rut.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el RUT del conductor.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        db.actualizarConductor(idSeleccionado, nombre, rut,
            campoTelefono.getText().trim(), campoEmail.getText().trim());
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Conductor actualizado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Solicita confirmación y elimina el conductor seleccionado de la base de datos.
     */
    private void accionEliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un conductor de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar este conductor?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            boolean eliminado = db.eliminarConductor(idSeleccionado);
            if (eliminado) {
                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(this,
                    "Conductor eliminado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar. El conductor puede estar asignado a un camión.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Limpia todos los campos del formulario y deselecciona la tabla.
     */
    private void limpiarCampos() {
        campoNombre.setText("");
        campoRut.setText("");
        campoTelefono.setText("");
        campoEmail.setText("");
        idSeleccionado = -1;
        tablaConductores.clearSelection();
    }
}
