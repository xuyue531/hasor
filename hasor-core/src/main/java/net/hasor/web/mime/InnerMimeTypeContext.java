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
package net.hasor.web.mime;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.XMLStreamException;
import org.more.util.ResourcesUtils;
import org.more.util.StringUtils;
import org.more.xml.stream.EndElementEvent;
import org.more.xml.stream.TextEvent;
import org.more.xml.stream.XmlAccept;
import org.more.xml.stream.XmlReader;
import org.more.xml.stream.XmlStreamEvent;
/**
 * 
 * @version : 2015年2月11日
 * @author 赵永春(zyc@hasor.net)
 */
class InnerMimeTypeContext extends ConcurrentHashMap<String, String> implements MimeType {
    private static final long serialVersionUID = -8955832291109288048L;
    public InnerMimeTypeContext(Object content) {
        // TODO Auto-generated constructor stub 
    }
    public String getMimeType(String suffix) {
        return this.get(suffix);
    }
    public void loadStream(String resourceName) throws XMLStreamException, IOException {
        List<InputStream> inStreamList = ResourcesUtils.getResourcesAsStream(resourceName);
        for (InputStream inStream : inStreamList) {
            this.loadStream(inStream);
        }
    }
    public void loadStream(InputStream inStream) throws XMLStreamException, IOException {
        new XmlReader(inStream).reader(new XmlAccept() {
            private StringBuffer stringBuffer = new StringBuffer();
            private String       extension    = null;
            private String       mimeType     = null;
            public void beginAccept() throws XMLStreamException {}
            public void sendEvent(XmlStreamEvent e) throws XMLStreamException, IOException {
                if (e instanceof TextEvent) {
                    TextEvent event = (TextEvent) e;
                    this.stringBuffer.append(event.getText());
                } else if (e instanceof EndElementEvent) {
                    EndElementEvent ee = (EndElementEvent) e;
                    if (StringUtils.equalsIgnoreCase(ee.getElementName(), "extension")) {
                        this.extension = this.stringBuffer.toString();
                    } else if (StringUtils.equalsIgnoreCase(ee.getElementName(), "mime-type")) {
                        this.mimeType = this.stringBuffer.toString();
                    } else if (StringUtils.equalsIgnoreCase(ee.getElementName(), "mime-mapping")) {
                        if (!StringUtils.isBlank(this.extension) && !StringUtils.isBlank(this.mimeType)) {
                            String key = this.extension.trim().toLowerCase();
                            String var = this.mimeType.trim();
                            if (InnerMimeTypeContext.this.containsKey(key) == false) {
                                InnerMimeTypeContext.this.put(key, var);
                            }
                        }
                        this.extension = null;
                        this.mimeType = null;
                    }
                    //
                    this.stringBuffer = new StringBuffer();
                }
            }
            public void endAccept() throws XMLStreamException {}
        }, null);
    }
}