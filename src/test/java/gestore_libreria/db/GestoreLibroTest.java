package gestore_libreria.db;

import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

public class GestoreLibroTest {
    private GestoreLibro manager;
    private Libro testBook1;
    private Libro testBook2;
    private Connection testConnection;

    //classe interna per istanziare un repository di test e un collegamento di test
    private class TestSQLiteBookRepository extends RepositoryLibroSQLite {
        @Override
        protected Connection getConnection() throws SQLException{
            return testConnection;
        }
    }

    @Before
    public void setUp() throws SQLException{

        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement statement = testConnection.createStatement();
        String createTable = """
                   CREATE TABLE IF NOT EXISTS books(
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       title TEXT NOT NULL,
                       author TEXT NOT NULL,
                       isbn TEXT,
                       genre TEXT,
                       rating INTEGER,
                       readingState TEXT,
                       coverPath TEXT
                       );
                   """;
        statement.executeUpdate(createTable);

        RepositoryLibroSQLite repository = new TestSQLiteBookRepository();
        manager = new GestoreLibroConcreto(repository);

        clearDatabase();

        testBook1 = new Libro.Costruttore("Il Nome della Rosa", "Umberto Eco")
                .isbn("9788845203004")
                .genere("Romanzo storico/Giallo")
                .valutazione(5)
                .statoLettura("LETTO")
                .build();

        testBook2 = new Libro.Costruttore("Se questo è un uomo", "Primo Levi")
                .isbn("9788806219472")
                .genere("Memoir/Storia")
                .valutazione(5)
                .statoLettura("DA LEGGERE")
                .build();
    }

    private void clearDatabase() throws SQLException{
        if(testConnection != null || !testConnection.isClosed()){
            Statement statement = testConnection.createStatement();
            statement.execute("DELETE FROM books");
            statement.close();
        }
    }

    @After
    public void closeConnection() throws SQLException{
        if(testConnection != null && !testConnection.isClosed()){
            testConnection.close();
        }
    }

    @Test
    public void testaggiungiLibro() throws SQLException{
        manager.aggiungiLibro(testBook1);

        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        assertEquals(1, allBooks.size());
        assertEquals("Il Nome della Rosa", allBooks.get(0).getTitolo());
        assertEquals("Umberto Eco", allBooks.get(0).getAutore());
        assertTrue(allBooks.get(0).getId() > 0);        //verifichiamo che sia stato assegnato correttamente un id
    }

    @Test
    public void testgetTuttiLibri() throws SQLException{
        manager.aggiungiLibro(testBook1);
        manager.aggiungiLibro(testBook2);

        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        assertEquals(2, allBooks.size());
    }

    @Test
    public void testaggiornaLibro() throws SQLException{
        manager.aggiungiLibro(testBook1);

        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        Libro originalBook = allBooks.get(0);

        Libro updatedBook = new Libro.Costruttore("Il Nome della Rosa - Update Test", "Umberto Eco")
                .id(originalBook.getId())
                .isbn("9788845203004")
                .genere("Romanzo storico/Giallo Epico")
                .valutazione(5)
                .statoLettura("LETTO")
                .build();
        manager.aggiornaLibro(originalBook, updatedBook);

        List<Libro> updatedBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        assertEquals(1, updatedBooks.size());
        assertEquals("Il Nome della Rosa - Update Test", updatedBooks.get(0).getTitolo());
        assertEquals("Umberto Eco", updatedBooks.get(0).getAutore());
        assertTrue(allBooks.get(0).getId() > 0);
    }

    @Test
    public void testeliminaLibro() throws SQLException{
        manager.aggiungiLibro(testBook1);

        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        manager.eliminaLibro(allBooks.get(0));
        List<Libro> remainingBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);

        assertEquals(0, remainingBooks.size());
    }

    @Test
    public void testFindByTitle() throws SQLException{
        manager.aggiungiLibro(testBook1);

        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        List<Libro> findBook = manager.trovaLibroPerTitolo("Il Nome della Rosa",CriterioOrdinamento.NESSUNO);
        assertEquals(allBooks.get(0).getTitolo(), findBook.get(0).getTitolo());
    }

    @Test
    public void testFindByAuthor() throws SQLException{
        manager.aggiungiLibro(testBook1);
        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        List<Libro> findBook = manager.trovaLibroPerAutore("Umberto Eco",CriterioOrdinamento.NESSUNO);
        assertEquals(allBooks.get(0).getAutore(), findBook.get(0).getAutore());
    }

    @Test
    public void testFilterByRating() throws SQLException{
        manager.aggiungiLibro(testBook1);
        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        List<Libro> filteredBook = manager.filtraLibroPerValutazione(5,CriterioOrdinamento.NESSUNO);
        assertEquals(allBooks.get(0).getValutazione(), filteredBook.get(0).getValutazione());
    }

    @Test
    public void testFilterByReadingState() throws SQLException{
        manager.aggiungiLibro(testBook1);
        List<Libro> allBooks = manager.getTuttiLibri(CriterioOrdinamento.NESSUNO);
        List<Libro> filteredBook = manager.filtraLibroPerStatoLettura("LETTO",CriterioOrdinamento.NESSUNO);
        assertEquals(allBooks.get(0).getStatoLettura(), filteredBook.get(0).getStatoLettura());
    }
}
