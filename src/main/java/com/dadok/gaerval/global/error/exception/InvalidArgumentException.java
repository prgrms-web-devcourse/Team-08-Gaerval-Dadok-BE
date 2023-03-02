package com.dadok.gaerval.global.error.exception;

import static com.dadok.gaerval.global.error.ErrorCode.*;

import com.dadok.gaerval.global.error.ErrorCode;

public class InvalidArgumentException extends BusinessException {

	private final ErrorCode errorCode = INVALID_ARGUMENT;

	public InvalidArgumentException(String message) {
		super(message);
	}

	public InvalidArgumentException(Object value, String valueName) {
		super(INVALID_ARGUMENT, String.format(INVALID_ARGUMENT.getMessage(), valueName, value));
	}

}
