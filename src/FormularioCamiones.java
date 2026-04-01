import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FormularioCamiones
 * Ventana CRUD para la gestión de camiones de la flota.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class FormularioCamiones extends JFrame {

    // --- Componentes del formulario ---
    private JTextField campoPatente;
    private JTextField campoMarca;
    private JTextField campoModelo;
    private JTextField campoAnio;
    private JComboBox<String> comboConductor;
    private JTable tablaCamiones;
    private DefaultTableModel modeloTabla;
    private ConexionDB db;
    private int idSeleccionado = -1;
    private List<Integer> conductorIds;

    /**
     * Constructor: configura la ventana y construye los componentes visuales.
     */
    public FormularioCamiones() {
        db = new ConexionDB();
        conductorIds = new ArrayList<>();
        setTitle("Hirata - Gestión de Camiones");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        inicializarComponentes();
        cargarConductoresCombo();
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

        // --- Campo: Patente ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Patente:"), gbc);
        campoPatente = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoPatente, gbc);

        // --- Campo: Marca ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Marca:"), gbc);
        campoMarca = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoMarca, gbc);

        // --- Campo: Modelo ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Modelo:"), gbc);
        campoModelo = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoModelo, gbc);

        // --- Campo: Año ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Año:"), gbc);
        campoAnio = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoAnio, gbc);

        // --- Campo: Conductor (JComboBox) ---
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Conductor:"), gbc);
        comboConductor = new JComboBox<>();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(comboConductor, gbc);

        add(panelCampos, BorderLayout.NORTH);

        // --- Tabla de camiones (columna ConductorID oculta) ---
        modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Patente", "Marca", "Modelo", "Año", "Conductor", "ConductorID"}, 0
        ) {
            /**
             * Impide la edición directa de celdas en la tabla.
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCamiones = new JTable(modeloTabla);
        // Ocultar la columna ConductorID de la vista
        tablaCamiones.removeColumn(tablaCamiones.getColumnModel().getColumn(6));

        tablaCamiones.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            /**
             * Carga los datos de la fila seleccionada en los campos del formulario.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int fila = tablaCamiones.getSelectedRow();
                    if (fila >= 0) {
                        idSeleccionado = Integer.parseInt((String) modeloTabla.getValueAt(fila, 0));
                        campoPatente.setText((String) modeloTabla.getValueAt(fila, 1));
                        campoMarca.setText((String) modeloTabla.getValueAt(fila, 2));
                        campoModelo.setText((String) modeloTabla.getValueAt(fila, 3));
                        campoAnio.setText((String) modeloTabla.getValueAt(fila, 4));
                        // Seleccionar el conductor correcto en el combo
                        String conductorIdStr = (String) modeloTabla.getValueAt(fila, 6);
                        seleccionarConductorEnCombo(conductorIdStr);
                    }
                }
            }
        });
        add(new JScrollPane(tablaCamiones), BorderLayout.CENTER);

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
     * Carga los conductores desde la base de datos en el JComboBox.
     * El primer item es "-- Sin asignar --" (sin conductor).
     */
    private void cargarConductoresCombo() {
        comboConductor.removeAllItems();
        conductorIds.clear();

        // Primer item: sin conductor asignado
        comboConductor.addItem("-- Sin asignar --");
        conductorIds.add(null);

        List<String[]> conductores = db.listarConductores();
        for (String[] c : conductores) {
            comboConductor.addItem(c[1] + " (" + c[2] + ")");
            conductorIds.add(Integer.parseInt(c[0]));
        }
    }

    /**
     * Selecciona el conductor correcto en el JComboBox según el ID.
     *
     * @param conductorIdStr ID del conductor como String, vacío si no tiene
     */
    private void seleccionarConductorEnCombo(String conductorIdStr) {
        if (conductorIdStr == null || conductorIdStr.isEmpty()) {
            comboConductor.setSelectedIndex(0);
            return;
        }
        int conductorId = Integer.parseInt(conductorIdStr);
        for (int i = 0; i < conductorIds.size(); i++) {
            if (conductorIds.get(i) != null && conductorIds.get(i) == conductorId) {
                comboConductor.setSelectedIndex(i);
                return;
            }
        }
        comboConductor.setSelectedIndex(0);
    }

    /**
     * Obtiene el ID del conductor seleccionado en el JComboBox.
     *
     * @return ID del conductor seleccionado, null si es "Sin asignar"
     */
    private Integer obtenerConductorIdSeleccionado() {
        int index = comboConductor.getSelectedIndex();
        if (index >= 0 && index < conductorIds.size()) {
            return conductorIds.get(index);
        }
        return null;
    }

    /**
     * Carga todos los camiones desde la base de datos en la tabla.
     */
    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<String[]> camiones = db.listarCamiones();
        for (String[] c : camiones) {
            modeloTabla.addRow(c);
        }
    }

    /**
     * Valida los campos y agrega un nuevo camión a la base de datos.
     */
    private void accionAgregar() {
        String patente = campoPatente.getText().trim();
        String marca = campoMarca.getText().trim();
        String modelo = campoModelo.getText().trim();
        String anioTexto = campoAnio.getText().trim();

        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la patente.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (marca.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la marca.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (modelo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el modelo.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "El año debe ser un número válido.",
                "Valor no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (anio < 1990 || anio > 2030) {
            JOptionPane.showMessageDialog(this,
                "El año debe estar entre 1990 y 2030.",
                "Valor no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer conductorId = obtenerConductorIdSeleccionado();
        db.agregarCamion(patente, marca, modelo, anio, conductorId);
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Camión agregado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Valida los campos y actualiza el camión seleccionado en la base de datos.
     */
    private void accionActualizar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un camión de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patente = campoPatente.getText().trim();
        String marca = campoMarca.getText().trim();
        String modelo = campoModelo.getText().trim();
        String anioTexto = campoAnio.getText().trim();

        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la patente.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (marca.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la marca.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (modelo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar el modelo.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "El año debe ser un número válido.",
                "Valor no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (anio < 1990 || anio > 2030) {
            JOptionPane.showMessageDialog(this,
                "El año debe estar entre 1990 y 2030.",
                "Valor no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Integer conductorId = obtenerConductorIdSeleccionado();
        db.actualizarCamion(idSeleccionado, patente, marca, modelo, anio, conductorId);
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Camión actualizado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Solicita confirmación y elimina el camión seleccionado de la base de datos.
     */
    private void accionEliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un camión de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar este camión?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            boolean eliminado = db.eliminarCamion(idSeleccionado);
            if (eliminado) {
                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(this,
                    "Camión eliminado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el camión.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Limpia todos los campos del formulario y deselecciona la tabla.
     */
    private void limpiarCampos() {
        campoPatente.setText("");
        campoMarca.setText("");
        campoModelo.setText("");
        campoAnio.setText("");
        comboConductor.setSelectedIndex(0);
        idSeleccionado = -1;
        tablaCamiones.clearSelection();
    }
}
