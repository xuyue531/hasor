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
package net.hasor.db.transaction.interceptor;
import net.hasor.core.MethodInvocation;
import net.hasor.db.transaction.TransactionStatus;
/**
 * 充当TransactionTemplate作用
 * @author 赵永春(zyc@hasor.net)
 * @version : 2013-10-30
 */
public interface TranOperations {
    /** 事务执行拦截器*/
    public Object execute(TransactionStatus tranStatus, MethodInvocation invocation) throws Throwable;
}