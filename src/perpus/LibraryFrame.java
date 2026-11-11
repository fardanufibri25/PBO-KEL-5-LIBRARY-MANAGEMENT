package perpus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LibraryFrame extends JFrame {
    private final BookService bookService;
    private final MemberService memberService;
    private final AuthenticationManager auth;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final DefaultTableModel bookModel = model("ID", "Judul", "Penulis", "Stok");
    private final DefaultTableModel memberModel = model("ID", "Nama");
    private final DefaultTableModel loanModel =
            model("ID Anggota", "Nama", "ID Buku", "Judul", "Jatuh Tempo");
    private final DefaultTableModel overdueModel =
            model("Anggota", "Buku", "Jatuh Tempo", "Terlambat (hari)");

    private JPanel mainPanel;
    private JComboBox<Member> memberCombo;
    private JComboBox<Book> bookCombo;
    private JTextField loginIdField;
    private JPasswordField loginPasswordField;
    private User currentUser;

    public LibraryFrame() {
        super("Manajemen Perpustakaan");
        bookService = new BookService();
        memberService = new MemberService(bookService);
        memberService.setRecords(
                FileManager.loadRecords(bookService.getAllBooks(), memberService.getAllMembers()));
        auth = new AuthenticationManager(memberService.getAllMembers());
        seedData();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(840, 560);
        setLocationRelativeTo(null);
        root.add(buildLoginPanel(), "login");
        setContentPane(root);
        cards.show(root, "login");
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                        "Terjadi kesalahan:\n" + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE)));
        SwingUtilities.invokeLater(() -> new LibraryFrame().setVisible(true));
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void seedData() {
        if (bookService.getAllBooks().isEmpty()) {
            bookService.addBook(new Book("B001", "Laskar Pelangi", "Andrea Hirata", 3));
            bookService.addBook(new Book("B002", "Bumi Manusia", "Pramoedya Ananta Toer", 2));
            bookService.addBook(new Book("B003", "Filosofi Teras", "Henry Manampiring", 4));
        }
        if (memberService.getAllMembers().isEmpty()) {
            memberService.addMember(new Member("M001", "Budi", "123"));
            memberService.addMember(new Member("M002", "Sari", "123"));
        }
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("Perpustakaan - Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JTextField idField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);
        loginIdField = idField;
        loginPasswordField = passwordField;
        JButton loginButton = new JButton("Login");
        JLabel hint = new JLabel("admin / admin123   |   M001 / 123");

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        panel.add(title, g);
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = 1;
        panel.add(new JLabel("ID"), g);
        g.gridx = 1;
        panel.add(idField, g);
        g.gridx = 0;
        g.gridy = 2;
        panel.add(new JLabel("Password"), g);
        g.gridx = 1;
        panel.add(passwordField, g);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        panel.add(loginButton, g);
        g.gridy = 4;
        panel.add(hint, g);

        Runnable login = () -> doLogin(idField.getText().trim(),
                new String(passwordField.getPassword()));
        loginButton.addActionListener(e -> login.run());
        passwordField.addActionListener(e -> login.run());
        return panel;
    }

    private void doLogin(String id, String password) {
        User user = auth.login(id, password);
        if (user == null) {
            warn("ID atau password salah.");
            return;
        }
        currentUser = user;
        if (mainPanel != null) {
            root.remove(mainPanel);
        }
        mainPanel = buildMainPanel();
        root.add(mainPanel, "main");
        refreshAll();
        cards.show(root, "main");
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole().equals("Admin");
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        JLabel who = new JLabel("Login sebagai: " + currentUser.getName()
                + " (" + currentUser.getRole() + ")");
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUser = null;
            loginIdField.setText("");
            loginPasswordField.setText("");
            cards.show(root, "login");
        });
        top.add(who, BorderLayout.WEST);
        top.add(logoutButton, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Buku", buildBookTab());
        if (isAdmin()) {
            tabs.addTab("Anggota", buildMemberTab());
        }
        tabs.addTab("Pinjam / Kembali", buildLoanTab());
        if (isAdmin()) {
            tabs.addTab("Overdue", buildOverdueTab());
        }

        panel.add(top, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBookTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTable table = new JTable(bookModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshBooks();

        if (!isAdmin()) {
            return panel;
        }

        JTextField idField = new JTextField(6);
        JTextField titleField = new JTextField(14);
        JTextField authorField = new JTextField(12);
        JTextField quantityField = new JTextField(4);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("ID"));
        form.add(idField);
        form.add(new JLabel("Judul"));
        form.add(titleField);
        form.add(new JLabel("Penulis"));
        form.add(authorField);
        form.add(new JLabel("Stok"));
        form.add(quantityField);

        JButton addButton = new JButton("Tambah");
        JButton editButton = new JButton("Ubah");
        JButton deleteButton = new JButton("Hapus");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(form);
        south.add(actions);
        panel.add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                return;
            }
            idField.setText(String.valueOf(bookModel.getValueAt(row, 0)));
            titleField.setText(String.valueOf(bookModel.getValueAt(row, 1)));
            authorField.setText(String.valueOf(bookModel.getValueAt(row, 2)));
            quantityField.setText(String.valueOf(bookModel.getValueAt(row, 3)));
        });

        addButton.addActionListener(e -> {
            Book book = readBookForm(idField, titleField, authorField, quantityField);
            if (book == null) {
                return;
            }
            if (!bookService.addBook(book)) {
                warn("ID buku sudah dipakai.");
                return;
            }
            refreshAll();
        });

        editButton.addActionListener(e -> {
            Book book = readBookForm(idField, titleField, authorField, quantityField);
            if (book == null) {
                return;
            }
            if (!bookService.updateBook(book)) {
                warn("Buku tidak ditemukan.");
                return;
            }
            refreshAll();
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                warn("Pilih buku dulu.");
                return;
            }
            String bookId = String.valueOf(bookModel.getValueAt(row, 0));
            if (!memberService.deleteBook(bookId)) {
                warn("Buku sedang dipinjam atau tidak ditemukan.");
                return;
            }
            refreshAll();
        });

        return panel;
    }

    private Book readBookForm(JTextField idField, JTextField titleField,
            JTextField authorField, JTextField quantityField) {
        String id = idField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        if (id.isEmpty() || title.isEmpty()) {
            warn("ID dan judul wajib diisi.");
            return null;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException ex) {
            warn("Stok harus berupa angka.");
            return null;
        }
        if (quantity < 0) {
            warn("Stok tidak boleh negatif.");
            return null;
        }
        return new Book(id, title, author, quantity);
    }

    private JPanel buildMemberTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTable table = new JTable(memberModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshMembers();

        JTextField idField = new JTextField(6);
        JTextField nameField = new JTextField(14);
        JTextField passwordField = new JTextField(10);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("ID"));
        form.add(idField);
        form.add(new JLabel("Nama"));
        form.add(nameField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JButton addButton = new JButton("Tambah");
        JButton editButton = new JButton("Ubah");
        JButton deleteButton = new JButton("Hapus");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(form);
        south.add(actions);
        panel.add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                return;
            }
            idField.setText(String.valueOf(memberModel.getValueAt(row, 0)));
            nameField.setText(String.valueOf(memberModel.getValueAt(row, 1)));
            Member selected = memberService.searchById(idField.getText());
            passwordField.setText(selected == null ? "" : selected.getPassword());
        });

        addButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String password = passwordField.getText().trim();
            if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
                warn("ID, nama, dan password wajib diisi.");
                return;
            }
            if (!memberService.addMember(new Member(id, name, password))) {
                warn("ID anggota sudah dipakai.");
                return;
            }
            refreshAll();
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                warn("Pilih anggota dulu.");
                return;
            }
            String memberId = String.valueOf(memberModel.getValueAt(row, 0));
            String error = memberService.deleteMember(memberId);
            if (error != null) {
                warn(error);
                return;
            }
            refreshAll();
        });

        editButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String password = passwordField.getText().trim();
            if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
                warn("ID, nama, dan password wajib diisi.");
                return;
            }
            if (!memberService.updateMember(new Member(id, name, password))) {
                warn("Anggota tidak ditemukan.");
                return;
            }
            refreshAll();
        });

        return panel;
    }

    private JPanel buildLoanTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        memberCombo = new JComboBox<>();
        bookCombo = new JComboBox<>();
        JButton issueButton = new JButton("Pinjam");
        JButton returnButton = new JButton("Kembalikan");

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        form.add(new JLabel("Anggota"));
        form.add(memberCombo);
        form.add(new JLabel("Buku"));
        form.add(bookCombo);
        form.add(issueButton);
        form.add(returnButton);

        JTable table = new JTable(loanModel);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshCombos();
        refreshLoans();

        issueButton.addActionListener(e -> {
            Member member = (Member) memberCombo.getSelectedItem();
            Book book = (Book) bookCombo.getSelectedItem();
            if (member == null || book == null) {
                warn("Pilih anggota dan buku.");
                return;
            }
            String error = memberService.issue(member.getId(), book.getBookId());
            if (error != null) {
                warn(error);
                return;
            }
            info("Buku berhasil dipinjam. Jatuh tempo 7 hari.");
            refreshAll();
        });

        returnButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                warn("Pilih baris pinjaman dulu.");
                return;
            }
            String memberId = String.valueOf(loanModel.getValueAt(row, 0));
            String bookId = String.valueOf(loanModel.getValueAt(row, 2));
            if (!isAdmin() && !memberId.equalsIgnoreCase(currentUser.getId())) {
                warn("Anda hanya bisa mengembalikan buku milik sendiri.");
                return;
            }
            String error = memberService.returnBook(memberId, bookId);
            if (error != null) {
                warn(error);
                return;
            }
            info("Buku berhasil dikembalikan.");
            refreshAll();
        });

        return panel;
    }

    private JPanel buildOverdueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JTable table = new JTable(overdueModel);
        JButton refreshButton = new JButton("Muat Ulang");
        refreshButton.addActionListener(e -> refreshOverdue());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.add(refreshButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        refreshOverdue();
        return panel;
    }

    private void refreshAll() {
        refreshBooks();
        refreshMembers();
        refreshCombos();
        refreshLoans();
        refreshOverdue();
    }

    private void refreshBooks() {
        bookModel.setRowCount(0);
        for (Book book : bookService.getAllBooks()) {
            bookModel.addRow(new Object[] { book.getBookId(), book.getTitle(),
                    book.getAuthor(), book.getQuantity() });
        }
    }

    private void refreshMembers() {
        memberModel.setRowCount(0);
        for (Member member : memberService.getAllMembers()) {
            memberModel.addRow(new Object[] { member.getId(), member.getName() });
        }
    }

    private void refreshCombos() {
        if (memberCombo == null || bookCombo == null) {
            return;
        }
        Object selectedMember = memberCombo.getSelectedItem();
        Object selectedBook = bookCombo.getSelectedItem();
        memberCombo.removeAllItems();
        for (Member member : memberService.getAllMembers()) {
            memberCombo.addItem(member);
        }
        bookCombo.removeAllItems();
        for (Book book : bookService.getAllBooks()) {
            bookCombo.addItem(book);
        }
        if (!isAdmin()) {
            Member self = memberService.searchById(currentUser.getId());
            if (self != null) {
                memberCombo.setSelectedItem(self);
                memberCombo.setEnabled(false);
                return;
            }
        }
        if (selectedMember != null) {
            memberCombo.setSelectedItem(selectedMember);
        }
        if (selectedBook != null) {
            bookCombo.setSelectedItem(selectedBook);
        }
    }

    private void refreshLoans() {
        loanModel.setRowCount(0);
        for (IssueRecord record : memberService.getActiveRecords()) {
            loanModel.addRow(new Object[] { record.getMember().getId(),
                    record.getMember().getName(), record.getBook().getBookId(),
                    record.getBook().getTitle(), record.getDueDate() });
        }
    }

    private void refreshOverdue() {
        overdueModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (IssueRecord record : memberService.getOverdue()) {
            long days = ChronoUnit.DAYS.between(record.getDueDate(), today);
            overdueModel.addRow(new Object[] { record.getMember().getName(),
                    record.getBook().getTitle(), record.getDueDate(), days });
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Perhatian",
                JOptionPane.WARNING_MESSAGE);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
