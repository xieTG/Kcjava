package com.xietg.kc.error;

import java.util.Map;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String code;
	private final HttpStatus status;
	private Map<String, Object> params;

	public BusinessException(String code, HttpStatus status, String message, Map<String, Object> params) {
		super(message);
		this.code = code;
		this.status = status;
		this.params = params == null ? Map.of() : params;
	}

	public BusinessException(HttpStatus status, String message) {
		super(message);
		this.code = "666";
		this.status = status;
		this.params = null;
	}

	public BusinessException( String message) {
		super(message);
		this.code = "666";
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
		this.params = null;
	}

	public void setParams(Map<String, Object> params)
	{
		this.params=params;
	}
	public String getCode() { return code; }
	public HttpStatus getStatus() { return status; }
	public Map<String, Object> getParams() { return params; }
	
    public static BusinessException unauthorized(String message) {
        return new BusinessException(HttpStatus.UNAUTHORIZED, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }
}
