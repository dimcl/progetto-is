package gestore_libreria.ui;

import gestore_libreria.model.Libro;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public class PannelloLibriUI extends JPanel {
    private JTable tabellaLibri;
    private ModelloTabellaLibri modelloTabella;
    private Consumer<Libro> onBookEditListener;
    private Consumer<Libro> onDeleteBookListener;
    private GestoreLibreriaUI gestoreLibreriaUI;

    public PannelloLibriUI(GestoreLibreriaUI gestoreLibreriaUI) {
        this.gestoreLibreriaUI = gestoreLibreriaUI;
        setLayout(new BorderLayout());
        
        initializeTable();
    }

    private void initializeTable() {
        // Crea il modello della tabella
        modelloTabella = new ModelloTabellaLibri();
        tabellaLibri = new JTable(modelloTabella);
        
        // Configurazione generale della tabella
        tabellaLibri.setRowHeight(80);
        tabellaLibri.setShowGrid(false); // Disabilito la griglia standard
        tabellaLibri.setIntercellSpacing(new Dimension(0, 1)); // Solo spazio orizzontale
        tabellaLibri.setBackground(new Color(240, 240, 240)); // Grigio chiaro
        tabellaLibri.setSelectionBackground(new Color(240, 240, 240)); // Stesso colore per eliminare evidenziazione
        
        // DISABILITO LA SELEZIONE PER MIGLIORARE LE PERFORMANCE
        tabellaLibri.setRowSelectionAllowed(false);
        tabellaLibri.setColumnSelectionAllowed(false);
        tabellaLibri.setCellSelectionEnabled(false);
        tabellaLibri.setFocusable(false);
        
        // Aggiungo solo le linee orizzontali
        tabellaLibri.setShowHorizontalLines(true);
        tabellaLibri.setShowVerticalLines(false);
        tabellaLibri.setGridColor(Color.BLACK);
        
        // Imposta un font più elegante per la tabella
        Font tableFont = new Font("Segoe UI", Font.PLAIN, 12);
        tabellaLibri.setFont(tableFont);
        
        // Font per le intestazioni
        Font headerFont = new Font("Segoe UI", Font.BOLD, 13);
        tabellaLibri.getTableHeader().setFont(headerFont);
        tabellaLibri.getTableHeader().setBackground(new Color(220, 220, 220)); // Grigio per header
        tabellaLibri.getTableHeader().setForeground(Color.BLACK);
        
        // Ottimizzazioni per le performance
        tabellaLibri.setFillsViewportHeight(true);
        tabellaLibri.getTableHeader().setReorderingAllowed(false); // Disabilita riordinamento colonne
        tabellaLibri.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        
        // Configurazione colonne
        configureColumns();
        
        // Scroll pane ottimizzato per performance massima
        JScrollPane scrollPane = new JScrollPane(tabellaLibri);
        
        // Incrementi più aggressivi per scroll veloce
        scrollPane.getVerticalScrollBar().setUnitIncrement(80); // Raddoppiato
        scrollPane.getVerticalScrollBar().setBlockIncrement(320); // Raddoppiato
        scrollPane.setWheelScrollingEnabled(true);
        
        // Ottimizzazioni viewport avanzate
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getViewport().putClientProperty("JViewport.isPaintingOrigin", Boolean.TRUE);
        
        // Disabilita doppio buffering per scroll immediato
        scrollPane.getViewport().setDoubleBuffered(false);
        tabellaLibri.setDoubleBuffered(false); // Modo più veloce
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private void configureColumns() {
        // Imposta larghezze colonne (rimosse Azioni e Percorso Copertina)
        tabellaLibri.getColumnModel().getColumn(0).setPreferredWidth(80);  // Copertina
        tabellaLibri.getColumnModel().getColumn(1).setPreferredWidth(200); // Titolo
        tabellaLibri.getColumnModel().getColumn(2).setPreferredWidth(150); // Autore
        tabellaLibri.getColumnModel().getColumn(3).setPreferredWidth(100); // Genere
        tabellaLibri.getColumnModel().getColumn(4).setPreferredWidth(100); // ISBN
        tabellaLibri.getColumnModel().getColumn(5).setPreferredWidth(80);  // Rating
        tabellaLibri.getColumnModel().getColumn(6).setPreferredWidth(100); // Stato
        tabellaLibri.getColumnModel().getColumn(7).setPreferredWidth(120); // Azioni (spostata alla fine)
        
        // Renderer e editor personalizzati
        tabellaLibri.getColumnModel().getColumn(0).setCellRenderer(new RenderImmagine());
        tabellaLibri.getColumnModel().getColumn(1).setCellRenderer(new RenderTitolo()); // Titolo (a sinistra, grassetto)
        tabellaLibri.getColumnModel().getColumn(2).setCellRenderer(new RenderTestoCentrato()); // Autore (centrato)
        tabellaLibri.getColumnModel().getColumn(3).setCellRenderer(new RenderTestoCentrato()); // Genere (centrato)
        tabellaLibri.getColumnModel().getColumn(4).setCellRenderer(new RenderTestoCentrato()); // ISBN (centrato)
        tabellaLibri.getColumnModel().getColumn(5).setCellRenderer(new RenderStelle());
        tabellaLibri.getColumnModel().getColumn(6).setCellRenderer(new RenderStato());
        tabellaLibri.getColumnModel().getColumn(7).setCellRenderer(new RenderBottone());
        tabellaLibri.getColumnModel().getColumn(7).setCellEditor(new EditorBottone());
    }

    public void setOnDeleteBookListener(Consumer<Libro> onDeleteBookListener) {
        this.onDeleteBookListener = onDeleteBookListener;
    }

    public void setOnBookClickListener(Consumer<Libro> onBookEditListener) {
        this.onBookEditListener = onBookEditListener;
    }

    public void displayBooks(List<Libro> books) {
        // Aggiornamento diretto senza flicker
        modelloTabella.setBooks(books);
        SwingUtilities.invokeLater(() -> modelloTabella.fireTableDataChanged());
    }

    // Modello della tabella personalizzato
    private class ModelloTabellaLibri extends DefaultTableModel {
        private List<Libro> books;
        private final String[] columnNames = {"Copertina", "Titolo", "Autore", "Genere", "ISBN", "Valutazione", "Stato", ""};

        public void setBooks(List<Libro> books) {
            this.books = books;
        }

        @Override
        public int getRowCount() {
            return books != null ? books.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (books == null || row >= books.size()) return null;
            
            Libro Libro = books.get(row);
            return switch (col) {
                case 0 -> Libro.getPercorsoCopertina(); // Per l'immagine
                case 1 -> Libro.getTitolo();
                case 2 -> Libro.getAutore();
                case 3 -> Libro.getGenere() != null ? Libro.getGenere() : "N/A";
                case 4 -> Libro.getIsbn() != null ? Libro.getIsbn() : "N/A";
                case 5 -> Libro.getValutazione();
                case 6 -> Libro.getStatoLettura();
                case 7 -> Libro; // Per i bottoni
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 7; // Solo la colonna delle azioni è editabile
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            // Non permettiamo modifiche dirette ai dati dalla tabella
            // Le modifiche avvengono tramite i bottoni
        }
    }

    // Renderer per il titolo (a sinistra, grassetto)
    private class RenderTitolo extends JLabel implements TableCellRenderer {
        public RenderTitolo() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setHorizontalAlignment(LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            // IGNORO isSelected per performance - sfondo sempre uguale
            setBackground(table.getBackground());
            setForeground(Color.BLACK);
            return this;
        }
    }

    // Renderer per testo centrato (autore, genere, ISBN)
    private class RenderTestoCentrato extends JLabel implements TableCellRenderer {
        public RenderTestoCentrato() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setHorizontalAlignment(CENTER); // Centrato
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "");
            // IGNORO isSelected per performance - sfondo sempre uguale
            setBackground(table.getBackground());
            setForeground(Color.BLACK);
            return this;
        }
    }

    // Renderer ottimizzato per i bottoni
    private class RenderBottone extends JPanel implements TableCellRenderer {
        private final JButton editButton;
        private final JButton deleteButton;
        private final Color GREEN = new Color(34, 139, 34);
        private final Color RED = Color.RED;

        public RenderBottone() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
            editButton = new JButton("Modifica");
            deleteButton = new JButton("Elimina");
            
            // Configurazione ottimizzata dei bottoni
            setupButton(editButton, GREEN, new Dimension(80, 25));
            setupButton(deleteButton, RED, new Dimension(80, 25));
            
            add(editButton);
            add(deleteButton);
            
            // Pre-imposta le proprietà per evitare ricalcoli
            setOpaque(true);
        }
        
        private void setupButton(JButton button, Color bgColor, Dimension size) {
            button.setPreferredSize(size);
            button.setBackground(bgColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(true);
            button.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            // IGNORO isSelected per performance - sfondo sempre uguale
            setBackground(table.getBackground());
            return this;
        }
    }

    // Editor per i bottoni
    private class EditorBottone extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton editButton;
        private JButton deleteButton;
        private Libro currentBook;

        public EditorBottone() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
            editButton = new JButton("Modifica");
            deleteButton = new JButton("Elimina");
            
            editButton.setPreferredSize(new Dimension(80, 25)); // Aumentato da 70 a 80
            deleteButton.setPreferredSize(new Dimension(80, 25)); // Aumentato da 70 a 80
            
            editButton.setBackground(new Color(34, 139, 34)); // Verde per modifica
            editButton.setForeground(Color.WHITE);
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            
            editButton.addActionListener(this);
            deleteButton.addActionListener(this);
            
            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentBook = (Libro) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentBook;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == editButton && onBookEditListener != null) {
                onBookEditListener.accept(currentBook);
            } else if (e.getSource() == deleteButton && onDeleteBookListener != null) {
                onDeleteBookListener.accept(currentBook);
            }
            fireEditingStopped();
        }
    }

    // Renderer per le immagini
    private class RenderImmagine extends JLabel implements TableCellRenderer {
        public RenderImmagine() {
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            String imagePath = (String) value;
            
            ImageIcon icon = GestoreLibreriaUI.loadAndScaleImage(imagePath, 60, 75);
            if (icon != null) {
                setIcon(icon);
                setText("");
            } else {
                ImageIcon placeholder = GestoreLibreriaUI.loadPlaceholderImage(60, 75);
                if (placeholder != null) {
                    setIcon(placeholder);
                    setText("");
                } else {
                    setIcon(null);
                    setText("N/A");
                }
            }
            
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setOpaque(true);
            return this;
        }
    }

    // Renderer per le stelle del rating
    // Renderer ottimizzato per le stelle
    private class RenderStelle extends JLabel implements TableCellRenderer {
        private final Color GOLD = new Color(255, 215, 0);
        private final Color GRAY = new Color(180, 180, 180);
        private final Font STAR_FONT = new Font("SansSerif", Font.PLAIN, 16);
        
        public RenderStelle() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
            setFont(STAR_FONT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            
            int rating = (Integer) value;
            
            // Crea la stringa delle stelle una sola volta
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                stars.append("\u2605 "); // Spazio tra stelle
            }
            setText(stars.toString().trim());
            
            // Usa HTML per colorare solo le stelle necessarie (più efficiente)
            if (rating > 0) {
                StringBuilder html = new StringBuilder("<html>");
                for (int i = 0; i < 5; i++) {
                    if (i < rating) {
                        html.append("<span style='color: rgb(255,215,0)'>\u2605</span>");
                    } else {
                        html.append("<span style='color: rgb(180,180,180)'>\u2605</span>");
                    }
                    if (i < 4) html.append(" ");
                }
                html.append("</html>");
                setText(html.toString());
            } else {
                setForeground(GRAY);
            }
            
            // IGNORO isSelected per performance - sfondo sempre uguale
            setBackground(table.getBackground());
            return this;
        }
    }

    // Renderer per lo stato di lettura
    private class RenderStato extends JLabel implements TableCellRenderer {
        public RenderStato() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
            String state = (String) value;
            setText(state != null ? state.toUpperCase() : "N/A");
            
            // Usa lo stesso colore di sfondo delle altre celle
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(Color.BLACK); // Testo nero per uniformità
            
            return this;
        }
    }
}
