package com.dadok.gaerval.domain.book_group.repository;

import static com.dadok.gaerval.domain.book.entity.QBook.*;
import static com.dadok.gaerval.domain.book_group.entity.QBookGroup.*;
import static com.dadok.gaerval.domain.book_group.entity.QGroupComment.*;
import static com.dadok.gaerval.domain.book_group.entity.QGroupMember.*;
import static com.dadok.gaerval.domain.user.entity.QUser.*;
import static com.querydsl.core.types.Projections.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import com.dadok.gaerval.domain.book_group.dto.request.BookGroupSearchRequest;
import com.dadok.gaerval.domain.book_group.dto.response.BookGroupDetailResponse;
import com.dadok.gaerval.domain.book_group.dto.response.BookGroupResponse;
import com.dadok.gaerval.domain.book_group.dto.response.BookGroupResponses;
import com.dadok.gaerval.domain.book_group.entity.BookGroup;
import com.dadok.gaerval.global.error.exception.ResourceNotfoundException;
import com.dadok.gaerval.global.util.QueryDslUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookGroupSupportImpl implements BookGroupSupport {

	private final JPAQueryFactory query;

	@Override
	public BookGroupResponses findAllBy(BookGroupSearchRequest request) {

		Sort.Direction direction = request.sortDirection().toDirection();

		List<BookGroupResponse> groupResponses = query.select(constructor(BookGroupResponse.class,
				bookGroup.id.as("bookGroupId"),
				bookGroup.title.as("title"),
				bookGroup.introduce.as("introduce"),
				bookGroup.maxMemberCount.as("maxMemberCount"),
				bookGroup.hasJoinPasswd.as("hasJoinPasswd"),
				bookGroup.isPublic.as("isPublic"),

				groupMember.count().as("memberCount"),
				groupComment.count().as("commentCount"),

				Projections.constructor(BookGroupResponse.BookResponse.class,
					book.id.as("bookId"),
					book.imageUrl.as("imageUrl")
				),

				Projections.constructor(BookGroupResponse.OwnerResponse.class,
					user.id.as("ownerId"),
					user.profileImage.as("ownerProfileUrl"),
					user.nickname.nickname.as("ownerNickname")
				)
			))
			.from(bookGroup)
			.innerJoin(book).on(book.id.eq(bookGroup.book.id))
			.innerJoin(user).on(user.id.eq(bookGroup.ownerId))
			.leftJoin(bookGroup.groupMembers, groupMember)
			.leftJoin(bookGroup.comments, groupComment)
			.where(
				QueryDslUtil.generateCursorWhereCondition(bookGroup.id, request.groupCursorId(), direction)
			)
			.groupBy(bookGroup.id,
				book.id,
				user.id
			)
			.orderBy(QueryDslUtil.getOrder(bookGroup.id, direction))
			.limit(request.pageSize() + 1)
			.fetch();

		Slice<BookGroupResponse> bookGroupResponses = QueryDslUtil.toSlice(groupResponses,
			PageRequest.of(0, request.pageSize(), Sort.by(direction, "id")));

		return new BookGroupResponses(bookGroupResponses);
	}

	@Override
	public BookGroupDetailResponse findBookGroup(Long requestUserId, Long groupId) {
		Tuple tuple = query
			.select(
				bookGroup.id,
				bookGroup.title,
				bookGroup.introduce,
				bookGroup.ownerId,

				bookGroup.startDate,
				bookGroup.endDate,
				bookGroup.maxMemberCount,
				bookGroup.hasJoinPasswd,
				bookGroup.isPublic,

				book.title,
				book.imageUrl,
				book.id,

				groupMember.count(),
				groupComment.count()
			)
			.from(bookGroup)
			.leftJoin(bookGroup.groupMembers, groupMember)
			.leftJoin(bookGroup.comments, groupComment)
			.leftJoin(bookGroup.book, book)
			.where(bookGroup.id.eq(groupId))
			.groupBy(bookGroup.id)
			.fetchOne();

		if (tuple == null) {
			throw new ResourceNotfoundException(BookGroup.class);
		}

		boolean isGroupMember =
			requestUserId != null && query.selectOne()
				.from(groupMember)
				.where(groupMember.bookGroup.id.eq(groupId),
					groupMember.user.id.eq(requestUserId)
				)
				.fetchFirst() != null;

		return new BookGroupDetailResponse(
			tuple.get(bookGroup.id),
			tuple.get(bookGroup.title),
			tuple.get(bookGroup.introduce),
			tuple.get(bookGroup.startDate),
			tuple.get(bookGroup.endDate),
			tuple.get(bookGroup.hasJoinPasswd),
			tuple.get(bookGroup.isPublic),
			tuple.get(bookGroup.maxMemberCount),
			tuple.get(groupMember.count()),
			tuple.get(groupComment.count()),
			new BookGroupDetailResponse.OwnerResponse(tuple.get(bookGroup.ownerId)),
			new BookGroupDetailResponse.BookResponse(tuple.get(book.id), tuple.get(book.imageUrl),
				tuple.get(book.title)),

			Objects.equals(requestUserId, tuple.get(bookGroup.ownerId)), isGroupMember
		);
	}

	@Override
	public BookGroupResponses findAllByUser(BookGroupSearchRequest request, Long userId) {
		Sort.Direction direction = request.sortDirection().toDirection();

		List<BookGroupResponse> groupResponses = query.select(constructor(BookGroupResponse.class,
				bookGroup.id.as("bookGroupId"),
				bookGroup.title.as("title"),
				bookGroup.introduce.as("introduce"),
				bookGroup.maxMemberCount.as("maxMemberCount"),
				bookGroup.hasJoinPasswd.as("hasJoinPasswd"),
				bookGroup.isPublic,

				groupMember.count().as("memberCount"),
				groupComment.count().as("commentCount"),
				Projections.constructor(BookGroupResponse.BookResponse.class,
					book.id.as("bookId"),
					book.imageUrl.as("imageUrl")
				),

				Projections.constructor(BookGroupResponse.OwnerResponse.class,
					user.id.as("ownerId"),
					user.profileImage.as("ownerProfileUrl"),
					user.nickname.nickname.as("ownerNickname")
				)
			))
			.from(bookGroup)
			.innerJoin(book).on(book.id.eq(bookGroup.book.id))
			.innerJoin(user).on(user.id.eq(bookGroup.ownerId))
			.leftJoin(bookGroup.groupMembers, groupMember)
			.leftJoin(bookGroup.comments, groupComment)
			.where(
				QueryDslUtil.generateCursorWhereCondition(bookGroup.id, request.groupCursorId(), direction),
				QueryDslUtil.generateIdWhereCondition(user.id, userId)
			)
			.groupBy(bookGroup.id,
				book.id,
				user.id
			)
			.orderBy(QueryDslUtil.getOrder(bookGroup.id, direction))
			.limit(request.pageSize() + 1)
			.fetch();

		Slice<BookGroupResponse> bookGroupResponses = QueryDslUtil.toSlice(groupResponses,
			PageRequest.of(0, request.pageSize(), Sort.by(direction, "id")));

		return new BookGroupResponses(bookGroupResponses);
	}

}
