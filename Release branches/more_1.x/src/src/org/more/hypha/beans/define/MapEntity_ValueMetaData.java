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
package org.more.hypha.beans.define;
import java.util.Map;
import org.more.StateException;
import org.more.hypha.beans.ValueMetaData;
/**
 * 表示一个{@link Map}类型的一个key value键值对的元信息描述。
 * @version 2010-9-17
 * @author 赵永春 (zyc@byshell.org)
 */
public class MapEntity_ValueMetaData extends Collection_ValueMetaData<ValueMetaData> {
    private ValueMetaData key   = null; //Key
    private ValueMetaData value = null; //Value
    /**该方法将会返回null*/
    public PropertyMetaTypeEnum getPropertyType() {
        return null;
    }
    /**设置KEY*/
    public void setKeyObject(ValueMetaData key) {
        this.key = key;
    }
    /**设置VAR*/
    public void setVarObject(ValueMetaData value) {
        this.value = value;
    }
    /**添加一个值到当前集合中，首次添加为key，二次添加为var，三次添加抛异常。*/
    public void addObject(ValueMetaData value) {
        if (this.key == null) {
            this.key = value;
            return;
        }
        if (this.value == null) {
            this.value = value;
            return;
        }
        throw new StateException("key，value都已经设置.");
    }
    public int size() {
        if (this.key == null && this.value == null)
            return 0;
        if (this.key != null && this.value != null)
            return 2;
        if (this.key == null || this.value == null)
            return 1;
        return 0;
    }
    /**获取key*/
    public ValueMetaData getKey() {
        return this.key;
    }
    /**设置key*/
    public void setKey(ValueMetaData key) {
        this.key = key;
    }
    /**获取value*/
    public ValueMetaData getValue() {
        return this.value;
    }
    /**设置value*/
    public void setValue(ValueMetaData value) {
        this.value = value;
    }
}