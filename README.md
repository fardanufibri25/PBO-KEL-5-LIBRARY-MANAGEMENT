# PBO Kelompok 5 — Manajemen Perpustakaan

Tugas besar mata kuliah Pemrograman Berorientasi Objek.
Aplikasi desktop Java Swing untuk mengelola buku, anggota, peminjaman, dan
keterlambatan. Tanpa database — data disimpan di file teks.

## Anggota Kelompok

| No | Nama | NIM | Bagian |
|----|------|-----|--------|
| 1 | _(isi nama)_ | _(isi NIM)_ | Model & Service |
| 2 | _(isi nama)_ | _(isi NIM)_ | GUI |
| 3 | _(isi nama)_ | _(isi NIM)_ | Diagram & laporan |

Dosen pengampu: _(isi nama dosen)_

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

## Struktur Folder

```
src/perpus/     kode program (11 file: 10 class + 1 interface)
data/           penyimpanan data (dibuat otomatis)
test/           SmokeTest.java, pengecekan logika tanpa GUI
out/            hasil compile (boleh dihapus)
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

- **Abstraction** — `User` tidak bisa dibuat langsung; method `getRole()`
  wajib diisi oleh setiap turunannya.
- **Inheritance** — `Admin` dan `Member` mewarisi seluruh field dan method
  `User`.
- **Polymorphism** — `getRole()` dioverride dengan hasil berbeda. GUI menyimpan
  user di variabel bertipe `User` (`LibraryFrame.currentUser`) lalu memanggil
  `getRole()` tanpa tahu objek aslinya. Interface `Searchable<T>` juga dipakai
  lewat tipe interface pada `BookService` dan `MemberService`.
- **Encapsulation** — semua field `private`, akses hanya lewat getter/setter.
- **Composition** — `BookService` memegang `Book`, `MemberService` memegang
  `IssueRecord` (bagian tidak hidup tanpa pemiliknya).
- **Aggregation** — `MemberService` menyimpan daftar `Member` yang bisa
  berdiri sendiri.
- **Dependency** — service memakai `FileManager`; GUI memakai service, tidak
  pernah menyentuh file langsung.

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
SmokeTest memeriksa 20 hal: login, hak akses, pinjam, stok, pinjam ganda,
pengembalian, stok habis, update anggota, dan aturan hapus data.
