/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 ForgeRock AS.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

package com.sun.identity.sm;

import com.sun.identity.security.EncodeAction;
import com.sun.identity.shared.xml.XMLUtils;

import java.security.AccessController;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.forgerock.openam.upgrade.UpgradeException;
import org.forgerock.openam.upgrade.UpgradeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author steve
 */
public abstract class AbstractUpgradeHelper implements UpgradeHelper {
    private static final String DEFAULT_VALUES_BEGIN = "<"
            + SMSUtils.ATTRIBUTE_DEFAULT_ELEMENT + ">";    
    private static final String DEFAULT_VALUES_END = "</"
            + SMSUtils.ATTRIBUTE_DEFAULT_ELEMENT + ">";
    private static final String VALUE_BEGIN = "<" + SMSUtils.ATTRIBUTE_VALUE + ">";
    private static final String VALUE_END = "</" + SMSUtils.ATTRIBUTE_VALUE + ">";
    protected final Set<String> attributes = new HashSet<String>();
    
    protected AttributeSchemaImpl updateDefaultValues(AttributeSchemaImpl attribute, Set<String> defaultValues) 
    throws UpgradeException {
        StringBuilder sb = new StringBuilder(100);
        
        sb.append(DEFAULT_VALUES_BEGIN);
        
        for (String defaultValue : defaultValues) {
            sb.append(VALUE_BEGIN);
            sb.append(SMSSchema.escapeSpecialCharacters(defaultValue));
            sb.append(VALUE_END);
        }
        
        sb.append(DEFAULT_VALUES_END);
        Document doc = XMLUtils.toDOMDocument(sb.toString(), null);

        Node attributeNode = updateNode(doc, SMSUtils.ATTRIBUTE_DEFAULT_ELEMENT, attribute.getAttributeSchemaNode());        
        attribute.update(attributeNode);
        
        return attribute;
    }

    /**
     * Encrypts all values in the provided set.
     *
     * <p>To be used when copying default values which need to be stored encrypted.</p>
     *
     * @param values The values to encrypt.
     * @return A Set containing the encrypted values.
     */
    protected Set<String> encryptValues(Set<String> values) {
        if (values.isEmpty()) {
            return values;
        }
        Set<String> encryptedValues = new HashSet<>();
        for (String value : values) {
            encryptedValues.add(AccessController.doPrivileged(new EncodeAction(value)));
        }
        return encryptedValues;
    }

    protected AttributeSchemaImpl updateChoiceValues(AttributeSchemaImpl attribute, Collection<String> choiceValues)
            throws UpgradeException {
        try {
            final Document choiceValuesDoc = XMLUtils.newDocument();
            final Element choiceValuesElement = choiceValuesDoc.createElement(SMSUtils.ATTRIBUTE_CHOICE_VALUES_ELEMENT);
            choiceValuesDoc.appendChild(choiceValuesElement);
            for (String choiceValue : choiceValues) {
                final Element choiceValueElement = choiceValuesDoc.createElement(SMSUtils
                        .ATTRIBUTE_CHOICE_VALUE_ELEMENT);
                choiceValueElement.appendChild(choiceValuesDoc.createTextNode(choiceValue));
                choiceValuesElement.appendChild(choiceValueElement);
            }

            final Node attributeNode = updateNode(choiceValuesDoc, SMSUtils.ATTRIBUTE_CHOICE_VALUES_ELEMENT,
                    attribute.getAttributeSchemaNode());
            attribute.update(attributeNode);
        } catch (ParserConfigurationException e) {
            throw new UpgradeException(e);
        }
        return attribute;
    }

    protected static Node updateNode(Document newDefaultValueNode, String element, Node node) {
        // NB. This method will only replace an existing child element,
        // it does not create one if one didn't exist before

        NodeList childNodes = node.getChildNodes();
        
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
           
            if (item.getNodeName().equals(element)) {
                node.removeChild(item);
                Node newNode = node.getOwnerDocument().importNode(newDefaultValueNode.getFirstChild(), true);
                node.appendChild(newNode);
            }
        }
       
        return node;
    }

    public AttributeSchemaImpl addNewAttribute(Set<AttributeSchemaImpl> existingAttrs, AttributeSchemaImpl newAttr)
            throws UpgradeException {
        return newAttr;
    }

    /**
     * Implement this method to perform modifications to an existing attribute based on custom logic. In order to
     * create a hook for a certain attribute, during upgradehelper initialization the attribute name should be
     * captured in {@link AbstractUpgradeHelper#attributes}.
     *
     * @param oldAttr The attribute schema definition currently specified.
     * @param newAttr The attribute schema definition we are planning to upgrade to.
     * @return If there is nothing to upgrade (i.e. there is no real difference between old and new attribute),
     * implementations MUST return <code>null</code>, otherwise either the amended attribute or the newAttr can be
     * returned directly.
     * @throws UpgradeException If there was an error while performing the attribute upgrade.
     */
    public abstract AttributeSchemaImpl upgradeAttribute(AttributeSchemaImpl oldAttr, AttributeSchemaImpl newAttr)
    throws UpgradeException;

    /**
     * Implement this method to perform modifications to a newly added attribute. In order to create a hook for
     * a certain attribute, during upgradehelper initialization the attribute name should be captured in
     * {@link AbstractUpgradeHelper#attributes}.
     *
     * @param newAttr The attribute schema definition we are planning to upgrade to.
     * @return If there is nothing to upgrade, implementations MUST return <code>null</code>,
     * otherwise the amended attribute can be returned directly.
     * @throws UpgradeException If there was an error while performing the attribute upgrade.
     */
    public AttributeSchemaImpl upgradeAttribute(AttributeSchemaImpl newAttr) throws UpgradeException {
        return null;
    }

    @Override
    public final Set<String> getAttributes() {
        return attributes;
    }
}
