import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

/**
 * FormularioMantenimiento
 * Ventana CRUD para la gestión de mantenimientos de la flota.
 * Empresa de Transporte Hirata - Sistema de Gestión de Flota
 */
public class FormularioMantenimiento extends JFrame {

    // --- Componentes del formulario ---
    private JTextField campoPatente;
    private JComboBox<String> comboTipo;
    private JTextArea campoDescripcion;
    private JTextField campoFecha;
    private JTable tablaMantenimiento;
    private DefaultTableModel modeloTabla;
    private ConexionDB db;
    private int idSeleccionado = -1;

    /**
     * Constructor: configura la ventana y construye los componentes visuales.
     */
    public FormularioMantenimiento() {
        db = new ConexionDB();
        setTitle("Hirata - Gestión de Mantenimiento");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 550);
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

        // --- Campo: Patente ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Patente:"), gbc);
        campoPatente = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoPatente, gbc);

        // --- Campo: Tipo (JComboBox) ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Tipo:"), gbc);
        comboTipo = new JComboBox<>(new String[]{"Preventivo", "Correctivo", "Revisión"});
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(comboTipo, gbc);

        // --- Campo: Descripción (JTextArea con scroll) ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTH;
        panelCampos.add(new JLabel("Descripción:"), gbc);
        campoDescripcion = new JTextArea(3, 20);
        campoDescripcion.setLineWrap(true);
        campoDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(campoDescripcion);
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        panelCampos.add(scrollDescripcion, gbc);

        // --- Campo: Fecha ---
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        panelCampos.add(new JLabel("Fecha (AAAA-MM-DD):"), gbc);
        campoFecha = new JTextField(LocalDate.now().toString());
        gbc.gridx = 1; gbc.weightx = 0.7;
        panelCampos.add(campoFecha, gbc);

        add(panelCampos, BorderLayout.NORTH);

        // --- Tabla de mantenimientos ---
        modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Patente", "Tipo", "Descripción", "Fecha"}, 0
        ) {
            /**
             * Impide la edición directa de celdas en la tabla.
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMantenimiento = new JTable(modeloTabla);
        tablaMantenimiento.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            /**
             * Carga los datos de la fila seleccionada en los campos del formulario.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int fila = tablaMantenimiento.getSelectedRow();
                    if (fila >= 0) {
                        idSeleccionado = Integer.parseInt((String) modeloTabla.getValueAt(fila, 0));
                        campoPatente.setText((String) modeloTabla.getValueAt(fila, 1));
                        comboTipo.setSelectedItem(modeloTabla.getValueAt(fila, 2));
                        campoDescripcion.setText((String) modeloTabla.getValueAt(fila, 3));
                        campoFecha.setText((String) modeloTabla.getValueAt(fila, 4));
                    }
                }
            }
        });
        add(new JScrollPane(tablaMantenimiento), BorderLayout.CENTER);

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
     * Carga todos los registros de mantenimiento desde la base de datos en la tabla.
     */
    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<String[]> mantenimientos = db.listarMantenimientos();
        for (String[] m : mantenimientos) {
            modeloTabla.addRow(m);
        }
    }

    /**
     * Valida que la fecha tenga el formato AAAA-MM-DD.
     *
     * @param fecha Cadena de texto con la fecha a validar
     * @return true si el formato es válido, false en caso contrario
     */
    private boolean validarFormatoFecha(String fecha) {
        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        try {
            LocalDate.parse(fecha);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida los campos y agrega un nuevo registro de mantenimiento a la base de datos.
     */
    private void accionAgregar() {
        String patente = campoPatente.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();
        String descripcion = campoDescripcion.getText().trim();
        String fecha = campoFecha.getText().trim();

        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la patente.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la fecha.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormatoFecha(fecha)) {
            JOptionPane.showMessageDialog(this,
                "La fecha debe tener el formato AAAA-MM-DD (ej: 2026-03-31).",
                "Formato no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        db.agregarMantenimiento(patente, tipo, descripcion, fecha);
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Mantenimiento registrado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Valida los campos y actualiza el registro de mantenimiento seleccionado.
     */
    private void accionActualizar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un registro de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patente = campoPatente.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();
        String descripcion = campoDescripcion.getText().trim();
        String fecha = campoFecha.getText().trim();

        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la patente.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar la fecha.",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarFormatoFecha(fecha)) {
            JOptionPane.showMessageDialog(this,
                "La fecha debe tener el formato AAAA-MM-DD (ej: 2026-03-31).",
                "Formato no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        db.actualizarMantenimiento(idSeleccionado, patente, tipo, descripcion, fecha);
        cargarTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this,
            "Mantenimiento actualizado correctamente.",
            "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Solicita confirmación y elimina el registro de mantenimiento seleccionado.
     */
    private void accionEliminar() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un registro de la tabla.",
                "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de eliminar este registro de mantenimiento?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            boolean eliminado = db.eliminarMantenimiento(idSeleccionado);
            if (eliminado) {
                cargarTabla();
                limpiarCampos();
                JOptionPane.showMessageDialog(this,
                    "Registro de mantenimiento eliminado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar el registro.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Limpia todos los campos del formulario y deselecciona la tabla.
     * Restablece la fecha al día de hoy.
     */
    private void limpiarCampos() {
        campoPatente.setText("");
        comboTipo.setSelectedIndex(0);
        campoDescripcion.setText("");
        campoFecha.setText(LocalDate.now().toString());
        idSeleccionado = -1;
        tablaMantenimiento.clearSelection();
    }
}
