package gestore_libreria.observer;

import gestore_libreria.db.GestoreLibro;
import gestore_libreria.db.ImplementatoreRepositoryLibro;
import gestore_libreria.db.GestoreLibroConcreto;
import gestore_libreria.db.RepositoryLibroSQLite;
import gestore_libreria.model.Libro;
import gestore_libreria.ui.PannelloLibriUI;
import gestore_libreria.ui.GestoreLibreriaUI;

import java.util.List;

/**
 * Implementazione concreta dell'interfaccia OsservatoreLibro.
 * Questo osservatore è responsabile dell'aggiornamento dell'interfaccia utente (UI)
 * quando vengono apportate modifiche allo stato del database.
 */
public class OsservatoreLibroConcreto implements OsservatoreLibro{

    //qui ho un'istanza del GestoreLibroConcreto
    private PannelloLibriUI bookPanel;
    private GestoreLibreriaUI gestoreLibreriaUI;
    private GestoreLibroConcreto db;

    /**
     * Costruisce una nuova istanza di OsservatoreLibroConcreto e richiama il metodo {@code aggiorna()}.
     *
     * @param gestoreLibreriaUI l'interfaccia utente principale dove vengono visualizzati i criteri di ricerca e il {@code bookPanel}
     * @param bookPanel Il pannello dell'interfaccia utente che questo osservatore deve aggiornare.
     * @param db Istanza del database utilizzato
     * @pre gestoreLibreriaUI non deve essere null
     * @pre booksPanel non deve essere null.
     * @post L'osservatore è inizializzato con un riferimento al pannello UI.
     * @throws IllegalArgumentException se PannelloLibriUI è null.
     */
    public OsservatoreLibroConcreto(GestoreLibreriaUI gestoreLibreriaUI, PannelloLibriUI bookPanel, GestoreLibroConcreto db) {
        this.bookPanel = bookPanel;
        this.db = db;
        this.gestoreLibreriaUI = gestoreLibreriaUI;
        this.db.aggiungi(this);
        aggiorna();
    }

    /**
     * Aggiorna la vista dell'interfaccia utente recuperando tutti i libri dal database
     * e visualizzandoli, quindi aggiorna lo stato dei menu Undo/Redo.
     * Questo metodo viene invocato quando l'oggetto osservabile (es. GestoreLibro)
     * notifica un cambiamento.
     *
     * @pre Il manager dei libri (db) e il pannello dei libri (bookPanel) devono essere stati inizializzati correttamente.
     * @post La lista completa dei libri è stata recuperata dal database.
     * @post Il metodo displayBooks del bookPanel è stato invocato con la lista aggiornata dei libri,
     * causando il refresh dell'interfaccia utente.
     * @post Lo stato dei menu Undo/Redo è stato aggiornato per riflettere la disponibilità di operazioni.
     */
    @Override
    public void aggiorna() {
        System.out.println("Aggiorno la bookView");
        List<Libro> books = db.getTuttiLibri(gestoreLibreriaUI.getCriterioOrdinamentoCorrente());
        bookPanel.displayBooks(books);
        gestoreLibreriaUI.updateUndoRedoMenuState();
    }

    /**
     * Rimuove questo osservatore dalla lista degli osservatori dell'oggetto osservabile (db). Attualmente eseguito
     * alla chiusura del programma
     *
     * @pre L'oggetto osservabile (db) non deve essere null.
     * @pre Questo osservatore deve essere precedentemente stato "sottoscritto" (attached) al db.
     * @post Se db non è null, questo osservatore è stato rimosso dalla sua lista di osservatori.
     * @post L'osservatore non riceverà più notifiche da db.
     */
    public void unsubscribe() {
        if(this.db != null) {
            this.db.rimuovi(this);
            System.out.println("Unsubscribed");
        }
    }
}
