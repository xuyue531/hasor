/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.quick.bean;
import java.util.ArrayList;
import java.util.List;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextAware;
import org.more.util.StringUtils;
/**
 * 提供 <code>@Bean</code>注解 功能支持。
 * @version : 2013-9-13
 * @author 赵永春 (zyc@byshell.org)
 */
public class InnerBeans extends Beans implements AppContextAware {
    private AppContext appContext;
    @Override
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }
    /**注册一个bean。 */
    @Override
    public <T> T getBean(String name) {
        List<BeanInfo> beanList = appContext.findBindingBean(BeanInfo.class);
        if (beanList.isEmpty()) {
            return null;
        }
        for (BeanInfo<?> info : beanList) {
            if (StringUtils.equalsIgnoreCase(info.getName(), name) == true) {
                return (T) appContext.getInstance(info.getReferInfo());
            }
        }
        return null;
    }
    @Override
    public List<String> getBeanNames() {
        List<String> names = new ArrayList<String>();
        List<BeanInfo> beanList = appContext.findBindingBean(BeanInfo.class);
        if (beanList.isEmpty()) {
            return names;
        }
        for (BeanInfo<?> info : beanList) {
            names.add(info.getName());
        }
        return names;
    }
}