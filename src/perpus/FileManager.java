package perpus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path BOOK_FILE = DATA_DIR.resolve("bookRecords.txt");
    private static final Path MEMBER_FILE = DATA_DIR.resolve("memberRecords.txt");
    private static final Path DUE_FILE = DATA_DIR.resolve("dueDates.txt");

    private FileManager() {
    }

    public static boolean isValidField(String value) {
        return value != null && !value.contains(";")
                && !value.contains("\n") && !value.contains("\r");
    }

    public static List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        for (String line : read(BOOK_FILE)) {
            String[] parts = line.split(";", -1);
            if (parts.length != 4) {
                throw invalidRow(BOOK_FILE, line);
            }
            try {
                books.add(new Book(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        Integer.parseInt(parts[3].trim())));
            } catch (NumberFormatException e) {
                throw invalidRow(BOOK_FILE, line);
            }
        }
        return books;
    }

    public static void saveBooks(List<Book> books) {
        List<String> lines = new ArrayList<>();
        for (Book book : books) {
            lines.add(book.getBookId() + ";" + book.getTitle() + ";" + book.getAuthor() + ";"
                    + book.getQuantity());
        }
        write(BOOK_FILE, lines);
    }

    public static List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        for (String line : read(MEMBER_FILE)) {
            String[] parts = line.split(";", -1);
            if (parts.length != 3) {
                throw invalidRow(MEMBER_FILE, line);
            }
            members.add(new Member(parts[0].trim(), parts[1].trim(), parts[2].trim()));
        }
        return members;
    }

    public static void saveMembers(List<Member> members) {
        List<String> lines = new ArrayList<>();
        for (Member member : members) {
            lines.add(member.getId() + ";" + member.getName() + ";" + member.getPassword());
        }
        write(MEMBER_FILE, lines);
    }

    public static List<IssueRecord> loadRecords(List<Book> books, List<Member> members) {
        List<IssueRecord> records = new ArrayList<>();
        for (String line : read(DUE_FILE)) {
            String[] parts = line.split(";", -1);
            if (parts.length != 3) {
                throw invalidRow(DUE_FILE, line);
            }
            Book book = findBook(books, parts[1].trim());
            Member member = findMember(members, parts[0].trim());
            if (book == null || member == null) {
                throw invalidRow(DUE_FILE, line);
            }
            try {
                records.add(new IssueRecord(member, book, LocalDate.parse(parts[2].trim())));
            } catch (DateTimeParseException e) {
                throw invalidRow(DUE_FILE, line);
            }
        }
        return records;
    }

    public static void saveRecords(List<IssueRecord> records) {
        List<String> lines = new ArrayList<>();
        for (IssueRecord record : records) {
            lines.add(record.getMember().getId() + ";" + record.getBook().getBookId() + ";"
                    + record.getDueDate());
        }
        write(DUE_FILE, lines);
    }

    private static Book findBook(List<Book> books, String id) {
        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(id)) {
                return book;
            }
        }
        return null;
    }

    private static Member findMember(List<Member> members, String id) {
        for (Member member : members) {
            if (member.getId().equalsIgnoreCase(id)) {
                return member;
            }
        }
        return null;
    }

    private static IllegalArgumentException invalidRow(Path path, String line) {
        return new IllegalArgumentException("Baris data tidak valid di " + path + ": " + line);
    }

    private static List<String> read(Path path) {
        try {
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            List<String> lines = new ArrayList<>(
                    Files.readAllLines(path, StandardCharsets.UTF_8));
            // Notepad lama menulis BOM di awal file; buang supaya id baris
            // pertama tetap cocok.
            if (!lines.isEmpty() && lines.get(0).startsWith("\uFEFF")) {
                lines.set(0, lines.get(0).substring(1));
            }
            return lines;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // shortcut: satu jendela saja; dua instance saling menimpa. Kunci file kalau perlu multi-user.
    private static void write(Path path, List<String> lines) {
        try {
            Files.createDirectories(DATA_DIR);
            // Tulis ke file sementara lalu pindahkan, supaya crash tidak
            // meninggalkan file data yang terpotong.
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
