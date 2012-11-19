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

package org.vaadin.henrik.drawer.widgetset.client.ui;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VCaption;

/**
 * @author Henrik Paul / IT Mill Ltd
 */
public class VDrawer extends Composite implements Paintable, Container,
        HasWidgets {

    private class DrawerAnimation extends Animation {
        private final boolean expand;

        private DrawerAnimation(final boolean expand) {
            this.expand = expand;
        }

        @Override
        protected void onUpdate(final double progress) {
            final double multiplier = expand ? progress : 1 - progress;

            if (multiplier == 0) {
                setDrawerVisible(expand);
            }

            drawer.setHeight((drawerHeight * multiplier) + "px");

            Util.notifyParentOfSizeChange(VDrawer.this, false);
        }

        /**
         * Ease-out interpolation only, instead of the default of both.
         */
        @Override
        protected double interpolate(final double progress) {
            // see http://stackoverflow.com/questions/196173/#196209
            return 1.5 * progress - 0.5 * Math.pow(progress, 3);

            // return 2 * progress - Math.pow(progress, 2);
            // return 1.2 * progress - 0.2 * Math.pow(progress, 6);
        }
    }

    private class CaptionPanel extends Composite {
        public CaptionPanel(final Widget icon, final Widget header) {
            final HorizontalPanel root = new HorizontalPanel();
            initWidget(root);

            setStylePrimaryName(CAPTION_CLASSNAME);
            root.setWidth("100%");
            root.add(icon);
            root.add(header);
            root.setCellWidth(header, "100%");
        }

        public HandlerRegistration addClickHandler(final ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }

    private class CaptionIcon extends Widget {
        public CaptionIcon() {
            final Element root = DOM.createDiv();
            root.setInnerHTML("&nbsp;");

            setElement(root);
            setExpanded(false);
        }

        public void setExpanded(final boolean isExpanded) {
            if (isExpanded) {
                getElement().setClassName(CAPTION_ICON_EXPANDED_CLASSNAME);
            } else {
                getElement().setClassName(CAPTION_ICON_COLLAPSED_CLASSNAME);
            }
        }
    }

    /** Set the tagname used to statically resolve widget from UIDL. */
    public static final String TAGNAME = "drawer";

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-" + TAGNAME;
    public static final String DRAWER_CLASSNAME = CLASSNAME + "-content";
    public static final String CAPTION_CLASSNAME = CLASSNAME + "-caption";
    public static final String CAPTION_ICON_EXPANDED_CLASSNAME = CAPTION_CLASSNAME
            + "-expanded";
    public static final String CAPTION_ICON_COLLAPSED_CLASSNAME = CAPTION_CLASSNAME
            + "-collapsed";

    public static final String ATTRIBUTE_DEFAULTCAPTION__STRING = "defcaption";
    public static final String ATTRIBUTE_ANIMATE__BOOLEAN = "animate";
    public static final String ATTRIBUTE_ANIMATION_DURATION__INT = "animationSpeed";
    public static final String ATTRIBUTE_DRAWER_HEIGHT__INT = "drawerHeight";
    public static final String ATTRIBUTE_DRAWER_HAS_CONTENT__BOOLEAN = "hascontent";
    public static final String ATTRIBUTE_CALCULATE_HEADER_WIDTH__BOOLEAN = "calculateWidth";

    public static final String VARIABLE_DRAWERVISIBLE__BOOLEAN = "drawerVisible";

    private final Panel root = new VerticalPanel();
    private final SimplePanel drawer = new SimplePanel();

    private final Widget loading = new Label();
    private String componentCaption = null;
    private boolean componentCaptionIsVisible = true;
    private String defaultCaption = "";

    private final Label captionLabel = new Label(defaultCaption);
    private final CaptionIcon captionIcon = new CaptionIcon();
    private CaptionPanel captionPanel = new CaptionPanel(captionIcon,
            captionLabel);

    private boolean drawerHasContents = false;

    private int animationDuration = 500;
    private int drawerHeight = -1;
    private boolean calculateDrawerHeight = true;
    private boolean calculateCaptionWidth = true;
    private boolean animate = false;

    private boolean disabled = false;

    /**
     * The Vaadin property, intended for a {@link VCaption}, to tell whether the
     * caption is invisible.
     */
    private final static String VAADIN_CAPTION_UIDL_INVISIBLE__BOOLEAN = "invisible";

    /**
     * The Vaadin property, intended for a {@link VCaption}, for the caption's
     * contents.
     */
    private final static String VAADIN_CAPTION_UIDL_TEXT__STRING = "caption";

    /** The Vaadin property telling whether a component is disabled or not. */
    private final static String VAADIN_DISABLED__BOOLEAN = "disabled";

    /** Component identifier in UIDL communications. */
    String uidlId;

    /** Reference to the server connection object. */
    ApplicationConnection client;

    /** how many times the component has been updated from the server side */
    private int updateCount = 0;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VDrawer() {
        super();
        initWidget(root);

        setStyleName(CLASSNAME);

        captionPanel = new CaptionPanel(captionIcon, captionLabel);
        captionPanel.addClickHandler(new ClickHandler() {
            public void onClick(final ClickEvent event) {
                toggleDrawerVisibility();
            }
        });

        root.add(captionPanel);
        root.add(drawer);
        drawer.setWidget(loading);

        drawer.setStylePrimaryName(DRAWER_CLASSNAME);
    }

    public void updateFromUIDL(final UIDL uidl,
            final ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        this.client = client;
        uidlId = uidl.getId();

        /*
         * we need to keep tabs on whether the component is disabled or not. We
         * get warnings if sending variable changes from disabled widgets.
         */
        if (uidl.hasAttribute(VAADIN_DISABLED__BOOLEAN)) {
            disabled = uidl.getBooleanAttribute(VAADIN_DISABLED__BOOLEAN);
        } else {
            disabled = false;
        }

        if (uidl.hasAttribute(ATTRIBUTE_DEFAULTCAPTION__STRING)) {
            defaultCaption = uidl
                    .getStringAttribute(ATTRIBUTE_DEFAULTCAPTION__STRING);
            updateCaptionInternal();
        }

        if (uidl.hasAttribute(ATTRIBUTE_ANIMATION_DURATION__INT)) {
            animationDuration = uidl
                    .getIntAttribute(ATTRIBUTE_ANIMATION_DURATION__INT);
        }

        if (uidl.hasAttribute(ATTRIBUTE_DRAWER_HEIGHT__INT)) {
            drawerHeight = uidl.getIntAttribute(ATTRIBUTE_DRAWER_HEIGHT__INT);

            if (drawerHeight == -1) {
                calculateDrawerHeight = true;
                adjustForAutoDimensions();
            } else {
                calculateDrawerHeight = false;
            }
        }

        if (uidl.hasAttribute(ATTRIBUTE_CALCULATE_HEADER_WIDTH__BOOLEAN)) {
            calculateCaptionWidth = uidl
                    .getBooleanAttribute(ATTRIBUTE_CALCULATE_HEADER_WIDTH__BOOLEAN);
        }

        // get the drawer Component
        if (uidl.getChildCount() > 0) {
            final UIDL drawerUIDL = uidl.getChildUIDL(0);
            final Paintable paintable = client.getPaintable(drawerUIDL);
            final Widget widgetCopyOfPaintable = (Widget) paintable;

            if (widgetCopyOfPaintable != drawer.getWidget()) {
                if (drawer.getWidget() == loading) {
                    // if it's the initial component, just remove it.
                    drawer.clear();
                    drawer.setHeight("0px");
                }

                final Paintable oldDrawerContent = (Paintable) drawer
                        .getWidget();
                drawer.setWidget(widgetCopyOfPaintable);

                if (oldDrawerContent != null) {
                    client.unregisterPaintable(oldDrawerContent);
                }

                final boolean wasVisible = isDrawerVisible();
                setDrawerVisible(true);
                paintable.updateFromUIDL(drawerUIDL, client);
                setDrawerVisible(wasVisible);

                if (calculateDrawerHeight) {
                    /*
                     * Since we have a new component, let's recalculate its
                     * size, and do the appropriate animagication.
                     */
                    drawerHeight = -1;
                    adjustForAutoDimensions();
                }

                drawerHasContents = uidl
                        .getBooleanAttribute(ATTRIBUTE_DRAWER_HAS_CONTENT__BOOLEAN);
                if (drawerHasContents) {
                    /*
                     * Since Vaadin doesn't notify the initial caption of a
                     * component, we need to do dig it out manually.
                     */
                    updateCaption(paintable, drawerUIDL);

                } else {
                    clearComponentCaption();
                    captionIcon.setExpanded(false);
                }
            }
        }

        /*
         * This needs to be before the Component attaching step or we're trying
         * to calculate dimensions with an invisible Widget. That's not what we
         * prefer to do.
         */
        if (uidl.hasVariable(VARIABLE_DRAWERVISIBLE__BOOLEAN)) {
            if (drawerHasContents) {
                animate = uidl.getBooleanAttribute(ATTRIBUTE_ANIMATE__BOOLEAN);
                final boolean contentIsVisible = uidl
                        .getBooleanVariable(VARIABLE_DRAWERVISIBLE__BOOLEAN);

                if (animate) {
                    DeferredCommand.addCommand(new Command() {
                        public void execute() {
                            new DrawerAnimation(contentIsVisible)
                                    .run(animationDuration);
                        }
                    });
                } else {
                    setDrawerVisible(contentIsVisible);

                    /*
                     * when the page is refreshed, make sure that we have the
                     * right height for the drawer
                     */
                    if (contentIsVisible) {
                        drawer.setHeight(drawerHeight + "px");
                    } else {
                        drawer.setHeight("0px");
                    }
                }

                captionIcon.setExpanded(contentIsVisible);
            } else {
                drawer.setHeight("0px");
                setDrawerVisible(false);
                captionIcon.setExpanded(false);
            }
        }
        // Util.notifyParentOfSizeChange(this, false);

        // hacky fix for the drawer area being visible upon first render
        updateCount++;
        if (updateCount == 1 && !isDrawerVisible()) {
            drawer.setHeight("0px");
        }
    }

    private void toggleDrawerVisibility() {
        if (uidlId != null && client != null && isAttached() && !disabled) {
            client.updateVariable(uidlId, VARIABLE_DRAWERVISIBLE__BOOLEAN,
                    !isDrawerVisible(), true);
        }
    }

    /**
     * <p>
     * Fix the drawer's height.
     * </p>
     * 
     * <p>
     * This method will both calculate the needed drawer's dimensions, but also
     * animate the size change if the drawer happened to be visible.
     * </p>
     */
    private void adjustForAutoDimensions() {
        if (isAttached() && drawer.getWidget() != null) {
            if (calculateDrawerHeight) {
                /*
                 * The height needs to be calculated, so that the open/close
                 * animations know how far to go.
                 */

                if (drawerHeight == -1) {
                    final boolean wasVisible = isDrawerVisible();
                    setDrawerVisible(true);
                    drawerHeight = Util.getRequiredHeight(drawer.getWidget());
                    setDrawerVisible(wasVisible);
                }

                if (isDrawerVisible()) {
                    final int toHeight = drawer.getWidget().getOffsetHeight();
                    final int toWidth = drawer.getWidget().getOffsetWidth();

                    if (animate) {
                        final int fromHeight = drawer.getOffsetHeight();
                        final int fromWidth = drawer.getOffsetWidth();
                        new Animation() {
                            @Override
                            protected void onUpdate(final double progress) {
                                final long grownHeight = Math
                                        .round((toHeight - fromHeight)
                                                * progress);
                                drawer.setHeight((fromHeight + grownHeight)
                                        + "px");

                                final long grownWidth = Math
                                        .round((toWidth - fromWidth) * progress);
                                drawer.setWidth((fromWidth + grownWidth) + "px");

                                Util.notifyParentOfSizeChange(VDrawer.this,
                                        false);
                            }
                        }.run(animationDuration / 2);

                    } else {
                        drawer.setHeight(toHeight + "px");
                        drawer.setWidth(toWidth + "px");
                    }
                }
            }

            if (calculateCaptionWidth) {
                /*
                 * The width of the header needs to be calculated, if the
                 * component has no explicit width, or the header width will not
                 * be the same when the drawer is visible and when it's hidden.
                 */

                final boolean wasVisible = isDrawerVisible();
                setDrawerVisible(true);
                captionPanel.setWidth(drawer.getWidget().getOffsetWidth()
                        + "px");
                setDrawerVisible(wasVisible);
            }
        }
    }

    public RenderSpace getAllocatedSpace(final Widget child) {
        final int childHeight = child.getOffsetHeight();
        final int childWidth = child.getOffsetWidth();

        final int myWidth = getOffsetWidth();
        // final int myHeight = getOffsetHeight();

        final int captionWidth = captionPanel.getOffsetWidth();

        final int finalHeight;
        final int finalWidth;

        if (calculateCaptionWidth) {
            finalWidth = Math.max(childWidth, Math.max(myWidth, captionWidth));
        } else {
            finalWidth = captionWidth;
        }

        if (calculateDrawerHeight) {
            finalHeight = Math.max(childHeight, drawerHeight);
        } else {
            finalHeight = drawerHeight;
        }

        return new RenderSpace(finalWidth, finalHeight);
    }

    public boolean hasChildComponent(final Widget component) {
        return component.getParent() == drawer;
    }

    public void replaceChildComponent(final Widget oldComponent,
            final Widget newComponent) {
        if (oldComponent == drawer.getWidget()) {
            drawer.setWidget(newComponent);
        }
    }

    public boolean requestLayout(final Set<Paintable> children) {
        if (isDrawerVisible()) {
            if (calculateDrawerHeight) {
                final int requiredHeight = Util.getRequiredHeight(drawer
                        .getWidget());
                drawer.setHeight(requiredHeight + "px");
            }

            if (calculateCaptionWidth) {
                final int requiredWidth = Util.getRequiredWidth(drawer
                        .getWidget());
                captionPanel.setWidth(requiredWidth + "px");
            }
            return false;
        } else {
            return true;
        }
    }

    public void updateCaption(final Paintable component, final UIDL uidl) {
        /*
         * Since the caption panel doubles as the component caption, this needs
         * some elbow grease.
         */

        if (component == drawer.getWidget()) {
            if (uidl.hasAttribute(VAADIN_CAPTION_UIDL_INVISIBLE__BOOLEAN)) {
                componentCaptionIsVisible = !uidl
                        .getBooleanAttribute(VAADIN_CAPTION_UIDL_INVISIBLE__BOOLEAN);
            }

            if (uidl.hasAttribute(VAADIN_CAPTION_UIDL_TEXT__STRING)) {
                componentCaption = uidl
                        .getStringAttribute(VAADIN_CAPTION_UIDL_TEXT__STRING);
            }

            updateCaptionInternal();
        }
    }

    private void updateCaptionInternal() {
        if (componentCaptionIsVisible && componentCaption != null) {
            captionLabel.setText(componentCaption);
        } else {
            captionLabel.setText(defaultCaption);
        }
    }

    /**
     * <p>
     * A means to clear the component's explicit caption.
     * </p>
     * <p>
     * This needs to be called manually, because Vaadin doesn't call
     * {@link #updateCaption(Paintable, UIDL)} on its own. A bummer, really.
     */
    private void clearComponentCaption() {
        componentCaption = null;
        componentCaptionIsVisible = false;
        updateCaptionInternal();
    }

    private boolean isDrawerVisible() {
        return !drawer.getElement().getStyle().getProperty("visibility")
                .equals("hidden");
    }

    private void setDrawerVisible(final boolean visible) {
        drawer.getElement().getStyle()
                .setProperty("visibility", visible ? "visible" : "hidden");
    }

    public void add(final Widget w) {
        drawer.add(w);
    }

    public void clear() {
        drawer.clear();
    }

    public Iterator<Widget> iterator() {
        return drawer.iterator();
    }

    public boolean remove(final Widget w) {
        return drawer.remove(w);
    }

    @Override
    public void setWidth(final String width) {
        super.setWidth(width);

        drawer.setWidth(width);
        captionPanel.setWidth(width);
    }
}
