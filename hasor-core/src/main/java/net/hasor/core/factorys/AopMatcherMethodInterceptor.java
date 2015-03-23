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
package net.hasor.core.factorys;
import java.lang.reflect.Method;
import net.hasor.core.ApiBinder.Matcher;
import net.hasor.core.MethodInterceptor;
import net.hasor.core.MethodInvocation;
import org.more.classcode.aop.AopInvocation;
/**
 * 
 * @version : 2014年5月22日
 * @author 赵永春 (zyc@byshell.org)
 */
class AopMatcherMethodInterceptor implements MethodInterceptor, org.more.classcode.aop.AopInterceptor {
    private Matcher<Class<?>> matcherClass  = null;
    private Matcher<Method>   matcherMethod = null;
    private MethodInterceptor interceptor   = null;
    //
    public AopMatcherMethodInterceptor(final Matcher<Class<?>> matcherClass, final Matcher<Method> matcherMethod, final MethodInterceptor interceptor) {
        this.matcherClass = matcherClass;
        this.matcherMethod = matcherMethod;
        this.interceptor = interceptor;
    }
    //
    public Matcher<Class<?>> getMatcherClass() {
        return matcherClass;
    }
    public Matcher<Method> getMatcherMethod() {
        return matcherMethod;
    }
    //
    public Object invoke(AopInvocation invocation) throws Throwable {
        return this.invoke(new ProxyAopInvocation(invocation));
    }
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        return this.interceptor.invoke(invocation);
    }
    //
    private static class ProxyAopInvocation implements MethodInvocation {
        private AopInvocation invocation;
        public ProxyAopInvocation(AopInvocation invocation) {
            this.invocation = invocation;
        }
        public Method getMethod() {
            return this.invocation.getMethod();
        }
        public Object[] getArguments() {
            return this.invocation.getArguments();
        }
        public Object proceed() throws Throwable {
            return this.invocation.proceed();
        }
        public Object getThis() {
            return this.invocation.getThis();
        }
    }
}