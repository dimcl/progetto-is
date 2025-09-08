package gestore_libreria.db;

import java.util.List;

import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;


public interface ImplementatoreRepositoryLibro {

    //definisco i metodi che devono essere presenti nei database concreti

    void save(Libro Libro);
    List<Libro> loadAll(CriterioOrdinamento criteria);
    List<Libro> findByTitle(String title, CriterioOrdinamento criteria);
    List<Libro> findByRating(int rating, CriterioOrdinamento criteria);
    List<Libro> findByReadingState(String readingState, CriterioOrdinamento criteria);
    List<Libro> findByAuthor(String author, CriterioOrdinamento criteria);
    List<Libro> findByIsbn(String isbn, CriterioOrdinamento criteria);
    List<Libro> findByGenre(String genre, CriterioOrdinamento criteria);
    void delete(Libro Libro);
    void aggiorna(Libro Libro);
}
