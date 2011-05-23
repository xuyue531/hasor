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
package org.more.hypha.context;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import org.more.log.ILog;
import org.more.log.LogFactory;
/**
* 
* @version : 2011-5-9
* @author ������ (zyc@byshell.org)
*/
class PropxyClassLoader extends ClassLoader {
    private static ILog log    = LogFactory.getLog(PropxyClassLoader.class);
    private ClassLoader loader = null;
    //
    public ClassLoader getLoader() {
        if (this.loader == null) {
            this.loader = ClassLoader.getSystemClassLoader();
            log.info("propxy is null, use ClassLoader.getSystemClassLoader().");
        }
        return this.loader;
    }
    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }
    //
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loader.loadClass(name);
    }
    public URL getResource(String name) {
        return this.loader.getResource(name);
    }
    public Enumeration<URL> getResources(String name) throws IOException {
        return this.loader.getResources(name);
    }
    public InputStream getResourceAsStream(String name) {
        return this.loader.getResourceAsStream(name);
    }
    public synchronized void setDefaultAssertionStatus(boolean enabled) {
        this.loader.setDefaultAssertionStatus(enabled);
    }
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled) {
        this.loader.setPackageAssertionStatus(packageName, enabled);
    }
    public synchronized void setClassAssertionStatus(String className, boolean enabled) {
        this.loader.setClassAssertionStatus(className, enabled);
    }
    public synchronized void clearAssertionStatus() {
        this.loader.clearAssertionStatus();
    }
}