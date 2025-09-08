package gestore_libreria.db;

import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;

import java.util.List;


public interface GestoreLibro {

    void aggiungiLibro(Libro Libro);

    List<Libro> getTuttiLibri(CriterioOrdinamento criteria);

    List<Libro> trovaLibroPerTitolo(String title, CriterioOrdinamento criteria);

    List<Libro> filtraLibroPerValutazione(int rating, CriterioOrdinamento criteria);

    List<Libro> filtraLibroPerStatoLettura(String readingState, CriterioOrdinamento criteria);

    List<Libro> trovaLibroPerAutore(String author, CriterioOrdinamento criteria);

    List<Libro> trovaLibroPerIsbn(String isbn, CriterioOrdinamento criteria);

    List<Libro> trovaLibroPerGenere(String genre, CriterioOrdinamento criteria);

    void aggiornaLibro(Libro oldBook, Libro Libro);

    void eliminaLibro(Libro Libro);

}
