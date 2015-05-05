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
package net.hasor.web.jstl.taglib;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import net.hasor.core.AppContext;
import net.hasor.web.startup.RuntimeListener;
/**
 * 
 * @version : 2013-12-23
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class AbstractHasorTag extends TagSupport {
    private static final long serialVersionUID = 954597728447849929L;
    //
    protected AppContext getAppContext() {
        ServletContext sc = this.pageContext.getServletContext();
        AppContext appContext = (AppContext) sc.getAttribute(RuntimeListener.AppContextName);
        if (appContext != null) {
            return appContext;
        }
        throw new NullPointerException("AppContext is undefined.");
    }
    //
    @Override
    public abstract int doStartTag() throws JspException;
    @Override
    public abstract void release();
}