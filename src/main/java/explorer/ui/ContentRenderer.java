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

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import explorer.core.api.MimeProvider;

@Component(name="Sling Explorer - Content Renderer")
@Service(value=ContentRenderer.class)
public class ContentRenderer {

	@Reference
	MimeTypeService mimes;
	
    private final Logger log = LoggerFactory.getLogger(getClass());
	
	private ServiceTracker tracker;
	
	private java.awt.Component defaultPanel;
	
	public java.awt.Component getComponent(Resource resource, String filter, String syntax){
		MimeProvider provider = getMimeProvider(filter);
		if (provider == null){
			log.info("no handler found for {}",syntax);
			return defaultPanel;
		}
		java.awt.Component component = provider.createComponent(resource, syntax);
		return component;
	}
	
	
	private MimeProvider getMimeProvider(String filterString){
		try {
			Filter filter =  FrameworkUtil.createFilter(filterString);
			for (ServiceReference sr: tracker.getServiceReferences()){
				if (filter.match(sr)){
					return (MimeProvider)tracker.getService(sr);
				}
			}
		} catch (InvalidSyntaxException e) {
			log.warn(e.getMessage());
		}
		return null;
	}
	
	public String mimeType(Resource resource) {
		ResourceMetadata metaData = resource.getResourceMetadata();
		String prop = metaData.getContentType();
		if (prop == null) {
			prop = mimes.getMimeType(resource.getName());
		}
		if (prop == null) {
			prop = "";
		}
		return prop;
	}
	
	@SuppressWarnings("serial")
	@Activate
	@Modified
	private void activate(ComponentContext context) throws InvalidSyntaxException{
		tracker = new ServiceTracker(context.getBundleContext(), MimeProvider.class.getName(),null);
		tracker.open();
		
		defaultPanel = new JPanel(){
			{
				add(new JLabel("No content display is available for this content"));
			}
		};
	}
	
	@Deactivate
	private void deactivate(){
		tracker.close();
	}
	
	
}
