package explorer.commands;

import static org.osgi.service.event.EventConstants.EVENT_TOPIC;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.commons.mime.MimeTypeService;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.osgi.service.event.EventHandler;

import explorer.ide.EventTypes;
import explorer.ide.TabEditor;

@Component(name = "Sling Explorer Command - Update Editor Pane ", description = "Updates the Editor Pane with the correct view")
@Service
@Properties(value = { @Property(name = EVENT_TOPIC, value = EventTypes.NEW_SELECTION) })
public class UpdateEditorPane implements EventHandler {

	@Reference
	TabEditor editor;

	@Reference
	MimeTypeService mimes;

	private Map<String, Object> tabs = new HashMap<String, Object>();

	@Override
	public void handleEvent(org.osgi.service.event.Event event) {
		Resource resource = (Resource) event.getProperty("data");
		if (resource.getPath().endsWith("jcr:content")){
			resource = resource.getParent();
		}
		Object view = tabs.get(resource.getPath());
		if (view != null) {
			editor.setSelectedComponent((java.awt.Component) view);
			return;
		}

		String reply = "";
		String syntax = mimeType(resource);

		if (syntax.contains("text") || syntax.contains("application")) {
			try {
				InputStream prop2 = resource.adaptTo(InputStream.class);
				byte[] temp = new byte[(int) prop2.available()];

				prop2.read(temp);
				reply = new String(temp);
			} catch (IOException e) {
				reply = "problem reading stream";
			}
		} else {
			return;
		}
		tabs.put(resource.getPath(), addEditor(resource.getName(),reply, syntax));
	}

	private String mimeType(Resource resource) {
		ResourceMetadata metaData = resource.getResourceMetadata();
		String prop = metaData.getContentType();
		if (prop == null) {
			prop = mimes.getMimeType(resource.getName());
		}
		if (prop == null) {
			prop = "";
		}
		return prop.replace("application/", "text/");
	}

	private java.awt.Component addEditor(String title,String content, String syntax) {
		RSyntaxTextArea editorTextArea = new RSyntaxTextArea(RSyntaxTextArea.INSERT_MODE);
		editorTextArea.setAntiAliasingEnabled(true);
		editorTextArea.setEditable(true);
		editorTextArea.setText(content);
		editorTextArea.setCaretPosition(0);
		editorTextArea.setCodeFoldingEnabled(true);
		editorTextArea.setEditable(true);
		editorTextArea.setSyntaxEditingStyle(syntax);
		RTextScrollPane editorScrollPane = new RTextScrollPane(editorTextArea);

		editor.addTab(title, null, editorScrollPane, null);
		editor.setSelectedComponent(editorScrollPane);
		return editorScrollPane;
	}

}
