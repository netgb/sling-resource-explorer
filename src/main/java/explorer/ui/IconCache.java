/*
Copyright 2016 JE Bailey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package explorer.ui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service(value=IconCache.class)
public class IconCache {


	public enum Type { application, code, css, db, doc, esp, file, folder, folder_open, gif, html, jpeg,audio, node_select_child, png, txt}
	
    private static final Logger log = LoggerFactory
			.getLogger(IconCache.class);
    /**
     * Loads an Icon for the handed (relative) name and modifier.
     * If no icon is found, null is returned.
     */
    public static Icon getIcon(Type type){
    	if (type == null){
    		type = Type.file;
    	}
		try {
			URL url = IconCache.class.getResource("/images/" + type.toString() + ".png");
			if (url != null) {
				return new ImageIcon(url);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
        return null;
    }
    
    
    
}
