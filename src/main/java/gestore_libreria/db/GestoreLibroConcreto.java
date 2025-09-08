package gestore_libreria.db;

import gestore_libreria.memento.CustodeCronologiaLibro;
import gestore_libreria.memento.MementoLibro;
import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;
import gestore_libreria.observer.Soggetto;

import java.util.List;

// Questa classe ora implementa l'interfaccia GestoreLibro.
public class GestoreLibroConcreto extends Soggetto implements GestoreLibro, CustodeCronologiaLibro.OnMementoListener  {

    private final ImplementatoreRepositoryLibro repository;
    private final CustodeCronologiaLibro historyManager;

    /**
     * Costruisce una nuova istanza di GestoreLibroConcreto.
     *
     * @param repository L'implementazione del repository per la gestione dei dati dei libri.
     * @pre repository non deve essere null.
     * @post L'oggetto GestoreLibroConcreto è stato inizializzato.
     * @post Il repository interno è stato impostato con l'istanza fornita.
     * @post Viene creata una nuova istanza di BookHistoryManager.
     * @post L'istanza corrente di GestoreLibroConcreto è registrata come listener per il ripristino dei memento nella cronologia.
     */
    public GestoreLibroConcreto(ImplementatoreRepositoryLibro repository) {
        this.repository = repository;
        this.historyManager = new CustodeCronologiaLibro();
        this.historyManager.setOnMementoRestoreListener(this);
    }

    /**
     * Restituisce l'istanza del gestore della cronologia (BookHistoryManager) associata a questo manager.
     *
     * @pre Nessuna pre-condizione specifica. Il manager deve essere stato inizializzato (il costruttore deve essere stato chiamato).
     * @post Restituisce un'istanza non null di BookHistoryManager.
     * @return L'istanza di {@code BookHistoryManager}.
     */
    public CustodeCronologiaLibro getHistoryManager() {
        return historyManager;
    }

    /**
     * Consente l'inserimento di un libro nel database
     *
     * @param Libro Il libro da aggiungere.
     * @pre Libro non deve essere null.
     * @pre Libro.getTitolo() non deve essere null o vuoto.
     * @pre Libro.getAutore() non deve essere null o vuoto.
     * @post Il libro è stato correttamente inserito nel database con codice ID univoco.
     * @post Dopo l'inserimento vengono notificati gli Observer.
     * @post Dopo l'inserimento si salva il libro nello stack hystoryManager.
     */
    @Override
    public void aggiungiLibro(Libro Libro) {
        repository.save(Libro);
        historyManager.save(new MementoLibro(Libro, MementoLibro.OperationType.ADD));
        super.notificaOsservatori();
    }

    /**
     * @pre il database deve essere in uno stato consistente.
     * @post Restituisce una lista non null di oggetti Libro contenuti nel database.
     * @post Restituisce una lista vuota se il database non contiene nessun oggetto Libro.
     * @return Una {@code List<Libro>} contenente i libri contenuti nel database.
     */
    @Override
    public List<Libro> getTuttiLibri(CriterioOrdinamento criteria) {
        return repository.loadAll(criteria);
    }

    /**
     * Permette di trovare i libri che all'interno del titolo contengono la stringa specificata
     *
     * @param title Stringa da cercare nel titolo dei libri.
     * @pre title non deve essere null.
     * @post Restituisce una lista non null di oggetti Libro che corrispondono al criterio di ricerca.
     * @post Se nessun libro rispetto il criterio viene restituita una lista vuota.
     * @return Una {@code List<Libro>} contenente i libri trovati.
     */
    @Override
    public List<Libro> trovaLibroPerTitolo(String title, CriterioOrdinamento criteria) {
        return repository.findByTitle(title, criteria);
    }

