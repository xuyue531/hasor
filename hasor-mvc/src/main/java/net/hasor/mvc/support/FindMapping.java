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
/**
 * 
 * @version : 2014年8月27日
 * @author 赵永春(zyc@hasor.net)
 */
class FindMapping {
    private String controllerPath = null;
    private String httpMethod     = null;
    //
    public FindMapping(String controllerPath, String httpMethod) {
        this.controllerPath = controllerPath;
        this.httpMethod = httpMethod;
        //
        if (httpMethod != null) {
            this.httpMethod = httpMethod.trim().toUpperCase();
        }
    }
    public boolean matching(MappingDefine invoke) {
        boolean one = invoke.matchingMapping(this.controllerPath);
        if (one == true) {
            one = invoke.matchingMethod(this.httpMethod);
        }
        return one;
    }
}