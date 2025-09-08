package gestore_libreria.db;

import gestore_libreria.model.Libro;
import gestore_libreria.model.CriterioOrdinamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//classe che implementa il database
public class RepositoryLibroSQLite implements ImplementatoreRepositoryLibro {

    protected Connection getConnection() throws SQLException {
        return DatabaseConnectionSingleton.getInstance();
    }

    public RepositoryLibroSQLite(){
        try{
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            //aggiungo un id come chiave del libro
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
            statement.execute(createTable);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }


    @Override
    public void save(Libro Libro) {
        //Per inserire il libro preparo la stringa sql con gli elementi da aggiungere seguiti da ? per ogni parametro
        String sql = """
                INSERT INTO books (title, author, isbn, genre, rating, readingState, coverPath)
                VALUES (?,?,?,?,?,?,?)
                """;
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, Libro.getTitolo());
            preparedStatement.setString(2,Libro.getAutore());
            preparedStatement.setString(3,Libro.getIsbn());
            preparedStatement.setString(4,Libro.getGenere());
            preparedStatement.setInt(5,Libro.getValutazione());
            preparedStatement.setString(6, Libro.getStatoLettura());
            preparedStatement.setString(7,Libro.getPercorsoCopertina());
            preparedStatement.executeUpdate();

            //ricavo il codice del libro e lo inserisco nell'oggetto
            try{
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if(resultSet.next()){
                    Libro.setId(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                System.err.println("Errore nel salvataggio del libro");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getOrderByCriteria(CriterioOrdinamento criteria){
        if(criteria == null || criteria == CriterioOrdinamento.NESSUNO){
            return "";
        }
        switch (criteria){
            case TITOLO_ASC:
                return " ORDER BY LOWER(title) ASC";
            case TITOLO_DESC:
                return " ORDER BY LOWER(title) DESC";
            case AUTORE_ASC:
                return " ORDER BY LOWER(author) ASC";
            case AUTORE_DESC:
                return " ORDER BY LOWER(author) DESC";
            case VALUTAZIONE_ASC:
                return " ORDER BY rating ASC, LOWER(title) ASC";
            case VALUTAZIONE_DESC:
                return " ORDER BY rating DESC, LOWER(title) ASC";
            default:
                return "";
        }
    }

    @Override
    public List<Libro> loadAll(CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books" + getOrderByCriteria(criteria);

        try{
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()){

                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return books;
    }

    @Override
    public List<Libro> findByTitle(String title, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?)" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + title + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dal titolo" + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Libro> findByRating(int rating, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE rating = ?" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1,rating);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dalla valutazione");
        }
        return books;
    }

    @Override
    public List<Libro> findByReadingState(String readingState, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(readingState) LIKE LOWER(?)" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + readingState + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dallo stato di lettura");
        }
        return books;
    }

    @Override
    public List<Libro> findByAuthor(String author, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(author) LIKE LOWER(?)" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + author + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dall'autore");
        }
        return books;
    }

    @Override
    public List<Libro> findByIsbn(String isbn, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(isbn) LIKE LOWER(?)" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + isbn + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dall'ISBN: " + e.getMessage());
        }
        return books;
    }

    @Override
    public List<Libro> findByGenre(String genre, CriterioOrdinamento criteria) {
        List<Libro> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(genre) LIKE LOWER(?)" + getOrderByCriteria(criteria);
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + genre + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Libro Libro = new Libro.Costruttore(resultSet.getString("title"), resultSet.getString("author"))
                        .id(resultSet.getInt("id"))
                        .isbn(resultSet.getString("isbn"))
                        .genere(resultSet.getString("genre"))
                        .valutazione(resultSet.getInt("rating"))
                        .statoLettura(resultSet.getString("readingState"))
                        .percorsoCopertina(resultSet.getString("coverPath"))
                        .build();
                books.add(Libro);
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca del libro dal genere: " + e.getMessage());
        }
        return books;
    }

    @Override
    public void delete(Libro Libro) {
        int id = Libro.getId();
        String sql = "Delete FROM books WHERE id = ?";
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            int deleteRow = preparedStatement.executeUpdate();
            if(deleteRow>0){
                System.out.println("Riga eliminata con successo");
            }else {
                System.out.println("Riga NON eliminata con successo");
            }

        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento del libro");
            e.printStackTrace();
        }
    }

    @Override
    public void aggiorna(Libro Libro) {
        String sql = """
                UPDATE books SET
                title=?,
                author=?,
                isbn=?,
                genre=?,
                rating=?,
                readingState=?,
                coverPath=?
                WHERE id=?
                """;
        try{
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, Libro.getTitolo());
            preparedStatement.setString(2, Libro.getAutore());
            preparedStatement.setString(3, Libro.getIsbn());
            preparedStatement.setString(4, Libro.getGenere());
            preparedStatement.setInt(5, Libro.getValutazione());
            preparedStatement.setString(6, Libro.getStatoLettura());
            preparedStatement.setString(7, Libro.getPercorsoCopertina());
            preparedStatement.setInt(8, Libro.getId());      //il libro aggiornato deve avere lo stesso id del libro da modificare

            int affectedRows = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento del libro");
            e.printStackTrace();
        }

    }


//    public static void main(String[] args) {
//        RepositoryLibroSQLite repo = new RepositoryLibroSQLite();
}
