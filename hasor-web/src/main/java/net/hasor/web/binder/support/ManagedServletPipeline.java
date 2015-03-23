/*
 * Copyright 2008-2009 the original 赵永春(zyc@hasor.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.web.binder.support;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import net.hasor.core.AppContext;
import net.hasor.web.WebAppContext;
/**
 * 
 * @version : 2013-4-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class ManagedServletPipeline {
    private ServletDefinition[] servletDefinitions;
    private volatile boolean    initialized = false;
    //
    public synchronized void initPipeline(final WebAppContext appContext, final Map<String, String> filterConfig) throws ServletException {
        if (this.initialized) {
            return;
        }
        this.servletDefinitions = this.collectServletDefinitions(appContext);
        for (ServletDefinition servletDefinition : this.servletDefinitions) {
            servletDefinition.init(appContext, filterConfig);
        }
        //everything was ok...
        this.initialized = true;
    }
    private ServletDefinition[] collectServletDefinitions(final WebAppContext appContext) {
        List<ServletDefinition> servletDefinitions = appContext.findBindingBean(ServletDefinition.class);
        Collections.sort(servletDefinitions, new Comparator<ServletDefinition>() {
            @Override
            public int compare(final ServletDefinition o1, final ServletDefinition o2) {
                int o1Index = o1.getIndex();
                int o2Index = o2.getIndex();
                return o1Index < o2Index ? -1 : o1Index == o2Index ? 0 : 1;
            }
        });
        return servletDefinitions.toArray(new ServletDefinition[servletDefinitions.size()]);
    }
    public boolean hasServletsMapped() {
        return this.servletDefinitions.length > 0;
    }
    //
    //
    public boolean service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IOException, ServletException {
        //stop at the first matching servlet and service
        for (ServletDefinition servletDefinition : this.servletDefinitions) {
            if (servletDefinition.service(servletRequest, servletResponse)) {
                return true;
            }
        }
        //there was no match...
        return false;
    }
    public void destroyPipeline(final AppContext appContext) {
        for (ServletDefinition servletDefinition : this.servletDefinitions) {
            servletDefinition.destroy();
        }
    }
    //
    //
    //
    //
    //
    /**返回一个RequestDispatcher*/
    RequestDispatcher getRequestDispatcher(final String path) {
        final String newRequestUri = path;
        // TODO 需要检查下面代码是否符合Servlet规范（带request参数情况下也需要检查）
        for (final ServletDefinition servletDefinition : this.servletDefinitions) {
            if (servletDefinition.matchesUri(path)) {
                return new RequestDispatcher() {
                    @Override
                    public void forward(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
                        if (servletResponse.isCommitted() == true) {
                            throw new ServletException("Response has been committed--you can only call forward before committing the response (hint: don't flush buffers)");
                        }
                        servletResponse.resetBuffer();
                        ServletRequest requestToProcess;
                        if (servletRequest instanceof HttpServletRequest) {
                            //使用RequestDispatcherRequestWrapper类处理request.getRequestURI方法的返回值
                            String servletPath = ((HttpServletRequest) servletRequest).getContextPath() + "/" + newRequestUri;
                            servletPath = servletPath.replaceAll("/{2,}", "/");
                            requestToProcess = new RequestDispatcherRequestWrapper(servletRequest, servletPath);
                        } else {
                            //通常不会进入这段代码.
                            requestToProcess = servletRequest;
                        }
                        servletRequest.setAttribute(ManagedServletPipeline.REQUEST_DISPATCHER_REQUEST, Boolean.TRUE);
                        /*执行转发*/
                        try {
                            servletDefinition.service(requestToProcess, servletResponse);
                        } finally {
                            servletRequest.removeAttribute(ManagedServletPipeline.REQUEST_DISPATCHER_REQUEST);
                        }
                    }
                    @Override
                    public void include(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
                        servletRequest.setAttribute(ManagedServletPipeline.REQUEST_DISPATCHER_REQUEST, Boolean.TRUE);
                        /*执行servlet方法*/
                        try {
                            servletDefinition.service(servletRequest, servletResponse);
                        } finally {
                            servletRequest.removeAttribute(ManagedServletPipeline.REQUEST_DISPATCHER_REQUEST);
                        }
                    }
                };
            }
        }
        //otherwise, can't process
        return null;
    }
    /** 使用RequestDispatcherRequestWrapper类处理request.getRequestURI方法的返回值*/
    public static final String REQUEST_DISPATCHER_REQUEST = "javax.servlet.forward.servlet_path";
    private static class RequestDispatcherRequestWrapper extends HttpServletRequestWrapper {
        private final String newRequestUri;
        public RequestDispatcherRequestWrapper(final ServletRequest servletRequest, final String newRequestUri) {
            super((HttpServletRequest) servletRequest);
            this.newRequestUri = newRequestUri;
        }
        @Override
        public String getRequestURI() {
            return this.newRequestUri;
        }
    }
}