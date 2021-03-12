package com.linglong.server.http.restful.api;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author Stereo on 2019/8/7.
 */
public abstract class AbstractRestActionApi implements RestActionApi {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RestActionApi.class);

    protected final RestActionHandler restActionHandler;
    protected final HashMap _methodMap = new HashMap();
    protected final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public AbstractRestActionApi(RestActionHandler restActionHandler) {
        this.restActionHandler = restActionHandler;
        Method[] methodList = RestActionApi.class.getMethods();
        for (int i = 0; i < methodList.length; i++) {
            Method method = methodList[i];
            if (_methodMap.get(method.getName()) == null)
                _methodMap.put(method.getName(), methodList[i]);
            Class[] param = method.getParameterTypes();
            String mangledName = method.getName() + "__" + param.length;
            _methodMap.put(mangledName, methodList[i]);
            _methodMap.put(mangleName(method), methodList[i]);
        }
    }

    protected String mangleName(Method method) {
        StringBuffer sb = new StringBuffer();
        sb.append(method.getName());
        Class[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            sb.append('_');
            sb.append(params[i].getName());
        }
        return sb.toString();
    }

    protected Method getMethod(String mangledName) {
        return (Method) _methodMap.get(mangledName);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //TODO
    }

    protected void renderJsonResult(HttpServletResponse response, Object result) throws IOException {
        renderJsonResult(response, result == null ? HttpStatus.NO_CONTENT : HttpStatus.OK, result == null ? null : JSON.toJSONString(result));
    }

    protected void renderJsonResult(HttpServletResponse response, HttpStatus httpStatus, String result) throws IOException {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(REST_API_ENCODING);
        response.setContentType(REST_API_CONTENT_TYPE);
        if (result != null) {
            response.getWriter().write(result);
            response.getWriter().flush();
        }
    }

    protected void renderJsonError(HttpServletResponse response, String path, HttpStatus httpStatus, Throwable ex) throws IOException {
        response.setStatus(httpStatus.value());
        response.setCharacterEncoding(REST_API_ENCODING);
        response.setContentType(REST_API_CONTENT_TYPE);
        response.getWriter().write(ErrorMessage.errorJson(path, httpStatus, ex));
        response.getWriter().flush();
    }

    protected static class ErrorMessage implements Serializable {
        private long timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        ErrorMessage(String path, HttpStatus httpStatus, Throwable ex) {
            this.timestamp = System.currentTimeMillis();
            this.status = httpStatus.value();
            this.error = httpStatus.getReasonPhrase();
            this.path = path;
            this.message = ex.getMessage();
        }

        static String errorJson(String path, HttpStatus httpStatus, Throwable ex) {
            return JSON.toJSONString(new ErrorMessage(path, httpStatus, ex));
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
