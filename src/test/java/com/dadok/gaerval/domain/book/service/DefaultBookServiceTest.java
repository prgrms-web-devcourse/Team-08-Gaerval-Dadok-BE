package com.dadok.gaerval.domain.book.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dadok.gaerval.domain.book.converter.BookMapper;
import com.dadok.gaerval.domain.book.dto.request.BookCreateRequest;
import com.dadok.gaerval.domain.book.dto.request.SortingPolicy;
import com.dadok.gaerval.domain.book.dto.response.BookResponse;
import com.dadok.gaerval.domain.book.dto.response.BookResponses;
import com.dadok.gaerval.domain.book.dto.response.SearchBookResponse;
import com.dadok.gaerval.domain.book.entity.Book;
import com.dadok.gaerval.domain.book.repository.BookRepository;
import com.dadok.gaerval.global.config.externalapi.ExternalBookApiOperations;
import com.dadok.gaerval.testutil.BookObjectProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DefaultBookServiceTest {

	@Mock
	private ExternalBookApiOperations externalBookApiOperations;

	@InjectMocks
	private DefaultBookService defaultBookService;

	@Mock
	private BookRepository bookRepository;

	@Mock
	private BookMapper bookMapper;

	@Mock
	private ObjectMapper objectMapper;


	@DisplayName("createBook - 도서를 저장하는데 성공한다.")
	@Test
	void createBook() {
		// given
		Book book = BookObjectProvider.createRequiredFieldBook();
		ReflectionTestUtils.setField(book, "id", 1234L);

		given(bookRepository.save(any()))
			.willReturn(book);

		// when
		BookCreateRequest bookCreateRequest = BookObjectProvider.createBookCreateRequest();
		Book savedBook = defaultBookService.createBook(bookCreateRequest);

		// then
		verify(bookRepository).save(any());
		assertEquals(book.getId(), savedBook.getId());
	}

	@DisplayName("getById - bookId로 조회에 성공한다.")
	@Test
	void getById() {
		// given
		Book book = BookObjectProvider.createRequiredFieldBook();
		given(bookRepository.findById(BookObjectProvider.bookId))
			.willReturn(Optional.of(book));

		// when
		Book findBook = defaultBookService.getById(BookObjectProvider.bookId);

		// then
		verify(bookRepository).findById(BookObjectProvider.bookId);
		assertEquals(book, findBook);
	}

	@DisplayName("findById - bookId로 조회에 성공한다.")
	@Test
	void findById() {
		// given
		Book book = BookObjectProvider.createRequiredFieldBook();
		given(bookRepository.findById(BookObjectProvider.bookId))
			.willReturn(Optional.of(book));

		// when
		Optional<Book> findBook = defaultBookService.findById(BookObjectProvider.bookId);

		// then
		verify(bookRepository).findById(BookObjectProvider.bookId);
		assertTrue(findBook.isPresent());
		assertEquals(book, findBook.get());
	}

	@DisplayName("findByIsbn - isbn으로 조회에 성공한다.")
	@Test
	void findByIsbn() {
		// given
		Book book = BookObjectProvider.createRequiredFieldBook();
		given(bookRepository.findById(BookObjectProvider.bookId))
			.willReturn(Optional.of(book));

		// when
		Optional<Book> findBook = defaultBookService.findById(BookObjectProvider.bookId);

		// then
		verify(bookRepository).findById(BookObjectProvider.bookId);
		assertTrue(findBook.isPresent());
		assertEquals(book, findBook.get());
	}

	@DisplayName("findDetailById - id로 조회에 성공한다.")
	@Test
	void findDetailById() {
		// given
		Book book = BookObjectProvider.createRequiredFieldBook();
		BookResponse expectedResponse = BookObjectProvider.createBookResponse();
		given(bookRepository.findById(BookObjectProvider.bookId))
			.willReturn(Optional.of(book));
		given(bookMapper.entityToBookResponse(book))
			.willReturn(expectedResponse);

		// when
		BookResponse actualResponse = defaultBookService.findDetailById(BookObjectProvider.bookId);

		// then
		verify(bookRepository).findById(BookObjectProvider.bookId);
		verify(bookMapper).entityToBookResponse(book);
		assertEquals(expectedResponse, actualResponse);
	}

	@DisplayName("findAllByKeyword - 키워드로 검색하는데 성공한다.")
	@Test
	void findAllByKeyword() throws JsonProcessingException {
		// given
		String keyword = "test";
		String result = """
        {
            "meta": {
                "is_end": true,
                "total_count": 1,
                "pageable_count": 1
            },
            "documents": [
                {
                    "authors": [
                        "%s"
                    ],
                    "contents": "%s",
                    "isbn": "%s",
                    "publisher": "%s",
                    "thumbnail": "%s",
                    "title": "%s",
                    "url": "%s"
                }
            ]
        }
        """.formatted(BookObjectProvider.author, BookObjectProvider.contents, BookObjectProvider.isbn, BookObjectProvider.publisher, BookObjectProvider.imageUrl, BookObjectProvider.title, BookObjectProvider.url);

		JsonNode jsonNode = new ObjectMapper().readTree(result);
		SearchBookResponse searchBookResponse = new SearchBookResponse(
			BookObjectProvider.title,
			BookObjectProvider.author,
			BookObjectProvider.isbn,
			BookObjectProvider.contents,
			BookObjectProvider.url,
			BookObjectProvider.imageUrl,
			BookObjectProvider.apiProvider,
			BookObjectProvider.publisher
		);

		List<SearchBookResponse> expectedResponses = Collections.singletonList(searchBookResponse);

		given(externalBookApiOperations.searchBooks(keyword, 1, 10, SortingPolicy.ACCURACY.getName()))
			.willReturn(result);
		given(objectMapper.readTree(result)).willReturn(jsonNode);
		given(bookMapper.entityToSearchBookResponse(any())).willReturn(searchBookResponse);

		// when
		BookResponses actualResponses = defaultBookService.findAllByKeyword(keyword);

		// then
		verify(externalBookApiOperations).searchBooks(keyword, 1, 10, SortingPolicy.ACCURACY.getName());
		verify(objectMapper).readTree(result);
		verify(bookMapper).entityToSearchBookResponse(any());
		assertEquals(expectedResponses.get(0).isbn(), actualResponses.searchBookResponseList().get(0).isbn());
		assertEquals(expectedResponses.get(0).title(), actualResponses.searchBookResponseList().get(0).title());
		assertEquals(expectedResponses.get(0).contents(), actualResponses.searchBookResponseList().get(0).contents());
		assertEquals(expectedResponses.get(0).url(), actualResponses.searchBookResponseList().get(0).url());
	}
}