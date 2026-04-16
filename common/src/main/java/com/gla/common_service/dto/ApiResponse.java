package com.gla.common_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uniform API response wrapper.
 * ALL services must wrap their responses in this class.
 *
 * Success:  { success:true,  message:"...", data:{...},  statusCode:200, timestamp:"..." }
 * Error:    { success:false, error:"...",               statusCode:4xx, timestamp:"..." }
 *
 * Usage:
 *   return ResponseEntity.ok(ApiResponse.success("Login successful", token));
 *   return ResponseEntity.badRequest().body(ApiResponse.error("Invalid input", 400));
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String  message;
    private T       data;
    private String  error;
    private int     statusCode;
    private String  timestamp;

    // ─── constructors ────────────────────────────────────────────────────────

    private ApiResponse() {
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // ─── factory helpers ─────────────────────────────────────────────────────

    /** 200 OK with payload */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success    = true;
        r.message    = message;
        r.data       = data;
        r.statusCode = 200;
        return r;
    }

    /** 201 Created with payload */
    public static <T> ApiResponse<T> created(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success    = true;
        r.message    = message;
        r.data       = data;
        r.statusCode = 201;
        return r;
    }

    /** Error with explicit status code */
    public static <T> ApiResponse<T> error(String errorMessage, int statusCode) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success    = false;
        r.error      = errorMessage;
        r.statusCode = statusCode;
        return r;
    }

    // ─── getters (required by Jackson) ───────────────────────────────────────

    public boolean isSuccess()   { return success;    }
    public String  getMessage()  { return message;    }
    public T       getData()     { return data;       }
    public String  getError()    { return error;      }
    public int     getStatusCode(){ return statusCode; }
    public String  getTimestamp(){ return timestamp;  }
}