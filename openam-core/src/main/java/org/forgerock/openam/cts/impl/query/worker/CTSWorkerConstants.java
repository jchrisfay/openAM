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
* Copyright 2016 ForgeRock AS.
*/
package org.forgerock.openam.cts.impl.query.worker;

import org.forgerock.openam.cts.worker.CTSWorkerProcess;

/**
 * Holds constants for the {@link CTSWorkerProcess} concepts.
 */
public final class CTSWorkerConstants {

    /**
     * Passed expiry date query implementation handle.
     */
    public static final String PASSED_EXPIRED_DATE = "PassedExpiredDate";

    /**
     * Max session time expired query implementation handle.
     */
    public static final String MAX_SESSION_TIME_EXPIRED = "MaxSessionTimeExpired";

    /**
     * Idle time expired query implementation handle.
     */
    public static final String IDLE_TIME_EXPIRED = "IdleTimeExpired";

    /**
     * Purge delay expired query implementation handle.
     */
    public static final String PURGE_DELAY_EXPIRED = "PurgeDelayExpired";

    /**
     * Task name reference for marking sessions once they have expired.
     */
    public static final String MARK_SESSION_TIME_EXPIRED = "MarkSessionTimeExpired";

    /**
     * Task name reference for deleting sessions that have expired.
     */
    public static final String DELETE_ALL_MAX_EXPIRED = "DeleteAllMaxExpired";

    /**
     * Task name reference for marking sessions whose idle time has expired.
     */
    public static final String MARK_IDLE_TIME_EXPIRED = "MarkIdleTimeExpired";

    /**
     * Task name reference for deleting sessions whose purge delay has expired.
     */
    public static final String DELETE_PURGE_DELAY_EXPIRED = "DeletePurgeDelayExpired";

    /**
     * Uninstantiable.
     */
    private CTSWorkerConstants() {
        //this section intentionally left blank
    }

}
