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
package net.hasor.mvc.support.valid;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.hasor.core.AppContext;
import net.hasor.core.AppContextAware;
import net.hasor.mvc.Validation;
import net.hasor.mvc.ValidationForm;
import net.hasor.mvc.WebCall;
import net.hasor.mvc.WebCallInterceptor;
import net.hasor.mvc.api.Valid;
import org.more.bizcommon.ResultDO;
import org.more.util.BeanUtils;
import org.more.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @version : 2014年8月27日
 * @author 赵永春(zyc@hasor.net)
 */
public class ValidationCallInterceptor implements WebCallInterceptor, AppContextAware {
    protected Logger   logger = LoggerFactory.getLogger(getClass());
    private AppContext appContext;
    private String     validAttrName;
    //
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        this.validAttrName = appContext.getEnvironment().getSettings().getString("hasor.mvcConfig.validAttrName", "valid");
    }
    //
    /** 执行调用 */
    public Object exeCall(Object[] args, WebCall call) throws Throwable {
        HashMap<String, ValidData> validList = new HashMap<String, ValidData>();
        Annotation[][] paramAnno = call.getMethodParamAnnos();
        boolean validResult = true;
        //
        for (int paramIndex = 0; paramIndex < paramAnno.length; paramIndex++) {
            Annotation[] annoArrays = paramAnno[paramIndex];
            for (Annotation anno : annoArrays) {
                if (anno == null || anno instanceof Valid == false) {
                    continue;
                }
                Valid valid = (Valid) anno;
                Object paramObj = args[paramIndex];
                List<ValidData> dataList = this.doValidData(paramObj, valid);
                if (dataList != null && dataList.isEmpty() == false) {
                    for (ValidData data : dataList) {
                        validList.put(data.getKey(), data);
                    }
                }
                break;
            }
        }
        //
        if (StringUtils.isBlank(this.validAttrName) == false) {
            call.getHttpRequest().setAttribute(this.validAttrName, validList);
        }
        //
        if (validResult == false) {
            return BeanUtils.getDefaultValue(call.getMethod().getReturnType());
        } else {
            return call.call(args);
        }
    }
    //
    /** 执行调用 */
    private List<ValidData> doValidData(Object paramObj, Valid valid) throws Throwable {
        List<ValidData> validList = new ArrayList<ValidData>();
        if (paramObj instanceof ValidationForm) {
            ResultDO<String> result = ((ValidationForm) paramObj).doValidation();
            ValidData vData = this.converResult(result);
            if (vData != null) {
                validList.add(vData);
            }
        }
        //
        String[] values = valid.value();
        if (values != null && values.length != 0) {
            for (String validName : values) {
                Validation validApp = this.appContext.findBindingBean(validName, Validation.class);
                if (validApp == null) {
                    continue;
                }
                ResultDO<String> result = validApp.doValidation(paramObj);
                ValidData vData = this.converResult(result);
                if (vData != null) {
                    validList.add(vData);
                }
            }
        }
        return validList;
    }
    private ValidData converResult(ResultDO<String> result) {
        return null;
    }
}