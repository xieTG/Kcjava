package com.xietg.kc.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.net.URI;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler
{

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, HttpServletRequest req)
	{

		ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
		pd.setTitle("Business rule violated");
		pd.setType(URI.create("https://api.app/errors/" + ex.getCode()));
		pd.setInstance(URI.create(req.getRequestURI()));

		pd.setProperty("code", ex.getCode());

		Map<String, Object> mergedParams = new LinkedHashMap<>();
		mergedParams.putAll(safeRequestInputs(req)); // automatic API input
		if (ex.getParams() != null)
		{
			mergedParams.putAll(ex.getParams()); // business-specific context overrides if same keys
		}
		pd.setProperty("params", mergedParams);

		return ResponseEntity.status(ex.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
	}

	private Map<String, Object> safeRequestInputs(HttpServletRequest req)
	{
		Map<String, Object> out = new LinkedHashMap<>();

		// Path variables
		@SuppressWarnings("unchecked")
		Map<String, String> pathVars = (Map<String, String>) req
				.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		if (pathVars != null && !pathVars.isEmpty())
			out.put("path", pathVars);

		// Query params (note: String[])
		Map<String, String[]> qp = req.getParameterMap();
		if (qp != null && !qp.isEmpty())
			out.put("query", qp);

		// Optional: safe headers (avoid auth/cookies)
		Map<String, String> headers = new LinkedHashMap<>();
		Enumeration<String> names = req.getHeaderNames();
		while (names != null && names.hasMoreElements())
		{
			String name = names.nextElement();
			String lower = name.toLowerCase(Locale.ROOT);
			if (lower.equals("authorization") || lower.equals("cookie") || lower.equals("set-cookie"))
				continue;
			headers.put(name, req.getHeader(name));
		}
		if (!headers.isEmpty())
			out.put("headers", headers);

		out.put("method", req.getMethod());
		out.put("uri", req.getRequestURI());
		return out;
	}
}