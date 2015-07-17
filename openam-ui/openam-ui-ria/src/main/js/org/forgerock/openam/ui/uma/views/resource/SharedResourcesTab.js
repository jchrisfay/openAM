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
 * Copyright 2015 ForgeRock AS.
 */

/*global define*/

define("org/forgerock/openam/ui/uma/views/resource/SharedResourcesTab", [
    "jquery",
    "underscore",
    "org/forgerock/commons/ui/common/main/AbstractView",
    "backbone",
    "backgrid",
    "org/forgerock/openam/ui/common/util/BackgridUtils",
    "org/forgerock/openam/ui/uma/views/share/CommonShare",
    "org/forgerock/commons/ui/common/main/Configuration",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/openam/ui/common/util/RealmHelper",
    "org/forgerock/openam/ui/uma/delegates/UMADelegate"
], function($, _, AbstractView, Backbone, Backgrid, BackgridUtils, CommonShare, Configuration, Constants, RealmHelper, UMADelegate) {

    var SharedResourcesTab = AbstractView.extend({
        template: "templates/uma/views/resource/ListResourceTab.html",
        baseTemplate: "templates/common/DefaultBaseTemplate.html",
        element: "#sharedResources",

        render: function(args, callback) {
            var self = this;

            UMADelegate.getUmaConfig().done(function(){

                // TODO: change endpoint
                var columns,
                    grid,
                    paginator,
                    ResourceSetCollection = Backbone.PageableCollection.extend({
                        url: RealmHelper.decorateURIWithRealm("/" + Constants.context + "/json/__subrealm__/users/" + Configuration.loggedUser.username + "/oauth2/resourcesets"),
                        queryParams:  BackgridUtils.getQueryParams({
                            _sortKeys: BackgridUtils.sortKeys,
                            _queryFilter: ['! resourceOwnerId eq "' + Configuration.loggedUser.username + '"'],
                            _pagedResultsOffset: BackgridUtils.pagedResultsOffset
                        }),
                        state: BackgridUtils.getState(),
                        parseState: BackgridUtils.parseState,
                        parseRecords: BackgridUtils.parseRecords,
                        sync: function (method, model, options) {
                           options.beforeSend = function (xhr) {
                               xhr.setRequestHeader("Accept-API-Version", "protocol=1.0,resource=1.0");
                           };
                           return BackgridUtils.sync(method, model, options);
                        }
                    });

                columns = [
                    {
                        name: "name",
                        label: $.t("uma.resources.list.sharedResources.grid.0"),
                        cell: BackgridUtils.UriExtCell,
                        headerCell: BackgridUtils.FilterHeaderCell.extend({
                            addClassName: "col-md-5"
                        }),
                        href: function(rawValue, formattedValue, model){
                            return "#uma/resources/" + model.get('_id');
                        },
                        editable: false
                    },

                    {
                        name: "resourceOwnerId",
                        label: $.t("uma.resources.list.sharedResources.grid.3"),
                        cell: "string",
                        editable: false
                    },
                    {
                        name: "scopes",
                        label: $.t("uma.resources.list.sharedResources.grid.2"),
                        cell: "string",
                        headerCell: BackgridUtils.ClassHeaderCell.extend({
                            className: "col-xs-7 col-md-6"
                        }),
                        editable: false
                    }
                ];

                if (Configuration.globalData.auth.uma && Configuration.globalData.auth.uma.resharingMode &&
                    Configuration.globalData.auth.uma.resharingMode === "IMPLICIT" ){
                    columns.push({
                        name: "share",
                        label: "",
                        cell: Backgrid.Cell.extend({
                            className: "fa fa-share",
                            events: { "click": "share" },
                            share: function(e) {
                                var shareView = new CommonShare();
                                shareView.renderDialog(this.model.get('_id'));
                            },
                            render: function () {
                                this.$el.attr({"title": $.t("uma.share.shareResource")});
                                this.delegateEvents();
                                return this;
                            }
                        }),
                        editable: false,
                        headerCell : BackgridUtils.ClassHeaderCell.extend({
                            className: "col-md-1"
                        })
                    });
                }

                self.data.resourceSetCollection = new ResourceSetCollection();
                self.data.resourceSetCollection.on("backgrid:sort", BackgridUtils.doubleSortFix);

                grid = new Backgrid.Grid({
                    columns: columns,
                    className:"backgrid table table-striped",
                    collection: self.data.resourceSetCollection,
                    emptyText: $.t("console.common.noResults")
                });

                paginator = new Backgrid.Extension.Paginator({
                    collection: self.data.resourceSetCollection,
                    windowSize: 3
                });

                self.parentRender(function() {
                    self.$el.find(".backgrid-container").append(grid.render().el);
                    self.$el.find(".pagination-container").append(paginator.render().el);
                    self.data.resourceSetCollection.fetch({reset: true, processData: false}).done(function () {
                        if (callback) {
                            callback();
                        }
                    });
                });

            });
        }
    });

    return new SharedResourcesTab();
});