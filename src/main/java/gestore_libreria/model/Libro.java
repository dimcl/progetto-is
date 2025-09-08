package gestore_libreria.model;

public class Libro {
    //dichiaro le variabili del libro
    private int id;
    //obbligatori
    private final String titolo;
    private final String autore;

    //facoltativi
    private final String isbn;
    private final String genere;
    private final int valutazione;
    private final String statoLettura;
    private final String percorsoCopertina;     //per le immagini di copertina

    /**
     * Costruisce un'istanza di Libro utilizzando un oggetto Costruttore.
     *
     * @param Costruttore Il Costruttore contenente i dati per costruire il libro.
     * @pre Costruttore non deve essere null.
     * @pre Costruttore.titolo non deve essere null o vuoto.
     * @pre Costruttore.autore non deve essere null o vuoto.
     * @pre Costruttore.valutazione deve essere un valore tra 0 e 5 (inclusi).
     * @pre Costruttore.statoLettura deve essere uno stato di lettura valido ("letto", "in lettura", "da leggere").
     * @post Viene creata una nuova istanza di Libro con i valori specificati dal Costruttore.
     * @post Tutti i campi 'final' del libro sono inizializzati e non modificabili successivamente.
     */
    private Libro(Costruttore Costruttore){
        this.titolo = Costruttore.titolo;
        this.autore = Costruttore.autore;
        this.isbn = Costruttore.isbn;
        this.genere = Costruttore.genere;
        this.valutazione = Costruttore.valutazione;
        this.statoLettura = Costruttore.statoLettura;
        this.percorsoCopertina = Costruttore.percorsoCopertina;
        this.id = Costruttore.id;
    }

    //getter per la lettura

    public String getTitolo() {
        return titolo;
    }

    public String getAutore() {
        return autore;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getGenere() {
        return genere;
    }

    public int getValutazione() {
        return valutazione;
    }

    public String getStatoLettura() {
        return statoLettura;
    }

    public String getPercorsoCopertina() {
        return percorsoCopertina;
    }

    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID del libro. Questo metodo è generalmente usato solo dal database
     * dopo che il libro è stato salvato e gli è stato assegnato un ID.
     *
     * @param id L'ID univoco da assegnare al libro.
     * @pre id deve essere un intero
     * @post L'ID del libro è impostato al valore fornito.
     */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Libro{" +
                "id='" + id + '\'' +
                ", titolo='" + titolo + '\'' +
                ", autore='" + autore + '\'' +
                ", isbn='" + isbn + '\'' +
                ", genere='" + genere + '\'' +
                ", valutazione=" + valutazione +
                ", statoLettura='" + statoLettura + '\'' +
                ", percorsoCopertina='" + percorsoCopertina + '\'' +
                '}';
    }

    //Costruttore per la costruzione dell'oggetto libro
    public static class Costruttore{


        //obbligatori
        private final String titolo;
        private final String autore;

        //facoltativi
        private String isbn = "";
        private String genere = "";
        private int valutazione = 0;
        private String statoLettura = "da leggere";
        private String percorsoCopertina = "";        //per le immagini di copertina
        private int id = 0;

        /**
         * Costruisce un nuovo Costruttore per l'oggetto Libro.
         *
         * @param title Il titolo obbligatorio del libro.
         * @param author L'autore obbligatorio del libro.
         * @pre title non deve essere null o vuoto.
         * @pre author non deve essere null o vuoto.
         * @post Il Costruttore è inizializzato con il titolo e l'autore specificati.
         * @post I campi facoltativi sono inizializzati con i loro valori di default.
         * @throws IllegalArgumentException se il titolo o l'autore non rispettano le pre-condizioni.
         */
        public Costruttore(String title, String author) {
            if(title == null || title.isBlank() || author == null || author.isEmpty()){
                throw new IllegalArgumentException("Titolo e autore sono obbligatori");
            }
            this.titolo = title;
            this.autore = author;
        }

        //restituisco un oggetto Costruttore per ogni campo inserito
        public Costruttore isbn(String isbn){
            this.isbn = isbn;
            return this;
        }

        public Costruttore genere(String genre){
            this.genere = genre;
            return this;
        }

        public Costruttore valutazione(int rating){
            if(rating < 0 || rating > 5){
                throw new IllegalArgumentException("Valore non valido, rating compreso tra 0 e 5");
            }
            this.valutazione = rating;
            return this;
        }

        public Costruttore statoLettura(String state) {
            if (!state.matches("letto|in lettura|da leggere"))
                this.statoLettura = "da leggere";
                //throw new IllegalArgumentException("Stato non valido");
            this.statoLettura = state;
            return this;
        }

        public Costruttore percorsoCopertina(String path) {
            this.percorsoCopertina = path;
            return this;
        }

        public Costruttore id(int id) {
            this.id = id;
            return this;
        }

        public Libro build() {
            return new Libro(this);
        }
    }
}
