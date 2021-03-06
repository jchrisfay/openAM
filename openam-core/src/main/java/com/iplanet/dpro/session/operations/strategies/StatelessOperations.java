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
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2016 ForgeRock AS.
 */

package com.iplanet.dpro.session.operations.strategies;

import static org.forgerock.openam.audit.AuditConstants.EventName.AM_SESSION_DESTROYED;
import static org.forgerock.openam.audit.AuditConstants.EventName.AM_SESSION_LOGGED_OUT;
import static org.forgerock.openam.utils.Time.currentTimeMillis;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.forgerock.openam.blacklist.Blacklist;
import org.forgerock.openam.blacklist.BlacklistException;
import org.forgerock.openam.session.authorisation.SessionChangeAuthorizer;
import org.forgerock.openam.sso.providers.stateless.StatelessSession;
import org.forgerock.openam.sso.providers.stateless.StatelessSessionManager;

import com.iplanet.dpro.session.*;
import com.iplanet.dpro.session.operations.SessionOperations;
import com.iplanet.dpro.session.service.InternalSession;
import com.iplanet.dpro.session.service.SessionAuditor;
import com.iplanet.dpro.session.service.SessionLogging;
import com.iplanet.dpro.session.share.SessionInfo;
import com.iplanet.sso.SSOToken;

/**
 * Handles client-side sessions.
 *
 * @since 13.0.0
 */
public class StatelessOperations implements SessionOperations {

    private final SessionOperations localOperations;
    private final StatelessSessionManager statelessSessionManager;
    private final Blacklist<Session> sessionBlacklist;
    private final SessionLogging sessionLogging;
    private final SessionAuditor sessionAuditor;
    private final SessionChangeAuthorizer sessionChangeAuthorizer;

    @Inject
    public StatelessOperations(final LocalOperations localOperations,
                               final StatelessSessionManager statelessSessionManager,
                               final Blacklist<Session> sessionBlacklist,
                               final SessionLogging sessionLogging,
                               final SessionAuditor sessionAuditor,
                               final SessionChangeAuthorizer sessionChangeAuthorizer) {
        this.localOperations = localOperations;
        this.statelessSessionManager = statelessSessionManager;
        this.sessionBlacklist = sessionBlacklist;
        this.sessionLogging = sessionLogging;
        this.sessionAuditor = sessionAuditor;
        this.sessionChangeAuthorizer = sessionChangeAuthorizer;
    }

    @Override
    public SessionInfo refresh(final Session session, final boolean reset) throws SessionException {
        final SessionInfo sessionInfo = statelessSessionManager.getSessionInfo(session.getID());
        if (sessionInfo.getExpiryTime(TimeUnit.MILLISECONDS) < currentTimeMillis()) {
            throw new SessionTimedOutException("Stateless session corresponding to client "
                    + sessionInfo.getClientID() + " timed out.");
        }
        return sessionInfo;
    }

    @Override
    public void logout(final Session session) throws SessionException {
        if (session instanceof StatelessSession) {
            SessionInfo sessionInfo = statelessSessionManager.getSessionInfo(session.getID());
            sessionLogging.logEvent(sessionInfo, SessionEvent.LOGOUT);
            // Required since not possible to mock SessionAuditor in test case
            if (sessionAuditor != null) {
                sessionAuditor.auditActivity(sessionInfo, AM_SESSION_LOGGED_OUT);
            }
        }
        try {
            sessionBlacklist.blacklist(session);
        } catch (BlacklistException e) {
            throw new SessionException(e);
        }
    }

    @Override
    public Session resolveSession(SessionID sessionID) throws SessionException {
        return statelessSessionManager.generate(sessionID);
    }

    @Override
    public void destroy(final Session requester, final Session session) throws SessionException {
        sessionChangeAuthorizer.checkPermissionToDestroySession(requester, session.getSessionID());

        if (session instanceof StatelessSession) {
            SessionInfo sessionInfo = statelessSessionManager.getSessionInfo(session.getID());
            sessionLogging.logEvent(sessionInfo, SessionEvent.DESTROY);
            // Required since not possible to mock SessionAuditor in test case
            if (sessionAuditor != null) {
                sessionAuditor.auditActivity(sessionInfo, AM_SESSION_DESTROYED);
            }
        }

        try {
            sessionBlacklist.blacklist(session);
        } catch (BlacklistException e) {
            throw new SessionException(e);
        }
    }

    @Override
    public void setProperty(final Session session, final String name, final String value) throws SessionException {
        localOperations.setProperty(session, name, value);
    }

    @Override
    public SessionInfo getSessionInfo(SessionID sid, boolean reset) throws SessionException {
        return statelessSessionManager.getSessionInfo(sid);
    }

    @Override
    public void addSessionListener(SessionID sessionId, String url) throws SessionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean checkSessionLocal(SessionID sessionId) throws SessionException {
        return false;
    }

    @Override
    public String getRestrictedTokenId(SessionID masterSid, TokenRestriction restriction) throws SessionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String deferenceRestrictedID(Session session, SessionID restrictedID) throws SessionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExternalProperty(SSOToken clientToken, SessionID sessionId, String name, String value) throws SessionException {
        localOperations.setExternalProperty(clientToken, sessionId, name, value);
    }

    @Override
    public void update(InternalSession session) {
        throw new UnsupportedOperationException();
    }

}
