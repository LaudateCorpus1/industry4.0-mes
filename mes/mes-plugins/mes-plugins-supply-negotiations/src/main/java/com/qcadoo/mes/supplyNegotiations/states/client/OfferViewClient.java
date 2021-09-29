/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.supplyNegotiations.states.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;
import com.qcadoo.mes.supplyNegotiations.states.aop.OfferStateChangeAspect;

@Service
public final class OfferViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private OfferStateChangeAspect offerStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return offerStateChangeAspect;
    }

}
