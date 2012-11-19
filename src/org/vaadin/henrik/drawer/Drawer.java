/*
 *    Copyright 2009 IT Mill Oy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.vaadin.henrik.drawer;

import java.util.Iterator;
import java.util.Map;

import org.vaadin.henrik.drawer.widgetset.client.ui.VDrawer;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Paintable;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * @author Henrik Paul / IT Mill Ltd
 */
@SuppressWarnings("serial")
@ClientWidget(VDrawer.class)
public class Drawer extends AbstractComponentContainer {

    public final static int DRAWER_HEIGHT_AUTO = -1;
    private final static int DEFAULT_ANIMATION_DURATION = 500;

    private String caption = "";

    /** Should the next visibility toggle be animated. */
    private boolean animationRequested = false;
    private boolean drawerOpen = false;

    private int animationDurationMillis = DEFAULT_ANIMATION_DURATION;

    private Component drawer = null;
    private int drawerHeight = DRAWER_HEIGHT_AUTO;

    /**
     * This is needed to know whether the sent Component is just an empty
     * placeholder (since <tt>null</tt> can't be sent), or sent by the user's
     * request.
     */
    private boolean drawerHasContent = false;

    private boolean calculateHeaderWidth = true;

    /**
     * <p>
     * Create a new {@link Drawer}.
     * </p>
     * 
     * <p>
     * The Drawer will initially be closed.
     * </p>
     */
    public Drawer() {
        this(null, null);
    }

    /**
     * <p>
     * Create a new {@link Drawer}
     * </p>
     * 
     * <p>
     * The Drawer will initially be closed.
     * </p>
     * 
     * @param drawer
     *            The {@link Component} to be shown in the drawer
     */
    public Drawer(final Component drawer) {
        this(null, drawer);
    }

    /**
     * <p>
     * Create a new {@link Drawer}.
     * </p>
     * 
     * <p>
     * The Drawer will initially be closed.
     * </p>
     * 
     * @param defaultCaption
     *            The string to be used as the default caption
     * @param drawer
     *            The {@link Component} to be shown in the drawer
     */
    public Drawer(final String defaultCaption, final Component drawer) {
        setDefaultCaption(defaultCaption);
        setDrawerComponent(drawer);
        requestRepaint();
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        final String captionString = (caption != null) ? caption : "";
        target.addAttribute(VDrawer.ATTRIBUTE_DEFAULTCAPTION__STRING,
                captionString);

        target.addAttribute(VDrawer.ATTRIBUTE_ANIMATION_DURATION__INT,
                animationDurationMillis);

        target.addAttribute(VDrawer.ATTRIBUTE_DRAWER_HEIGHT__INT, drawerHeight);

        target.addAttribute(VDrawer.ATTRIBUTE_CALCULATE_HEADER_WIDTH__BOOLEAN,
                calculateHeaderWidth);

        target.addVariable(this, VDrawer.VARIABLE_DRAWERVISIBLE__BOOLEAN,
                drawerOpen);
        target.addAttribute(VDrawer.ATTRIBUTE_ANIMATE__BOOLEAN,
                animationRequested);
        animationRequested = false;

        final Paintable paintableDrawerWidget = (drawer != null) ? drawer
                : new Label();
        paintableDrawerWidget.paint(target);
        target.addAttribute(VDrawer.ATTRIBUTE_DRAWER_HAS_CONTENT__BOOLEAN,
                drawerHasContent);
    }

    /**
     * Check whether the Drawer is currently open.
     * 
     * @return <code>true</code> iff the drawer is shown.
     */
    public boolean isDrawerOpen() {
        return drawerOpen;
    }

    /**
     * Set whether the drawer should be open or not. The transition will not be
     * animated.
     * 
     * @param drawerIsOpen
     *            <code>true</code> iff the drawer should be opened. This method
     *            does nothing, if the <code>drawerIsOpen</code> is the same as
     *            the state the drawer already has.
     * @see #setDrawerOpen(boolean, boolean)
     * @see #toggleDrawer()
     */
    public void setDrawerOpen(final boolean drawerIsOpen) {
        setDrawerOpen(drawerIsOpen, false);
    }

    /**
     * Set whether the drawer should be open or not.
     * 
     * @param drawerIsOpen
     *            <code>true</code> iff the drawer should be opened. This method
     *            does nothing, if the <code>drawerIsOpen</code> is the same as
     *            the state the drawer already has.
     * @param animate
     *            <code>true</code> if the transition should be animated.
     *            <code>false</code> if the transition should be immediate.
     * @see #setAnimationDurationMillis(int)
     * @see #toggleDrawer(boolean)
     */
    public void setDrawerOpen(final boolean drawerIsOpen, final boolean animate) {
        if (drawerOpen != drawerIsOpen) {
            drawerOpen = drawerIsOpen;
            animationRequested = animate;
            requestRepaint();
        }
    }