    /**
     * Permette di trovare i libri in base alla valutazione specificata
     *
     * @param rating La valutazione come parametro di ricerca (int)
     * @pre rating deve essere un valore intero compreso da 1 a 5
     * @post restituisce una lista non null di oggetti Libro che corrispondono al criterio di ricerca
     * @post Se nessun libro rispetto il criterio viene restituita una lista vuota
     * @return Una {@code List<Libro>} contenente i libri trovati
     */
    @Override
    public List<Libro> filtraLibroPerValutazione(int rating, CriterioOrdinamento criteria) {
        return repository.findByRating(rating, criteria);
    }

    /**
     *Filtra i libri in base allo stato di lettura specificato.
     *
     * @param readingState lo stato di lettura da filtrare (es. "LETTO", "IN LETTURA", "DA LEGGERE")
     * @pre readingState non deve essere null o vuoto
     * @pre readingState deve corrispondere obbligatoriamente a uno degli stati di lettura predefiniti ("LETTO", "IN LETTURA", "DA LEGGERE")
     * @post restituisce una lista non null di oggetti Libro che hanno lo stato di lettura specificato
     * @post Se nessun libro rispetto il criterio viene restituita una lista vuota
     * @return Una {@code List<Libro>} contenente i libri con lo stato di lettura specificato.
     */
    @Override
    public List<Libro> filtraLibroPerStatoLettura(String readingState, CriterioOrdinamento criteria) {
        return repository.findByReadingState(readingState, criteria);
    }

    /**
     * Trova i libri il cui autore contiene la stringa specificata (case-insensitive).
     *
     * @param author La stringa da cercare nel nome dell'autore dei libri.
     * @pre author non deve essere null.
     * @post Restituisce una lista non null di oggetti Libro che corrispondono al criterio di ricerca.
     * @post Se nessun libro corrisponde al criterio, la lista restituita è vuota.
     * @return Una {@code List<Libro>} contenente i libri trovati.
     */
    @Override
    public List<Libro> trovaLibroPerAutore(String author, CriterioOrdinamento criteria) {
        return repository.findByAuthor(author, criteria);
    }

    /**
     * Trova i libri il cui ISBN contiene la stringa specificata (case-insensitive).
     *
     * @param isbn La stringa da cercare nell'ISBN dei libri.
     * @pre isbn non deve essere null.
     * @post Restituisce una lista non null di oggetti Libro che corrispondono al criterio di ricerca.
     * @post Se nessun libro corrisponde al criterio, la lista restituita è vuota.
     * @return Una {@code List<Libro>} contenente i libri trovati.
     */
    @Override
    public List<Libro> trovaLibroPerIsbn(String isbn, CriterioOrdinamento criteria) {
        return repository.findByIsbn(isbn, criteria);
    }

    /**
     * Trova i libri il cui genere contiene la stringa specificata (case-insensitive).
     *
     * @param genre La stringa da cercare nel genere dei libri.
     * @pre genre non deve essere null.
     * @post Restituisce una lista non null di oggetti Libro che corrispondono al criterio di ricerca.
     * @post Se nessun libro corrisponde al criterio, la lista restituita è vuota.
     * @return Una {@code List<Libro>} contenente i libri trovati.
     */
    @Override
    public List<Libro> trovaLibroPerGenere(String genre, CriterioOrdinamento criteria) {
        return repository.findByGenre(genre, criteria);
    }

    /**
     * Aggiorna un libro esistente nel database e ne salva lo stato per le operazioni di undo/redo.
     *
     * @param oldBook Lo stato precedente del libro prima dell'aggiornamento. Usato per l'undo.
     * @param Libro Il libro con i dati aggiornati.
     * @pre oldBook non deve essere null.
     * @pre newBook non deve essere null.
     * @pre newBook.getId() deve corrispondere all'ID di un libro esistente nel database.
     * @pre newBook.getTitolo() non deve essere null o vuoto.
     * @pre newBook.getAutore() non deve essere null o vuoto.
     * @post Lo stato del libro nel database è aggiornato con i dati di {@code newBook}.
     * @post Un memento di tipo UPDATE è salvato nella cronologia, contenente sia {@code newBook} che {@code oldBook}.
     * @post Tutti gli osservatori sono notificati del cambiamento.
     *
     */
    @Override
    public void aggiornaLibro(Libro oldBook, Libro Libro) {
        historyManager.save(new MementoLibro(Libro, MementoLibro.OperationType.UPDATE, oldBook));
        repository.aggiorna(Libro);
        super.notificaOsservatori();
    }

