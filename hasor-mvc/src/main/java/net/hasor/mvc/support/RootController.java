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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextAware;
/**
 * 根控制器
 * @version : 2014年8月28日
 * @author 赵永春(zyc@hasor.net)
 */
class RootController implements AppContextAware {
    private AppContext      appContext  = null;
    private MappingDefine[] invokeArray = new MappingDefine[0];
    private AtomicBoolean   inited      = new AtomicBoolean(false);
    //
    public void setAppContext(AppContext appContext) {
        if (!this.inited.compareAndSet(false, true)) {
            return;/*避免被初始化多次*/
        }
        this.appContext = appContext;
        //1.find
        List<MappingDefine> mappingList = this.appContext.findBindingBean(MappingDefine.class);
        Collections.sort(mappingList, new Comparator<MappingDefine>() {
            public int compare(MappingDefine o1, MappingDefine o2) {
                return o1.getMappingTo().compareToIgnoreCase(o2.getMappingTo()) * -1;
            }
        });
        //2.init
        for (MappingDefine define : mappingList) {
            this.initDefine(define);
        }
        MappingDefine[] defineArrays = mappingList.toArray(new MappingDefine[mappingList.size()]);
        if (defineArrays != null) {
            this.invokeArray = defineArrays;
        }
    }
    /** @return 获取AppContext*/
    protected AppContext getAppContext() {
        return this.appContext;
    }
    /**
     * 初始化 {@link MappingDefine}
     * @param define 等待初始化的MappingDefine
     */
    protected void initDefine(MappingDefine define) {
        if (define != null) {
            define.init(this.appContext);
        }
    }
    /**
     * 查找符合路径的 {@link MappingDefine}
     * @param controllerPath 匹配的路径
     * @return 返回匹配的MappingDefine。
     */
    public final MappingDefine findMapping(String controllerPath) {
        for (MappingDefine invoke : this.invokeArray) {
            if (this.matchingMapping(controllerPath, invoke) == true) {
                return invoke;
            }
        }
        return null;
    }
    /**
     * 查找符合 {@link FindMapping}要求的 {@link MappingDefine}
     * @param findMapping 匹配器
     * @return 返回匹配的MappingDefine。
     */
    public MappingDefine findMapping(FindMapping findMapping) {
        for (MappingDefine invoke : this.invokeArray) {
            if (findMapping.matching(invoke) == true) {
                return invoke;
            }
        }
        return null;
    }
    /**
     * 执行匹配
     * @param controllerPath 匹配的路径
     * @param atInvoke 匹配的{@link MappingDefine}
     * @return 返回是否匹配成功。
     */
    protected boolean matchingMapping(String controllerPath, MappingDefine atInvoke) {
        if (atInvoke == null) {
            return false;
        }
        return atInvoke.matchingMapping(controllerPath);
    }
}