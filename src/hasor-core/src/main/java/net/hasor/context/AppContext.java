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
package net.hasor.context;
import org.more.UndefinedException;
import com.google.inject.Injector;
/**
 * 应用程序上下文
 * @version : 2013-3-26
 * @author 赵永春 (zyc@hasor.net)
 */
public interface AppContext extends InitContext, LifeCycle {
    /**通过名获取Bean的类型。*/
    public <T> Class<T> getBeanType(String name);
    /**如果存在目标类型的Bean则返回Bean的名称。*/
    public String getBeanName(Class<?> targetClass);
    /**获取已经注册的Bean名称。*/
    public String[] getBeanNames();
    /**获取bean信息。*/
    public BeanInfo getBeanInfo(String name);
    /**通过名称创建bean实例，使用guice，如果获取的bean不存在则会引发{@link UndefinedException}类型异常。*/
    public <T> T getBean(String name);
    /**通过类型创建该类实例，使用guice*/
    public <T> T getInstance(Class<T> beanType);
    /**获得Guice环境。*/
    public Injector getGuice();
    /**事件管理器。*/
    public EventManager getEventManager();
    /**获得所有模块*/
    public ModuleInfo[] getModules();
    /**是否完成初始化工作*/
    public boolean isInit();
}