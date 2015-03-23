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
package net.hasor.quick.j2ee;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import net.hasor.quick.plugin.Plugin;
import net.hasor.quick.setting.Settings;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
import org.more.logger.LoggerHelper;
import org.more.util.StringUtils;
/**
 * 
 * @version : 2013-9-26
 * @author 赵永春(zyc@hasor.net)
 */
@Plugin
public class JavaEEPlugin extends WebModule {
    public void loadModule(WebApiBinder apiBinder) throws Throwable {
        if (apiBinder instanceof WebApiBinder == false)
            return;
        //1.LoadFilter.
        this.loadFilter(apiBinder);
        //2.LoadServlet.
        this.loadServlet(apiBinder);
    }
    //
    /**装载Filter*/
    protected void loadFilter(WebApiBinder apiBinder) {
        //1.获取
        Set<Class<?>> webFilterSet = apiBinder.findClass(WebFilter.class);
        if (webFilterSet == null)
            return;
        List<Class<? extends Filter>> webFilterList = new ArrayList<Class<? extends Filter>>();
        for (Class<?> cls : webFilterSet) {
            if (cls == WebFilter.class) {
                continue;
            }
            if (Filter.class.isAssignableFrom(cls) == false) {
                LoggerHelper.logWarn("not implemented Filter :%s", cls);
            } else {
                webFilterList.add((Class<? extends Filter>) cls);
            }
        }
        //2.注册
        for (Class<? extends Filter> filterType : webFilterList) {
            WebFilter filterAnno = filterType.getAnnotation(WebFilter.class);
            Map<String, String> initMap = this.toMap(filterAnno.initParams());
            apiBinder.filter(null, filterAnno.value()).through(filterAnno.sort(), filterType, initMap);
            //
            String filterName = StringUtils.isBlank(filterAnno.filterName()) ? filterType.getSimpleName() : filterAnno.filterName();
            LoggerHelper.logInfo("loadFilter %s[%s] bind %s on %s.", filterName, getIndexStr(filterAnno.sort()), filterType, filterAnno.value());
        }
    }
    //
    /**装载Servlet*/
    protected void loadServlet(WebApiBinder apiBinder) {
        //1.获取
        Set<Class<?>> webServletSet = apiBinder.findClass(WebServlet.class);
        if (webServletSet == null)
            return;
        List<Class<? extends HttpServlet>> webServletList = new ArrayList<Class<? extends HttpServlet>>();
        for (Class<?> cls : webServletSet) {
            if (cls == WebServlet.class) {
                continue;
            }
            if (HttpServlet.class.isAssignableFrom(cls) == false) {
                LoggerHelper.logWarn("not implemented HttpServlet :%s", cls);
            } else {
                webServletList.add((Class<? extends HttpServlet>) cls);
            }
        }
        //2.注册
        for (Class<? extends HttpServlet> servletType : webServletList) {
            WebServlet servletAnno = servletType.getAnnotation(WebServlet.class);
            Map<String, String> initMap = this.toMap(servletAnno.initParams());
            String servletName = StringUtils.isBlank(servletAnno.servletName()) ? servletType.getSimpleName() : servletAnno.servletName();
            int sortInt = servletAnno.loadOnStartup();
            //
            apiBinder.serve(null, servletAnno.value()).with(sortInt, servletType, initMap);
            LoggerHelper.logInfo("loadServlet %s[%s] bind %s on %s.", servletName, getIndexStr(sortInt), servletType, servletAnno.value());
        }
    }
    //
    /**转换参数*/
    protected Map<String, String> toMap(WebInitParam[] initParams) {
        Map<String, String> initMap = new HashMap<String, String>();
        if (initParams != null) {
            for (WebInitParam param : initParams) {
                if (StringUtils.isBlank(param.name()) == false) {
                    initMap.put(param.name(), param.value());
                }
            }
        }
        return initMap;
    }
    //
    /***/
    private static String getIndexStr(int index) {
        int allRange = 1000;
        /*-----------------------------------------*/
        int minStartIndex = Integer.MIN_VALUE;
        int minStopIndex = Integer.MIN_VALUE + allRange;
        for (int i = minStartIndex; i < minStopIndex; i++) {
            if (index == i) {
                return "Min" + ((index == Integer.MIN_VALUE) ? "" : ("+" + String.valueOf(i + Math.abs(Integer.MIN_VALUE))));
            }
        }
        int maxStartIndex = Integer.MAX_VALUE;
        int maxStopIndex = Integer.MAX_VALUE - allRange;
        for (int i = maxStartIndex; i > maxStopIndex; i--) {
            if (index == i) {
                return "Max" + ((index == Integer.MAX_VALUE) ? "" : ("-" + Math.abs(Integer.MAX_VALUE - i)));
            }
        }
        return String.valueOf(index);
    }
}