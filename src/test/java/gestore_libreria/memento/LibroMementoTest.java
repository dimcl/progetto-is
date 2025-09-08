package gestore_libreria.memento;

import gestore_libreria.model.Libro;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LibroMementoTest {
    private Libro testBook1;
    private Libro testBookNew;
    private Libro testBookOld;
    private MementoLibro memento1;
    private MementoLibro memento2;


    @Before
    public void setUp() {
        testBook1 = new Libro.Costruttore("Gomorra", "Roberto Saviano")
                .isbn("9788804568905")
                .genere("Inchiesta giornalistica")
                .valutazione(4)
                .statoLettura("DA LEGGERE")
                .build();

        testBookNew = new Libro.Costruttore("L'Amica Geniale", "Elena Ferrante")
                .isbn("9788866328880")
                .genere("Romanzo contemporaneo")
                .valutazione(4)
                .statoLettura("LETTO")
                .build();
        testBookOld = new Libro.Costruttore("L'Amica Geniale - bozza", "Elena Ferrante")
                .isbn("9788866328880")
                .genere("Romanzo contemporaneo")
                .valutazione(4)
                .statoLettura("DA LEGGERE")
                .build();
        memento1 = new MementoLibro(testBook1, MementoLibro.OperationType.ADD);
        memento2 = new MementoLibro(testBookNew, MementoLibro.OperationType.UPDATE, testBookOld);


    }
    @Test(expected = IllegalArgumentException.class)
    public void testNoUpdateConstructor(){
        new MementoLibro(testBook1, MementoLibro.OperationType.UPDATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoAddNoDeleteConstructor(){
        new MementoLibro(testBookNew, MementoLibro.OperationType.ADD, testBookOld);
        new MementoLibro(testBookNew, MementoLibro.OperationType.REMOVE, testBookOld);
    }

    @Test
    public void testGetBookState() {
        assertEquals(testBook1, memento1.getBookState());
    }

    @Test
    public void testGetOperationType() {
        assertEquals(MementoLibro.OperationType.ADD, memento1.getOperationType());
    }

    @Test
    public void getPreviousBookState() {
        assertEquals(testBookOld, memento2.getPreviousBookState());
    }
}
