package com.dadok.gaerval.domain.bookshelf.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dadok.gaerval.domain.book.dto.request.BookCreateRequest;
import com.dadok.gaerval.domain.book.entity.Book;
import com.dadok.gaerval.domain.book.service.BookService;
import com.dadok.gaerval.domain.bookshelf.dto.response.DetailBookshelfResponse;
import com.dadok.gaerval.domain.bookshelf.dto.response.PopularBookshelvesOfJobResponses;
import com.dadok.gaerval.domain.bookshelf.dto.response.SummaryBookshelfResponse;
import com.dadok.gaerval.domain.bookshelf.entity.Bookshelf;
import com.dadok.gaerval.domain.bookshelf.entity.BookshelfItem;
import com.dadok.gaerval.domain.bookshelf.repository.BookshelfItemRepository;
import com.dadok.gaerval.domain.bookshelf.repository.BookshelfRepository;
import com.dadok.gaerval.domain.job.entity.JobGroup;
import com.dadok.gaerval.domain.user.entity.User;
import com.dadok.gaerval.domain.user.service.UserService;
import com.dadok.gaerval.global.error.exception.ResourceNotfoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DefaultBookshelfService implements BookshelfService {

	private final BookshelfRepository bookshelfRepository;

	private final BookshelfItemRepository bookshelfItemRepository;

	private final BookService bookService;

	private final UserService userService;

	@Override
	@Transactional(readOnly = true)
	public Bookshelf getById(Long bookshelfId) {
		return bookshelfRepository.findById(bookshelfId)
			.orElseThrow(() -> new ResourceNotfoundException(Bookshelf.class));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Bookshelf> findById(Long bookshelfId) {
		return bookshelfRepository.findById(bookshelfId);
	}

	@Override
	@Transactional(readOnly = true)
	public DetailBookshelfResponse findDetailById(Long bookshelfId) {
		return null;
	}

	@Override
	public Long createBookshelf(User user) {
		Bookshelf bookshelf = Bookshelf.create(user);
		return bookshelfRepository.save(bookshelf).getId();
	}

	@Override
	@Transactional(readOnly = true)
	public PopularBookshelvesOfJobResponses findPopularBookshelvesByJob(User user, String jobGroup) {
		JobGroup searchJobGroup = JobGroup.findJobGroup(jobGroup);
		List<SummaryBookshelfResponse> summaryBookshelfResponses = bookshelfRepository.findAllByJob(searchJobGroup,
			PageRequest.of(0, 5, Sort.by(Sort.Order.desc("bookshelfItems.size"))), user.getId());
		summaryBookshelfResponses.forEach(bookshelf -> {
			bookshelf.setBooks(bookshelf.getBooks().stream().limit(5).toList());
		});
		return new PopularBookshelvesOfJobResponses(jobGroup, summaryBookshelfResponses);
	}

	@Override
	public Long insertBookSelfItem(User user, Long bookshelfId, BookCreateRequest bookCreateRequest) {
		Bookshelf bookshelf = validationBookshelfUser(user, bookshelfId);
		Book book = bookService.findByIsbn(bookCreateRequest.isbn())
			.orElseGet(() -> bookService.createBook(bookCreateRequest));
		BookshelfItem bookshelfItem = BookshelfItem.create(bookshelf, book);
		bookshelfItemRepository.save(bookshelfItem);
		return bookshelfId;
	}

	@Override
	public Long removeBookSelfItem(User user, Long bookshelfId, Long bookId) {
		Bookshelf bookshelf = validationBookshelfUser(user, bookshelfId);
		Book book = bookService.findById(bookId).orElseThrow(() -> new ResourceNotfoundException(Book.class));
		BookshelfItem bookshelfItem = bookshelfItemRepository.findByBookshelfAndBook(bookshelf, book)
			.orElseThrow(() -> new ResourceNotfoundException(BookshelfItem.class));
		bookshelfItemRepository.deleteById(bookshelfItem.getId());
		return bookshelfId;
	}

	@Override
	public SummaryBookshelfResponse findSummaryBookshelf(User user) {
		SummaryBookshelfResponse summaryBookshelf = bookshelfRepository.findByUser(user);
		summaryBookshelf.setBooks(summaryBookshelf.getBooks().stream().limit(5).toList());
		return summaryBookshelf;
	}

	@Override
	public SummaryBookshelfResponse findSummaryBookshelf(Long userId) {
		User user = userService.getById(userId);
		SummaryBookshelfResponse summaryBookshelf = bookshelfRepository.findByUser(user);
		summaryBookshelf.setBooks(summaryBookshelf.getBooks().stream().limit(5).toList());
		return summaryBookshelf;
	}

	private Bookshelf validationBookshelfUser(User user, Long bookshelfId) {
		Bookshelf bookshelf = bookshelfRepository.findById(bookshelfId)
			.orElseThrow(() -> new ResourceNotfoundException(Bookshelf.class));
		bookshelf.validateOwner(user.getId());
		return bookshelf;
	}
}
