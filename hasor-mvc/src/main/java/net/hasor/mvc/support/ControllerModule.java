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
package net.hasor.mvc.support;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import net.hasor.core.ApiBinder;
import net.hasor.mvc.ModelController;
import net.hasor.mvc.api.result.Forword;
import net.hasor.mvc.api.result.Include;
import net.hasor.mvc.api.result.Redirect;
import net.hasor.mvc.support.result.ForwordResultProcess;
import net.hasor.mvc.support.result.IncludeResultProcess;
import net.hasor.mvc.support.result.RedirectResultProcess;
import net.hasor.web.WebApiBinder;
import net.hasor.web.WebModule;
/***
 * 创建MVC环境
 * @version : 2014-1-13
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class ControllerModule extends WebModule {
    private static AtomicBoolean initController = new AtomicBoolean(false);
    //
    public void loadModule(final WebApiBinder apiBinder) throws Throwable {
        logger.info("work at ControllerModule. -> {}", this.getClass());
        //
        //1.create LoadHellper
        LoadHellper helper = new LoadHellper() {
            protected ApiBinder apiBinder() {
                return apiBinder;
            }
            protected ControllerModule module() {
                return ControllerModule.this;
            }
        };
        //
        //2.load config
        this.loadController(helper);
        //
        //3.install-避免初始化多次
        if (initController.compareAndSet(false, true)) {
            helper.loadResultProcess(Forword.class, ForwordResultProcess.class);
            helper.loadResultProcess(Include.class, IncludeResultProcess.class);
            helper.loadResultProcess(Redirect.class, RedirectResultProcess.class);
            //
            apiBinder.bindType(ResultDefineList.class, apiBinder.autoAware(new ResultDefineList()));
            apiBinder.bindType(RootController.class).toInstance(apiBinder.autoAware(new RootController()));
            apiBinder.filter("/*").through(new ControllerFilter());
        }
    }
    //
    protected abstract void loadController(LoadHellper helper);
    /**
     * 创建 {@link MappingInfoDefine}
     * @param atMethod 映射的方法
     * @param strategyFactory CallStrategy 工厂。
     * @return 返回mvc定义。
     */
    public MappingInfoDefine createMappingDefine(Class<? extends ModelController> clazz) {
        return new MappingInfoDefine(clazz);
    }
}