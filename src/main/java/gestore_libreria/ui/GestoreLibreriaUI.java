package gestore_libreria.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.formdev.flatlaf.FlatLightLaf;
import gestore_libreria.db.*;
import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;
import gestore_libreria.observer.OsservatoreLibroConcreto;

/**
 * Classe principale dell'interfaccia grafica per la gestione della libreria.
 * Estende {@link JFrame}.
 * Integra e concretizza le operazioni possibili con la libreria, visualizzazione, modifica, aggiunta, rimozione e gestione
 * dello stato dei libri.
 * Include anche undo/redo e import/export del database
 */

public class GestoreLibreriaUI extends JFrame{

    private GestoreLibroConcreto db;
    private OsservatoreLibroConcreto OsservatoreLibro;
    private PannelloLibriUI PannelloLibriUI;

    private JMenuItem undo;
    private JMenuItem redo;

    private CriterioOrdinamento currentSortCriteria = CriterioOrdinamento.NESSUNO; // Default

    /**
     * Costruttore
     *
     * @param db Istanza di {@link GestoreLibroConcreto} per la gestione dei dati dei libri
     * @pre {@code db} deve essere instanziato correttamente
     * @post l'interfaccia utente viene visualizzata
     */
    public GestoreLibreriaUI(GestoreLibroConcreto db){
        super(""); // Rimuovo il titolo della finestra
        this.db = db;
        inizializzaUI();
    }

    /**
     * Inzializza l'interfaccia utente, configurando layout, meno pannelli e listener
     */
    private void inizializzaUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        setJMenuBar(creaMenuBar());

        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // Solo sezione destra (centro) che ora occupa tutta la larghezza
        JPanel rightPanel = inizializzaSezioneDX();

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        this.PannelloLibriUI = new PannelloLibriUI(this);
        rightPanel.add(PannelloLibriUI, BorderLayout.CENTER);

        PannelloLibriUI.setOnBookClickListener(this::mostraDialogModificaLibro);
        PannelloLibriUI.setOnDeleteBookListener(this::AzioneMenuPopup);

        this.OsservatoreLibro = new OsservatoreLibroConcreto(this,this.PannelloLibriUI,this.db);

        updateUndoRedoMenuState();

        setVisible(true);

