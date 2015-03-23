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
package net.hasor.core.context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.hasor.core.ApiBinder;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextAware;
import net.hasor.core.BindInfo;
import net.hasor.core.Environment;
import net.hasor.core.EventContext;
import net.hasor.core.Hasor;
import net.hasor.core.Module;
import net.hasor.core.Provider;
import net.hasor.core.Settings;
import net.hasor.core.StartModule;
import net.hasor.core.binder.AbstractBinder;
import net.hasor.core.context.listener.ContextInitializeListener;
import net.hasor.core.context.listener.ContextShutdownListener;
import net.hasor.core.context.listener.ContextStartListener;
import net.hasor.core.factorys.BindInfoDefineManager;
import net.hasor.core.factorys.BindInfoFactory;
import net.hasor.core.info.AbstractBindInfoProviderAdapter;
import org.more.builder.ReflectionToStringBuilder;
import org.more.builder.ToStringStyle;
import org.more.logger.LoggerHelper;
import org.more.util.ArrayUtils;
import org.more.util.StringUtils;
/**
 * 抽象类 AbstractAppContext 是 {@link AppContext} 接口的基础实现。
 * <p>它包装了大量细节代码，可以方便的通过子类来创建独特的上下文支持。<p>
 * 
 * 提示：initContext 方法是整个类的入口方法。
 * @version : 2013-4-9
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractAppContext implements AppContext {
    public Class<?> getBeanType(String bindID) {
        Hasor.assertIsNotNull(bindID, "bindID is null.");
        LoggerHelper.logFinest("bindID is %s.", bindID);
        //
        BindInfoDefineManager defineManager = this.getBindInfoFactory().getManager();
        AbstractBindInfoProviderAdapter<?> bindInfo = defineManager.getBindInfoByID(bindID);
        //
        if (bindInfo != null) {
            Class<?> bindType = bindInfo.getBindType();
            LoggerHelper.logFinest("bindID(%s) is %s.", bindType);
            return bindType;
        }
        LoggerHelper.logFine("bindID(%s) does not exist.", bindID);
        return null;
    }
    public boolean containsBindID(String bindID) {
        Hasor.assertIsNotNull(bindID, "bindID is null.");
        LoggerHelper.logFinest("bindID is %s.", bindID);
        //
        BindInfoDefineManager defineManager = this.getBindInfoFactory().getManager();
        AbstractBindInfoProviderAdapter<?> bindInfo = defineManager.getBindInfoByID(bindID);
        return bindInfo != null;
    }
    public String[] getBindIDs() {
        BindInfoDefineManager defineManager = this.getBindInfoFactory().getManager();
        Iterator<? extends AbstractBindInfoProviderAdapter<?>> adapterList = defineManager.getBindInfoIterator();
        if (adapterList == null || adapterList.hasNext() == false) {
            LoggerHelper.logFine("bindIDs is empty");
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> names = new ArrayList<String>();
        while (adapterList.hasNext()) {
            AbstractBindInfoProviderAdapter<?> adapter = adapterList.next();
            String name = adapter.getBindName();
            if (StringUtils.isBlank(name) == false) {
                names.add(name);
            }
        }
        String[] nameArray = names.toArray(new String[names.size()]);
        LoggerHelper.logFinest("bindIDs is %s", ReflectionToStringBuilder.toString(nameArray, ToStringStyle.SIMPLE_STYLE));
        return nameArray;
    }
    public <T> BindInfo<T> getBindInfo(String bindID) {
        BindInfoDefineManager defineManager = this.getBindInfoFactory().getManager();
        return defineManager.getBindInfoByID(bindID);
    }
    public <T> T getInstance(String bindID) {
        Hasor.assertIsNotNull(bindID, "bindID is null.");
        LoggerHelper.logFinest("bindIDs is %s", bindID);
        //
        BindInfoDefineManager defineManager = this.getBindInfoFactory().getManager();
        AbstractBindInfoProviderAdapter<T> bindInfo = defineManager.getBindInfoByID(bindID);
        if (bindInfo != null) {
            return this.getInstance(bindInfo);
        }
        return null;
    }
    /*---------------------------------------------------------------------------------------Bean*/
    /**如果存在目标类型的Bean则返回Bean的名称。*/
    public String[] getNames(final Class<?> targetClass) {
        Hasor.assertIsNotNull(targetClass, "targetClass is null.");
        //
        String[] returnData = this.getBindInfoFactory().getNamesOfType(targetClass);
        if (returnData == null) {
            LoggerHelper.logFine("names of %s is empty.", targetClass);
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        LoggerHelper.logFinest("names of %s = %s.", targetClass, ReflectionToStringBuilder.toString(returnData, ToStringStyle.SIMPLE_STYLE));
        return returnData;
    }
    /**创建Bean。*/
    public <T> T getInstance(final Class<T> targetClass) {
        Hasor.assertIsNotNull(targetClass, "targetClass is null.");
        LoggerHelper.logFinest("instance type is %s.", targetClass);
        //
        BindInfoFactory factory = this.getBindInfoFactory();
        BindInfo<T> info = factory.getBindInfo(null, targetClass);
        if (info != null) {
            Provider<T> provider = this.getProvider(info);
            if (provider != null) {
                return provider.get();
            } else {
                LoggerHelper.logFine("instance type(%s) bindInfo(%s), but provider is null.", targetClass, info);
            }
        } else {
            LoggerHelper.logFine("instance type(%s) bindInfo is null.", targetClass);
        }
        return factory.getDefaultInstance(targetClass);
    };
    /**创建Bean。*/
    public <T> T getInstance(final BindInfo<T> info) {
        return this.getBindInfoFactory().getInstance(info);
    }
    /**创建Bean。*/
    public <T> Provider<T> getProvider(final BindInfo<T> info) {
        if (info == null) {
            return null;
        }
        if (info instanceof AbstractBindInfoProviderAdapter) {
            AbstractBindInfoProviderAdapter<T> adapter = (AbstractBindInfoProviderAdapter<T>) info;
            Provider<T> provider = adapter.getCustomerProvider();
            if (provider != null) {
                return provider;
            }
        }
        return new Provider<T>() {
            public T get() {
                return getBindInfoFactory().getInstance(info);
            }
        };
    };
    /**获取用于创建Bean对象的{@link BindInfoFactory}接口*/
    protected abstract BindInfoFactory getBindInfoFactory();
    //
    /*------------------------------------------------------------------------------------Binding*/
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> List<T> findBindingBean(final Class<T> bindType) {
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfoFactory infoFactory = this.getBindInfoFactory();
        String[] namesOfType = this.getBindInfoFactory().getNamesOfType(bindType);
        if (namesOfType == null || namesOfType.length == 0) {
            return new ArrayList<T>(0);
        }
        ArrayList<T> returnData = new ArrayList<T>();
        for (String name : namesOfType) {
            BindInfo<T> info = infoFactory.getBindInfo(name, bindType);
            Provider<T> provider = this.getProvider(info);
            if (provider != null) {
                T obj = provider.get();
                if (obj != null) {
                    returnData.add(obj);
                }
            }
        }
        return returnData;
    };
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> List<Provider<T>> findBindingProvider(final Class<T> bindType) {
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfoFactory infoFactory = this.getBindInfoFactory();
        String[] namesOfType = this.getBindInfoFactory().getNamesOfType(bindType);
        if (namesOfType == null || namesOfType.length == 0) {
            return new ArrayList<Provider<T>>(0);
        }
        ArrayList<Provider<T>> returnData = new ArrayList<Provider<T>>();
        for (String name : namesOfType) {
            BindInfo<T> info = infoFactory.getBindInfo(name, bindType);
            Provider<T> provider = this.getProvider(info);
            if (provider != null) {
                returnData.add(provider);
            }
        }
        return returnData;
    };
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> T findBindingBean(final String withName, final Class<T> bindType) {
        Hasor.assertIsNotNull(withName, "withName is null.");
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfo<T> info = this.getBindInfoFactory().getBindInfo(withName, bindType);
        if (info != null) {
            Provider<T> provider = this.getProvider(info);
            if (provider != null) {
                return provider.get();
            }
        }
        return null;
    };
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> Provider<T> findBindingProvider(final String withName, final Class<T> bindType) {
        Hasor.assertIsNotNull(withName, "withName is null.");
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfo<T> typeRegister = this.getBindInfoFactory().getBindInfo(withName, bindType);
        if (typeRegister != null) {
            return this.getProvider(typeRegister);
        }
        return null;
    };
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> List<BindInfo<T>> findBindingRegister(final Class<T> bindType) {
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfoFactory infoFactory = this.getBindInfoFactory();
        String[] namesOfType = this.getBindInfoFactory().getNamesOfType(bindType);
        if (namesOfType == null || namesOfType.length == 0) {
            return new ArrayList<BindInfo<T>>(0);
        }
        ArrayList<BindInfo<T>> returnData = new ArrayList<BindInfo<T>>();
        for (String name : namesOfType) {
            BindInfo<T> info = infoFactory.getBindInfo(name, bindType);
            if (info != null) {
                returnData.add(info);
            }
        }
        return returnData;
    };
    /**通过一个类型获取所有绑定到该类型的上的对象实例。*/
    public <T> BindInfo<T> findBindingRegister(final String withName, final Class<T> bindType) {
        Hasor.assertIsNotNull(withName, "withName is null.");
        Hasor.assertIsNotNull(bindType, "bindType is null.");
        //
        BindInfo<T> typeRegister = this.getBindInfoFactory().getBindInfo(withName, bindType);
        if (typeRegister != null) {
            return typeRegister;
        }
        return null;
    };
    //
    /*------------------------------------------------------------------------------------Process*/
    /**查找Module。*/
    protected Module[] findModules() throws Throwable {
        return new Module[0];
    }
    /**开始进入初始化过程.*/
    protected void doInitialize() throws Throwable {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextInitializeListener) {
            ApiBinder apiBinder = this.newApiBinder(null);
            ((ContextInitializeListener) registerFactory).doInitialize(apiBinder);
        }
    }
    /**初始化过程完成.*/
    protected void doInitializeCompleted() {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextInitializeListener) {
            ((ContextInitializeListener) registerFactory).doInitializeCompleted(this);
        }
    }
    /**开始进入容器启动过程.*/
    protected void doStart() {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextStartListener) {
            ((ContextStartListener) registerFactory).doStart(this);
        }
    }
    /**容器启动完成。*/
    protected void doStartCompleted() {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextStartListener) {
            ((ContextStartListener) registerFactory).doStartCompleted(this);
        }
    }
    /**开始进入容器停止.*/
    protected void doShutdown() {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextShutdownListener) {
            ((ContextShutdownListener) registerFactory).doShutdown(this);
        }
    }
    /**容器启动停止。*/
    protected void doShutdownCompleted() {
        BindInfoFactory registerFactory = this.getBindInfoFactory();
        if (registerFactory instanceof ContextShutdownListener) {
            ((ContextShutdownListener) registerFactory).doShutdownCompleted(this);
        }
    }
    //
    /*--------------------------------------------------------------------------------------Utils*/
    /**为模块创建ApiBinder。*/
    protected ApiBinder newApiBinder(final Module forModule) {
        return new AbstractBinder(this.getEnvironment()) {
            protected BindInfoDefineManager getBuilderRegister() {
                return getBindInfoFactory().getManager();
            }
        };
    }
    /**当完成所有初始化过程之后调用，负责向 Context 绑定一些预先定义的类型。*/
    protected void doBind(final ApiBinder apiBinder) {
        final AbstractAppContext appContet = this;
        /*绑定Environment对象的Provider*/
        apiBinder.bindType(Environment.class).toProvider(new Provider<Environment>() {
            public Environment get() {
                return appContet.getEnvironment();
            }
        });
        /*绑定Settings对象的Provider*/
        apiBinder.bindType(Settings.class).toProvider(new Provider<Settings>() {
            public Settings get() {
                return appContet.getEnvironment().getSettings();
            }
        });
        /*绑定AppContext对象的Provider*/
        apiBinder.bindType(AppContext.class).toProvider(new Provider<AppContext>() {
            public AppContext get() {
                return appContet;
            }
        });
    }
    //
    /*------------------------------------------------------------------------------------Creater*/
    private boolean startState = false;
    public boolean isStart() {
        return this.startState;
    }
    /**安装模块的工具方法。*/
    protected void installModule(Module module) throws Throwable {
        LoggerHelper.logFine("installModule %s.", module);
        if (this.isStart()) {
            throw new IllegalStateException("AppContent is started.");
        }
        if (module == null) {
            return;
        }
        LoggerHelper.logInfo("loadModule %s.", module);
        ApiBinder apiBinder = this.newApiBinder(module);
        module.loadModule(apiBinder);
    }
    public synchronized final void start(Module... modules) throws Throwable {
        if (this.isStart()) {
            LoggerHelper.logWarn("Hasor Started!");
            return;
        }
        final AbstractAppContext appContext = this;
        EventContext ec = appContext.getEnvironment().getEventContext();
        /*1.Init*/
        LoggerHelper.logInfo("begin start.");
        LoggerHelper.logInfo("doInitialize now.");
        appContext.doInitialize();
        /*2.Bind*/
        ArrayList<Module> findModules = new ArrayList<Module>();
        findModules.addAll(Arrays.asList(this.findModules()));
        findModules.addAll(Arrays.asList(modules));
        for (Module module : findModules) {
            this.installModule(module);
        }
        ApiBinder apiBinder = appContext.newApiBinder(null);
        LoggerHelper.logInfo("AppContext doBind.");
        appContext.doBind(apiBinder);
        /*3.引发事件*/
        ec.fireSyncEvent(EventContext.ContextEvent_Initialized, apiBinder);
        appContext.doInitializeCompleted();
        LoggerHelper.logInfo("doInitialize completed!");
        //
        /*3.Start*/
        LoggerHelper.logInfo("doStart now.");
        appContext.doStart();
        /*2.执行Aware通知*/
        List<AppContextAware> awareList = appContext.findBindingBean(AppContextAware.class);
        if (awareList.isEmpty() == false) {
            for (AppContextAware weak : awareList) {
                weak.setAppContext(appContext);
            }
        }
        /*3.发送启动事件*/
        ec.fireSyncEvent(EventContext.ContextEvent_Started, appContext);
        LoggerHelper.logInfo("doStartCompleted now.");
        appContext.doStartCompleted();/*用于扩展*/
        //
        for (Module module : findModules) {
            if (module instanceof StartModule) {
                ((StartModule) module).onStart(this);
            }
        }
        /*3.打印状态*/
        this.startState = true;
        LoggerHelper.logInfo("doStart completed!");
        LoggerHelper.logInfo("Hasor Started!");
    }
    public synchronized final void shutdown() {
        if (!this.isStart()) {
            return;
        }
        final AbstractAppContext appContext = this;
        EventContext ec = appContext.getEnvironment().getEventContext();
        /*1.Init*/
        LoggerHelper.logInfo("doShutdown now.");
        appContext.doShutdown();
        /*3.引发事件*/
        ec.fireSyncEvent(EventContext.ContextEvent_Shutdown, this);
        LoggerHelper.logInfo("doShutdownCompleted now.");
        appContext.doShutdownCompleted();
        this.startState = false;
        LoggerHelper.logInfo("doShutdown completed!");
    }
}