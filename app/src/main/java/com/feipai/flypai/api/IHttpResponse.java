package com.feipai.flypai.api;

/**
 * HTTP 访问响应
 *
 * @author yanglin
 */
public interface IHttpResponse<T> {
    void onResponseSuccess(T t);


    void onResponseError(ApiException apiException);
}
