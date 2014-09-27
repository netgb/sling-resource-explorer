package explorer.ide.tree;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.ObservationManager;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import explorer.core.api.SessionProvider;
import explorer.ide.EventTypes;
import explorer.ide.UIEvent;

@org.apache.felix.scr.annotations.Component(name = "Sling Explorer UI - Tree Renderer", description = "Displays the node based tree")
@Service(value = JTree.class)
@SuppressWarnings("serial")
public class JcrJTree extends JTree {

	static {
		// Set the magic property which makes the first click outside the popup
		// capable of selecting tree nodes, as well as dismissing the popup.
		UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
	}

	@Reference
	DefaultTreeCellRenderer jcrTreeNodeRenderer;

	@Reference
	TreeModel treeModel;

	@Reference
	EventAdmin eventAdmin;

	@Reference
	SessionProvider provider;

	ObservationManager observer;

	@Activate
	private void activate() throws UnsupportedRepositoryOperationException, RepositoryException {
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				showMenuIfPopupTrigger(e);
			}

			public void mouseClicked(MouseEvent e) {
				showMenuIfPopupTrigger(e);
			}

			public void mouseReleased(MouseEvent e) {
				showMenuIfPopupTrigger(e);
			}

			private void showMenuIfPopupTrigger(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					// set the new selections before showing the popup
					setSelectedItemsOnPopupTrigger(e);
					eventAdmin.postEvent(new Event(EventTypes.SHOW_TREE_MENU, new HashMap<String, Object>() {
						{
							Point menuLocation = new Point(e.getX() + 3, e.getY() + 3);
							put("source", e.getSource());
							put("data", menuLocation);
						}
					}));
				}
			}

			/**
			 * Fix for right click not selecting tree nodes - We want to
			 * implement the following behavior which matches windows explorer:
			 * If the item under the click is not already selected, clear the
			 * current selections and select the item, prior to showing the
			 * popup. If the item under the click is already selected, keep the
			 * current selection(s)
			 */
			private void setSelectedItemsOnPopupTrigger(MouseEvent e) {
				TreePath p = getPathForLocation(e.getX(), e.getY());
				if (!getSelectionModel().isPathSelected(p)) {
					getSelectionModel().setSelectionPath(p);
				}
			}
		});
		setCellRenderer(jcrTreeNodeRenderer);
		setModel(treeModel);

		setToolTipText("");
		addTreeSelectiionListeners();

		Session session = provider.getSession("key");
		// Listen for changes to our orders
		observer = session.getWorkspace().getObservationManager();
		final String path = "/";
		int events = javax.jcr.observation.Event.PROPERTY_ADDED|javax.jcr.observation.Event.NODE_ADDED;
		//observer.addEventListener(this, javax.jcr.observation.Event.NODE_ADDED, path, true, null, null, false);

	}
	
	@Deactivate
	public void deactivate() throws RepositoryException{
		if(observer != null) {
			//observer.removeEventListener(this);
        }
	}

	@Override
	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value != null) {
			if (value instanceof Resource) {
				Resource resource = (Resource) value;
				return resource.getName();
			}
		}
		return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
	}

	private void addTreeSelectiionListeners() {

		getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (getLastSelectedPathComponent() != null) {
					eventAdmin.postEvent(new UIEvent(EventTypes.NEW_SELECTION, this, getLastSelectedPathComponent()));
				}
			}

		});

	}



}
