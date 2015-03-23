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
package net.hasor.core.factorys;
import java.lang.reflect.Method;
import java.util.Iterator;
import net.hasor.core.ApiBinder.Matcher;
import net.hasor.core.info.AbstractBindInfoProviderAdapter;
import net.hasor.core.BindInfoBuilder;
import net.hasor.core.MethodInterceptor;
/**
 * 
 * @version : 2014-3-17
 * @author 赵永春(zyc@hasor.net)
 */
public interface BindInfoDefineManager {
    /**
     * 创建一个{@link BindInfoBuilder}。
     * @param bindType 绑定类型
     * @return 返回 BindInfoBuilder。
     */
    public <T> BindInfoBuilder<T> createBuilder(Class<T> bindType);
    /**
     * 注册Aop
     * @param matcherClass 类型筛选器
     * @param matcherMethod 方法筛选器
     * @param interceptor 拦截器
     */
    public void addAop(Matcher<Class<?>> matcherClass, Matcher<Method> matcherMethod, MethodInterceptor interceptor);
    /**当有{@link #createBuilder(Class)}或者{@link #addAop(Matcher, Matcher, MethodInterceptor)}操作之后需要执行一次该方法。*/
    public void doFinish();
    //
    /**
     * 根据bindType查找{@link AbstractBindInfoProviderAdapter}迭代器
     * @param bindType 绑定类型
     * @return 返回迭代器。
     */
    public <T> Iterator<? extends AbstractBindInfoProviderAdapter<T>> getBindInfoIterator(final Class<T> bindType);
    /**
     * 根据bindType查找{@link AbstractBindInfoProviderAdapter}迭代器
     * @return 返回迭代器。
     */
    public Iterator<AbstractBindInfoProviderAdapter<?>> getBindInfoIterator();
    //
    public <T> AbstractBindInfoProviderAdapter<T> getBindInfoByID(String bindID);
}