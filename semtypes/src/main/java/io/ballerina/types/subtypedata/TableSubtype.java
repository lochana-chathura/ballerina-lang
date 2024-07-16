/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.ListDefinition;

import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_TABLE;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;

/**
 * TableSubtype.
 *
 * @since 2201.8.0
 */
public final class TableSubtype {

    private TableSubtype() {
    }

    public static SemType tableContaining(Env env, SemType typeParameter,
                                          SemType normalizedKc, SemType normalizedKs,
                                          CellAtomicType.CellMutability mut) {

        assert SemTypes.isSubtypeSimple(typeParameter, PredefinedType.MAPPING);
        ListDefinition typeParamArrDef = new ListDefinition();
        SemType typeParamArray = typeParamArrDef.defineListTypeWrapped(env, typeParameter, mut);

        ListDefinition listDef = new ListDefinition();
        SemType tupleType = listDef.tupleTypeWrapped(env, typeParamArray, normalizedKc, normalizedKs);
        Bdd bdd = (Bdd) subtypeData(tupleType, BT_LIST);
        return createBasicSemType(BT_TABLE, bdd);
    }

    public static SemType tableContaining(Env env, SemType typeParameter,
                                          SemType normalizedKc, SemType normalizedKs) {
        return tableContaining(env, typeParameter, normalizedKc, normalizedKs, CELL_MUT_LIMITED);
    }

    public static SemType tableContaining(Env env, SemType mappingType, CellAtomicType.CellMutability mut) {
        SemType normalizedKc = PredefinedType.VAL; // TODO: Ideally this should be anydata
        SemType normalizedKs = PredefinedType.VAL; // TODO: Ideally this should be string[]
        return tableContaining(env, mappingType, normalizedKc, normalizedKs, mut);
    }

    public static SemType tableContaining(Env env, SemType mappingType) {
        return tableContaining(env, mappingType, CELL_MUT_LIMITED);
    }
}