        // Discrivo l'observer nel caso in cui decidiamo di chiudere la finestra
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (OsservatoreLibro != null) {
                    OsservatoreLibro.unsubscribe();
                }
            }
        });

    }

    /**
     * Mostra i dettagli di un libro selezionato, permette la modifica e l'eliminazione di un oggetto {@link Libro}
     * @pre {@code Libro} non deve essere null.
     * @post Se modificato, il libro viene aggiornato nel database e l'interfaccia viene aggiornata.
     * @post Se eliminato, il libro viene rimosso dal database e l'interfaccia viene aggiornata.
     * @param Libro
     */
    private void mostraDettagliLibro(Libro Libro) {
        // Creo JLabel invece di JTextField per una visualizzazione pi? pulita
        JLabel titoloLabel = new JLabel(Libro.getTitolo());
        JLabel autoreLabel = new JLabel(Libro.getAutore());
        JLabel isbnLabel = new JLabel(Libro.getIsbn() != null ? Libro.getIsbn() : "N/A");
        JLabel genreLabel = new JLabel(Libro.getGenere() != null ? Libro.getGenere() : "N/A");
        JLabel ratingLabel = new JLabel(String.valueOf(Libro.getValutazione()));
        JLabel statoLabel = new JLabel(Libro.getStatoLettura() != null ? Libro.getStatoLettura() : "DA LEGGERE");
        JLabel copertinaLabel = new JLabel(Libro.getPercorsoCopertina() != null ? Libro.getPercorsoCopertina() : "Nessuna immagine");

        // Stile per le etichette dei valori
        Font valueFont = new Font("SansSerif", Font.PLAIN, 14);
        titoloLabel.setFont(valueFont);
        autoreLabel.setFont(valueFont);
        isbnLabel.setFont(valueFont);
        genreLabel.setFont(valueFont);
        ratingLabel.setFont(valueFont);
        statoLabel.setFont(valueFont);
        copertinaLabel.setFont(valueFont);

        // Immagine senza bordo
        JLabel imagePreview = new JLabel();
        int width = 120;
        int height = 180;
        imagePreview.setPreferredSize(new Dimension(width, height));

        ImageIcon selectedIcon = loadAndScaleImage(Libro.getPercorsoCopertina(), width, height);
        if (selectedIcon != null) {
            imagePreview.setIcon(selectedIcon);
        } else {
            ImageIcon placeholder = loadPlaceholderImage(width, height);
            if (placeholder != null) {
                imagePreview.setIcon(placeholder);
            } else {
                imagePreview.setText("Immagine non disponibile");
            }
        }

        // Pannello principale con layout a griglia per avere etichette a fianco dei valori
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Font per le etichette dei campi
        Font labelFont = new Font("SansSerif", Font.BOLD, 14);

        // Titolo
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titoloFieldLabel = new JLabel("Titolo:");
        titoloFieldLabel.setFont(labelFont);
        detailsPanel.add(titoloFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(titoloLabel, gbc);

        // Autore
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel autoreFieldLabel = new JLabel("Autore:");
        autoreFieldLabel.setFont(labelFont);
        detailsPanel.add(autoreFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(autoreLabel, gbc);

        // Genere
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel genereFieldLabel = new JLabel("Genere:");
        genereFieldLabel.setFont(labelFont);
        detailsPanel.add(genereFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(genreLabel, gbc);

        // ISBN
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel isbnFieldLabel = new JLabel("ISBN:");
        isbnFieldLabel.setFont(labelFont);
        detailsPanel.add(isbnFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(isbnLabel, gbc);

        // Rating
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel ratingFieldLabel = new JLabel("Valutazione (1-5):");
        ratingFieldLabel.setFont(labelFont);
        detailsPanel.add(ratingFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(ratingLabel, gbc);

        // Stato lettura
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel statoFieldLabel = new JLabel("Stato lettura:");
        statoFieldLabel.setFont(labelFont);
        detailsPanel.add(statoFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(statoLabel, gbc);

        // Copertina
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel copertinaFieldLabel = new JLabel("Copertina:");
        copertinaFieldLabel.setFont(labelFont);
        detailsPanel.add(copertinaFieldLabel, gbc);
        gbc.gridx = 1;
        detailsPanel.add(copertinaLabel, gbc);

        // Layout finale con immagine a sinistra e dettagli a destra
        JPanel BookPanel = new JPanel(new BorderLayout());
        BookPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Immagine senza bordo
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.add(imagePreview);
        
        BookPanel.add(imagePanel, BorderLayout.WEST);
        BookPanel.add(detailsPanel, BorderLayout.CENTER);

        String[] options = {"Modifica", "<html><font color='red'>Elimina</font></html>"};
        int choice = JOptionPane.showOptionDialog(this, BookPanel, "Dettagli Libro",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, null);
        
        if (choice == 0) {
            // Modalit? modifica: crea un nuovo dialog con campi modificabili
            mostraDialogModificaLibro(Libro);
        } else if (choice == 1) {
            String[] deleteOptions = {"Sì", "No"};
            int confirm = JOptionPane.showOptionDialog(this,
                    "Sei sicuro di voler eliminare il libro '" + Libro.getTitolo() + "'?",
                    "Conferma Eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, deleteOptions, deleteOptions[1]);
            if (confirm == 0) {
                db.eliminaLibro(Libro);
                JOptionPane.showMessageDialog(this, "Libro eliminato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Mostra il dialog per la modifica di un libro
     */
    private void mostraDialogModificaLibro(Libro Libro) {
        JTextField titoloField = new JTextField(Libro.getTitolo(), 20);
        JTextField autoreField = new JTextField(Libro.getAutore(), 20);
        JTextField isbnField = new JTextField(Libro.getIsbn() != null ? Libro.getIsbn() : "", 20);
        JTextField genreField = new JTextField(Libro.getGenere() != null ? Libro.getGenere() : "", 20);
        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(Libro.getValutazione(), 1, 5, 1));
        String[] statiLettura = {"DA LEGGERE", "IN LETTURA", "LETTO"};
        JComboBox<String> statoCombo = new JComboBox<>(statiLettura);
        statoCombo.setSelectedItem(Libro.getStatoLettura() != null ? Libro.getStatoLettura() : "DA LEGGERE");

        JTextField imagePathField = new JTextField(Libro.getPercorsoCopertina() != null ? Libro.getPercorsoCopertina() : "", 15);
        imagePathField.setEditable(false);
        JButton browseBtn = new JButton("Sfoglia");

        JLabel imagePreview = new JLabel();
        int width = 120;
        int height = 180;
        imagePreview.setPreferredSize(new Dimension(width, height));

        ImageIcon selectedIcon = loadAndScaleImage(Libro.getPercorsoCopertina(), width, height);
        if (selectedIcon != null) {
            imagePreview.setIcon(selectedIcon);
        } else {
            ImageIcon placeholder = loadPlaceholderImage(width, height);
            if (placeholder != null) {
                imagePreview.setIcon(placeholder);
            } else {
                imagePreview.setText("Immagine non disponibile");
            }
        }

        JPanel imageWrapper = new JPanel();
        imageWrapper.setLayout(new BorderLayout());
        imageWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        imageWrapper.add(imagePreview, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Autore:"));
        panel.add(autoreField);
        panel.add(new JLabel("Genere:"));
        panel.add(genreField);
        panel.add(new JLabel("ISBN:"));
        panel.add(isbnField);
        panel.add(new JLabel("Valutazione (1-5):"));
        panel.add(ratingSpinner);
        panel.add(new JLabel("Stato lettura:"));
        panel.add(statoCombo);
        panel.add(new JLabel("Copertina:"));
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(imagePathField);
        filePanel.add(browseBtn);
        panel.add(filePanel);

        JPanel BookPanel = new JPanel(new BorderLayout());
        BookPanel.add(panel, BorderLayout.CENTER);
        BookPanel.add(imageWrapper, BorderLayout.NORTH);
        imageWrapper.setLayout(new FlowLayout(FlowLayout.CENTER));

        browseButtonAction(browseBtn, imagePathField, imagePreview, width, height);

        String[] modifyOptions = {"OK", "Annulla"};
        int result = JOptionPane.showOptionDialog(this, BookPanel, "Modifica Libro", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, modifyOptions, modifyOptions[0]);
        if (result == 0) {
            String newTitolo = titoloField.getText().trim();
            String newAutore = autoreField.getText().trim();
            String newIsbn = isbnField.getText().trim();
            String newGenre = genreField.getText().trim();
            int newRating = (Integer) ratingSpinner.getValue();
            String newStato = (String) statoCombo.getSelectedItem();
            String newPath = imagePathField.getText().trim();

            if (!newTitolo.isEmpty() && !newAutore.isEmpty()) {
                Libro updatedBook = new Libro.Costruttore(newTitolo, newAutore)
                        .id(Libro.getId())
                        .isbn(newIsbn)
                        .valutazione(newRating)
                        .statoLettura(newStato)
                        .percorsoCopertina(newPath)
                        .genere(newGenre)
                        .build();

                db.aggiornaLibro(Libro, updatedBook);
                JOptionPane.showMessageDialog(this, "Libro modificato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Titolo e autore sono obbligatori.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Utilizzato in {@code mostraDettagliLibro()} imposta la modificabilit? dei campi dei dettagli del libro.
     *
     * @param editable True per rendere i campi modificabili, false per renderli non modificabili.
     * @param titoloField Campo del titolo.
     * @param autoreField Campo dell'autore.
     * @param isbnField Campo dell'ISBN.
     * @param genreField Campo del genere.
     * @param ratingSpinner Spinner del rating.
     * @param statoCombo ComboBox dello stato di lettura.
     * @param browseBtn Pulsante per la selezione dell'immagine.
     * @param imagePathField Campo del percorso dell'immagine.
     * @pre Tutti i parametri devono essere inizializzati.
     * @post I campi sono impostati come modificabili o non modificabili in base al parametro {@code editable}.
     */
    private void setFieldsEditable(boolean editable, JTextField titoloField, JTextField autoreField, JTextField isbnField, JTextField genreField, JSpinner ratingSpinner,
                                   JComboBox<String> statoCombo, JButton browseBtn, JTextField imagePathField) {
        titoloField.setEditable(editable);
        autoreField.setEditable(editable);
        isbnField.setEditable(editable);
        genreField.setEditable(editable);
        ratingSpinner.setEnabled(editable);
        statoCombo.setEnabled(editable);
        browseBtn.setEnabled(editable);
        browseBtn.setVisible(editable);
        if(!editable){
            imagePathField.setColumns(20);
        }else{
            imagePathField.setColumns(15);
        }
    }

    /**
     * Gestisce l'azione di eliminazione di un libro tramite un menu contestuale.
     *
     * @param Libro Il libro da eliminare.
     * @pre {@code Libro} non deve essere null.
     * @post Se confermato, il libro viene rimosso dal database e l'interfaccia viene aggiornata.
     */
    private void AzioneMenuPopup(Libro Libro) {
        String[] deleteOptions = {"Sì", "No"};
        int confirm = JOptionPane.showOptionDialog(this,
                "Sei sicuro di voler eliminare il libro '" + Libro.getTitolo() + "'?",
                "Conferma Eliminazione",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, deleteOptions, deleteOptions[1]);

        if (confirm == 0) {
            db.eliminaLibro(Libro);
            JOptionPane.showMessageDialog(this,
                    "Libro eliminato con successo!",
                    "Successo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Crea la barra dei menu dell'applicazione, includendo le voci per file, modifica e criterio di visualizzazione.
     *
     * @return La barra dei menu configurata.
     * @post La barra dei menu contiene le voci per esportare/importare il database, uscire, le operazioni di undo/redo e le impostazioni di visualizzazione.
     */
    private JMenuBar creaMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Modifica");
        JMenu viewMenu = new JMenu("Visualizza");

        //sezione file

        JMenuItem exportDB = new JMenuItem("Esporta Database");
        exportDB.addActionListener(e -> esportaDatabase());

        JMenuItem importDB = new JMenuItem("Importa Database");
        importDB.addActionListener(e -> importaDatabase());

        JMenuItem exit = new JMenuItem("Esci");
        exit.addActionListener(e -> {
            DatabaseConnectionSingleton.closeConnection();
            System.exit(0);

        });

        //sezione edit
        undo = new JMenuItem("Annulla");
        undo.addActionListener(e -> {
            db.getHistoryManager().annulla();
        });

        redo = new JMenuItem("Ripeti");
        redo.addActionListener(e -> {
            db.getHistoryManager().ripeti();
        });

        //sezione view
        JMenuItem sortByTitleAsc = new JMenuItem("Ordina per Titolo (A-Z)");
        sortByTitleAsc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.TITOLO_ASC;
            refreshBookListView();
        });
        viewMenu.add(sortByTitleAsc);


        JMenuItem sortByTitleDesc = new JMenuItem("Ordina per Titolo (Z-A)");
        sortByTitleDesc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.TITOLO_DESC;
            refreshBookListView();
        });
        viewMenu.add(sortByTitleDesc);
        viewMenu.addSeparator();

        JMenuItem sortByAuthorAsc = new JMenuItem("Ordina per Autore (A-Z)");
        sortByAuthorAsc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.AUTORE_ASC;
            refreshBookListView();
        });
        viewMenu.add(sortByAuthorAsc);

        JMenuItem sortByAuthorDesc = new JMenuItem("Ordina per Autore (Z-A)");
        sortByAuthorDesc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.AUTORE_DESC;
            refreshBookListView();
        });
        viewMenu.add(sortByAuthorDesc);
        viewMenu.addSeparator();

        JMenuItem sortByRatingAsc = new JMenuItem("?Ordina per Rating (Crescente)");
        sortByRatingAsc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.VALUTAZIONE_ASC;
            refreshBookListView();
        });
        viewMenu.add(sortByRatingAsc);

        JMenuItem sortByRatingDesc = new JMenuItem("?Ordina per Rating (Decrescente)");
        sortByRatingDesc.addActionListener(e -> {
            this.currentSortCriteria = CriterioOrdinamento.VALUTAZIONE_DESC;
            refreshBookListView();
        });
        viewMenu.add(sortByRatingDesc);



        fileMenu.add(exportDB);
        fileMenu.add(importDB);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        editMenu.add(undo);
        editMenu.add(redo);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    /**
     * Ritorna il metodo di ordinamneto scelto dall'utente
     * @return il criterio di ordinamento corrente
     */
    public CriterioOrdinamento getCriterioOrdinamentoCorrente() {
        return currentSortCriteria;
    }

    /**
     * metodo per aggiornare la listView quando viene cambiato il criterio di  ordinamento
     * @pre {@code db != null}
     * @post vengono notificati tutti gli observer
     */
    private void refreshBookListView(){
        if (db != null){
            db.notificaOsservatori();
        }
    }

    /**
     * Aggiorna lo stato abilitato/disabilitato delle voci di menu undo e redo in base alla possibilit? di eseguire tali operazioni.
     *
     * @post Le voci di menu undo e redo sono abilitate solo se le rispettive operazioni sono disponibili.
     */
    public void updateUndoRedoMenuState() {
        if (db != null && db.getHistoryManager() != null) {
            undo.setEnabled(db.getHistoryManager().puoAnnullare());
            redo.setEnabled(db.getHistoryManager().puoRipetere());
        }
    }

    private void esportaDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Esporta Database");
        fileChooser.setSelectedFile(new File("books_backup.db"));

        int userSelection = fileChooser.showOpenDialog(this);

        if(userSelection == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            try{
                DatabaseConnectionSingleton.closeConnection();
                Files.copy(new File("Books_db.db").toPath(), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Database esportato con successo.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Errore nell'esportazione del database.");
            }finally {
                try {
                    DatabaseConnectionSingleton.getInstance();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                PannelloLibriUI.displayBooks(db.getTuttiLibri(this.currentSortCriteria));
            }
        }
    }

    private void importaDatabase(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importa Database");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQLite Database Files (*.db)", "db"));

        int userSelection = fileChooser.showOpenDialog(this);

        if(userSelection == JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(this, "L'importazione sovrascriver? il database esistente, sei sicuro di voler continuare?","Conferma Importazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if(confirm == JOptionPane.YES_OPTION){
                try{
                    DatabaseConnectionSingleton.closeConnection();
                    Files.copy(selectedFile.toPath(), new File("Books_db.db").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(this, "Database importato con successo.");

                    DatabaseConnectionSingleton.getInstance();

                    db = new GestoreLibroConcreto(new RepositoryLibroSQLite());
                    OsservatoreLibro.unsubscribe();
                    OsservatoreLibro = new OsservatoreLibroConcreto(this,this.PannelloLibriUI,this.db);

                    PannelloLibriUI.displayBooks(db.getTuttiLibri(this.currentSortCriteria));
                }catch (IOException | SQLException e){
                    JOptionPane.showMessageDialog(this, "Errore durante l'importazione del database: " + e.getMessage(), "Errore Importazione", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    private JPanel inizializzaSezioneDX(){
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Creo un pannello superiore con layout orizzontale: barra di ricerca a sinistra (50%) e filtri a destra (50%)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Pannello per il logo e bottone aggiungi libro
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 20)); // Aggiungo margine destro di 20px
        
        // Logo centrato
        JLabel logoLabel = createStyledLogo();
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.add(logoLabel);
        
        // Bottone aggiungi libro a destra con margine
        JButton addBookBtn = addBookButton();
        
        titlePanel.add(centerPanel, BorderLayout.CENTER);
        titlePanel.add(addBookBtn, BorderLayout.EAST);
        
        // Pannello orizzontale che contiene ricerca (sinistra) e filtri (destra)
        JPanel horizontalPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 riga, 2 colonne con gap di 10px
        horizontalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // PARTE SINISTRA: Barra di ricerca (50% larghezza)
        JPanel searchSection = new JPanel(new BorderLayout());
        searchSection.setBorder(BorderFactory.createTitledBorder("Ricerca"));
        
        JPanel searchBarPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(); // Rimosso il placeholder "Search"
        searchField.putClientProperty("JTextField.roundRect", true);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.BLACK);
        searchField.setCaretColor(Color.BLACK);
        searchField.setPreferredSize(new Dimension(120, 30));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        String[] searchCriteria = {"Titolo", "Autore", "ISBN", "Genere"};
        JComboBox<String> searchCriteriaCombo = new JComboBox<>(searchCriteria);
        searchCriteriaCombo.setPreferredSize(new Dimension(100, 30));
        searchCriteriaCombo.putClientProperty("JComboBox.is=roundReact",true);
        searchCriteriaCombo.setBackground(new Color(220, 220, 220));
        searchCriteriaCombo.setForeground(Color.BLACK);

        searchBarPanel.add(searchField, BorderLayout.CENTER);
        searchBarPanel.add(searchCriteriaCombo, BorderLayout.EAST);
        searchSection.add(searchBarPanel, BorderLayout.CENTER);
        
        // PARTE DESTRA: Filtri (50% larghezza)
        JPanel filtersSection = createFiltersSection();
        
        // Aggiungo le due sezioni al pannello orizzontale
        horizontalPanel.add(searchSection);
        horizontalPanel.add(filtersSection);
        
        // Layout finale: titolo in alto, poi pannello orizzontale con ricerca e filtri
        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(horizontalPanel, BorderLayout.CENTER);
        
        rightPanel.add(topPanel, BorderLayout.NORTH);

        // Listener per la ricerca
        searchField.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            String criterion = searchCriteriaCombo.getSelectedItem().toString();
            
            if (!searchText.isEmpty()) {
                List<Libro> searchResults;
                switch (criterion) {
                    case "Titolo":
                        searchResults = db.trovaLibroPerTitolo(searchText, this.currentSortCriteria);
                        break;
                    case "Autore":
                        searchResults = db.trovaLibroPerAutore(searchText, this.currentSortCriteria);
                        break;
                    case "ISBN":
                        searchResults = db.trovaLibroPerIsbn(searchText, this.currentSortCriteria);
                        break;
                    case "Genere":
                        searchResults = db.trovaLibroPerGenere(searchText, this.currentSortCriteria);
                        break;
                    default:
                        searchResults = db.getTuttiLibri(this.currentSortCriteria);
                        break;
                }
                PannelloLibriUI.displayBooks(searchResults);
            } else {
                PannelloLibriUI.displayBooks(db.getTuttiLibri(this.currentSortCriteria));
            }
        });
        return rightPanel;
    }

    /**
     * Crea la sezione dei filtri per la parte destra del layout orizzontale
     * (All, Letti, In lettura, Da leggere e stelle di valutazione)
     */
    private JPanel createFiltersSection() {
        JPanel filtersSection = new JPanel(new BorderLayout());
        filtersSection.setBorder(BorderFactory.createTitledBorder("Filtri"));
        
        // Pannello per i pulsanti di stato (senza "Tutti")
        JPanel StatoBottoni = new JPanel();
        StatoBottoni.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton lettiBtn = new JButton("Letti");
        JButton inLetturaBtn = new JButton("In lettura");
        JButton daLeggereBtn = new JButton("Da leggere");

        Dimension buttonSize = new Dimension(80, 25); // Bottoni pi? piccoli per adattarsi al 50%
        Color selectedColor = new Color(180, 180, 180); // Grigio scuro per selezione
        Color defaultColor = new Color(240, 240, 240); // Grigio chiaro per default

        List<JButton> stateButtons = new ArrayList<>();
        stateButtons.add(lettiBtn);
        stateButtons.add(inLetturaBtn);
        stateButtons.add(daLeggereBtn);

        for (JButton btn : stateButtons) {
            btn.setPreferredSize(buttonSize);
            btn.setMinimumSize(buttonSize);
            btn.setMaximumSize(buttonSize);
            btn.setBackground(defaultColor);
            btn.setForeground(Color.BLACK);
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setMargin(new Insets(0, 3, 0, 3));
            StatoBottoni.add(btn);
        }

        lettiBtn.addActionListener(e -> {
            PannelloLibriUI.displayBooks(db.filtraLibroPerStatoLettura("LETTO", this.currentSortCriteria));
            highlightButton(lettiBtn, stateButtons, selectedColor, defaultColor);
        });

        inLetturaBtn.addActionListener(e -> {
            PannelloLibriUI.displayBooks(db.filtraLibroPerStatoLettura("IN LETTURA", this.currentSortCriteria));
            highlightButton(inLetturaBtn, stateButtons, selectedColor, defaultColor);
        });

        daLeggereBtn.addActionListener(e -> {
            PannelloLibriUI.displayBooks(db.filtraLibroPerStatoLettura("DA LEGGERE", this.currentSortCriteria));
            highlightButton(daLeggereBtn, stateButtons, selectedColor, defaultColor);
        });

        // Pannello centrale per le stelle di valutazione
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        
        for (int i = 1; i <= 5; i++) {
            JButton starButton = new JButton("★");
            starButton.setFont(starButton.getFont().deriveFont(14f)); // Font pi? piccolo
            starButton.setForeground(Color.ORANGE);
            starButton.setBackground(new Color(240, 240, 240));
            starButton.setFocusPainted(false);
            starButton.setBorderPainted(false);
            starButton.setOpaque(true);
            starButton.setMargin(new Insets(0, 0, 0, 0));
            starButton.setPreferredSize(new Dimension(20, 20)); // Stelle pi? piccole

            final int currentRating = i;
            starButton.addActionListener(e -> {
                PannelloLibriUI.displayBooks(db.filtraLibroPerValutazione(currentRating, this.currentSortCriteria));
                highlightButton(starButton, stateButtons, selectedColor, defaultColor);
            });
            starsPanel.add(starButton);
            stateButtons.add(starButton);
        }
        
        // Pannello per il bottone "Tutti" a destra
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton AllBtn = new JButton("Tutti");
        AllBtn.setPreferredSize(buttonSize);
        AllBtn.setMinimumSize(buttonSize);
        AllBtn.setMaximumSize(buttonSize);
        AllBtn.setBackground(defaultColor);
        AllBtn.setForeground(Color.BLACK);
        AllBtn.setFocusPainted(false);
        AllBtn.setOpaque(true);
        AllBtn.setBorderPainted(false);
        AllBtn.setHorizontalAlignment(SwingConstants.CENTER);
        AllBtn.setMargin(new Insets(0, 3, 0, 3));
        rightPanel.add(AllBtn);
        stateButtons.add(AllBtn);

        AllBtn.addActionListener(e -> {
            PannelloLibriUI.displayBooks(db.getTuttiLibri(this.currentSortCriteria));
            highlightButton(AllBtn, stateButtons, selectedColor, defaultColor);
        });

        // Imposta il bottone "Tutti" come selezionato di default
        highlightButton(AllBtn, stateButtons, selectedColor, defaultColor);

        // Layout: bottoni di stato a sinistra, stelle al centro, "Tutti" a destra
        JPanel filtersContent = new JPanel(new BorderLayout());
        filtersContent.add(StatoBottoni, BorderLayout.WEST);
        filtersContent.add(starsPanel, BorderLayout.CENTER);
        filtersContent.add(rightPanel, BorderLayout.EAST);
        
        filtersSection.add(filtersContent, BorderLayout.CENTER);
        
        return filtersSection;
    }

    private void highlightButton(JButton selectedButton, List<JButton> buttons, Color selectedColor, Color defaultColor) {
        for (JButton btn : buttons) {
            btn.setBackground(defaultColor);
        }
        selectedButton.setBackground(selectedColor);
    }

    /**
     * Crea un logo stilizzato per l'applicazione con tema VS Code Light
     */
    private JLabel createStyledLogo() {
        // Logo semplice e funzionale
        JLabel logoLabel = new JLabel("Gestore Libreria");
        
        // Stile del logo - VS Code Light
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE); // Testo bianco per contrasto con sfondo blu
        
        // Sfondo blu VS Code
        logoLabel.setOpaque(true);
        Color blueVSCode = new Color(0, 122, 204); // Blu caratteristico di VS Code
        logoLabel.setBackground(blueVSCode);
        
        // Bordo arrotondato con colore VS Code
        logoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(blueVSCode, 2, true), // Bordo blu VS Code
            BorderFactory.createEmptyBorder(8, 15, 8, 15) // Padding interno
        ));
        
        // Allineamento centrale
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        return logoLabel;
    }

    private JButton addBookButton(){
        JButton addBookBtn = new JButton("+ Aggiungi libro");
        
        // Stile VS Code per il bottone
        addBookBtn.setPreferredSize(new Dimension(140, 28)); // Leggermente più grande
        addBookBtn.setBackground(new Color(0, 122, 204)); // Blu VS Code
        addBookBtn.setForeground(new Color(255, 255, 255)); // Testo bianco
        addBookBtn.setFocusPainted(false);
        addBookBtn.setBorderPainted(true);
        addBookBtn.setOpaque(true);
        // Bordo sottile per effetto moderno
        addBookBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 180), 1));
        
        // Effetto hover (opzionale)
        addBookBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addBookBtn.setBackground(new Color(28, 151, 234)); // Blu più chiaro al hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addBookBtn.setBackground(new Color(0, 122, 204)); // Ritorna al colore originale
            }
        });

        //logica per il pannello per aggiungere un libro
        addBookBtn.addActionListener(e -> {
            JTextField titoloField = new JTextField(20);
            JTextField autoreField = new JTextField(20);
            JTextField isbnField = new JTextField(20);
            JTextField genreField = new JTextField(20);
            JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            JComboBox<String> statoCombo = new JComboBox<>(new String[]{"letto", "in lettura", "da leggere"});
            JTextField imagePathField = new JTextField(15);
            imagePathField.setEditable(false);
            JButton browseBtn = new JButton("Sfoglia");

            //Immagine di copertina
            JLabel imagePreview = new JLabel();
            int width = 120;
            int height = 180;
            imagePreview.setPreferredSize(new Dimension(width, height));

            ImageIcon placeholder = loadPlaceholderImage(width, height);
            if (placeholder != null) {
                imagePreview.setIcon(placeholder);
            } else {
                imagePreview.setText("Immagine non disponibile");
            }


            JPanel imageWrapper = new JPanel();
            imageWrapper.setLayout(new BorderLayout());
            imageWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
            imageWrapper.add(imagePreview, BorderLayout.CENTER);


            //comportamento tasto sfoglia
            browseButtonAction(browseBtn, imagePathField, imagePreview, width, height);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Titolo*:"));
            panel.add(titoloField);
            panel.add(new JLabel("Autore*:"));
            panel.add(autoreField);
            panel.add(new JLabel("Genere:"));
            panel.add(genreField);
            panel.add(new JLabel("ISBN:"));
            panel.add(isbnField);
            panel.add(new JLabel("Valutazione (1-5):"));
            panel.add(ratingSpinner);
            panel.add(new JLabel("Stato lettura:"));
            panel.add(statoCombo);
            panel.add(new JLabel("Copertina:"));
            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filePanel.add(imagePathField);
            filePanel.add(browseBtn);
            panel.add(filePanel);

            // Layout generale
            JPanel BookPanel = new JPanel(new BorderLayout());
            BookPanel.add(panel, BorderLayout.CENTER);
            BookPanel.add(imageWrapper, BorderLayout.NORTH);
            imageWrapper.setLayout(new FlowLayout(FlowLayout.CENTER));


            String[] addOptions = {"OK", "Annulla"};
            int result = JOptionPane.showOptionDialog(null, BookPanel, "Nuovo libro", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, addOptions, addOptions[0]);
            if (result == 0) {
                String titolo = titoloField.getText().trim();
                String autore = autoreField.getText().trim();
                String isbn = isbnField.getText().trim();
                String genre = genreField.getText().trim();
                int rating = (Integer) ratingSpinner.getValue();
                String stato = (String) statoCombo.getSelectedItem();
                String path = "/images/segnaposto_immagine.png";
                if(!imagePathField.getText().trim().isEmpty()){
                    path = imagePathField.getText().trim();
                }

                if (!titolo.isEmpty() && !autore.isEmpty()) {
                    Libro nuovoLibro = new Libro.Costruttore(titolo, autore).isbn(isbn)
                            .valutazione(rating)
                            .statoLettura(stato)
                            .percorsoCopertina(path)
                            .genere(genre)
                            .build();
                    System.out.println("Creato libro: " + nuovoLibro.toString());
                    db.aggiungiLibro(nuovoLibro);
                } else {
                    JOptionPane.showMessageDialog(null, "Titolo e autore sono obbligatori.");
                }
            }
        });
        return addBookBtn;
    }

    public static ImageIcon loadPlaceholderImage(int width, int height) {
        try {
            java.io.InputStream imageStream = GestoreLibreriaUI.class.getResourceAsStream("/images/segnaposto_immagine.png");
            if (imageStream != null) {
                ImageIcon icon = new ImageIcon(javax.imageio.ImageIO.read(imageStream));
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } else {
                System.err.println("Immagine placeholder non trovata nel JAR");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dell'immagine placeholder: " + e.getMessage());
            return null;
        }
    }

    private void browseButtonAction(JButton browseBtn, JTextField imagePathField, JLabel imagePreview, int width, int height){
        browseBtn.addActionListener(ev -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());

                ImageIcon selectedIcon = loadAndScaleImage(selectedFile.getAbsolutePath(), width, height);
                if (selectedIcon != null) {
                    imagePreview.setIcon(selectedIcon); // sostituisce il placeholder
                } else {
                    JOptionPane.showMessageDialog(null, "Impossibile caricare l'immagine.");
                }
            }
        });
    }

    public static ImageIcon loadAndScaleImage(String path, int width, int height) {
        try {
            // Se ? un percorso delle risorse, carica dal JAR
            if (path != null && (path.startsWith("/images/") || path.equals("/images/segnaposto_immagine.png"))) {
                java.io.InputStream imageStream = GestoreLibreriaUI.class.getResourceAsStream(path);
                if (imageStream != null) {
                    ImageIcon icon = new ImageIcon(javax.imageio.ImageIO.read(imageStream));
                    Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }

            // Altrimenti, carica come file esterno
            if (path != null && !path.isEmpty()) {
                ImageIcon icon = new ImageIcon(path);
                if (icon.getIconWidth() > 0) {
                    Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Errore nel caricamento immagine da: " + path + ". " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        try{
            // Imposta un tema chiaro moderno simile a VS Code Light
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Colori personalizzati per un look VS Code Light con sfondo grigio
            Color lightGrayBackground = new Color(245, 245, 245);     // Sfondo grigio molto chiaro
            Color grayBackground = new Color(240, 240, 240);          // Sfondo grigio principale
            Color graySurface = new Color(250, 250, 250);            // Superfici componenti (quasi bianco)
            Color grayBorder = new Color(220, 220, 220);             // Bordi grigi chiari
            Color blueAccent = new Color(0, 122, 204);               // Blu accent VS Code (invariato)
            Color textPrimary = new Color(51, 51, 51);               // Testo scuro per buon contrasto
            
            // Sfondi principali - Tema grigio chiaro
            UIManager.put("Panel.background", grayBackground);
            UIManager.put("Button.background", grayBorder);
            UIManager.put("TextField.background", graySurface);
            UIManager.put("TextArea.background", graySurface);
            UIManager.put("List.background", graySurface);
            UIManager.put("Table.background", graySurface);
            UIManager.put("ScrollPane.background", grayBackground);
            UIManager.put("MenuBar.background", lightGrayBackground);
            UIManager.put("Menu.background", lightGrayBackground);
            UIManager.put("TabbedPane.background", grayBackground);
            
            // Componenti aggiuntivi - Tema grigio
            UIManager.put("Tree.background", graySurface);
            UIManager.put("SplitPane.background", grayBackground);
            UIManager.put("ScrollPane.viewport.background", grayBackground);
            UIManager.put("Viewport.background", grayBackground);
            UIManager.put("ToolBar.background", lightGrayBackground);
            UIManager.put("Label.background", grayBackground);
            UIManager.put("CheckBox.background", grayBackground);
            UIManager.put("RadioButton.background", grayBackground);
            UIManager.put("ComboBox.background", graySurface);
            UIManager.put("Spinner.background", graySurface);
            UIManager.put("ProgressBar.background", grayBackground);
            UIManager.put("Slider.background", grayBackground);
            UIManager.put("PopupMenu.background", graySurface);
            UIManager.put("MenuItem.background", graySurface);
            UIManager.put("OptionPane.background", grayBackground);
            UIManager.put("Dialog.background", grayBackground);
            
            // Testi - Colori scuri per buon contrasto su sfondo chiaro
            UIManager.put("Label.foreground", textPrimary);
            UIManager.put("Button.foreground", textPrimary);
            UIManager.put("TextField.foreground", textPrimary);
            UIManager.put("TextArea.foreground", textPrimary);
            UIManager.put("List.foreground", textPrimary);
            UIManager.put("Table.foreground", textPrimary);
            UIManager.put("Tree.foreground", textPrimary);
            UIManager.put("Menu.foreground", textPrimary);
            UIManager.put("MenuItem.foreground", textPrimary);
            UIManager.put("ComboBox.foreground", textPrimary);
            
            // Selezioni e accenti - Blu VS Code
            UIManager.put("List.selectionBackground", blueAccent);
            UIManager.put("Table.selectionBackground", blueAccent);
            UIManager.put("Tree.selectionBackground", blueAccent);
            UIManager.put("Button.select", new Color(230, 230, 230));
            UIManager.put("TextField.selectionBackground", blueAccent);
            UIManager.put("TextArea.selectionBackground", blueAccent);
            
            // Bordi grigi chiari
            UIManager.put("Component.borderColor", grayBorder);
            UIManager.put("TextField.borderColor", grayBorder);
            UIManager.put("Button.borderColor", grayBorder);
            
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Impossibile caricare FlatLaf");
        }
        ImplementatoreRepositoryLibro repo = new RepositoryLibroSQLite();
        GestoreLibroConcreto db = new GestoreLibroConcreto(repo);
        SwingUtilities.invokeLater(() -> {
            GestoreLibreriaUI UI = new GestoreLibreriaUI(db);
            UI.setVisible(true);
        });
    }

}

