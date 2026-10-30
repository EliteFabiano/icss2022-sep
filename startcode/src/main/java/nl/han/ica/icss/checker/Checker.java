package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Checker {

    private LinkedList<String> allDeclaredVariables; // Alle gedeclareerde variabelen in het hele bestand

    public void check(AST ast) {
        allDeclaredVariables = new LinkedList<>();
        checkStatements(ast.root.body, new HashMap<>());
    }

    private void checkStatements(List<ASTNode> statements, Map<String, ExpressionType> declaredVariables) {
        for (ASTNode statement : statements) {
            checkNode(statement, declaredVariables);
        }
    }

    private void checkStatementsInNewScope(List<ASTNode> statements, Map<String, ExpressionType> outerVariables) {
        // Maakt een nieuwe scope aan waarin de buitenstaande variabelen beschikbaar
        // blijven.
        checkStatements(statements, new HashMap<>(outerVariables));
    }

    private void checkNode(ASTNode node, Map<String, ExpressionType> declaredVariables) {
        if (node instanceof VariableAssignment) {
            checkAssignment((VariableAssignment) node, declaredVariables);
            return;
        } else if (node instanceof VariableReference) {
            checkVariableUse((VariableReference) node, declaredVariables);
            return;
        } else if (node instanceof Stylerule) {
            checkStatementsInNewScope(((Stylerule) node).body, declaredVariables);
            return;
        } else if (node instanceof IfClause) {
            checkIfStatement((IfClause) node, declaredVariables);
            return;
        } else if (node instanceof ElseClause) {
            checkStatementsInNewScope(((ElseClause) node).body, declaredVariables);
            return;
        } else if (node instanceof AddOperation) {
            checkOperation((AddOperation) node, declaredVariables);
            return;
        } else if (node instanceof SubtractOperation) {
            checkOperation((SubtractOperation) node, declaredVariables);
            return;
        } else if (node instanceof MultiplyOperation) {
            checkOperation((MultiplyOperation) node, declaredVariables);
            return;
        }

        for (ASTNode child : node.getChildren()) {
            checkNode(child, declaredVariables);
        }
    }

    private void checkAssignment(VariableAssignment assignment, Map<String, ExpressionType> declaredVariables) {
        // Check of de expressie van de assignment geen ongedefinieerde variables bevat.
        checkNode(assignment.expression, declaredVariables);

        ExpressionType expressionType = getExpressionType(assignment.expression, declaredVariables);

        declaredVariables.put(assignment.name.name, expressionType);

        if (!allDeclaredVariables.contains(assignment.name.name)) {
            allDeclaredVariables.add(assignment.name.name);
        }
    }

    private void checkVariableUse(VariableReference variableUse, Map<String, ExpressionType> declaredVariables) {
        if (!declaredVariables.containsKey(variableUse.name)) {
            if (allDeclaredVariables.contains(variableUse.name)) {
                variableUse.setError("Variabele " + variableUse.name + " valt buiten scope");
            } else {
                variableUse.setError("Variabele " + variableUse.name + " is niet gedefinieerd");
            }
        }
    }

    private void checkIfStatement(IfClause ifStatement, Map<String, ExpressionType> declaredVariables) {
        checkNode(ifStatement.conditionalExpression, declaredVariables);
        checkStatementsInNewScope(ifStatement.body, declaredVariables);
        if (getExpressionType(ifStatement.conditionalExpression, declaredVariables) != ExpressionType.BOOL) {
            ifStatement.setError("If condition " + ifStatement.conditionalExpression + " moet een boolean zijn");
        }
        if (ifStatement.elseClause != null) {
            checkStatementsInNewScope(ifStatement.elseClause.body, declaredVariables);
        }
    }

    private void checkOperation(ASTNode operation, Map<String, ExpressionType> declaredVariables) {
        for (ASTNode child : operation.getChildren()) {
            checkNode(child, declaredVariables);
            if (getExpressionType(child, declaredVariables) == ExpressionType.COLOR) {
                operation.setError("Kleuren mogen niet gebruikt worden in een operatie");
            }
        }
    }

    private ExpressionType getExpressionType(ASTNode expression, Map<String, ExpressionType> declaredVariables) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof VariableReference) {
            return declaredVariables.getOrDefault(((VariableReference) expression).name, ExpressionType.UNDEFINED);
        }
        return ExpressionType.UNDEFINED;
    }
}
