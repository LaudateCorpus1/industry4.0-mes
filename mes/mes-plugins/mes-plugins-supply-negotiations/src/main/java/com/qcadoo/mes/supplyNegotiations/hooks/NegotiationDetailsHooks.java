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
package com.qcadoo.mes.supplyNegotiations.hooks;

import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationFields.*;
import static com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields.*;
import static com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateStringValues.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.NegotiationProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.mes.supplyNegotiations.states.constants.NegotiationStateChangeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class NegotiationDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_REQUEST_FOR_QUOTATIONS = "requestForQuotations";

    private static final String L_OFFERS = "offers";

    private static final String L_SHOW_REQUEST_FOR_QUOTATIONS_FOR_GIVEN_NEGOTIATION = "showRequestForQuotationsForGivenNegotiation";

    private static final String L_SHOW_OFFERS_FOR_GIVEN_NEGOTIATION = "showOffersForGivenNegotiation";

    private static final String L_SHOW_OFFERS_ITEMS_FOR_GIVEN_NEGOTIATION = "showOffersItemsForGivenNegotiation";

    private static final String L_ORDERED_QUANTITY = "orderedQuantity";

    private static final String L_NEGOTIATION_PRODUCT_ID = "negotiationProductId";

    private static final String L_STATUS = "status";

    private static final String L_GENERATE = "generate";

    private static final String L_CANCEL = "cancel";

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generateNegotiationNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, SupplyNegotiationsConstants.PLUGIN_IDENTIFIER,
                SupplyNegotiationsConstants.MODEL_NEGOTIATION, L_FORM, NUMBER);
    }

    public void changeFieldsEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        if (negotiationForm.getEntityId() == null) {
            changeFieldsEnabled(view, true, false);
        } else {
            if (COMPLETED.equals(state) || DECLINED.equals(state) || GENERATED_REQUESTS.equals(state)) {
                changeFieldsEnabled(view, false, false);
            } else {
                changeFieldsEnabled(view, true, true);
            }
        }
    }

    private void changeFieldsEnabled(final ViewDefinitionState view, final boolean enabledForm, final boolean enabledGrid) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(L_FORM);

        GridComponent negotiationProducts = (GridComponent) view.getComponentByReference(NEGOTIATION_PRODUCTS);

        negotiationForm.setFormEnabled(enabledForm);
        negotiationProducts.setEnabled(enabledGrid);
        negotiationProducts.setEditable(enabledGrid);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup requestForQuotations = (RibbonGroup) window.getRibbon().getGroupByName(L_REQUEST_FOR_QUOTATIONS);
        RibbonGroup offers = (RibbonGroup) window.getRibbon().getGroupByName(L_OFFERS);

        RibbonGroup status = window.getRibbon().getGroupByName(L_STATUS);

        RibbonActionItem generate = status.getItemByName(L_GENERATE);
        RibbonActionItem cancel = status.getItemByName(L_CANCEL);

        RibbonActionItem showRequestForQuotationsForGivenNegotiation = (RibbonActionItem) requestForQuotations
                .getItemByName(L_SHOW_REQUEST_FOR_QUOTATIONS_FOR_GIVEN_NEGOTIATION);

        RibbonActionItem showOffersForGivenNegotiation = (RibbonActionItem) offers
                .getItemByName(L_SHOW_OFFERS_FOR_GIVEN_NEGOTIATION);
        RibbonActionItem showOffersItemsForGivenNegotiation = (RibbonActionItem) offers
                .getItemByName(L_SHOW_OFFERS_ITEMS_FOR_GIVEN_NEGOTIATION);

        boolean isEnabled = ((negotiationForm.getEntityId() != null) && hasNegotiationProducts(negotiationForm.getEntity()));

        updateButtonState(generate, isEnabled);
        updateButtonState(cancel, isEnabled);
        updateButtonState(showRequestForQuotationsForGivenNegotiation, isEnabled);
        updateButtonState(showOffersForGivenNegotiation, isEnabled);
        updateButtonState(showOffersItemsForGivenNegotiation, isEnabled);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private boolean hasNegotiationProducts(final Entity negotiation) {
        return dataDefinitionService
                .get(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER, SupplyNegotiationsConstants.MODEL_NEGOTIATION_PRODUCT).find()
                .add(SearchRestrictions.belongsTo(NegotiationProductFields.NEGOTIATION, negotiation)).setMaxResults(1)
                .uniqueResult() != null;
    }

    public void changeApprovedNotApprovedLeftQuantity(final ViewDefinitionState view) {
        FormComponent negotiationForm = (FormComponent) view.getComponentByReference("form");

        Long negotiationId = negotiationForm.getEntityId();

        if (negotiationId != null) {
            String queryNotApprovedDeliveries = getNotApprovedDeliveriesQuery(negotiationId);
            String queryApprovedDeliveries = getApprovedDeliveriesQuery(negotiationId);

            changeApprovedNotApprovedLeftQuantity(negotiationId, queryNotApprovedDeliveries, queryApprovedDeliveries);
        }
    }

    public void changeApprovedNotApprovedLeftQuantity(final Long negotiationId, final String queryNotApprovedDeliveries,
            final String queryApprovedDeliveries) {
        if (negotiationId == null) {
            return;
        }

        Entity negotiation = supplyNegotiationsService.getNegotiation(negotiationId);

        DataDefinition orderedProductDD = deliveriesService.getOrderedProductDD();

        List<Entity> notApprovedDeliveredProducts = orderedProductDD.find(queryNotApprovedDeliveries).list().getEntities();
        List<Entity> approvedDeliveredProducts = orderedProductDD.find(queryApprovedDeliveries).list().getEntities();

        List<Entity> negotiationProducts = negotiation.getHasManyField(NEGOTIATION_PRODUCTS);

        Map<Long, Entity> negotiationProductsMap = Maps.newHashMap();

        for (Entity negotiationProduct : negotiationProducts) {
            negotiationProduct.setField(DRAFT_DELIVERED_QUANTITY, BigDecimal.ZERO);
            negotiationProduct.setField(APPROVED_DELIVERED_QUANTITY, BigDecimal.ZERO);
            negotiationProduct.setField(LEFT_QUANTITY, BigDecimal.ZERO);

            negotiationProductsMap.put(negotiationProduct.getId(), negotiationProduct);
        }

        for (Entity notApprovedDeliveredProduct : notApprovedDeliveredProducts) {
            Entity negotiationProduct = negotiationProductsMap
                    .get(notApprovedDeliveredProduct.getField(L_NEGOTIATION_PRODUCT_ID));

            if (negotiationProduct != null) {
                negotiationProduct.setField(DRAFT_DELIVERED_QUANTITY, notApprovedDeliveredProduct.getField(L_ORDERED_QUANTITY));
            }
        }

        for (Entity approvedDeliveredProduct : approvedDeliveredProducts) {
            Entity negotiationProduct = negotiationProductsMap.get(approvedDeliveredProduct.getField(L_NEGOTIATION_PRODUCT_ID));

            if (negotiationProduct != null) {
                negotiationProduct.setField(APPROVED_DELIVERED_QUANTITY, approvedDeliveredProduct.getField(L_ORDERED_QUANTITY));
            }
        }

        for (Entry<Long, Entity> negotiationProductEntry : negotiationProductsMap.entrySet()) {
            Entity negotiationProduct = negotiationProductEntry.getValue();

            BigDecimal neededQuantity = convertNullToZero(negotiationProduct.getDecimalField(NEEDED_QUANTITY));
            BigDecimal draftDeliveredQuantity = convertNullToZero(negotiationProduct.getDecimalField(DRAFT_DELIVERED_QUANTITY));
            BigDecimal approvedDeliveredQuantity = convertNullToZero(negotiationProduct
                    .getDecimalField(APPROVED_DELIVERED_QUANTITY));

            BigDecimal leftQuantity = neededQuantity.subtract(
                    draftDeliveredQuantity.add(approvedDeliveredQuantity, numberService.getMathContext()),
                    numberService.getMathContext());

            negotiationProduct.setField(LEFT_QUANTITY, leftQuantity);

            negotiationProduct.getDataDefinition().save(negotiationProduct);
        }
    }

    private String getNotApprovedDeliveriesQuery(final Long negotiationId) {
        String queryNotApprovedDeliveries = String.format("SELECT np.id AS " + L_NEGOTIATION_PRODUCT_ID
                + ", SUM(op.orderedQuantity) AS " + L_ORDERED_QUANTITY + " FROM #deliveries_orderedProduct op "
                + "JOIN op.offer.negotiation.negotiationProducts np WHERE op.offer.negotiation.id = %s "
                + "AND op.delivery.state IN ('" + DeliveryStateStringValues.DRAFT + "', '" + DeliveryStateStringValues.PREPARED
                + "', '" + DeliveryStateStringValues.DURING_CORRECTION + "') AND op.product.id = np.product.id GROUP BY np.id",
                negotiationId);

        return queryNotApprovedDeliveries;
    }

    private String getApprovedDeliveriesQuery(final Long negotiationId) {
        String queryApprovedDeliveries = String.format("SELECT np.id AS " + L_NEGOTIATION_PRODUCT_ID
                + ", SUM(op.orderedQuantity) AS " + L_ORDERED_QUANTITY + " FROM #deliveries_orderedProduct op "
                + "JOIN op.offer.negotiation.negotiationProducts np WHERE op.offer.negotiation.id = %s "
                + "AND op.delivery.state IN ('" + DeliveryStateStringValues.APPROVED + "', '"
                + DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING + "') AND op.product.id = np.product.id " + "GROUP BY np.id",
                negotiationId);

        return queryApprovedDeliveries;
    }

    private BigDecimal convertNullToZero(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("loggingsGrid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(
                NegotiationStateChangeFields.STATUS, Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }

}
