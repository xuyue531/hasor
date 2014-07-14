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
package net.hasor.test.runner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextAware;
import net.hasor.core.Module;
import net.hasor.core.RegisterInfo;
import net.hasor.core.context.AbstractResourceAppContext;
import net.hasor.core.context.HasorFactory;
import net.hasor.core.context.adapter.RegisterFactoryCreater;
import net.hasor.test.junit.ContextConfiguration;
import net.hasor.test.junit.DaemonThread;
import net.hasor.test.junit.TestOrder;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.more.util.BeanUtils;
/**
 * 
 * @version : 2014年7月8日
 * @author 赵永春(zyc@hasor.net)
 */
public class HasorUnitRunner extends BlockJUnit4ClassRunner {
    private AppContext      appContext   = null;
    private RegisterInfo<?> typeRegister = null;
    //
    public HasorUnitRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        try {
            String configResource = AbstractResourceAppContext.DefaultSettings;
            RegisterFactoryCreater factoryCreater = null;
            //1.获取配置信息
            ContextConfiguration config = klass.getAnnotation(ContextConfiguration.class);
            List<Module> loadModule = new ArrayList<Module>();
            if (config != null) {
                configResource = config.value();
                factoryCreater = config.factoryCreater().newInstance();
                for (Class<? extends Module> mod : config.loadModules())
                    loadModule.add(mod.newInstance());
            }
            //2.初始化绑定Test
            loadModule.add(new Module() {
                public void loadModule(ApiBinder apiBinder) throws Throwable {
                    typeRegister = apiBinder.bindType(klass).uniqueName().toInfo();
                }
            });
            this.appContext = HasorFactory.createAppContext(configResource, factoryCreater, loadModule.toArray(new Module[loadModule.size()]));
            //3.
            if (this.appContext == null)
                throw new NullPointerException("HasorFactory.createAppContext return null.");
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }
    //
    protected List<FrameworkMethod> computeTestMethods() {
        //1.获取带有 @Test 注解的方法
        List<FrameworkMethod> toRunMethodList = super.computeTestMethods();
        //2.检查是否Test方法中同时带有DaemonThread注解的方法。
        for (FrameworkMethod method : toRunMethodList) {
            if (method.getAnnotation(DaemonThread.class) != null)
                throw new IllegalStateException("test method cannot be used at the same time, @Test, @DaemonThread");
        }
        //3.获取测试方法上的 @Order 注解，并对所有的测试方法重新排序
        Collections.sort(toRunMethodList, new Comparator<FrameworkMethod>() {
            public int compare(FrameworkMethod m1, FrameworkMethod m2) {
                TestOrder o1 = m1.getAnnotation(TestOrder.class);
                TestOrder o2 = m2.getAnnotation(TestOrder.class);
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return o1.value() - o2.value();
            }
        });
        return toRunMethodList;
    }
    //
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        //1.准备要执行的线程
        List<FrameworkMethod> methodList = getTestClass().getAnnotatedMethods(DaemonThread.class);//有单例问题，每个Test都会调用该方法。
        final ArrayList<Thread> daemonThreads = new ArrayList<Thread>();
        for (FrameworkMethod threadMethod : methodList) {
            Thread daemonThread = new TestThread(test, threadMethod);
            daemonThread.setDaemon(true);
            daemonThreads.add(daemonThread);
        }
        //2.Return Statement
        final Statement invokerStatement = super.methodInvoker(method, test);
        return new Statement() {
            public void evaluate() throws Throwable {
                try {
                    /*A.启动监控线程*/
                    for (Thread thread : daemonThreads)
                        thread.start();
                    invokerStatement.evaluate();
                } finally {
                    /*b.终止监控线程*/
                    for (Thread thread : daemonThreads) {
                        thread.suspend();
                        thread.interrupt();
                    }
                }
            }
        };
    }
    //
    protected Object createTest() throws Exception {
        Object testUnit = this.appContext.getInstance(this.typeRegister);
        if (testUnit != null && testUnit instanceof AppContextAware)
            ((AppContextAware) testUnit).setAppContext(this.appContext);
        return testUnit;
    }
    //
    private static class TestThread extends Thread {
        private Object          targetObject = null;
        private FrameworkMethod method       = null;
        public TestThread(Object targetObject, FrameworkMethod method) {
            super("daemonThread:" + method.getName());
            this.targetObject = targetObject;
            this.method = method;
        }
        public void run() {
            ArrayList<Object> args = new ArrayList<Object>();
            Class<?>[] params = this.method.getMethod().getParameterTypes();
            if (params != null) {
                for (Class<?> param : params)
                    args.add(BeanUtils.getDefaultValue(param));
            }
            try {
                this.method.invokeExplosively(this.targetObject, args.toArray());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}