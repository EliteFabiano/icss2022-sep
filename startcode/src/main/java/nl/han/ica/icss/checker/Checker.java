package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;

import java.util.LinkedList;
import java.util.List;

public class Checker {

    private LinkedList<String> allDeclaredVariables; // Alle gedeclareerde variabelen in het hele bestand

    public void check(AST ast) {
        allDeclaredVariables = new LinkedList<>();
        checkStatements(ast.root.body, new LinkedList<>());
    }

    private void checkStatements(List<ASTNode> statements, LinkedList<String> declaredVariables) {
        for (ASTNode statement : statements) {
            checkNode(statement, declaredVariables);
        }
    }

    private void checkStatementsInNewScope(List<ASTNode> statements, LinkedList<String> outerVariables) {
        // Maakt een nieuwe scope aan waarin de buitenstaande variabelen beschikbaar
        // blijven.
        checkStatements(statements, new LinkedList<>(outerVariables));
    }

    private void checkNode(ASTNode node, LinkedList<String> declaredVariables) {
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

    private void checkAssignment(VariableAssignment assignment, LinkedList<String> declaredVariables) {
        // Check of de expressie van de assignment geen ongedefinieerde variables bevat.
        checkNode(assignment.expression, declaredVariables);
        declaredVariables.add(assignment.name.name);
        if (!allDeclaredVariables.contains(assignment.name.name)) {
            allDeclaredVariables.add(assignment.name.name);
        }
    }

    private void checkVariableUse(VariableReference variableUse, LinkedList<String> declaredVariables) {
        if (!declaredVariables.contains(variableUse.name)) {
            if (allDeclaredVariables.contains(variableUse.name)) {
                variableUse.setError("Variabele " + variableUse.name + " valt buiten scope");
            } else {
                variableUse.setError("Variabele " + variableUse.name + " is niet gedefinieerd");
            }
        }
    }

    private void checkIfStatement(IfClause ifStatement, LinkedList<String> declaredVariables) {
        checkNode(ifStatement.conditionalExpression, declaredVariables);
        checkStatementsInNewScope(ifStatement.body, declaredVariables);

        if (ifStatement.elseClause != null) {
            checkStatementsInNewScope(ifStatement.elseClause.body, declaredVariables);
        }
    }
}
