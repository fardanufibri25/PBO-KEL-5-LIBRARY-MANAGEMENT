package perpus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberService implements Searchable<Member> {
    private final BookService bookService;
    private final List<Member> members;
    private List<IssueRecord> records = new ArrayList<>();

    public MemberService(BookService bookService) {
        this.bookService = bookService;
        this.members = FileManager.loadMembers();
    }

    public void setRecords(List<IssueRecord> records) {
        this.records = records;
    }

    public List<Member> getAllMembers() {
        return members;
    }

    public List<IssueRecord> getActiveRecords() {
        return records;
    }

    public List<IssueRecord> getOverdue() {
        List<IssueRecord> result = new ArrayList<>();
        for (IssueRecord record : records) {
            if (record.isOverdue()) {
                result.add(record);
            }
        }
        return result;
    }

    @Override
    public Member searchById(String memberId) {
        for (Member member : members) {
            if (member.getId().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }

    public boolean addMember(Member member) {
        if (searchById(member.getId()) != null) {
            return false;
        }
        members.add(member);
        save();
        return true;
    }

    public boolean updateMember(Member member) {
        Member existing = searchById(member.getId());
        if (existing == null) {
            return false;
        }
        existing.setName(member.getName());
        existing.setPassword(member.getPassword());
        save();
        return true;
    }

    public String deleteMember(String memberId) {
        Member member = searchById(memberId);
        if (member == null) {
            return "Anggota tidak ditemukan.";
        }
        for (IssueRecord record : records) {
            if (record.getMember().getId().equalsIgnoreCase(memberId)) {
                return "Anggota masih punya pinjaman aktif.";
            }
        }
        members.remove(member);
        save();
        return null;
    }

    public boolean deleteBook(String bookId) {
        for (IssueRecord record : records) {
            if (record.getBook().getBookId().equalsIgnoreCase(bookId)) {
                return false;
            }
        }
        return bookService.deleteBook(bookId);
    }

    public String issue(String memberId, String bookId) {
        Member member = searchById(memberId);
        if (member == null) {
            return "Anggota tidak ditemukan.";
        }
        Book book = bookService.searchById(bookId);
        if (book == null) {
            return "Buku tidak ditemukan.";
        }
        if (book.getQuantity() <= 0) {
            return "Stok buku habis.";
        }
        for (IssueRecord record : records) {
            if (record.getMember().getId().equalsIgnoreCase(memberId)
                    && record.getBook().getBookId().equalsIgnoreCase(bookId)) {
                return "Anggota ini masih meminjam buku tersebut.";
            }
        }
        book.setQuantity(book.getQuantity() - 1);
        records.add(new IssueRecord(member, book, LocalDate.now().plusDays(7)));
        bookService.save();
        save();
        return null;
    }

    public String returnBook(String memberId, String bookId) {
        IssueRecord found = null;
        for (IssueRecord record : records) {
            if (record.getMember().getId().equalsIgnoreCase(memberId)
                    && record.getBook().getBookId().equalsIgnoreCase(bookId)) {
                found = record;
                break;
            }
        }
        if (found == null) {
            return "Data pinjaman tidak ditemukan.";
        }
        records.remove(found);
        Book book = found.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookService.save();
        save();
        return null;
    }

    private void save() {
        FileManager.saveMembers(members);
        FileManager.saveRecords(records);
    }
}
