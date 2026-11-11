# PBO Kelompok 5 — Manajemen Perpustakaan

Tugas besar mata kuliah Pemrograman Berorientasi Objek.
Aplikasi desktop Java Swing untuk mengelola buku, anggota, peminjaman, dan
keterlambatan. Tanpa database — data disimpan di file teks.

## Fitur

- Login admin dan anggota, menu menyesuaikan hak akses
- CRUD buku (tambah, ubah, hapus, lihat)
- CRUD anggota (tambah, ubah, hapus, lihat)
- Pinjam dan kembali buku, stok berkurang/bertambah otomatis
- Daftar buku terlambat (overdue) beserta jumlah hari
- Data tersimpan permanen di folder `data/`
- Data awal otomatis dibuat saat pertama kali dijalankan

## Akun Demo

| ID | Password | Hak akses |
|----|----------|-----------|
| `admin` | `admin123` | semua menu (Buku, Anggota, Pinjam, Overdue) |
| `M001` | `123` | Buku dan Pinjam/Kembali saja |

## Cara Menjalankan

Butuh JDK 17 atau lebih baru.

```
javac -d out src/perpus/*.java
java -cp out perpus.LibraryFrame
```

Jika `javac` tidak dikenali padahal JDK sudah terpasang, panggil lewat path
lengkap JDK, misalnya:

```
& "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" -d out src\perpus\*.java
& "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -cp out perpus.LibraryFrame
```

## Panduan Pemakaian

Langkah lengkap memakai aplikasi (login, CRUD, pinjam, overdue, demo):
[`docs/panduan.md`](docs/panduan.md).

## Struktur Folder

```
PBO LIB MANAGEMENT/
├── src/perpus/          kode program Java (10 class + 1 interface)
│   ├── User.java
│   ├── Admin.java
│   ├── Member.java
│   ├── Book.java
│   ├── IssueRecord.java
│   ├── Searchable.java
│   ├── BookService.java
│   ├── MemberService.java
│   ├── AuthenticationManager.java
│   ├── FileManager.java
│   └── LibraryFrame.java
├── test/SmokeTest.java   pengecekan logika tanpa GUI
├── docs/
│   ├── diagram-class.md  diagram class
│   ├── panduan.md        panduan pemakaian
│   └── img/              screenshot GUI
├── data/                 file data, dibuat otomatis
├── out/                  hasil compile, boleh dihapus
├── .gitignore
└── README.md
```

## Diagram Class

Diagram lengkap: [`docs/diagram-class.md`](docs/diagram-class.md)
(GitHub merender diagramnya otomatis).

## Struktur Class

| Class | Jenis | Tugas |
|---|---|---|
| `User` | abstract class | data dasar pengguna, `getRole()` abstrak |
| `Admin` | class | turunan `User`, role "Admin" |
| `Member` | class | turunan `User`, role "Member" |
| `Book` | class | data buku dan stok |
| `IssueRecord` | class | satu transaksi pinjam (anggota, buku, jatuh tempo) |
| `Searchable<T>` | interface | kontrak `searchById()` |
| `BookService` | class | CRUD buku, memegang daftar `Book` |
| `MemberService` | class | CRUD anggota, pinjam, kembali, overdue |
| `AuthenticationManager` | class | login admin dan anggota |
| `FileManager` | class | baca/tulis file teks |
| `LibraryFrame` | class | seluruh GUI + `main()` |

## Penerapan PBO

| Konsep | Penerapan di program | Penjelasan untuk presentasi |
|---|---|---|
| **Abstraction** | `User` adalah abstract class dengan method abstrak `getRole()`. | `User` berisi hal umum tentang pengguna. Program tidak membuat `User` langsung; turunannya harus menentukan role. |
| **Inheritance** | `Admin` dan `Member` mewarisi `User`. | Kedua jenis pengguna memakai data dasar yang sama seperti ID, nama, dan password, tanpa menulis ulang field tersebut. |
| **Polymorphism** | `LibraryFrame.currentUser` bertipe `User`; `Admin` dan `Member` memberi hasil `getRole()` berbeda. | GUI memanggil method yang sama, tetapi hasilnya mengikuti jenis objek yang sedang login. |
| **Encapsulation** | Field pada class model dibuat `private`; perubahan data dilakukan lewat method seperti getter/setter dan service. | Data tidak diakses langsung dari luar class. Service juga menjaga aturan saat data buku atau pinjaman diubah. |
| **Composition** | `BookService` mengelola daftar `Book`; `MemberService` menyimpan daftar `IssueRecord`. | Service mengelola data yang dipakai sebagai bagian dari pekerjaannya. Hubungan ini digambarkan pada diagram class. |
| **Aggregation** | `MemberService` menyimpan daftar `Member`. | Anggota tetap dapat ada sebagai data tersendiri, terlepas dari daftar yang dikelola service. |
| **Interface** | `BookService` dan `MemberService` mengimplementasikan `Searchable<T>`. | Keduanya menyediakan `searchById()`. `SmokeTest` memanggil `BookService` melalui tipe `Searchable<Book>`. |
| **Dependency** | `BookService` dan `MemberService` memakai `FileManager`; GUI memakai service. | Tiap bagian meminta pekerjaan dari bagian lain, misalnya service meminta `FileManager` menyimpan data. |

### Contoh alur peminjaman

1. Admin atau anggota memilih anggota dan buku di `LibraryFrame`.
2. `MemberService.issue()` mengecek data anggota, buku, stok, dan pinjaman ganda.
3. Jika valid, stok dikurangi dan `IssueRecord` dibuat dengan jatuh tempo 7 hari.
4. Service meminta `BookService` dan `FileManager` menyimpan perubahan ke file teks.

## Format Data

File teks dengan pemisah `;` di folder `data/`.

```
bookRecords.txt    B001;Laskar Pelangi;Andrea Hirata;3
memberRecords.txt  M001;Budi;123
dueDates.txt       M001;B001;2026-11-18
```

`dueDates.txt` hanya menyimpan id anggota, id buku, dan tanggal jatuh tempo.
Saat dibuka, `FileManager` mencocokkan id tersebut dengan objek `Member` dan
`Book` yang sudah dimuat.

## Cara Demo Overdue

1. Login sebagai `admin`, pinjam satu buku.
2. Tutup program.
3. Ubah tanggal di `data/dueDates.txt` menjadi tanggal yang sudah lewat.
4. Jalankan lagi, buka tab **Overdue**.

## Pengujian

```
javac -cp out -d out test/SmokeTest.java
java -cp out SmokeTest
```

Jalankan dari folder kosong agar file data asli tidak tertimpa.
SmokeTest memeriksa 30 hal: login, hak akses, pinjam, stok, pinjam ganda,
pengembalian, stok habis, update anggota, dan aturan hapus data.