    /**
     * Elimina un libro dal database e ne salva lo stato per le operazioni di undo/redo.
     *
     * @param Libro Il libro da eliminare.
     * @pre Libro non deve essere null.
     * @pre Libro.getId() deve corrispondere all'ID di un libro esistente nel database.
     * @post Il libro è rimosso dal database.
     * @post Un memento di tipo REMOVE è salvato nella cronologia, contenente lo stato del libro prima della rimozione.
     * @post Tutti gli osservatori sono notificati del cambiamento.
     */
    @Override
    public void eliminaLibro(Libro Libro) {
        historyManager.save(new MementoLibro(Libro, MementoLibro.OperationType.REMOVE));
        repository.delete(Libro);
        super.notificaOsservatori();
    }

/**
 * Ripristina lo stato di un'operazione del libro in base al memento e alla direzione specificata.
 * Questo metodo gestisce le operazioni di Undo e Redo per aggiunte, rimozioni e aggiornamenti.
 *
 * @param memento Il memento che contiene lo stato del libro e il tipo di operazione.
 * @param direction La direzione dell'azione (UNDO o REDO).
 * @pre memento non deve essere null.
 * @pre memento.getBookState() non deve essere null per le operazioni ADD e REMOVE.
 * @pre memento.getPreviousBookState() non deve essere null per l'operazione UPDATE in caso di UNDO.
 * @post Il database viene modificato per riflettere lo stato del libro come specificato dal memento e dalla direzione.
 * @post Se l'operazione è un UNDO di ADD, il libro viene rimosso.
 * @post Se l'operazione è un REDO di ADD, il libro viene riaggiunto.
 * @post Se l'operazione è un UNDO di REMOVE, il libro viene riaggiunto.
 * @post Se l'operazione è un REDO di REMOVE, il libro viene rimosso.
 * @post Se l'operazione è un UNDO di UPDATE, il libro viene ripristinato allo stato precedente.
 * @post Se l'operazione è un REDO di UPDATE, il libro viene ripristinato allo stato successivo.
 * @post Tutti gli osservatori sono notificati del cambiamento dopo il ripristino.
 */
    @Override
    public void restore(MementoLibro memento, CustodeCronologiaLibro.ActionDirection direction) {
        switch (memento.getOperationType()) {
            case ADD:
                if (direction == CustodeCronologiaLibro.ActionDirection.UNDO) {
                    repository.delete(memento.getBookState());
                    System.out.println("Undo ADD: Rimosso libro " + memento.getBookState().getTitolo());
                } else {
                    repository.save(memento.getBookState());
                    System.out.println("Redo ADD: Riaggiunto libro " + memento.getBookState().getTitolo());
                }
                break;
            case REMOVE:
                if (direction == CustodeCronologiaLibro.ActionDirection.UNDO) {
                    repository.save(memento.getBookState());
                    System.out.println("Undo DELETE: Riaggiunto libro " + memento.getBookState().getTitolo());
                } else {
                    repository.delete(memento.getBookState());
                    System.out.println("Redo DELETE: Rimosso libro " + memento.getBookState().getTitolo());
                }
                break;
            case UPDATE:
                if (direction == CustodeCronologiaLibro.ActionDirection.UNDO) {
                    repository.aggiorna(memento.getPreviousBookState());
                    System.out.println("Undo UPDATE: Ripristinato libro " + memento.getPreviousBookState().getTitolo() + " allo stato precedente.");
                } else {
                    repository.aggiorna(memento.getBookState());
                    System.out.println("Redo UPDATE: Ripristinato libro " + memento.getBookState().getTitolo() + " allo stato successivo.");
                }
                break;
        }
        super.notificaOsservatori(); // Notifica la UI dopo il ripristino
    }
}
