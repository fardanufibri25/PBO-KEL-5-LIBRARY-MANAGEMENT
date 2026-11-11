package perpus;

/**
 * Kontrak pencarian data berdasarkan id.
 * Dipakai BookService dan MemberService supaya keduanya bisa dipanggil
 * lewat tipe interface yang sama.
 */
public interface Searchable<T> {
    T searchById(String id);
}
