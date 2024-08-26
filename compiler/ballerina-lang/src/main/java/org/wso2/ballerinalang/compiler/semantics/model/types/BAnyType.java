/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static io.ballerina.types.PredefinedType.ANY;
import static io.ballerina.types.PredefinedType.VAL_READONLY;

/**
 * @since 0.94
 */
public class BAnyType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    private boolean nullable = true;
    private boolean isNilLifted = false;

    public BAnyType(BTypeSymbol tsymbol) {
        this(tsymbol, ANY);
    }

    public BAnyType(int tag, BTypeSymbol tsymbol, boolean nullable) {
        super(tag, tsymbol, ANY);
        this.nullable = nullable;
    }

    public BAnyType(int tag, BTypeSymbol tsymbol, Name name, long flags, boolean nullable) {
        super(tag, tsymbol, flags, ANY);
        this.name = name;
        this.nullable = nullable;
    }

    private BAnyType(BTypeSymbol tsymbol, SemType semType) {
        super(TypeTags.ANY, tsymbol, semType);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flag) {
        this(tsymbol, name, flag, ANY);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flags, SemType semType) {
        super(TypeTags.ANY, tsymbol, semType);
        this.name = name;
        this.setFlags(flags);
    }

    public static BAnyType newNilLiftedBAnyType(BTypeSymbol tsymbol) {
        BAnyType result = new BAnyType(tsymbol, Core.diff(ANY, PredefinedType.NIL));
        result.isNilLifted = true;
        return result;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANY;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(getFlags(), Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public SemType semType() {
        SemType semType = ANY;
        if (Symbols.isFlagOn(getFlags(), Flags.READONLY)) {
            semType = Core.intersect(semType, VAL_READONLY);
        }

        if (isNilLifted) {
            semType = Core.diff(semType, PredefinedType.NIL);
        }
        return semType;
    }
}
