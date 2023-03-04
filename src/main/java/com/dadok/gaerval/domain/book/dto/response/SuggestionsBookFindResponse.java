package com.dadok.gaerval.domain.book.dto.response;

import com.dadok.gaerval.domain.job.entity.JobGroup;

public record SuggestionsBookFindResponse(
	Long bookId,
	String imageUrl,
	String title,
	String author,
	String isbn,
	String publisher,
	String url,
	String jobGroup,
	String jobName,
	Long count
) {

	public SuggestionsBookFindResponse(Long bookId, String imageUrl, String title, String author, String isbn,
		String publisher, String url, JobGroup jobGroup, JobGroup.JobName jobName, Long count) {
		this(
			bookId,
			imageUrl,
			title,
			author,
			isbn,
			publisher,
			url,
			jobGroup.getGroupName(),
			jobName.getJobName(),
			count
		);
	}
}
