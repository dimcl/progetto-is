package gestore_libreria.observer;

import gestore_libreria.db.GestoreLibroConcreto;
import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;
import gestore_libreria.ui.PannelloLibriUI;
import gestore_libreria.ui.GestoreLibreriaUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OsservatoreLibroConcretoTest {

    static class TestGestoreLibreriaUI extends GestoreLibreriaUI {
        public int updateUndoRedoCallCount = 0;

        public TestGestoreLibreriaUI(GestoreLibroConcreto db) {
            super(db);
        }

        @Override
        public void updateUndoRedoMenuState() {
            updateUndoRedoCallCount++;
        }
    }

    static class TestBooksPanelUI extends PannelloLibriUI {
        public int displayBooksCallCount = 0;
        public List<Libro> displayedBooks = new ArrayList<>();

        public TestBooksPanelUI(GestoreLibreriaUI gestoreLibreriaUI) {
            super(gestoreLibreriaUI);
        }

        @Override
        public void displayBooks(List<Libro> books) {
            displayBooksCallCount++;
            displayedBooks.clear();
            displayedBooks.addAll(books);
        }
    }

    static class TestBookManager extends GestoreLibroConcreto {
        private final List<OsservatoreLibro> observers = new ArrayList<>();
        private List<Libro> books = new ArrayList<>();

        public TestBookManager() {
            super(null);
        }

        public void setBooks(List<Libro> books) {
            this.books = books;
        }

        @Override
        public List<Libro> getTuttiLibri(CriterioOrdinamento criteria) {
            return new ArrayList<>(books);
        }

        @Override
        public void aggiungi(OsservatoreLibro observer) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }

        @Override
        public void rimuovi(OsservatoreLibro observer) {
            observers.remove(observer);
        }

        public boolean isObserverAttached(OsservatoreLibro observer) {
            return observers.contains(observer);
        }

        public void notificaOsservatori() {
            List<OsservatoreLibro> observerList = new ArrayList<>(observers);
            for (OsservatoreLibro o : observerList) {
                o.aggiorna();
            }
        }
    }

    private TestGestoreLibreriaUI testUI;
    private TestBooksPanelUI testBookPanel;
    private TestBookManager testManager;
    private OsservatoreLibroConcreto observer;

    @BeforeEach
    void setUp() {
        testManager = new TestBookManager();
        testUI = new TestGestoreLibreriaUI(testManager);
        testBookPanel = new TestBooksPanelUI(testUI);

        observer = new OsservatoreLibroConcreto(testUI, testBookPanel, testManager);

    }

    @Test
    void testConstructor() {

        // Verifica che l'osservatore sia registrato
        assertTrue(testManager.isObserverAttached(observer));

        // Verifica che l'update iniziale sia stato chiamato
        assertEquals(1, testBookPanel.displayBooksCallCount);
        assertEquals(1, testUI.updateUndoRedoCallCount);
    }

    @Test
    void testUpdate() {

        testBookPanel.displayBooksCallCount = 0;
        testUI.updateUndoRedoCallCount = 0;

        List<Libro> testBooks = new ArrayList<>();
        Libro testBook1 = new Libro.Costruttore("Il Nome della Rosa", "Umberto Eco")
                .isbn("9788845203004")
                .genere("Romanzo storico/Giallo")
                .valutazione(5)
                .statoLettura("LETTO")
                .build();
        testBooks.add(testBook1);
        testManager.setBooks(testBooks);

        observer.aggiorna();

        assertEquals(1, testBookPanel.displayBooksCallCount);
        assertEquals(1, testBooks.size(), testBookPanel.displayedBooks.size());
        assertEquals("Il Nome della Rosa", testBookPanel.displayedBooks.get(0).getTitolo());

        assertEquals(1, testUI.updateUndoRedoCallCount);
    }


    @Test
    void testUnsubscribe() {
        assertTrue(testManager.isObserverAttached(observer));

        observer.unsubscribe();

        assertFalse(testManager.isObserverAttached(observer));
    }

    @Test
    void testExternalNotifyObserver() {

        testBookPanel.displayBooksCallCount = 0;
        testUI.updateUndoRedoCallCount = 0;

        List<Libro> newBooks = new ArrayList<>();
        Libro testBook1 = new Libro.Costruttore("Se questo è un uomo", "Primo Levi")
                .isbn("9788806219472")
                .genere("Memoir/Storia")
                .valutazione(5)
                .statoLettura("DA LEGGERE")
                .build();
        newBooks.add(testBook1);
        testManager.setBooks(newBooks);

        // Simuliamo una notifica esterna
        testManager.notificaOsservatori();

        assertEquals(1, testBookPanel.displayBooksCallCount);
        assertEquals(1, testBookPanel.displayedBooks.size());
        assertEquals("Se questo è un uomo", testBookPanel.displayedBooks.get(0).getTitolo());
    }

    @Test
    void testMultipleUpdates() {

        // Esegui tre aggiornamenti
        observer.aggiorna();
        observer.aggiorna();
        observer.aggiorna();

        // Il numero di update che ci si aspetta è 4, uno del costruttore e tre invocati dal seguente test
        assertEquals(4, testBookPanel.displayBooksCallCount);
        assertEquals(4, testUI.updateUndoRedoCallCount);
    }
}
