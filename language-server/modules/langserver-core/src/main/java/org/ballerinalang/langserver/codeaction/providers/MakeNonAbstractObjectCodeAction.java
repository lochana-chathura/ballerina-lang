/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.spi.PositionDetails;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.Whitespace;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Code Action for making object non abstract type.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class MakeNonAbstractObjectCodeAction extends AbstractCodeActionProvider {
    private static final String NO_IMPL_FOUND_FOR_FUNCTION = "no implementation found for the function";

    @Override
    public boolean isEnabled() {
        // TODO: Convert this code into new object semantics
        return false;
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    PositionDetails positionDetails,
                                                    List<Diagnostic> allDiagnostics, SyntaxTree syntaxTree,
                                                    LSContext context) {
        if (!(diagnostic.getMessage().startsWith(NO_IMPL_FOUND_FOR_FUNCTION))) {
            return Collections.emptyList();
        }

        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);

        Optional<BLangTypeDefinition> objType = getObjectTypeDefinition(context, position.getLine(),
                                                                        position.getCharacter());
        if (objType.isEmpty()) {
            return null;
        }

        String simpleObjName = getObjectName(diagnosticMessage);
        if (simpleObjName.isEmpty()) {
            return null;
        }

        Set<Whitespace> whitespaces = objType.get().getWS();
        whitespaces.addAll(objType.get().typeNode.getWS());
        Iterator<Whitespace> iterator = whitespaces.iterator();

        String commandTitle = String.format(CommandConstants.MAKE_OBJ_ABSTRACT_TITLE, simpleObjName);
        int colBeforeObjKeyword = objType.get().pos.lineRange().startLine().offset();
        boolean isFirst = true;
        StringBuilder str = new StringBuilder();
        while (iterator.hasNext()) {
            Whitespace next = iterator.next();
            if ("object".equals(next.getPrevious())) {
                break;
            }
            String ws = next.getWs();
            if (!isFirst && !";".equals(ws)) {
                str.append(ws);
            }
            str.append(next.getPrevious());
            isFirst = false;
        }
        colBeforeObjKeyword += str.toString().length();

        String editText = " abstract";
        Position pos = new Position(objType.get().pos.lineRange().startLine().line() - 1, colBeforeObjKeyword - 1);

        List<TextEdit> edits = Collections.singletonList(new TextEdit(new Range(pos, pos), editText));
        return Collections.singletonList(createQuickFixCodeAction(commandTitle, edits, uri));
    }

    private static String getObjectName(String diagnosticMessage) {
        Matcher matcher = CommandConstants.FUNC_IN_ABSTRACT_OBJ_PATTERN.matcher(diagnosticMessage);
        Matcher matcher2 = CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION_PATTERN.matcher(diagnosticMessage);
        String simpleObjName = "";
        if (matcher.find() && matcher.groupCount() > 1) {
            String objectName = matcher.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
        } else if (matcher2.find() && matcher2.groupCount() > 1) {
            String objectName = matcher2.group(2);
            int colonIndex = objectName.lastIndexOf(":");
            simpleObjName = (colonIndex > -1) ? objectName.substring(colonIndex + 1) : objectName;
        }
        return simpleObjName;
    }

    private static Optional<BLangTypeDefinition> getObjectTypeDefinition(LSContext context, int line, int column) {
        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        return bLangPackage.topLevelNodes.stream()
                .filter(topLevelNode -> {
                    if (topLevelNode instanceof BLangTypeDefinition) {
                        Location pos =
                                topLevelNode.getPosition();
                        return ((pos.lineRange().startLine().line() == line ||
                                pos.lineRange().endLine().line() == line ||
                                (pos.lineRange().startLine().line() < line
                                        && pos.lineRange().endLine().line() > line)) &&
                                (pos.lineRange().startLine().offset() <= column
                                        && pos.lineRange().endLine().offset() <= column));
                    }
                    return false;
                }).findAny().map(t -> (BLangTypeDefinition) t);
    }
}
