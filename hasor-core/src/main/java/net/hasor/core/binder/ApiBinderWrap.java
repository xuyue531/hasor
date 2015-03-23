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
package net.hasor.core.binder;
import java.lang.reflect.Method;
import java.util.Set;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContextAware;
import net.hasor.core.Environment;
import net.hasor.core.MethodInterceptor;
import net.hasor.core.Module;
import net.hasor.core.Provider;
/**
 * 标准的 {@link ApiBinder} 接口包装类。
 * @version : 2013-4-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class ApiBinderWrap implements ApiBinder {
    private ApiBinder apiBinder = null;
    //
    public ApiBinderWrap(ApiBinder apiBinder) {
        this.apiBinder = apiBinder;
    }
    public Environment getEnvironment() {
        return this.apiBinder.getEnvironment();
    }
    public <T extends AppContextAware> T autoAware(final T aware) {
        return this.apiBinder.autoAware(aware);
    }
    public Set<Class<?>> findClass(final Class<?> featureType) {
        return this.apiBinder.findClass(featureType);
    }
    public void installModule(final Module module) throws Throwable {
        this.apiBinder.installModule(module);
    }
    public void bindInterceptor(String matcherExpression, MethodInterceptor interceptor) {
        this.apiBinder.bindInterceptor(matcherExpression, interceptor);
    }
    public void bindInterceptor(Matcher<Class<?>> matcherClass, Matcher<Method> matcherMethod, MethodInterceptor interceptor) {
        this.apiBinder.bindInterceptor(matcherClass, matcherMethod, interceptor);
    }
    public <T> NamedBindingBuilder<T> bindType(Class<T> type) {
        return this.apiBinder.bindType(type);
    }
    public <T> MetaDataBindingBuilder<T> bindType(Class<T> type, T instance) {
        return this.apiBinder.bindType(type, instance);
    }
    public <T> InjectPropertyBindingBuilder<T> bindType(Class<T> type, Class<? extends T> implementation) {
        return this.apiBinder.bindType(type, implementation);
    }
    public <T> ScopedBindingBuilder<T> bindType(Class<T> type, Provider<T> provider) {
        return this.apiBinder.bindType(type, provider);
    }
    public <T> InjectPropertyBindingBuilder<T> bindType(String withName, Class<T> type) {
        return this.apiBinder.bindType(withName, type);
    }
    public <T> MetaDataBindingBuilder<T> bindType(String withName, Class<T> type, T instance) {
        return this.apiBinder.bindType(withName, type, instance);
    }
    public <T> InjectPropertyBindingBuilder<T> bindType(String withName, Class<T> type, Class<? extends T> implementation) {
        return this.apiBinder.bindType(withName, type, implementation);
    }
    public <T> LifeBindingBuilder<T> bindType(String withName, Class<T> type, Provider<T> provider) {
        return this.apiBinder.bindType(withName, type, provider);
    }
}