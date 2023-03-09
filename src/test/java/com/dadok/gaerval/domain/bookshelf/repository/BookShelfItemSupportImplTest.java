package com.dadok.gaerval.domain.bookshelf.repository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

import com.dadok.gaerval.domain.book.repository.BookRepository;
import com.dadok.gaerval.domain.bookshelf.dto.request.BooksInBookShelfFindRequest;
import com.dadok.gaerval.domain.bookshelf.entity.Bookshelf;
import com.dadok.gaerval.domain.bookshelf.entity.BookshelfItem;
import com.dadok.gaerval.domain.bookshelf.entity.BookshelfItemType;
import com.dadok.gaerval.domain.job.repository.JobRepository;
import com.dadok.gaerval.domain.user.entity.Authority;
import com.dadok.gaerval.domain.user.entity.Role;
import com.dadok.gaerval.domain.user.entity.User;
import com.dadok.gaerval.domain.user.entity.UserAuthority;
import com.dadok.gaerval.domain.user.repository.AuthorityRepository;
import com.dadok.gaerval.global.oauth.OAuth2Attribute;
import com.dadok.gaerval.global.util.SortDirection;
import com.dadok.gaerval.repository.CustomDataJpaTest;
import com.dadok.gaerval.testutil.BookObjectProvider;
import com.dadok.gaerval.testutil.JobObjectProvider;
import com.dadok.gaerval.testutil.UserObjectProvider;

import lombok.RequiredArgsConstructor;

@CustomDataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class BookShelfItemSupportImplTest {

	private final BookshelfItemRepository bookshelfItemRepository;

	private final JobRepository jobRepository;

	private final BookshelfRepository bookshelfRepository;

	private final AuthorityRepository authorityRepository;

	private final BookRepository bookRepository;

	@DisplayName("findAllWithBooks - id = null, Type = Read, Sort = DESC, PageSize = 10")
	@Test
	void findAllWithBooks_() {
		BooksInBookShelfFindRequest booksInBookShelfFindRequest = new BooksInBookShelfFindRequest(
			BookshelfItemType.READ, 10, null, SortDirection.DESC);
		bookshelfItemRepository.findAllInBookShelf(null, booksInBookShelfFindRequest);
	}

	@DisplayName("findAllWithBooks - id = 50L, Type = Read, Sort = DESC, PageSize = 10")
	@Test
	void findAllWithBooks_withId() {
		BooksInBookShelfFindRequest booksInBookShelfFindRequest = new BooksInBookShelfFindRequest(
			BookshelfItemType.READ, 10, 50L, SortDirection.DESC);
		bookshelfItemRepository.findAllInBookShelf(50L, booksInBookShelfFindRequest);
	}

	@DisplayName("findAllWithBooks - id = 50L, Type = null Sort = DESC, PageSize = 10")
	@Test
	void findAllWithBooks_typeNull() {
		BooksInBookShelfFindRequest booksInBookShelfFindRequest = new BooksInBookShelfFindRequest(
			null, 10, 50L, SortDirection.DESC);
		bookshelfItemRepository.findAllInBookShelf(50L, booksInBookShelfFindRequest);
	}

	@DisplayName("findAllWithBooks - id = 50L, Type = Read, Sort = ASC, PageSize = 10")
	@Test
	void findAllWithBooks_sortASC() {
		BooksInBookShelfFindRequest booksInBookShelfFindRequest = new BooksInBookShelfFindRequest(
			BookshelfItemType.READ, 10, 50L, SortDirection.ASC);
		bookshelfItemRepository.findAllInBookShelf(50L, booksInBookShelfFindRequest);
	}

	@DisplayName("findBookshelfItemUsersByBook - 쿼리 테스트")
	@Test
	void findBookshelfItemUsersByBook() {
		// Given
		var job = jobRepository.save(JobObjectProvider.backendJob());
		Authority authority = authorityRepository.getReferenceById(Role.USER);
		OAuth2Attribute oAuth2Attribute = UserObjectProvider.kakaoAttribute();
		User user = User.createByOAuth(oAuth2Attribute, UserAuthority.create(authority));
		user.changeJob(job);
		var bookshelf = Bookshelf.create(user);
		var book = bookRepository.save(BookObjectProvider.createRequiredFieldBook());
		BookshelfItem.create(bookshelf, book);
		bookshelfRepository.save(bookshelf);

		// When
		var response = bookshelfItemRepository.findBookshelfItemUsersByBook(book.getId(), user.getId(), 5);

		// Then
		assertThat(response.isInMyBookshelf()).isEqualTo(true);
		assertThat(response.totalCount()).isEqualTo(1);
		assertThat(response.users().get(0).userId()).isEqualTo(user.getId());
	}

	@DisplayName("findBookshelfItemUsersByBook - 쿼리 테스트")
	@Test
	void findBookshelfItemUsersByBook_empty() {
		// Given // When
		var response = bookshelfItemRepository.findBookshelfItemUsersByBook(2L, 3L, 5);

		// Then
		assertThat(response.isInMyBookshelf()).isEqualTo(false);
		assertThat(response.totalCount()).isEqualTo(0);
	}
}