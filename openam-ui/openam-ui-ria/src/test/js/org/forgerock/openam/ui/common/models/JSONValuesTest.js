/**
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

define([
    "org/forgerock/openam/ui/common/models/JSONValues"
], (JSONValues) => {
    describe("org/forgerock/openam/ui/common/models/JSONValues", () => {
        describe("#constructor", () => {
            let values;

            beforeEach(() => {
                values = new JSONValues({
                    "_id": "",
                    "_type": {},
                    "globalSimpleKey": "globalSimpleValue",
                    "globalCollectionKey": {
                        "collectionItem_1": "collectionItemValue_1",
                        "collectionItem_2": "collectionItemValue_12"
                    },
                    "defaults": {
                        "defaultsCollection": {
                            "collectionItem1": "value1",
                            "collectionItem2": "value2"
                        },
                        "defaultsSimple": "simpleValue"
                    },
                    "dynamic": {
                        "dynamicSimple": "simpleValue"
                    }
                });
            });

            it("groups the top-level simple values under a \"global\" value", () => {
                expect(values.raw).to.contain.keys("global");
                expect(values.raw.global).to.contain.keys("globalSimpleKey");
            });

            it("does not group the top-level collection values under a \"global\" value", () => {
                expect(values.raw).to.contain.keys("global");
                expect(values.raw.global).to.not.contain.keys("globalCollectionKey");
            });

            it("groups simple values in \"defaults\" into a pseudo collection \"realmDefaults\"", () => {
                expect(values.raw).to.contain.keys("realmDefaults");
                expect(values.raw.realmDefaults).to.contain.keys("defaultsSimple");
            });

            it("flattens values in \"defaults\" onto the top-level values", () => {
                expect(values.raw).to.contain.keys("defaultsCollection");
            });

            it("contains \"_defaultsCollectionProperties\"", () => {
                expect(values.raw).to.contain.keys("_defaultsCollectionProperties");
            });
        });
        describe("#addInheritance", () => {
            const jsonValues = new JSONValues({
                propertyKey: "value"
            });

            let values;

            beforeEach(() => {
                values = jsonValues.addInheritance({
                    propertyKey: {
                        inherited: true
                    }
                });
            });

            it("creates an object for each property key", () => {
                expect(values.raw.propertyKey).to.be.an("object");
            });

            it("creates a \"value\" attribute on the property object", () => {
                expect(values.raw.propertyKey.value).to.exist;
                expect(values.raw.propertyKey.value).eq("value");
            });

            it("creates a \"inherited\" attribute on the property object", () => {
                expect(values.raw.propertyKey.inherited).to.exist;
                expect(values.raw.propertyKey.inherited).to.be.true;
            });
        });
        describe("#removeInheritance", () => {
            it("flattens each inherited property into a single value", () => {
                const jsonValues = new JSONValues({
                    propertyKey: {
                        value: "value",
                        inherited: true
                    }
                });

                const values = jsonValues.removeInheritance();

                expect(values.raw.propertyKey).eq("value");
            });
        });
        describe("#toJSON", () => {
            let values;
            let valueWithDefaultsCollectionProperties;

            beforeEach(() => {
                values = new JSONValues({
                    "_id": {},
                    "_type": {},
                    "globalValue": {},
                    "realmDefaults": {
                        "defaultsSimple": "simpleValue"
                    },
                    "dynamic": {
                        "dynamicSimple": "simpleValue"
                    }
                }).toJSON();

                valueWithDefaultsCollectionProperties = new JSONValues({
                    "_id": {},
                    "_type": {},
                    "_defaultsCollectionProperties": ["defaultsCollection", "defaultsCollection2"],
                    "defaultsCollection": {
                        "collectionItem1": "value1",
                        "collectionItem2": "value2"
                    },
                    "defaultsCollection2": {
                        "collectionItem1": "value1",
                        "collectionItem2": "value2"
                    }
                }).toJSON();
            });

            it("returns an JSON string", () => {
                expect(values).to.be.a("string");
                expect(JSON.parse(values)).to.not.throw;
            });

            it("returns global values at the top-level", () => {
                expect(JSON.parse(values)).to.contain.keys("globalValue");
            });

            it("returns defaults values under a \"defaults\" property", () => {
                expect(JSON.parse(values)).to.contain.keys("defaults");
                expect(JSON.parse(values).defaults).to.contain.keys("defaultsSimple");
            });

            it("returns \"_id\" at the top-level", () => {
                expect(JSON.parse(values)).to.contain.keys("_id");
            });

            it("returns \"_type\" at the top-level", () => {
                expect(JSON.parse(values)).to.contain.keys("_type");
            });

            // valueWithDefaultsCollectionProperties
            it("constructs \"defaults\" property from the defaults collection values", () => {
                expect(JSON.parse(valueWithDefaultsCollectionProperties))
                    .to.contain.keys("defaults");
            });

            it("returns defaults collection values under a \"defaults\" property", () => {
                expect(JSON.parse(valueWithDefaultsCollectionProperties).defaults)
                    .to.contain.keys("defaultsCollection");
                expect(JSON.parse(valueWithDefaultsCollectionProperties).defaults)
                    .to.contain.keys("defaultsCollection2");
            });

            it("deletes meta info key \"_defaultsCollectionProperties\" from the values", () => {
                expect(JSON.parse(valueWithDefaultsCollectionProperties))
                    .to.not.contain.keys("_defaultsCollectionProperties");
            });
        });
    });
});
