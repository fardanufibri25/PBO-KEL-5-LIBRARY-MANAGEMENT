package perpus;

import java.time.LocalDate;

public class IssueRecord {
    private Member member;
    private Book book;
    private LocalDate dueDate;

    public IssueRecord(Member member, Book book, LocalDate dueDate) {
        this.member = member;
        this.book = book;
        this.dueDate = dueDate;
    }

    public Member getMember() {
        return member;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isOverdue() {
        return dueDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return member.getName() + " - " + book.getTitle() + " (jatuh tempo " + dueDate + ")";
    }
}
