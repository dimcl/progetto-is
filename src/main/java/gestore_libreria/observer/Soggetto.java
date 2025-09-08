package gestore_libreria.observer;

import java.util.List;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Soggetto {
    private List<OsservatoreLibro> observers;

    public Soggetto() {
        observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Ci permette di iscrivere il nostro ConcreteSubject alla lista degli observers per
     * essere notificato
     *
     * @param observer contiene la logica {@code aggiorna()} e {@code unsubscribe()}, è l'elemento che verrà inserito nella lista
     * @pre {@code observer} non deve essere null
     * @pre {@code observer} non deve essere contenuto nella lista
     * @post l'observer è stato correttamente inserito nella lista
     */
    public void aggiungi(OsservatoreLibro observer) {
        if(!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Ci permette di disiscrivere il nostro ConcreteSubject dalla lista degli observer
     * @param observer ccontiene la logica {@code aggiorna()} e {@code unsubscribe()}, è l'elemento che verrà inserito
     * nella lista
     * @pre {@code observer} non deve essere null
     * @pre {@code observer} deve essere contenuto nella lista
     * @post l'observer è stato correttamente rimosso dalla lista degli observer
     * @post l'observer non verrà più notificato
     */
    public void rimuovi(OsservatoreLibro observer) {
        if(observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    /**
     * Notifica tutti gli observer iscritti invocando il loro metodo {@code aggiorna()}
     * @post tutti gli observer contenuti nella lista sono stati notificati
     */
    public void notificaOsservatori() {
        for (OsservatoreLibro observer : observers) {
            observer.aggiorna();
        }
    }
}
