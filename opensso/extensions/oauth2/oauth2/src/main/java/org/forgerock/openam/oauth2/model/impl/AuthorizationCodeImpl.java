/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2012 ForgeRock. All rights reserved.
 */

package org.forgerock.openam.oauth2.model.impl;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.restlet.ext.oauth2.model.AuthorizationCode;
import org.forgerock.restlet.ext.oauth2.model.SessionClient;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jonathan
 * Date: 26/3/12
 * Time: 10:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class AuthorizationCodeImpl extends TokenImpl implements AuthorizationCode {

    private boolean issued = false;

    // TODO javadoc
    public AuthorizationCodeImpl(String id, String userID, SessionClient client, String realm, Set<String> scope, boolean issued, long expireTime) {
        super(id, userID, client, realm, scope, expireTime);
        this.issued = issued;
    }

    // TODO javadoc
    public AuthorizationCodeImpl(JsonValue value) {
        super(value);
        this.issued = value.get("issued").asBoolean();
    }

    // TODO javadoc
    public JsonValue asJson() {
        JsonValue value = super.asJson();
        value.put("type", "authorization_code");
        value.put("issued", issued);
        return value;
    }
    
    public void setIssued(boolean issued) {
        this.issued = issued;
    }

    @Override
    public boolean isTokenIssued() {
        return issued;
    }

}
