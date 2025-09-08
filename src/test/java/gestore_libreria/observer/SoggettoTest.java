package gestore_libreria.observer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
public class SoggettoTest {

    private Soggetto Soggetto;
    private TestObserver observer1;
    private TestObserver observer2;

    private static class ConcreteTestSubject extends Soggetto {}

    private static class TestObserver implements OsservatoreLibro {

        private int updateCount = 0;

        @Override
        public void aggiorna() {
            updateCount++;
        }

        public int getUpdateCount() {
            return updateCount;
        }
    }

    @Before
    public void setUp() {
        Soggetto = new ConcreteTestSubject();
        observer1 = new TestObserver();
        observer2 = new TestObserver();
    }

    @Test
    public void testaggiungi() {
        Soggetto.aggiungi(observer1);
        Soggetto.aggiungi(observer2);

        Soggetto.notificaOsservatori();
        assertEquals(1, observer1.getUpdateCount());
        assertEquals(1, observer2.getUpdateCount());
    }

    @Test
    public void testAttachDuplicate() {
        Soggetto.aggiungi(observer1);
        Soggetto.aggiungi(observer1);

        Soggetto.notificaOsservatori();

        assertEquals(1, observer1.getUpdateCount());
    }

    @Test
    public void testrimuovi() {
        Soggetto.aggiungi(observer1);
        Soggetto.aggiungi(observer2);

        Soggetto.rimuovi(observer1);
        Soggetto.notificaOsservatori();
        assertEquals(0, observer1.getUpdateCount());
        assertEquals(1, observer2.getUpdateCount());
    }
}
