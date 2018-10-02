package net.sciencestudio.autodialog.view.android.support;

import android.util.ArrayMap;
import android.util.AttributeSet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AutoAttributeSet implements AttributeSet {

    private ArrayMap<String, String> attributes;

    public AutoAttributeSet() {
        this.attributes = new ArrayMap<>();
    }

    public void put(String namespace, String attribute, String value) {
        attributes.put(qualified(namespace, attribute), value);
    }

    private String qualified(String namespace, String attribute) {
        return namespace + ":" + attribute;
    }

    private boolean hasAttribute(String namespace, String attribute) {
        return attributes.containsKey(qualified(namespace, attribute));
    }

    private <T> T parse(int index, Function<String, T> parser, T defaultValue) {
        return parser.apply(attributes.keyAt(index));

    }

    private <T> T parse(String namespace, String attribute, Function<String, T> parser, T defaultValue) {
        if (hasAttribute(namespace, attribute)) {
            return parser.apply(getAttributeValue(namespace, attribute));
        } else {
            return defaultValue;
        }
    }


    /**
     * Returns the number of attributes available in the set.
     * <p>
     * which this method corresponds to when parsing a compiled XML file.</p>
     *
     * @return A positive integer, or 0 if the set is empty.
     */
    @Override
    public int getAttributeCount() {
        return attributes.size();
    }

    /**
     * Returns the name of the specified attribute.
     * <p>
     * which this method corresponds to when parsing a compiled XML file.</p>
     *
     * @param index Index of the desired attribute, 0...count-1.
     * @return A String containing the name of the attribute, or null if the
     * attribute cannot be found.
     */
    @Override
    public String getAttributeName(int index) {
        String key = attributes.keyAt(index);
        if (key.contains(":")) {
            key = key.split(":", 2)[1];
        }
        return attributes.keyAt(index);
    }

    /**
     * Returns the value of the specified attribute as a string representation.
     *
     * @param index Index of the desired attribute, 0...count-1.
     * @return A String containing the value of the attribute, or null if the
     * attribute cannot be found.
     */
    @Override
    public String getAttributeValue(int index) {
        return attributes.valueAt(index);
    }

    /**
     * Returns the value of the specified attribute as a string representation.
     * The lookup is performed using the attribute name.
     *
     * @param namespace The namespace of the attribute to get the value from.
     * @param name      The name of the attribute to get the value from.
     * @return A String containing the value of the attribute, or null if the
     * attribute cannot be found.
     */
    @Override
    public String getAttributeValue(String namespace, String name) {
        String key = namespace + ":" + name;
        return attributes.get(key);
    }

    /**
     * Returns a description of the current position of the attribute set.
     * For instance, if the attribute set is loaded from an XML document,
     * the position description could indicate the current line number.
     *
     * @return A string representation of the current position in the set,
     * may be null.
     */
    @Override
    public String getPositionDescription() {
        //TODO: What do?
        return "";
    }

    /**
     * Return the resource ID associated with the given attribute name.  This
     * will be the identifier for an attribute resource, which can be used by
     * styles.  Returns 0 if there is no resource associated with this
     * attribute.
     * <p>
     * <p>Note that this is different than {@link #getAttributeResourceValue}
     * in that it returns a resource identifier for the attribute name; the
     * other method returns this attribute's value as a resource identifier.
     *
     * @param index Index of the desired attribute, 0...count-1.
     * @return The resource identifier, 0 if none.
     */
    @Override
    public int getAttributeNameResource(int index) {
        //TODO: What do?
        return 0;
    }

    /**
     * Return the index of the value of 'attribute' in the list 'options'.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    Name of attribute to retrieve.
     * @param options      List of strings whose values we are checking against.
     * @param defaultValue Value returned if attribute doesn't exist or no
     *                     match is found.
     * @return Index in to 'options' or defaultValue.
     */
    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        String key = namespace + ":" + attribute;
        List<String> optList = Arrays.asList(options);

        if (!attributes.containsKey(key)) {
            return defaultValue;
        }

        if (optList.contains(key)) {
            return optList.indexOf(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Return the boolean value of 'attribute'.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    The attribute to retrieve.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        return parse(namespace, attribute, Boolean::parseBoolean, defaultValue);
    }

    /**
     * Return the value of 'attribute' as a resource identifier.
     * <p>
     * <p>Note that this is different than {@link #getAttributeNameResource}
     * in that it returns the value contained in this attribute as a
     * resource identifier (i.e., a value originally of the form
     * "@package:type/resource"); the other method returns a resource
     * identifier that identifies the name of the attribute.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    The attribute to retrieve.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        //TODO: What do?
        return defaultValue;
    }

    /**
     * Return the integer value of 'attribute'.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    The attribute to retrieve.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return parse(namespace, attribute, Integer::parseInt, defaultValue);
    }

    /**
     * Return the boolean value of 'attribute' that is formatted as an
     * unsigned value.  In particular, the formats 0xn...n and #n...n are
     * handled.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    The attribute to retrieve.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return parse(namespace, attribute, Integer::parseUnsignedInt, defaultValue);
    }

    /**
     * Return the float value of 'attribute'.
     *
     * @param namespace    Namespace of attribute to retrieve.
     * @param attribute    The attribute to retrieve.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        return parse(namespace, attribute, Float::parseFloat, defaultValue);
    }

    /**
     * Return the index of the value of attribute at 'index' in the list
     * 'options'.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param options      List of strings whose values we are checking against.
     * @param defaultValue Value returned if attribute doesn't exist or no
     *                     match is found.
     * @return Index in to 'options' or defaultValue.
     */
    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        String key = attributes.keyAt(index);
        List<String> optList = Arrays.asList(options);
        if (optList.contains(key)) {
            return optList.indexOf(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Return the boolean value of attribute at 'index'.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return parse(index, Boolean::parseBoolean, defaultValue);
    }

    /**
     * Return the value of attribute at 'index' as a resource identifier.
     * <p>
     * <p>Note that this is different than {@link #getAttributeNameResource}
     * in that it returns the value contained in this attribute as a
     * resource identifier (i.e., a value originally of the form
     * "@package:type/resource"); the other method returns a resource
     * identifier that identifies the name of the attribute.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        //TODO: What do?
        return defaultValue;
    }

    /**
     * Return the integer value of attribute at 'index'.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        return parse(index, Integer::parseInt, defaultValue);
    }

    /**
     * Return the integer value of attribute at 'index' that is formatted as an
     * unsigned value.  In particular, the formats 0xn...n and #n...n are
     * handled.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return parse(index, Integer::parseUnsignedInt, defaultValue);
    }

    /**
     * Return the float value of attribute at 'index'.
     *
     * @param index        Index of the desired attribute, 0...count-1.
     * @param defaultValue What to return if the attribute isn't found.
     * @return Resulting value.
     */
    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        return parse(index, Float::parseFloat, defaultValue);
    }

    /**
     * Return the value of the "id" attribute or null if there is not one.
     * Equivalent to getAttributeValue(null, "id").
     *
     * @return The id attribute's value or null.
     */
    @Override
    public String getIdAttribute() {
        return null;
    }

    /**
     * Return the value of the "class" attribute or null if there is not one.
     * Equivalent to getAttributeValue(null, "class").
     *
     * @return The class attribute's value or null.
     */
    @Override
    public String getClassAttribute() {
        return null;
    }

    /**
     * Return the integer value of the "id" attribute or defaultValue if there
     * is none.
     * Equivalent to getAttributeResourceValue(null, "id", defaultValue);
     *
     * @param defaultValue What to return if the "id" attribute isn't found.
     * @return int Resulting value.
     */
    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        return defaultValue;
    }

    /**
     * Return the value of the "style" attribute or 0 if there is not one.
     * Equivalent to getAttributeResourceValue(null, "style").
     *
     * @return The style attribute's resource identifier or 0.
     */
    @Override
    public int getStyleAttribute() {
        //TODO: What do?
        return 0;
    }
}
