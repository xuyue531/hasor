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
import java.util.ArrayList;
import java.util.List;
import net.hasor.core.ApiBinder.Matcher;
import net.hasor.core.BindInfo;
import net.hasor.core.Provider;
import net.hasor.core.info.AbstractBindInfoProviderAdapter;
import net.hasor.core.info.CustomerProvider;
import net.hasor.core.info.DefaultBindInfoProviderAdapter;
import org.more.classcode.MoreClassLoader;
import org.more.classcode.aop.AopClassConfig;
import org.more.classcode.aop.AopMatcher;
import org.more.util.ExceptionUtils;
/**
 * 
 * @version : 2014年7月4日
 * @author 赵永春(zyc@hasor.net)
 */
public class HasorRegisterFactory extends AbstractBindInfoFactory {
    /**创建对象*/
    protected <T> T createObject(Class<T> newType, HasorBindInfoProviderAdapter<T> infoAdapter) throws Exception {
        return super.createObject(newType);
    }
    public <T> T getInstance(BindInfo<T> bindInfo) {
        if (bindInfo instanceof CustomerProvider) {
            //1.可能存在的 CustomerProvider。 
            CustomerProvider<T> adapter = (CustomerProvider<T>) bindInfo;
            Provider<T> provider = adapter.getCustomerProvider();
            if (provider != null) {
                return provider.get();
            }
        }
        //
        //2.HasorBindInfoProviderAdapter 类型。
        if (bindInfo instanceof HasorBindInfoProviderAdapter == true) {
            //3.Build 对象。
            HasorBindInfoProviderAdapter<T> infoAdapter = (HasorBindInfoProviderAdapter<T>) bindInfo;
            try {
                AopClassConfig cc = infoAdapter.buildEngine(this.aopList);
                Class<?> newType = null;
                if (cc.hasChange() == true) {
                    newType = cc.toClass();
                } else {
                    newType = cc.getSuperClass();
                }
                return (T) createObject((Class<T>) newType, infoAdapter);
            } catch (Exception e) {
                throw ExceptionUtils.toRuntimeException(e);
            }
        }
        return this.getDefaultInstance(bindInfo.getBindType());
    }
    //
    //
    //
    //
    //
    protected void configBindInfo(AbstractBindInfoProviderAdapter<Object> bindInfo, Object context) {
        if (bindInfo.getBindType().isAssignableFrom(AopMatcherMethodInterceptor.class)) {
            Provider<?> aopProvider = bindInfo.getCustomerProvider();
            AopMatcherMethodInterceptor aop = (AopMatcherMethodInterceptor) aopProvider.get();
            this.aopList.add(aop);
        }
    }
    private ClassLoader                       masterLosder = new MoreClassLoader();
    private List<AopMatcherMethodInterceptor> aopList      = new ArrayList<AopMatcherMethodInterceptor>();
    protected BindInfoDefineManager createDefineManager() {
        return new AbstractBindInfoDefineManager() {
            protected <T> AbstractBindInfoProviderAdapter<T> createRegisterInfoAdapter(Class<T> bindingType) {
                return new HasorBindInfoProviderAdapter<T>(bindingType, masterLosder);
            }
        };
    }
    private static class HasorBindInfoProviderAdapter<T> extends DefaultBindInfoProviderAdapter<T> {
        private ClassLoader masterLosder = null;
        public HasorBindInfoProviderAdapter(Class<T> bindingType, ClassLoader masterLosder) {
            super(bindingType);
            this.masterLosder = masterLosder;
        }
        private AopClassConfig engine = null;
        /**获取用于创建Bean的 Engine。*/
        public AopClassConfig buildEngine(List<AopMatcherMethodInterceptor> aopList) {
            if (this.engine == null) {
                Class<?> superType = this.getSourceType();
                superType = (superType == null) ? this.getBindType() : superType;
                this.engine = new AopClassConfig(superType, this.masterLosder);
                for (AopMatcherMethodInterceptor aop : aopList) {
                    if (aop.getMatcherClass().matches(superType) == false) {
                        continue;
                    }
                    AopMatcher aopMatcher = new HasorAopMatcher(aop.getMatcherMethod());
                    this.engine.addAopInterceptor(aopMatcher, aop);
                }
            }
            return this.engine;
        }
    }
    private static class HasorAopMatcher implements AopMatcher {
        private Matcher<Method> matcherMethod = null;
        public HasorAopMatcher(Matcher<Method> matcherMethod) {
            this.matcherMethod = matcherMethod;
        }
        public boolean matcher(Method target) {
            return this.matcherMethod.matches(target);
        }
    }
}