    /**
     * Toggle the drawer from open to close, or vice versa. This will not be
     * animated.
     * 
     * @see #toggleDrawer(boolean)
     * @see #setDrawerOpen(boolean)
     */
    public void toggleDrawer() {
        toggleDrawer(false);
    }

    /**
     * Toggle the drawer from open to close, or vice versa.
     * 
     * @param animate
     *            <code>true</code> iff the opening or closing of the drawer
     *            should be animated.
     * @see #setAnimationDurationMillis(int)
     * @see #setDrawerOpen(boolean, boolean)
     */
    public void toggleDrawer(final boolean animate) {
        setDrawerOpen(!isDrawerOpen(), animate);
    }

    @Override
    public void changeVariables(final Object source,
            @SuppressWarnings("rawtypes") final Map variables) {
        if (variables.containsKey(VDrawer.VARIABLE_DRAWERVISIBLE__BOOLEAN)) {
            // the request came from the client, so this is always animated.
            setDrawerOpen(
                    (Boolean) variables
                            .get(VDrawer.VARIABLE_DRAWERVISIBLE__BOOLEAN),
                    true);
            requestRepaint();
        }
    }

    /**
     * Get the duration to render the opening or closing of the drawer.
     * 
     * @return The drawer's render time in milliseconds
     */
    public int getAnimationDurationMillis() {
        return animationDurationMillis;
    }

