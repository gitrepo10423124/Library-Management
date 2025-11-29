package com.group.library.service;

public class LibraryService {
    public final BookService bookService = new BookService();
    public final MemberService memberService = new MemberService();
    public final LoanService loanService = new LoanService();
    public final HistoryService historyService = new HistoryService();
}
