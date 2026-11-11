package perpus;

import java.util.List;

public class BookService implements Searchable<Book> {
    private final List<Book> books;

    public BookService() {
        this.books = FileManager.loadBooks();
    }

    public List<Book> getAllBooks() {
        return List.copyOf(books);
    }

    @Override
    public Book searchById(String bookId) {
        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(bookId)) {
                return book;
            }
        }
        return null;
    }

    public boolean addBook(Book book) {
        if (searchById(book.getBookId()) != null) {
            return false;
        }
        books.add(book);
        save();
        return true;
    }

    public boolean updateBook(Book book) {
        Book existing = searchById(book.getBookId());
        if (existing == null) {
            return false;
        }
        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setQuantity(book.getQuantity());
        save();
        return true;
    }

    public boolean deleteBook(String bookId) {
        Book existing = searchById(bookId);
        if (existing == null) {
            return false;
        }
        books.remove(existing);
        save();
        return true;
    }

    public void save() {
        FileManager.saveBooks(books);
    }
}
