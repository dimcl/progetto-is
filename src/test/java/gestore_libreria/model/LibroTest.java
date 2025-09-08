package gestore_libreria.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LibroTest {
    private Libro testBook;

    @Before
    public void setUp() throws Exception {
        testBook = new Libro.Costruttore("Il Gattopardo", "Giuseppe Tomasi di Lampedusa")
                .isbn("9788807901908")
                .genere("Romanzo storico")
                .valutazione(1)
                .statoLettura("DA LEGGERE")
                .percorsoCopertina("/immagini/gattopardo.jpg")
                .id(1)
                .build();
    }

    @Test
    public void testGetters(){
        assertEquals("Il Gattopardo", testBook.getTitolo());
        assertEquals("Giuseppe Tomasi di Lampedusa", testBook.getAutore());
        assertEquals("Romanzo storico", testBook.getGenere());
        assertEquals(1, testBook.getValutazione());
        assertEquals("DA LEGGERE", testBook.getStatoLettura());
        assertEquals("/immagini/gattopardo.jpg", testBook.getPercorsoCopertina());
        assertEquals(1, testBook.getId());
    }

    @Test
    public void testSetId(){
        testBook.setId(2);
        assertEquals(2, testBook.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBuilderNullTitle(){
        new Libro.Costruttore(null, "Giuseppe Tomasi di Lampedusa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBuilderNullAuthor(){
        new Libro.Costruttore("Il Gattopardo", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setInvalidRating(){
        new Libro.Costruttore("Il Gattopardo", "Giuseppe Tomasi")
                .valutazione(6);
    }
}
