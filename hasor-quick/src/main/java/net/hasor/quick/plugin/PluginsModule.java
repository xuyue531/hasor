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
package net.hasor.quick.plugin;
import java.util.Set;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import org.more.logger.LoggerHelper;
/**
 * 插件体系支持
 * @version : 2013-4-8
 * @author 赵永春 (zyc@hasor.net)
 */
public class PluginsModule implements Module {
    /**初始化.*/
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        Set<Class<?>> pluginSet = apiBinder.findClass(Plugin.class);
        if (pluginSet == null)
            return;
        //
        for (Class<?> pluginClass : pluginSet) {
            if (pluginClass == Plugin.class || Module.class.isAssignableFrom(pluginClass) == false) {
                continue;
            }
            try {
                LoggerHelper.logInfo("loadModule ‘%s’ starting.", pluginClass);
                Module plugin = (Module) pluginClass.newInstance();
                apiBinder.installModule(plugin);
                LoggerHelper.logInfo("loadModule ‘%s’ --> No Error.", pluginClass);
            } catch (Throwable e) {
                LoggerHelper.logWarn("loadModule ‘%s’ --> Exception.", pluginClass);
                LoggerHelper.logWarn(pluginClass.getName(), e);
            }
        }
    }
}