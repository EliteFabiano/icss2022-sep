package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;

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

    private void checkStatements(List<ASTNode> statements, Map<String, Boolean> declaredVariables) {
        for (ASTNode statement : statements) {
            checkNode(statement, declaredVariables);
        }
    }

    private void checkStatementsInNewScope(List<ASTNode> statements, Map<String, Boolean> outerVariables) {
        // Maakt een nieuwe scope aan waarin de buitenstaande variabelen beschikbaar
        // blijven.
        checkStatements(statements, new HashMap<>(outerVariables));
    }

    private void checkNode(ASTNode node, Map<String, Boolean> declaredVariables) {
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
        }

        for (ASTNode child : node.getChildren()) {
            checkNode(child, declaredVariables);
        }
    }

    private void checkAssignment(VariableAssignment assignment, Map<String, Boolean> declaredVariables) {
        // Check of de expressie van de assignment geen ongedefinieerde variables bevat.
        checkNode(assignment.expression, declaredVariables);

        boolean isBoolean = isBooleanExpression(assignment.expression, declaredVariables);

        declaredVariables.put(assignment.name.name, isBoolean);

        if (!allDeclaredVariables.contains(assignment.name.name)) {
            allDeclaredVariables.add(assignment.name.name);
        }
    }

    private void checkVariableUse(VariableReference variableUse, Map<String, Boolean> declaredVariables) {
        if (!declaredVariables.containsKey(variableUse.name)) {
            if (allDeclaredVariables.contains(variableUse.name)) {
                variableUse.setError("Variabele " + variableUse.name + " valt buiten scope");
            } else {
                variableUse.setError("Variabele " + variableUse.name + " is niet gedefinieerd");
            }
        }
    }

    private void checkIfStatement(IfClause ifStatement, Map<String, Boolean> declaredVariables) {
        checkNode(ifStatement.conditionalExpression, declaredVariables);
        checkStatementsInNewScope(ifStatement.body, declaredVariables);
        if (!isBooleanExpression(ifStatement.conditionalExpression, declaredVariables)) {
            ifStatement.setError("If condition " + ifStatement.conditionalExpression + " moet een boolean zijn");
        }
        if (ifStatement.elseClause != null) {
            checkStatementsInNewScope(ifStatement.elseClause.body, declaredVariables);
        }
    }

    private boolean isBooleanExpression(ASTNode expression, Map<String, Boolean> declaredVariables) {
        if (expression instanceof BoolLiteral) {
            return true;
        } else if (expression instanceof VariableReference) {
            if (declaredVariables.get(((VariableReference) expression).name).equals(Boolean.TRUE)) {
                return true;
            }
        }
        return false;
    }
}