    /**
     * Set the duration to render the opening or closing the drawer.
     * 
     * @param animationDurationMillis
     *            The animation duration in milliseconds <i>(1000ms == 1s)</i>
     */
    public void setAnimationDurationMillis(final int animationDurationMillis) {
        this.animationDurationMillis = animationDurationMillis;

        // no need to request repaint for this
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} instead.
     * @throws UnsupportedOperationException
     *             guaranteed.
     */
    @Override
    @Deprecated
    public void setHeight(final float height, final int unit) {
        throw new UnsupportedOperationException(
                "use setDrawerHeight() instead.");
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} instead.
     * @throws UnsupportedOperationException
     *             guaranteed.
     */
    @Override
    @Deprecated
    public void setHeight(final String height) {
        throw new UnsupportedOperationException(
                "use setDrawerHeight() instead.");
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} instead.
     * @throws UnsupportedOperationException
     *             guaranteed.
     */
    @Deprecated
    @Override
    public void setHeight(final float height) {
        throw new UnsupportedOperationException(
                "use setDrawerHeight() instead.");
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} instead.
     * @throws UnsupportedOperationException
     *             guaranteed.
     */
    @Deprecated
    @Override
    public void setHeightUnits(final int unit) {
        throw new UnsupportedOperationException(
                "use setDrawerHeight() instead.");
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} and
     *             {@link #setWidth(String)} (or {@link #setWidth(float, int)})
     *             instead.
     * @throws UnsupportedOperationException
     *             guaranteed.
     */
    @Deprecated
    @Override
    public void setSizeFull() {
        throw new UnsupportedOperationException("use setDrawerHeight() and "
                + "setWidth() instead.");
    }

    /**
     * @deprecated Use {@link #setDrawerHeight(int)} and
     *             {@link #setWidth(String)} (or {@link #setWidth(float, int)})
     *             instead.
     */
    @Deprecated
    @Override
    public void setSizeUndefined() {
        setDrawerHeight(DRAWER_HEIGHT_AUTO);
        setWidth(null);
    }

    /**
     * Set the height of the drawer-portion of the widget.
     * 
     * @param drawerHeightPixels
     *            The height of the drawer in pixels. If the value is
     *            {@link Drawer#DRAWER_HEIGHT_AUTO}, the height of the drawer is
     *            calculated automatically, according to the {@link Component}
     *            inside.
     */
    public void setDrawerHeight(final int drawerHeightPixels) {
        if (drawerHeightPixels >= 0 || drawerHeightPixels == DRAWER_HEIGHT_AUTO) {
            drawerHeight = drawerHeightPixels;
            requestRepaint();
        } else {
            throw new IllegalArgumentException(
                    "Argument must be 0 or more, or Drawer.DRAWER_HEIGHT_AUTO.");
        }
    }

    /**
     * Define the {@link Component} that will be displayed in the drawer.
     * 
     * @param component
     *            The component to be displayed in the drawer. If
     *            <code>null</code>, the current component will be removed.
     */
    public void setDrawerComponent(final Component component) {
        if (drawer != null) {
            super.removeComponent(drawer);
        }

        drawer = component;
        drawerHasContent = (component != null);

        if (component != null) {
            super.addComponent(drawer);
        }

        requestRepaint();
    }

    /**
     * Remove the {@link Component} currently in the drawer.
     * 
     * @return The removed Component. <code>null</code> if there is no Component
     *         inside.
     */
    public Component removeDrawerComponent() {
        final Component drawerContent = getDrawerContent();
        setDrawerComponent(null);
        return drawerContent;
    }

    /**
     * Get the current drawer content.
     * 
     * @return The current {@link Component} in the drawer. <code>null</code> if
     *         there is no Component inside.
     */
    public Component getDrawerContent() {
        return drawer;
    }

    /**
     * Get the default caption of the drawer.
     * 
     * @return The string that has been set as the default caption
     */
    public String getDefaultCaption() {
        return caption;
    }

    /**
     * Get the currently shown caption.
     * 
     * @return If a {@link Component} is in the drawer and it has a caption,
     *         that will be returned. Otherwise, the default caption is
     *         returned.
     * @see #getDefaultCaption()
     * @see #setDefaultCaption(String)
     * @see #setDrawerComponent(Component)
     */
    public String getVisibleCaption() {
        final Component drawerContent = getDrawerContent();
        if (drawerContent != null && drawerContent.getCaption() != null) {
            return drawerContent.getCaption();
        } else {
            return getDefaultCaption();
        }
    }

    /**
     * <p>
     * Set the drawer's default caption
     * </p>
     * 
     * <p>
     * If there is no {@link Component} in the drawer, or that Component has no
     * caption of its own, this default caption will be shown.
     * </p>
     * 
     * @param caption
     *            The caption that will be shown by default.
     */
    public void setDefaultCaption(final String caption) {
        this.caption = caption;
        requestRepaint();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The {@link Drawer} has only one {@link Component} at a maximum.
     * </p>
     */
    public Iterator<Component> getComponentIterator() {
        return new Iterator<Component>() {
            private boolean hasNotShownDrawer = true;

            public boolean hasNext() {
                return hasNotShownDrawer && drawer != null;
            }

            public Component next() {
                if (hasNotShownDrawer) {
                    hasNotShownDrawer = false;
                    return drawer;
                } else {
                    return null;
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @deprecated Use {@link #setDrawerComponent(Component)} instead.
     */
    @Deprecated
    public void replaceComponent(final Component oldComponent,
            final Component newComponent) {
        if (oldComponent == drawer) {
            setDrawerComponent(newComponent);
        }
    }

    /**
     * <p>
     * Add a component into the Drawer.
     * </p>
     * 
     * <p>
     * <em>Note:</em> The drawer can only contain one Component. If there
     * already is a component, this method does nothing.
     * </p>
     * 
     * @deprecated Use {@link #setDrawerComponent(Component)} instead.
     */
    @Deprecated
    @Override
    public void addComponent(final Component c) {
        if (drawer != null) {
            setDrawerComponent(c);
        }
    }

    /**
     * Remove the drawer component.
     * 
     * @deprecated Use {@link #removeDrawerComponent()} instead.
     */
    @Deprecated
    @Override
    public void removeAllComponents() {
        setDrawerComponent(null);
    }

    /**
     * <p>
     * Remove the drawer component.
     * </p>
     * 
     * @param c
     *            The Component to remove. If <code>c</code> is not the current
     *            drawer component, this method does nothing.
     * @deprecated Use {@link #removeDrawerComponent()} instead.
     */
    @Deprecated
    @Override
    public void removeComponent(final Component c) {
        if (c == drawer) {
            setDrawerComponent(null);
        }
    }

    @Override
    public void setWidth(final float width, final int unit) {
        /*
         * All setWidth() calls seem to trickle down to this method call. Inform
         * the client side that the caption-width needs to be fixed.
         */
        super.setWidth(width, unit);

        /*
         * the header width will be caluclated, if the component has undefined
         * width
         */
        calculateHeaderWidth = (width == -1 && unit == UNITS_PIXELS);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This returns the {@link Drawer}'s own caption, and should not be confused
     * with {@link #getDefaultCaption()}.
     * </p>
     * 
     * @see #getDefaultCaption()
     */
    @Override
    public String getCaption() {
        return super.getCaption();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This sets the {@link Drawer}'s own caption, and should not be confused
     * with {@link #setDefaultCaption(String)}.
     * </p>
     * 
     * @see #setDefaultCaption(String)
     */
    @Override
    public void setCaption(final String caption) {
        super.setCaption(caption);
    }
}
