package gestore_libreria.memento;

import gestore_libreria.model.Libro;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CustodeCronologiaLibroTest {

    private CustodeCronologiaLibro CustodeCronologiaLibro;
    private Libro testBook1;
    private Libro testBook2;
    private MementoLibro testBookMemento1;
    private MementoLibro testBookMemento2;


    @Before
    public void setUp() throws Exception {
        CustodeCronologiaLibro = new CustodeCronologiaLibro();
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
        assertFalse(CustodeCronologiaLibro.puoAnnullare());
        assertFalse(CustodeCronologiaLibro.puoRipetere());

        CustodeCronologiaLibro.save(testBookMemento1);

        assertTrue(CustodeCronologiaLibro.puoAnnullare());
        assertFalse(CustodeCronologiaLibro.puoRipetere());

        CustodeCronologiaLibro.save(testBookMemento2);
        CustodeCronologiaLibro.annulla();

        assertTrue(CustodeCronologiaLibro.puoRipetere());
    }

    @Test
    public void testUndo() {
        CustodeCronologiaLibro.save(testBookMemento1);
        MementoLibro MementoLibro = CustodeCronologiaLibro.annulla();

        assertEquals(testBookMemento1, MementoLibro);
        assertFalse(CustodeCronologiaLibro.puoAnnullare());
        assertTrue(CustodeCronologiaLibro.puoRipetere());
    }

    @Test
    public void testRedo() {
        CustodeCronologiaLibro.save(testBookMemento1);
        CustodeCronologiaLibro.annulla();

        MementoLibro MementoLibro = CustodeCronologiaLibro.ripeti();
        assertEquals(testBookMemento1, MementoLibro);
        assertTrue(CustodeCronologiaLibro.puoAnnullare());
        assertFalse(CustodeCronologiaLibro.puoRipetere());
    }

    @Test
    public void testpuoAnnullare() {
        assertFalse(CustodeCronologiaLibro.puoAnnullare());
        CustodeCronologiaLibro.save(testBookMemento1);
        assertTrue(CustodeCronologiaLibro.puoAnnullare());
        CustodeCronologiaLibro.annulla();
        assertFalse(CustodeCronologiaLibro.puoAnnullare());
    }

    @Test
    public void testpuoRipetere() {
        assertFalse(CustodeCronologiaLibro.puoRipetere());
        CustodeCronologiaLibro.save(testBookMemento1);
        CustodeCronologiaLibro.annulla();
        assertTrue(CustodeCronologiaLibro.puoRipetere());
        CustodeCronologiaLibro.ripeti();
        assertFalse(CustodeCronologiaLibro.puoRipetere());
    }

    @Test
    public void testCleanAll() {
        CustodeCronologiaLibro.save(testBookMemento1);
        CustodeCronologiaLibro.save(testBookMemento2);

        assertTrue(CustodeCronologiaLibro.puoAnnullare());
        CustodeCronologiaLibro.cleanAll();

        assertFalse(CustodeCronologiaLibro.puoAnnullare());
        assertFalse(CustodeCronologiaLibro.puoRipetere());
    }
}
