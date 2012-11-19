package org.vaadin.henrik.drawer;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class DrawerApplication extends Application {
    @Override
    public void init() {
        final Window mainWindow = new Window("Drawer Application");
        setMainWindow(mainWindow);

        final ComponentContainer container = new CssLayout();
        container.setStyleName("mainlayout");
        mainWindow.setContent(container);
        mainWindow.getContent().setSizeFull();

        final Panel left = new Panel("\"Real World\" Examples");
        left.setWidth("300px");
        container.addComponent(left);

        final Layout logContent = new CssLayout();
        logContent.addComponent(new Label(
                "12:34:56 Ð User \"foobar\" logged in"));
        logContent.addComponent(new Label(
                "12:54:32 Ð Someone hacked the mainframe"));
        logContent.addComponent(new Label(
                "23:45:01 Ð User \"foobar\" logged out"));
        final Drawer logDrawer = new Drawer("Log", logContent);
        logDrawer.setWidth("100%");

        left.addComponent(new Button("New log entry",
                new Button.ClickListener() {
                    public void buttonClick(final ClickEvent event) {
                        logContent.addComponent(new Label("00:00:00 Ð yeah"));
                    }
                }));
        left.addComponent(logDrawer);

        final HorizontalLayout actionsContent = new HorizontalLayout();
        actionsContent.addComponent(new Button("Unhack mainframe"));
        actionsContent.addComponent(new Button("Order pizza"));
        actionsContent.addComponent(new Button(";)"));
        actionsContent.setWidth("100%");
        final Drawer actionsDrawer = new Drawer("Actions", actionsContent);
        actionsDrawer.setWidth("100%");
        left.addComponent(actionsDrawer);

        final Embedded examplecom = new Embedded();
        examplecom.setSource(new ExternalResource("http://example.com/"));
        examplecom.setMimeType("text/html");
        examplecom.setType(Embedded.TYPE_BROWSER);
        examplecom.setHeight("150px");
        final Drawer examplecomDrawer = new Drawer("example.com", examplecom);
        examplecomDrawer.setWidth("100%");
        left.addComponent(examplecomDrawer);

        // debugging

        final VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(new TwinColSelect());
        verticalLayout.addComponent(new Label("foo"));
        verticalLayout.setSizeUndefined();

        getMainWindow().addComponent(
                new Button("add to fluid", new Button.ClickListener() {
                    public void buttonClick(final ClickEvent event) {
                        verticalLayout.addComponent(new Label("bar"));
                    }
                }));

        getMainWindow()
                .addComponent(new Drawer("fluid height", verticalLayout));

        final SplitPanel sp = new SplitPanel(SplitPanel.ORIENTATION_HORIZONTAL);
        sp.setSizeFull();
        sp.setSplitPosition(150, Sizeable.UNITS_PIXELS);
        mainWindow.addComponent(sp);

        sp.setFirstComponent(new Tree());

        final TabSheet ts = new TabSheet();
        sp.setSecondComponent(ts);

        final VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setMargin(false);
        panelLayout.setSpacing(false);

        final Panel panel = new Panel("Panel", panelLayout);
        ts.addTab(panel, "panel", null);

        panel.setSizeFull();

        final Drawer drawer = new Drawer(
                "foo",
                new Label(
                        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."));
        drawer.setWidth("100%");
        drawer.setDrawerOpen(true);
        panel.addComponent(drawer);

        final Drawer disabledEnabledDrawer = new Drawer("Disabled/Enabled",
                new Label("nothing here"));
        panel.addComponent(new Button("toggle enabled",
                new Button.ClickListener() {
                    public void buttonClick(final ClickEvent event) {
                        disabledEnabledDrawer.setEnabled(!disabledEnabledDrawer
                                .isEnabled());
                    }
                }));
        panel.addComponent(disabledEnabledDrawer);
    }
}
