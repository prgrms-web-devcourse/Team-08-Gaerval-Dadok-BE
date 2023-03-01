package com.dadok.gaerval.domain.book.dto.response;

public record SearchBookResponse(String title, String author, String isbn, String contents, String url,
								 String imageUrl, String apiProvider, String publisher) {
}
