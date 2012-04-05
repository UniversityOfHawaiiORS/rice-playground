/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.uif.component;

import org.kuali.rice.krad.uif.modifier.ComponentModifier;
import org.kuali.rice.krad.uif.service.ViewHelperService;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.widget.Tooltip;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * All classes of the UIF that are used as a rendering element implement the
 * component interface. This interface defines basic properties and methods that
 * all such classes much implement. All components within the framework have the
 * following structure:
 * <ul>
 * <li>Dictionary Configuration/Composition</li>
 * <li>Java Class (the Component implementation</li>
 * <li>>JSP Template Renderer</li>
 * </ul>
 *
 * There are three basic types of components:
 * <ul>
 * <li>Container Components: <code>View</code>, <code>Group</code></li>
 * <li>Field Components: <code>Field</code></li>
 * <li>Widget Components: <code>Widget</code></li>
 * </ul>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * @see org.kuali.rice.krad.uif.container.Container
 * @see org.kuali.rice.krad.uif.field.Field
 * @see org.kuali.rice.krad.uif.widget.Widget
 */
public interface Component extends Configurable, Serializable, Ordered, ScriptEventSupport {

    /**
     * The unique id (within a given tree) for the component
     *
     * <p>
     * The id will be used by renderers to set the HTML element id. This gives a
     * way to find various elements for scripting. If the id is not given, a
     * default will be generated by the framework
     * </p>
     *
     * @return String id
     */
    public String getId();

    /**
     * Sets the unique id (within a given tree) for the component
     *
     * @param id - string to set as the component id
     */
    public void setId(String id);

    /**
     * Holds the id for the component that can be used to request new instances of that component from the
     * {@link org.kuali.rice.krad.uif.util.ComponentFactory}
     *
     * <p>
     * During component refreshes the component is reinitialized and the lifecycle is performed again to
     * reflect the component state based on the latest updates (data, other component state). Since the lifecycle
     * is only performed on the component, a new instance with configured initial state needs to be retrieved. Some
     * component instances, such as those that are nested or created in code, cannot be obtained from the spring
     * factory. For those the initial state is captured during the perform initialize phase and the factory id
     * generated for referencing retrieving that configuration during a refresh
     * </p>
     *
     * @return String bean id for component
     */
    public String getFactoryId();

    /**
     * Sets the factory id that backs the component instance
     *
     * @param factoryId
     */
    public void setFactoryId(String factoryId);

    /**
     * The name for the component type
     *
     * <p>
     * This is used within the rendering layer to pass the component instance
     * into the template. The component instance is exported under the name
     * given by this method.
     * </p>
     *
     * @return String type name
     */
    public String getComponentTypeName();

    /**
     * The path to the JSP file that should be called to render the component
     *
     * <p>
     * The path should be relative to the web root. An attribute will be
     * available to the component to use under the name given by the method
     * <code>getComponentTypeName</code>. Based on the component type,
     * additional attributes could be available for use. See the component
     * documentation for more information on such attributes.
     * </p>
     *
     * <p>
     * e.g. '/krad/WEB-INF/jsp/tiles/component.jsp'
     * </p>
     *
     * @return String representing the template path
     */
    public String getTemplate();

    /**
     * Setter for the components template
     *
     * @param template
     */
    public void setTemplate(String template);

    /**
     * A title for the component. Depending on the component can be used in
     * various ways. For example with a Container component the title is used to
     * set the header text. For components like controls other other components
     * that render an HTML element it is used to set the HTML title attribute
     *
     * @return String title for component
     */
    public String getTitle();

    /**
     * Setter for the components title
     *
     * @param title
     */
    public void setTitle(String title);

    /**
     * Should be called to initialize the component
     *
     * <p>
     * Where components can set defaults and setup other necessary state. The
     * initialize method should only be called once per component lifecycle and
     * is invoked within the initialize phase of the view lifecylce.
     * </p>
     *
     * @param view - view instance in which the component belongs
     * @param model - object instance containing the view data
     * @see ViewHelperService#performInitialization(org.kuali.rice.krad.uif.view.View, Object)
     */
    public void performInitialization(View view, Object model);

    /**
     * Called after the initialize phase to perform conditional logic based on
     * the model data
     *
     * <p>
     * Where components can perform conditional logic such as dynamically
     * generating new fields or setting field state based on the given data
     * </p>
     *
     * @param view - view instance to which the component belongs
     * @param model - Top level object containing the data (could be the form or a
     * top level business object, dto)
     */
    public void performApplyModel(View view, Object model, Component parent);

    /**
     * The last phase before the view is rendered. Here final preparations can
     * be made based on the updated view state
     *
     * @param view - view instance that should be finalized for rendering
     * @param model - top level object containing the data
     * @param parent - parent component
     */
    public void performFinalize(View view, Object model, Component parent);

    /**
     * List of components that are contained within the component and should be sent through
     * the lifecycle
     *
     * <p>
     * Used by <code>ViewHelperService</code> for the various lifecycle
     * callbacks
     * </p>
     *
     * @return List<Component> child components
     */
    public List<Component> getComponentsForLifecycle();

    /**
     * List of components that are maintained by the component as prototypes for creating other component instances
     *
     * <p>
     * Prototypes are held for configuring how a component should be created during the lifecycle. An example of this
     * are the fields in a collection group that are created for each collection record. They only participate in the
     * initialize phase.
     * </p>
     *
     * @return List<Component> child component prototypes
     */
    public List<Component> getComponentPrototypes();

    /**
     * List of components that are contained within the List of <code>PropertyReplacer</code> in component
     *
     * <p>
     * Used to get all the nested components in the property replacer's
     * </p>
     *
     * @return List<Component> <code>PropertyReplacer</code> child components
     */
    public List<Component> getPropertyReplacerComponents();      
    
    /**
     * <code>ComponentModifier</code> instances that should be invoked to
     * initialize the component
     *
     * <p>
     * These provide dynamic initialization behavior for the component and are
     * configured through the components definition. Each initializer will get
     * invoked by the initialize method.
     * </p>
     *
     * @return List of component modifiers
     * @see ViewHelperService#performInitialization(org.kuali.rice.krad.uif.view.View, Object)
     */
    public List<ComponentModifier> getComponentModifiers();

    /**
     * Setter for the components List of <code>ComponentModifier</code>
     * instances
     *
     * @param componentModifiers
     */
    public void setComponentModifiers(List<ComponentModifier> componentModifiers);

    /**
     * Indicates whether the component should be rendered in the UI
     *
     * <p>
     * If set to false, the corresponding component template will not be invoked
     * (therefore nothing will be rendered to the UI).
     * </p>
     *
     * @return boolean true if the component should be rendered, false if it
     *         should not be
     */
    public boolean isRender();

    /**
     * Setter for the components render indicator
     *
     * @param render
     */
    public void setRender(boolean render);

    /**
     * Indicates whether the component should be hidden in the UI
     *
     * <p>
     * How the hidden data is maintained depends on the views persistence mode.
     * If the mode is request, the corresponding data will be rendered to the UI
     * but not visible. If the mode is session, the data will not be rendered to
     * the UI but maintained server side.
     * </p>
     *
     * <p>
     * For a <code>Container</code> component, the hidden setting will apply to
     * all contained components (making a section hidden makes all fields within
     * the section hidden)
     * </p>
     *
     * @return boolean true if the component should be hidden, false if it
     *         should be visible
     */
    public boolean isHidden();

    /**
     * Setter for the hidden indicator
     *
     * @param hidden
     */
    public void setHidden(boolean hidden);

    /**
     * Indicates whether the component can be edited
     *
     * <p>
     * When readOnly the controls and widgets of <code>Field</code> components
     * will not be rendered. If the Field has an underlying value it will be
     * displayed readOnly to the user.
     * </p>
     *
     * <p>
     * For a <code>Container</code> component, the readOnly setting will apply
     * to all contained components (making a section readOnly makes all fields
     * within the section readOnly)
     * </p>
     * </p>
     *
     * @return boolean true if the component should be readOnly, false if is
     *         allows editing
     */
    public boolean isReadOnly();

    /**
     * Setter for the read only indicator
     *
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly);

    /**
     * Indicates whether the component is required
     *
     * <p>
     * At the general component level required means there is some action the
     * user needs to take within the component. For example, within a section it
     * might mean the fields within the section should be completed. At a field
     * level, it means the field should be completed. This provides the ability
     * for the renderers to indicate the required action.
     * </p>
     *
     * @return boolean true if the component is required, false if it is not
     *         required
     */
    public Boolean getRequired();

    /**
     * Setter for the required indicator
     *
     * @param required
     */
    public void setRequired(Boolean required);

    /**
     * CSS style string to be applied to the component
     *
     * <p>
     * Any style override or additions can be specified with this attribute.
     * This is used by the renderer to set the style attribute on the
     * corresponding element.
     * </p>
     *
     * <p>
     * e.g. 'color: #000000;text-decoration: underline;'
     * </p>
     *
     * @return String css style string
     */
    public String getStyle();

    /**
     * Setter for the components style
     *
     * @param style
     */
    public void setStyle(String style);

    /**
     * CSS style class(s) to be applied to the component
     *
     * <p>
     * Declares style classes for the component. Multiple classes are specified
     * with a space delimiter. This is used by the renderer to set the class
     * attribute on the corresponding element. The class(s) declared must be
     * available in the common style sheets or the style sheets specified for
     * the view
     * </p>
     *
     *
     * @return List<String> css style classes to apply
     */
    public List<String> getStyleClasses();

    /**
     * Setter for the components style classes
     *
     * @param styleClasses
     */
    public void setStyleClasses(List<String> styleClasses);

    /**
     * Adds a single style to the list of styles on this component
     *
     * @param styleClass
     */
    public void addStyleClass(String styleClass);

    /**
     * TODO: javadoc
     *
     * @param itemStyle
     */
    public void appendToStyle(String itemStyle);

    /**
     * Number of places the component should take up horizontally in the
     * container
     *
     * <p>
     * All components belong to a <code>Container</code> and are placed using a
     * <code>LayoutManager</code>. This property specifies how many places
     * horizontally the component should take up within the container. This is
     * only applicable for table based layout managers. Default is 1
     * </p>
     *
     * TODO: this should not be on component interface since it only applies if
     * the layout manager supports it, need some sort of layoutOptions map for
     * field level options that depend on the manager
     *
     * @return int number of columns to span
     */
    public int getColSpan();

    /**
     * Setter for the components column span
     *
     * @param colSpan
     */
    public void setColSpan(int colSpan);

    /**
     * Number of places the component should take up vertically in the container
     *
     * <p>
     * All components belong to a <code>Container</code> and are placed using a
     * <code>LayoutManager</code>. This property specifies how many places
     * vertically the component should take up within the container. This is
     * only applicable for table based layout managers. Default is 1
     * </p>
     *
     * TODO: this should not be on component interface since it only applies if
     * the layout manager supports it, need some sort of layoutOptions map for
     * field level options that depend on the manager
     *
     * @return int number of rows to span
     */
    public int getRowSpan();

    /**
     * Setter for the component row span
     *
     * @param rowSpan
     */
    public void setRowSpan(int rowSpan);

    /**
     * Context map for the component
     *
     * <p>
     * Any el statements configured for the components properties (e.g.
     * title="@{foo.property}") are evaluated using the el context map. This map
     * will get populated with default objects like the model, view, and request
     * from the <code>ViewHelperService</code>. Other components can push
     * further objects into the context so that they are available for use with
     * that component. For example, <code>Field</code> instances that are part
     * of a collection line as receive the current line instance
     * </p>
     *
     * <p>
     * Context map also provides objects to methods that are invoked for
     * <code>GeneratedField</code> instances
     * </p>
     *
     * <p>
     * The Map key gives the name of the variable that can be used within
     * expressions, and the Map value gives the object instance for which
     * expressions containing the variable should evaluate against
     * </p>
     *
     * <p>
     * NOTE: Calling getContext().putAll() will skip updating any configured property replacers for the
     * component. Instead you should call #pushAllToContext
     * </p>
     *
     * @return Map<String, Object> context
     */
    public Map<String, Object> getContext();

    /**
     * Setter for the context Map
     *
     * @param context
     */
    public void setContext(Map<String, Object> context);

    /**
     * Places the given object into the context Map for the component with the
     * given name
     *
     * <p>
     * Note this also will push context to property replacers configured on the component.
     * To place multiple objects in the context, you should use #pushAllToContext since that
     * will call this method for each and update property replacers. Using #getContext().putAll()
     * will bypass property replacers.
     * </p>
     *
     * @param objectName - name the object should be exposed under in the context map
     * @param object - object instance to place into context
     */
    public void pushObjectToContext(String objectName, Object object);

    /**
     * Places each entry of the given Map into the context for the component
     *
     * <p>
     * Note this will call #pushObjectToContext for each entry which will update any configured property
     * replacers as well. This should be used in place of getContext().putAll()
     * </p>
     *
     * @param objects - Map<String, Object> objects to add to context, where the entry key will be the context key
     * and the entry value will be the context value
     */
    public void pushAllToContext(Map<String, Object> objects);

    /**
     * List of <code>PropertyReplacer</code> instances that will be evaluated
     * during the view lifecycle to conditionally set properties on the
     * <code>Component</code> based on expression evaluations
     *
     * @return List<PropertyReplacer> replacers to evaluate
     */
    public List<PropertyReplacer> getPropertyReplacers();

    /**
     * Setter for the components property substitutions
     *
     * @param propertyReplacers
     */
    public void setPropertyReplacers(List<PropertyReplacer> propertyReplacers);

    /**
     * Options that are passed through to the Component renderer. The Map key is
     * the option name, with the Map value as the option value. See
     * documentation on the particular widget render for available options.
     *
     * @return Map<String, String> options
     */
    public Map<String, String> getTemplateOptions();

    /**
     * Setter for the template's options
     *
     * @param templateOptions
     */
    public void setTemplateOptions(Map<String, String> templateOptions);

    /**
     * Options that are passed through to the Component renderer. See
     * documentation on the particular component render for available options.
     *
     * @return String options
     */
    public String getTemplateOptionsJSString();

    /**
     * Setter for the template's options
     *
     * @param templateOptionsJSString
     */
    public void setTemplateOptionsJSString(String templateOptionsJSString);

    /**
     * Can be used to order a component within a List of other components, lower
     * numbers are placed higher up in the list, while higher numbers are placed
     * lower in the list
     *
     * @return int ordering number
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder();

    /**
     * Setter for the component's order
     *
     * @param order
     */
    public void setOrder(int order);

    /**
     * Name of the method that should be invoked for finalizing the component
     * configuration (full method name, without parameters or return type)
     *
     * <p>
     * Note the method can also be set with the finalizeMethodInvoker
     * targetMethod property. If the method is on the configured
     * <code>ViewHelperService</code>, only this property needs to be configured
     * </p>
     *
     * <p>
     * The model backing the view will be passed as the first argument method and then
     * the <code>Component</code> instance as the second argument. If any additional method
     * arguments are declared with the finalizeMethodAdditionalArguments, they will then
     * be passed in the order declared in the list
     * </p>
     *
     * <p>
     * If the component is selfRendered, the finalize method can return a string which
     * will be set as the component's renderOutput. The selfRendered indicator will also
     * be set to true on the component.
     * </p>
     *
     * @return String method name
     */
    public String getFinalizeMethodToCall();

    /**
     * List of Object instances that should be passed as arguments to the finalize method
     *
     * <p>
     * These arguments are passed to the finalize method after the standard model and component
     * arguments. They are passed in the order declared in the list
     * </p>
     *
     * @return List<Object> additional method arguments
     */
    public List<Object> getFinalizeMethodAdditionalArguments();

    /**
     * <code>MethodInvokerConfig</code> instance for the method that should be invoked
     * for finalizing the component configuration
     *
     * <p>
     * MethodInvoker can be configured to specify the class or object the method
     * should be called on. For static method invocations, the targetClass
     * property can be configured. For object invocations, that targetObject
     * property can be configured
     * </p>
     *
     * <p>
     * If the component is selfRendered, the finalize method can return a string which
     * will be set as the component's renderOutput. The selfRendered indicator will also
     * be set to true on the component.
     * </p>
     *
     * @return MethodInvokerConfig instance
     */
    public MethodInvokerConfig getFinalizeMethodInvoker();

    /**
     * Indicates whether the component contains its own render output (through
     * the renderOutput property)
     *
     * <p>
     * If self rendered is true, the corresponding template for the component
     * will not be invoked and the renderOutput String will be written to the
     * response as is.
     * </p>
     *
     * @return boolean true if component is self rendered, false if not (renders
     *         through template)
     */
    public boolean isSelfRendered();

    /**
     * Setter for the self render indicator
     *
     * @param selfRendered
     */
    public void setSelfRendered(boolean selfRendered);

    /**
     * Rendering output for the component that will be sent as part of the
     * response (can contain static text and HTML)
     *
     * @return String render output
     */
    public String getRenderOutput();

    /**
     * Setter for the component's render output
     *
     * @param renderOutput
     */
    public void setRenderOutput(String renderOutput);

    /**
     * Indicates whether the component should be stored with the session view regardless of configuration
     *
     * <p>
     * By default the framework nulls out any components that do not have a refresh condition or are needed for
     * collection processing. This can be a problem if custom application code is written to refresh a component
     * without setting the corresponding component flag. In this case this property can be set to true to force the
     * framework to keep the component in session. Defaults to false
     * </p>
     *
     * @return boolean true if the component should be stored in session, false if not
     */
    public boolean isPersistInSession();

    /**
     * Setter for the indicator to force persistence of the component in session
     *
     * @param persistInSession
     */
    public void setPersistInSession(boolean persistInSession);

    /**
     * Security object that indicates what authorization (permissions) exist for the component
     *
     * @return ComponentSecurity instance
     */
    public ComponentSecurity getComponentSecurity();

    /**
     * Setter for the components security object
     *
     * @param componentSecurity
     */
    public void setComponentSecurity(ComponentSecurity componentSecurity);

    /**
     * @return the progressiveRender
     */
    public String getProgressiveRender();

    /**
     * @param progressiveRender the progressiveRender to set
     */
    public void setProgressiveRender(String progressiveRender);

    /**
     * @return the conditionalRefresh
     */
    public String getConditionalRefresh();

    /**
     * @param conditionalRefresh the conditionalRefresh to set
     */
    public void setConditionalRefresh(String conditionalRefresh);

    /**
     * @return the progressiveDisclosureControlNames
     */
    public List<String> getProgressiveDisclosureControlNames();

    /**
     * @return the progressiveDisclosureConditionJs
     */
    public String getProgressiveDisclosureConditionJs();

    /**
     * @return the conditionalRefreshConditionJs
     */
    public String getConditionalRefreshConditionJs();

    /**
     * @return the conditionalRefreshControlNames
     */
    public List<String> getConditionalRefreshControlNames();

    /**
     * @return the progressiveRenderViaAJAX
     */
    public boolean isProgressiveRenderViaAJAX();

    /**
     * @param progressiveRenderViaAJAX the progressiveRenderViaAJAX to set
     */
    public void setProgressiveRenderViaAJAX(boolean progressiveRenderViaAJAX);

    /**
     * If true, when the progressiveRender condition is satisfied, the component
     * will always be retrieved from the server and shown(as opposed to being
     * stored on the client, but hidden, after the first retrieval as is the
     * case with the progressiveRenderViaAJAX option). <b>By default, this is
     * false, so components with progressive render capabilities will always be
     * already within the client html and toggled to be hidden or visible.</b>
     *
     * @return the progressiveRenderAndRefresh
     */
    public boolean isProgressiveRenderAndRefresh();

    /**
     * @param progressiveRenderAndRefresh the progressiveRenderAndRefresh to set
     */
    public void setProgressiveRenderAndRefresh(boolean progressiveRenderAndRefresh);

    /**
     * Specifies a property by name that when it value changes will
     * automatically perform a refresh on this component. This can be a comma
     * separated list of multiple properties that require this component to be
     * refreshed when any of them change. <Br>DO NOT use with progressiveRender
     * unless it is know that progressiveRender condition will always be
     * satisfied before one of these fields can be changed.
     *
     * @return the refreshWhenChanged
     */
    public String getRefreshWhenChanged();

    /**
     * @param refreshWhenChanged the refreshWhenChanged to set
     */
    public void setRefreshWhenChanged(String refreshWhenChanged);

    /**
     * Indicates the component can be refreshed by an action
     *
     * <p>
     * This is set by the framework for configured ajax action buttons, should not be set in
     * configuration
     * </p>
     *
     * @return boolean true if the component is refreshed by an action, false if not
     */
    public boolean isRefreshedByAction();

    /**
     * Setter for the refresjed by action indicator
     *
     * <p>
     * This is set by the framework for configured ajax action buttons, should not be set in
     * configuration
     * </p>
     *
     * @param refreshedByAction
     */
    public void setRefreshedByAction(boolean refreshedByAction);

    /**
     * Indicates whether data contained within the component should be reset (set to default) when the
     * component is refreshed
     *
     * @return boolean true if data should be refreshed, false if data should remain as is
     */
    public boolean isResetDataOnRefresh();

    /**
     * Setter for the reset data on refresh indicator
     *
     * @param resetDataOnRefresh
     */
    public void setResetDataOnRefresh(boolean resetDataOnRefresh);

    /**
     * Result of the conditionalRefresh expression, true if satisfied, otherwise false.
     * Note: not currently used for any processing, required by the expression evaluator.
     *
     * @return the refresh
     */
    public boolean isRefresh();

    /**
     * @param refresh the refresh to set
     */
    public void setRefresh(boolean refresh);

    /**
     * Control names which will refresh this component when they are changed, added
     * internally
     *
     * @return the refreshWhenChangedControlNames
     */
    public List<String> getRefreshWhenChangedControlNames();

    /**
     * Add a data attribute to the dataAttributes map
     * @param key
     * @param value
     */
    public void addDataAttribute(String key, String value);

    public Map<String, String> getDataAttributes();

    /**
     * DataAttributes that will be written to the html and/or through script to be consumed by jQuery.
     * The attributes that are complex objects (contain {}) they will be written through script.
     * The attritubes that are simple (contain no objects) will be written directly to the html of the
     * component using standard data-.
     * Either way they can be access through .data() call in jQuery
     * @param dataAttributes
     */
    public void setDataAttributes(Map<String, String> dataAttributes);

    /**
     * Returns js that will add data to this component by the element which matches its id.
     * This will return script for only the complex data elements (containing {});
     * @return jQuery data script for adding complex data attributes
     */
    public String getComplexDataAttributesJs();

    /**
     * Returns a string that can be put into a the tag of a component to add data attributes inline.
     * This does not include the complex attributes which contain {}
     * @return html string for data attributes for the simple attributes
     */
    public String getSimpleDataAttributes();

    /**
     * Returns js that will add data to this component by the element which matches its id.
     * <p>This will return script for all the complex data elements.
     * This method is useful for controls that are implemented as spring form tags</p>
     * @return jQuery data script for adding all data attributes
     */
    public String getAllDataAttributesJs();

    /**
     * Tooltip widget that should decorate the element
     *
     * @return Tooltip
     */
    public Tooltip getToolTip();

    /**
     * Setter for the component tooltip widget instance
     *
     * @param toolTip
     */
    public void setToolTip(Tooltip toolTip);

}
