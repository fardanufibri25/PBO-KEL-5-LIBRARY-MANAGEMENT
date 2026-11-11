# Diagram Class — Manajemen Perpustakaan

Diagram di bawah mengikuti kode di `src/perpus/` (10 class + 1 interface).
GitHub merender blok Mermaid ini otomatis.

```mermaid
classDiagram
    class User {
        <<abstract>>
        -String id
        -String name
        -String password
        +User(id, name, password)
        +getId() String
        +getName() String
        +getPassword() String
        +setName(name) void
        +setPassword(password) void
        +getRole() String
        +toString() String
    }

    class Admin {
        +Admin(id, name, password)
        +getRole() String
    }

    class Member {
        +Member(id, name, password)
        +getRole() String
    }

    class Book {
        -String bookId
        -String title
        -String author
        -int quantity
        +Book(bookId, title, author, quantity)
        +getBookId() String
        +getTitle() String
        +setTitle(title) void
        +getAuthor() String
        +setAuthor(author) void
        +getQuantity() int
        +setQuantity(quantity) void
        +toString() String
    }

    class IssueRecord {
        -Member member
        -Book book
        -LocalDate dueDate
        +IssueRecord(member, book, dueDate)
        +getMember() Member
        +getBook() Book
        +getDueDate() LocalDate
        +isOverdue() boolean
        +toString() String
    }

    class Searchable~T~ {
        <<interface>>
        +searchById(id) T
    }

    class BookService {
        -List~Book~ books
        +BookService()
        +getAllBooks() List~Book~
        +searchById(id) Book
        +addBook(book) boolean
        +updateBook(book) boolean
        +deleteBook(id) boolean
        +save() void
    }

    class MemberService {
        -BookService bookService
        -List~Member~ members
        -List~IssueRecord~ records
        +MemberService(bookService)
        +setRecords(records) void
        +getAllMembers() List~Member~
        +getActiveRecords() List~IssueRecord~
        +getOverdue() List~IssueRecord~
        +searchById(id) Member
        +addMember(member) boolean
        +updateMember(member) boolean
        +deleteMember(id) String
        +deleteBook(id) boolean
        +issue(memberId, bookId) String
        +returnBook(memberId, bookId) String
    }

    class AuthenticationManager {
        -Admin admin
        -List~Member~ members
        +AuthenticationManager(members)
        +login(id, password) User
    }

    class FileManager {
        -Path DATA_DIR
        -Path BOOK_FILE
        -Path MEMBER_FILE
        -Path DUE_FILE
        +loadBooks() List~Book~
        +saveBooks(books) void
        +loadMembers() List~Member~
        +saveMembers(members) void
        +loadRecords(books, members) List~IssueRecord~
        +saveRecords(records) void
    }

    class LibraryFrame {
        -BookService bookService
        -MemberService memberService
        -AuthenticationManager auth
        -User currentUser
        +LibraryFrame()
        +main(args) void
    }

    User <|-- Admin
    User <|-- Member
    Searchable <|.. BookService
    Searchable <|.. MemberService

    BookService "1" *-- "0..*" Book : mengelola
    MemberService "1" *-- "0..*" IssueRecord : mengelola
    MemberService "1" o-- "0..*" Member : mengelola
    MemberService "1" --> "1" BookService : memakai

    AuthenticationManager "1" o-- "0..*" Member
    AuthenticationManager "1" --> "1" Admin : membuat

    IssueRecord "0..*" --> "1" Member
    IssueRecord "0..*" --> "1" Book

    LibraryFrame ..> BookService
    LibraryFrame ..> MemberService
    LibraryFrame ..> AuthenticationManager
    BookService ..> FileManager
    MemberService ..> FileManager
```

## Arti panah

| Simbol | Arti | Contoh di proyek |
|---|---|---|
| `──▷` garis penuh, segitiga kosong | inheritance | `Admin`, `Member` → `User` |
| `┄┄▷` garis putus, segitiga kosong | realization (interface) | `BookService`, `MemberService` → `Searchable` |
| `──◆` belah ketupat penuh | composition | `BookService` ◆ `Book`, `MemberService` ◆ `IssueRecord` |
| `──◇` belah ketupat kosong | aggregation | `MemberService` ◇ `Member` |
| `──▶` panah penuh | association | `IssueRecord` → `Book`, `IssueRecord` → `Member` |
| `┄┄▶` panah putus | dependency | service → `FileManager`, `LibraryFrame` → service |
| `1` / `0..*` | multiplicity | `MemberService` 1 — `0..*` `Member` |

## Penerapan PBO

- **Abstraction** — `User` abstract, `getRole()` wajib diisi turunan.
- **Inheritance** — `Admin` dan `Member` mewarisi `User`.
- **Polymorphism** — `getRole()` dioverride; `LibraryFrame.currentUser` bertipe
  `User` tapi perilakunya beda. `Searchable<T>` dipakai lewat tipe interface.
- **Encapsulation** — semua field `private`, akses lewat getter/setter.
- **Composition / Aggregation / Dependency** — lihat tabel panah di atas.

## Lokasi file

| Class | File |
|---|---|
| `User` | `src/perpus/User.java` |
| `Admin` | `src/perpus/Admin.java` |
| `Member` | `src/perpus/Member.java` |
| `Book` | `src/perpus/Book.java` |
| `IssueRecord` | `src/perpus/IssueRecord.java` |
| `Searchable` | `src/perpus/Searchable.java` |
| `BookService` | `src/perpus/BookService.java` |
| `MemberService` | `src/perpus/MemberService.java` |
| `AuthenticationManager` | `src/perpus/AuthenticationManager.java` |
| `FileManager` | `src/perpus/FileManager.java` |
| `LibraryFrame` | `src/perpus/LibraryFrame.java` |
