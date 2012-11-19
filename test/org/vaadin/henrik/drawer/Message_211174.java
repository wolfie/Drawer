package org.vaadin.henrik.drawer;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Test for <a href=
 * "http://vaadin.com/forum/-/message_boards/message/194874#_19_message_211174"
 * >http
 * ://vaadin.com/forum/-/message_boards/message/194874#_19_message_211174</a>
 * */
@SuppressWarnings("serial")
public class Message_211174 extends Application {

    private final Layout layout = new VerticalLayout();
    private Drawer drawer;

    @Override
    public void init() {
        setMainWindow(new Window("test", layout));
        doTest();
        layout.addComponent(new Button("x", new Button.ClickListener() {
            public void buttonClick(final ClickEvent event) {
                drawer.toggleDrawer();
            }
        }));
    }

    private void doTest() {
        final Layout layout = new VerticalLayout();
        layout.setHeight("100px");
        layout.addComponent(new Button("First"));
        layout.addComponent(new Button("Second"));

        drawer = new Drawer("Button drawer", layout);
        drawer.setDrawerHeight(100);
        drawer.setAnimationDurationMillis(100);
        // Drawer is designed to work best with explicitly defined widths.
        drawer.setWidth("20em");

        // draweropen doens't work...
        drawer.setDrawerOpen(true);

        this.layout.addComponent(drawer);
    }
}
