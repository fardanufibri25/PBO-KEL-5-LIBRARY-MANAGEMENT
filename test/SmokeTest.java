import perpus.AuthenticationManager;
import perpus.Book;
import perpus.BookService;
import perpus.FileManager;
import perpus.Member;
import perpus.MemberService;
import perpus.Searchable;
import perpus.User;

/**
 * Cek logika tanpa GUI. Jalankan dari folder kosong supaya file data
 * tidak menimpa data asli:
 *   java -cp out SmokeTest
 */
public class SmokeTest {
    private static int failed = 0;

    public static void main(String[] args) {
        BookService books = new BookService();
        MemberService members = new MemberService(books);
        members.setRecords(FileManager.loadRecords(books.getAllBooks(), members.getAllMembers()));

        books.addBook(new Book("B001", "Buku Uji", "Penulis", 2));
        books.addBook(new Book("B002", "Buku Terbatas", "Penulis", 1));
        members.addMember(new Member("M001", "Uji", "123"));
        members.addMember(new Member("M002", "Uji Dua", "123"));

        AuthenticationManager auth = new AuthenticationManager(members.getAllMembers());
        check("login admin", auth.login("admin", "admin123") != null);
        check("login member", auth.login("M001", "123") != null);
        check("password salah ditolak", auth.login("M001", "salah") == null);
        User user = auth.login("M001", "123");
        check("polimorfisme getRole", user != null && user.getRole().equals("Member"));

        check("update anggota", members.updateMember(new Member("M002", "Nama Baru", "999")));
        check("nama anggota berubah", members.searchById("M002").getName().equals("Nama Baru"));
        Searchable<Book> searchable = books;
        check("polimorfisme interface", searchable.searchById("B001") != null);

        check("pinjam berhasil", members.issue("M001", "B001") == null);
        check("stok berkurang", books.searchById("B001").getQuantity() == 1);
        check("pinjam dobel ditolak", members.issue("M001", "B001") != null);
        check("satu pinjaman aktif", members.getActiveRecords().size() == 1);
        check("belum overdue", members.getOverdue().isEmpty());

        check("kembali berhasil", members.returnBook("M001", "B001") == null);
        check("stok pulih", books.searchById("B001").getQuantity() == 2);
        check("pinjaman kosong", members.getActiveRecords().isEmpty());

        check("pinjam stok terakhir", members.issue("M001", "B002") == null);
        check("stok habis ditolak", members.issue("M002", "B002") != null);
        check("anggota lain tetap bisa pinjam B001", members.issue("M002", "B001") == null);

        check("hapus anggota berpinjaman ditolak", members.deleteMember("M002") != null);
        check("hapus buku berpinjaman ditolak", !members.deleteBook("B001"));
        check("kembalikan buku anggota", members.returnBook("M002", "B001") == null);
        check("hapus buku setelah dikembalikan", members.deleteBook("B001"));

        System.out.println(failed == 0 ? "SEMUA CEK LULUS" : failed + " CEK GAGAL");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "OK   " : "GAGAL") + " " + name);
        if (!ok) {
            failed++;
        }
    }
}
