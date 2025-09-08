package gestore_libreria.memento;

import gestore_libreria.model.Libro;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CopertinaCronologiaLibroTest {

    private CopertinaCronologiaLibro copertinaCronologiaLibro;
    private Libro testBook1;
    private Libro testBook2;
    private MementoLibro testBookMemento1;
    private MementoLibro testBookMemento2;


    @Before
    public void setUp() throws Exception {
        copertinaCronologiaLibro = new CopertinaCronologiaLibro();
        testBook1 = new Libro.Costruttore("La Coscienza di Zeno", "Italo Svevo")
                .isbn("9788807900895")
                .genere("Romanzo psicologico")
                .valutazione(4)
                .statoLettura("IN LETTURA")
                .build();

        testBook2 = new Libro.Costruttore("Cristo si è fermato a Eboli", "Carlo Levi")
                .isbn("9788806216112")
                .genere("Memoir/Letteratura")
                .valutazione(1)
                .statoLettura("DA LEGGERE")
                .build();
        testBookMemento1 = new MementoLibro(testBook1, MementoLibro.OperationType.ADD);
        testBookMemento2 = new MementoLibro(testBook2, MementoLibro.OperationType.REMOVE);
    }

    @Test
    public void testSave() {
        assertFalse(copertinaCronologiaLibro.puoAnnullare());
        assertFalse(copertinaCronologiaLibro.puoRipetere());

        copertinaCronologiaLibro.save(testBookMemento1);

        assertTrue(copertinaCronologiaLibro.puoAnnullare());
        assertFalse(copertinaCronologiaLibro.puoRipetere());

        copertinaCronologiaLibro.save(testBookMemento2);
        copertinaCronologiaLibro.annulla();

        assertTrue(copertinaCronologiaLibro.puoRipetere());
    }

    @Test
    public void testUndo() {
        copertinaCronologiaLibro.save(testBookMemento1);
        MementoLibro MementoLibro = copertinaCronologiaLibro.annulla();

        assertEquals(testBookMemento1, MementoLibro);
        assertFalse(copertinaCronologiaLibro.puoAnnullare());
        assertTrue(copertinaCronologiaLibro.puoRipetere());
    }

    @Test
    public void testRedo() {
        copertinaCronologiaLibro.save(testBookMemento1);
        copertinaCronologiaLibro.annulla();

        MementoLibro MementoLibro = copertinaCronologiaLibro.ripeti();
        assertEquals(testBookMemento1, MementoLibro);
        assertTrue(copertinaCronologiaLibro.puoAnnullare());
        assertFalse(copertinaCronologiaLibro.puoRipetere());
    }

    @Test
    public void testpuoAnnullare() {
        assertFalse(copertinaCronologiaLibro.puoAnnullare());
        copertinaCronologiaLibro.save(testBookMemento1);
        assertTrue(copertinaCronologiaLibro.puoAnnullare());
        copertinaCronologiaLibro.annulla();
        assertFalse(copertinaCronologiaLibro.puoAnnullare());
    }

    @Test
    public void testpuoRipetere() {
        assertFalse(copertinaCronologiaLibro.puoRipetere());
        copertinaCronologiaLibro.save(testBookMemento1);
        copertinaCronologiaLibro.annulla();
        assertTrue(copertinaCronologiaLibro.puoRipetere());
        copertinaCronologiaLibro.ripeti();
        assertFalse(copertinaCronologiaLibro.puoRipetere());
    }

    @Test
    public void testCleanAll() {
        copertinaCronologiaLibro.save(testBookMemento1);
        copertinaCronologiaLibro.save(testBookMemento2);

        assertTrue(copertinaCronologiaLibro.puoAnnullare());
        copertinaCronologiaLibro.cleanAll();

        assertFalse(copertinaCronologiaLibro.puoAnnullare());
        assertFalse(copertinaCronologiaLibro.puoRipetere());
    }
}
